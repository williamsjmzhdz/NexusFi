# NexusFi: Documento de Lógica Funcional y Operativa

## 1. Resumen y Filosofía del Sistema

NexusFi es una aplicación de finanzas personales diseñada para un único usuario, centrada en un sistema de presupuesto proactivo basado en porcentajes. El objetivo es dar claridad y control sobre los flujos de dinero, tratando cada ingreso como un recurso que se distribuye sistemáticamente en "cubetas" (categorías) predefinidas.

### Principios Fundamentales

1. **Integridad Contable**: El sistema debe garantizar que no se cree ni se destruya dinero. El balance total del sistema solo cambia con ingresos y gastos. Los traspasos y reasignaciones siempre deben sumar cero.

2. **Integridad de Asignación**: La suma de los porcentajes de todas las categorías raíz siempre debe ser 100%. Para subcategorías, pueden sumar menos de 100% (el resto permanece en el padre).

3. **Límite de Profundidad**: Máximo 2 niveles de jerarquía (categorías raíz y subcategorías). No se permiten sub-subcategorías.

## 2. Autenticación de Usuario

### 2.1. Funcionalidad: Registro e Inicio de Sesión

- **Propósito**: Proteger el acceso a la aplicación para que solo usuarios autenticados puedan ver y gestionar sus finanzas.

- **Implementación Actual**:
  1. El usuario se registra mediante `POST /api/v1/auth/register` con email, contraseña, nombre y apellido.
  2. La contraseña se hashea con BCrypt antes de almacenarse.
  3. Se devuelve un JWT (JSON Web Token) válido por 24 horas.
  4. Para iniciar sesión: `POST /api/v1/auth/login` con email y contraseña.
  5. El JWT se incluye en el header `Authorization: Bearer <token>` para todas las peticiones protegidas.
  6. Todos los endpoints (excepto `/api/v1/auth/**`) requieren autenticación.

## 3. Gestión de Ingresos

### 3.1. Funcionalidad: Registrar un Nuevo Ingreso

- **Propósito**: Registrar la entrada de dinero al sistema y distribuirlo automáticamente entre las categorías.

- **Lógica de Operación**: Se crea un único registro en income_records que dispara el Proceso de Asignación Automática, distribuyendo el monto total entre las categorías activas según sus porcentajes y actualizando sus balances. La distribución es **recursiva**: si una categoría tiene subcategorías, su porción se redistribuye entre ellas.

- **Ejemplo Práctico**:
  - **Contexto**: Tienes dos categorías principales: Fixed Expenses (60%) y Savings (40%). A su vez, Fixed Expenses tiene dos subcategorías: Rent (50%) y Utilities (30%) - suman 80%, dejando 20% en el padre.
  - **Acción**: Registras un ingreso de $10,000.
  - **Resultado en el Sistema**:
    1. Se crea un registro en income_records por $10,000.
    2. Distribución a nivel raíz (100%):
       - $6,000 a la categoría Fixed Expenses (60%)
       - $4,000 a la categoría Savings (40%)
    3. Redistribución de Fixed Expenses ($6,000):
       - $3,000 a Rent (50% de $6,000)
       - $1,800 a Utilities (30% de $6,000)
       - $1,200 permanece en Fixed Expenses (20% restante)
    4. Se crean movements de tipo ASSIGNMENT para cada distribución.
    5. Los current_balance se actualizan:
       - Savings: $4,000
       - Fixed Expenses: $1,200 (el resto que no fue a subcategorías)
       - Rent: $3,000
       - Utilities: $1,800

## 4. Gestión de Categorías

### 4.1. Restricciones de Jerarquía

- **Máximo 2 niveles**: Solo se permiten categorías raíz (nivel 1) y subcategorías (nivel 2).
- **No sub-subcategorías**: Intentar crear una categoría bajo una subcategoría devuelve error 400.
- **Mensaje de error**: "Cannot create sub-subcategory. Maximum 2 levels allowed (category and subcategory)"

### 4.2. Reglas de Porcentajes

| Nivel | Descripción | Regla de Porcentaje |
|-------|-------------|---------------------|
| Nivel 1 (Raíz) | Categorías principales | Deben sumar exactamente 100% |
| Nivel 2 (Sub) | Subcategorías de una categoría raíz | Pueden sumar ≤ 100% |

**Nota sobre el resto**: Si las subcategorías suman menos de 100%, el porcentaje restante del ingreso distribuido permanece en la categoría padre.

### 4.3. Funcionalidad: Crear Categoría / Subcategoría

- **Propósito**: Permitir al usuario definir nuevas "cubetas" para su presupuesto.

- **Lógica de Operación**: Al crear, la UI debe forzar al usuario a que la suma de los porcentajes de las categorías raíz siempre sea 100%. Para subcategorías, pueden sumar menos de 100%.

- **Ejemplo Práctico**:
  - **Contexto**: Tienes Fixed Expenses (60%) y Savings (40%).
  - **Acción 1 - Nueva categoría raíz**: Quieres añadir Investments.
  - **Resultado**: Debes redistribuir. Ej: Fixed Expenses 50%, Savings 40%, Investments 10%.
  - **Acción 2 - Nueva subcategoría**: Quieres añadir "Gym" bajo Fixed Expenses.
  - **Resultado**: Creas Gym con un porcentaje (ej: 20%). Si ya tienes Rent (50%) y Utilities (30%), ahora suman 100%. Puedes tener menos (ej: 80%) y el 20% restante queda en Fixed Expenses.

### 4.4. Funcionalidad: Editar Nombre de Categoría

- **Propósito**: Modificar el nombre de una categoría o subcategoría.

- **Lógica de Operación**: Se ejecuta una operación UPDATE sobre el campo name del registro de la categoría.

- **Condición**: El nuevo nombre debe ser único entre sus categorías hermanas.

### 4.5. Funcionalidad: Rebalancear Porcentajes

- **Propósito**: Permitir un ajuste dinámico de la estrategia de presupuesto sin necesidad de crear o archivar nada.

- **Lógica de Operación**: Esta funcionalidad permite modificar los assigned_percentage de un grupo de categorías hermanas.

- **Restricciones**:
  - Categorías raíz: Deben sumar 100%
  - Subcategorías: Pueden sumar hasta 100% (el resto queda en el padre)

- **Ejemplo Práctico**:
  - **Contexto**: Tienes Fixed Expenses (50%), Savings (40%) y Investments (10%).
  - **Acción**: Decides que quieres ser más agresivo con la inversión. Accedes a la función "Rebalancear Porcentajes".
  - **Resultado en el Sistema**: La UI te muestra los porcentajes actuales. Cambias Investments a 20%. El sistema te indica que tienes un 10% excedente y que debes reducirlo de las otras categorías. Ajustas Savings a 30%. Ahora la suma 50 + 30 + 20 = 100. Guardas los cambios y el sistema actualiza los tres registros en la base de datos.

### 4.6. Funcionalidad: Archivar Categoría (Borrado Lógico)

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

### 4.7. Funcionalidad: Desarchivar Categoría

- **Propósito**: Reactivar una categoría previamente archivada.

- **Lógica de Operación**: Para desarchivar, el sistema obliga al usuario a asignar un nuevo porcentaje, rebalanceando el de las hermanas.

- **Ejemplo Práctico**:
  - **Contexto**: Gym está archivada (0%) y Health Insurance tiene el 100%.
  - **Acción**: Desarchivas Gym.
  - **Resultado en el Sistema**: La UI te pide asignarle un porcentaje. Le das un 20%, lo que te obliga a reducir el de Health Insurance a 80%. Al guardar, el sistema actualiza los porcentajes y cambia is_active de Gym a TRUE.

## 5. Gestión de Movimientos Financieros

### 5.1. Funcionalidad: Registrar un Gasto

- **Propósito**: Registrar una salida de dinero del sistema desde una categoría específica.

- **Lógica de Operación**: 
  1. Se valida que la categoría tenga balance suficiente
  2. Se crea un registro en expense_records
  3. Se crea un movement de tipo EXPENSE (monto negativo)
  4. Se actualiza el current_balance de la categoría

### 5.2. Funcionalidad: Transferir entre Categorías

- **Propósito**: Mover dinero de una categoría a otra (operación zero-sum).

- **Lógica de Operación**:
  1. Se valida que la categoría origen tenga balance suficiente
  2. Se crea un registro en transfers
  3. Se crean 2 movements:
     - TRANSFER negativo en categoría origen
     - TRANSFER positivo en categoría destino
  4. Se actualizan ambos current_balance

### 5.3. Funcionalidad: Consultar Movimientos

- **Propósito**: Ver el historial de todas las transacciones.

- **Endpoints disponibles**:
  - `GET /api/v1/movements` - Todos los movimientos
  - `GET /api/v1/movements/{id}` - Movimiento específico
  - `GET /api/v1/movements/type/{type}` - Filtrar por tipo (ASSIGNMENT, EXPENSE, TRANSFER)
  - `GET /api/v1/movements/category/{categoryId}` - Filtrar por categoría

## 6. API REST Endpoints

### 6.1. Autenticación (públicos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Registro de usuario |
| POST | `/api/v1/auth/login` | Inicio de sesión |

### 6.2. Categorías (protegidos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/categories` | Listar todas |
| GET | `/api/v1/categories/{id}` | Obtener una |
| GET | `/api/v1/categories/tree` | Árbol jerárquico |
| GET | `/api/v1/categories/root` | Solo raíz |
| GET | `/api/v1/categories/{id}/subcategories` | Subcategorías |
| GET | `/api/v1/categories/remaining` | Porcentaje disponible |
| POST | `/api/v1/categories` | Crear (con parentId para sub) |
| PUT | `/api/v1/categories/{id}` | Actualizar |
| DELETE | `/api/v1/categories/{id}` | Eliminar (soft delete) |

### 6.3. Ingresos (protegidos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/incomes` | Listar todos |
| GET | `/api/v1/incomes/{id}` | Obtener uno |
| POST | `/api/v1/incomes` | Registrar ingreso |

### 6.4. Gastos (protegidos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/expenses` | Listar todos |
| GET | `/api/v1/expenses/{id}` | Obtener uno |
| GET | `/api/v1/expenses/category/{id}` | Por categoría |
| POST | `/api/v1/expenses` | Registrar gasto |

### 6.5. Transferencias (protegidos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/transfers` | Listar todas |
| GET | `/api/v1/transfers/{id}` | Obtener una |
| GET | `/api/v1/transfers/category/{id}` | Por categoría |
| POST | `/api/v1/transfers` | Ejecutar transferencia |

### 6.6. Movimientos (protegidos, solo lectura)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/movements` | Listar todos |
| GET | `/api/v1/movements/{id}` | Obtener uno |
| GET | `/api/v1/movements/type/{type}` | Por tipo |
| GET | `/api/v1/movements/category/{id}` | Por categoría |
