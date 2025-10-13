# NexusFi: Documento de Lógica Funcional y Operativa

## 1. Resumen y Filosofía del Sistema

NexusFi es una aplicación de finanzas personales diseñada para un único usuario, centrada en un sistema de presupuesto proactivo basado en porcentajes. El objetivo es dar claridad y control sobre los flujos de dinero, tratando cada ingreso como un recurso que se distribuye sistemáticamente en "cubetas" (categorías) predefinidas.

### Principios Fundamentales

1. **Integridad Contable**: El sistema debe garantizar que no se cree ni se destruya dinero. El balance total del sistema solo cambia con ingresos y gastos. Los traspasos y reasignaciones siempre deben sumar cero.

2. **Integridad de Asignación**: La suma de los porcentajes de todas las categorías "hermanas" (aquellas bajo el mismo padre) siempre debe ser 100%. Esta regla es inviolable y debe ser reforzada por la lógica de la aplicación en todo momento.

## 2. Autenticación de Usuario

### 2.1. Funcionalidad: Inicio de Sesión

- **Propósito**: Proteger el acceso a la aplicación para que solo el propietario pueda ver y gestionar sus finanzas.

- **Lógica de Operación**:
  1. La aplicación está diseñada para un único usuario. No existirá una funcionalidad de registro público.
  2. El usuario se crea manualmente en la base de datos (users) en el momento del despliegue. La contraseña se almacena hasheada con bcrypt.
  3. Al acceder a la aplicación, el sistema (gestionado por Spring Security) interceptará al usuario y lo redirigirá a una página de login.
  4. El usuario ingresará su email y contraseña. El sistema hasheará la contraseña ingresada y la comparará con el hash almacenado en la base de datos.
  5. Si las credenciales son válidas, se crea una sesión y se le da acceso al panel principal.
  6. Todas las rutas y endpoints de la aplicación, a excepción de la página de login, requerirán una sesión de usuario autenticada.

## 3. Gestión de Ingresos

### 3.1. Funcionalidad: Registrar un Nuevo Ingreso

- **Propósito**: Registrar la entrada de dinero al sistema y distribuirlo automáticamente entre las categorías.

- **Lógica de Operación**: Se crea un único registro en income_records que dispara el Proceso de Asignación Automática, distribuyendo el monto total entre las categorías activas según sus porcentajes y actualizando sus balances.

- **Ejemplo Práctico**:
  - **Contexto**: Tienes dos categorías principales: Fixed Expenses (60%) y Savings (40%). A su vez, Fixed Expenses tiene dos subcategorías: Rent (50%) y Utilities (50%).
  - **Acción**: Registras un ingreso de $10,000.
  - **Resultado en el Sistema**:
    1. Se crea un registro en income_records por $10,000.
    2. Se crean los siguientes movements de tipo ASSIGNMENT:
       - - $6,000 a la categoría Fixed Expenses.
       - - $4,000 a la categoría Savings.
    3. El sistema toma los $6,000 de Fixed Expenses y los redistribuye a sus hijas:
       - - $3,000 a la subcategoría Rent (50% de $6,000).
       - - $3,000 a la subcategoría Utilities (50% de $6,000).
    4. Los current_balance de las cuatro categorías se actualizan con estos montos.

## 4. Gestión de Categorías

### 4.1. Funcionalidad: Crear Categoría / Subcategoría

- **Propósito**: Permitir al usuario definir nuevas "cubetas" para su presupuesto.

- **Lógica de Operación**: Al crear, la UI debe forzar al usuario a que la suma de los porcentajes de las categorías hermanas siempre sea 100%.

- **Ejemplo Práctico**:
  - **Contexto**: Tienes Fixed Expenses (60%) y Savings (40%).
  - **Acción**: Quieres añadir una nueva categoría principal, Investments.
  - **Resultado en el Sistema**: La UI te pide asignar un porcentaje a Investments (ej. 10%) y te obliga a reducir el 10% restante de las otras dos. Ajustas Fixed Expenses a 50% y Savings a 40%. Al guardar, el sistema valida que 50 + 40 + 10 = 100 y crea el nuevo registro.

### 4.2. Funcionalidad: Editar Nombre de Categoría

- **Propósito**: Modificar el nombre de una categoría o subcategoría.

- **Lógica de Operación**: Se ejecuta una operación UPDATE sobre el campo name del registro de la categoría.

- **Condición**: El nuevo nombre debe ser único entre sus categorías hermanas.

### 4.3. Funcionalidad: Rebalancear Porcentajes

- **Propósito**: Permitir un ajuste dinámico de la estrategia de presupuesto sin necesidad de crear o archivar nada.

- **Lógica de Operación**: Esta funcionalidad permite modificar los assigned_percentage de un grupo de categorías hermanas.

- **Ejemplo Práctico**:
  - **Contexto**: Tienes Fixed Expenses (50%), Savings (40%) y Investments (10%).
  - **Acción**: Decides que quieres ser más agresivo con la inversión. Accedes a la función "Rebalancear Porcentajes".
  - **Resultado en el Sistema**: La UI te muestra los porcentajes actuales. Cambias Investments a 20%. El sistema te indica que tienes un 10% excedente y que debes reducirlo de las otras categorías. Ajustas Savings a 30%. Ahora la suma 50 + 30 + 20 = 100. Guardas los cambios y el sistema actualiza los tres registros en la base de datos.

### 4.4. Funcionalidad: Archivar Categoría (Borrado Lógico)

- **Propósito**: Ocultar una categoría que ya no se usa, sin perder su historial y manteniendo la integridad del sistema.

- **Lógica de Operación**: Antes de archivar, el sistema valida que el current_balance y el assigned_percentage de la categoría sean 0.00.

- **Ejemplo Práctico**:
  - **Contexto**: Quieres archivar la subcategoría Gym, que tiene un balance de $500 y un porcentaje del 20% de su categoría padre. Su hermana, Health Insurance, tiene el 80%.
  - **Acción**: Intentas archivar Gym.
  - **Resultado en el Sistema**:
    1. **Bloqueo 1**: El sistema muestra: "Error: El balance es de $500. Por favor, transfiera los fondos."
    2. **Tu Acción**: Realizas un traspaso de $500 de Gym a Health Insurance. El balance de Gym ahora es $0.
    3. **Bloqueo 2**: Intentas archivar de nuevo. El sistema muestra: "Error: El porcentaje es 20%. Por favor, use la función 'Rebalancear Porcentajes'."
    4. **Tu Acción**: Vas a "Rebalancear". Pones Gym en 0% y Health Insurance en 100%.
    5. **Éxito**: Ahora que ambas condiciones se cumplen, el sistema te permite archivar Gym, cambiando su estado is_active a FALSE.

### 4.5. Funcionalidad: Desarchivar Categoría

- **Propósito**: Reactivar una categoría previamente archivada.

- **Lógica de Operación**: Para desarchivar, el sistema obliga al usuario a asignar un nuevo porcentaje, rebalanceando el de las hermanas.

- **Ejemplo Práctico**:
  - **Contexto**: Gym está archivada (0%) y Health Insurance tiene el 100%.
  - **Acción**: Desarchivas Gym.
  - **Resultado en el Sistema**: La UI te pide asignarle un porcentaje. Le das un 20%, lo que te obliga a reducir el de Health Insurance a 80%. Al guardar, el sistema actualiza los porcentajes y cambia is_active de Gym a TRUE.

## 5. Gestión de Movimientos Financieros

### 5.1. Funcionalidad: Registrar un Gasto

- **Propósito**: Registrar una salida de dinero final del sistema desde una c
