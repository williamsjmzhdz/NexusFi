# NexusFi 💰

**Personal Finance Management System with Percentage-Based Budgeting**

NexusFi es una aplicación de finanzas personales diseñada para un único usuario, centrada en un sistema de presupuesto proactivo basado en porcentajes. El objetivo es dar claridad y control sobre los flujos de dinero, tratando cada ingreso como un recurso que se distribuye sistemáticamente en "cubetas" (categorías) predefinidas.

---

## 🎯 Características Principales

- 📊 **Presupuesto basado en porcentajes** - Distribución automática de ingresos
- 🌳 **Categorías jerárquicas** - Organiza tu dinero en categorías y subcategorías
- 💸 **Gestión de gastos** - Registra y rastrea todos tus gastos
- 🔄 **Transferencias entre categorías** - Mueve dinero entre cubetas
- 📈 **Reportes y dashboard** - Visualiza tu situación financiera
- 🔐 **Seguro y privado** - Aplicación de usuario único

---

## 🏗️ Tecnologías

- **Backend**: Java 17, Spring Boot 3.2.0
- **Base de Datos**: PostgreSQL 14+
- **ORM**: JPA/Hibernate
- **Seguridad**: Spring Security con BCrypt
- **Build**: Maven

---

## 📁 Estructura del Proyecto

```
NexusFi/
├── docs/                  # Documentación completa del proyecto
│   ├── DATA_MODEL.md      # Modelo de datos detallado
│   ├── QUICK_REFERENCE.md # Guía rápida de referencia
│   └── ...
├── database/              # Scripts SQL
│   └── schema.sql         # Schema completo de la base de datos
├── src/                   # Código fuente
│   ├── main/
│   │   ├── java/          # Código Java
│   │   └── resources/     # Configuración y recursos
│   └── test/              # Tests
├── pom.xml                # Dependencias Maven
└── requirements.md        # Requisitos funcionales detallados
```

---

## 🚀 Comenzando

### Prerrequisitos

- Java 17 o superior
- PostgreSQL 14 o superior
- Maven 3.8+

### Instalación

1. **Clonar el repositorio**

   ```bash
   git clone <repository-url>
   cd NexusFi
   ```

2. **Crear la base de datos**

   ```bash
   createdb nexusfi
   psql -U postgres -d nexusfi -f database/schema.sql
   ```

3. **Configurar la aplicación**

   Edita `src/main/resources/application.yml` con tus credenciales:

   ```yaml
   spring:
     datasource:
       username: tu_usuario
       password: tu_contraseña
   ```

4. **Compilar y ejecutar**

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Acceder a la aplicación**

   Abre tu navegador en: `http://localhost:8080`

   Usuario de prueba:

   - Email: `user@nexusfi.com`
   - Password: `password123`

---

## 📚 Documentación

- **[Requisitos Funcionales](requirements.md)** - Lógica funcional y operativa completa
- **[Modelo de Datos](docs/DATA_MODEL.md)** - Documentación técnica del modelo de datos
- **[Guía Rápida](docs/QUICK_REFERENCE.md)** - Referencia rápida de tablas, queries y conceptos
- **[Documentación del Data Model](docs/README_DATA_MODEL.md)** - Guía de inicio del modelo de datos

---

## 🔧 Desarrollo

### Estado del Proyecto

🚧 **En Desarrollo Activo** 🚧

- [x] Diseño del modelo de datos
- [x] Schema de base de datos
- [x] Entidades JPA
- [ ] Repositorios
- [ ] Servicios de negocio
- [ ] API REST
- [ ] Frontend
- [ ] Tests

### Próximos Pasos

1. Crear repositorios Spring Data JPA
2. Implementar servicios de negocio
3. Desarrollar controladores REST API
4. Crear frontend (Thymeleaf/React)
5. Implementar tests unitarios e integración

---

## 🎓 Principios Fundamentales

### 1. Integridad Contable

El sistema garantiza que no se cree ni se destruya dinero. El balance total del sistema solo cambia con ingresos y gastos. Los traspasos y reasignaciones siempre deben sumar cero.

### 2. Integridad de Asignación

La suma de los porcentajes de todas las categorías "hermanas" (aquellas bajo el mismo padre) siempre debe ser 100%. Esta regla es inviolable y está reforzada por la lógica de la aplicación.

---

## 💡 Ejemplo de Uso

### Registrar un Ingreso

```
Ingreso: $10,000
Categorías:
  - Gastos Fijos (60%) → $6,000
    - Renta (50%) → $3,000
    - Servicios (50%) → $3,000
  - Ahorros (40%) → $4,000

El sistema distribuye automáticamente el ingreso según los porcentajes.
```

### Realizar un Gasto

```
Gasto: $100 desde "Servicios"
Balance de Servicios: $3,000 → $2,900
Balance Total del Sistema: $10,000 → $9,900
```

### Transferir entre Categorías

```
Transferencia: $500 de "Ahorros" a "Gastos Fijos"
Ahorros: $4,000 → $3,500
Gastos Fijos: $6,000 → $6,500
Balance Total: Sin cambios (operación zero-sum)
```

---

## 🤝 Contribuciones

Este es un proyecto personal de aprendizaje. Si tienes sugerencias o encuentras bugs, siéntete libre de abrir un issue.

---

## 📝 Licencia

Este proyecto es de uso personal y educativo.

---

## 👤 Autor

**Francisco Williams Jiménez Hernández**

Desarrollado como proyecto de aprendizaje para dominar Spring Boot, JPA, y arquitectura de aplicaciones financieras.

---

## 🙏 Agradecimientos

- Spring Boot por el excelente framework
- PostgreSQL por la robusta base de datos
- La comunidad de desarrolladores por compartir conocimiento

---

**¡Construyendo NexusFi paso a paso! 🚀**
