# NexusFi 💰

**Personal Finance Management System with Percentage-Based Budgeting**

NexusFi es una aplicación de finanzas personales diseñada para un único usuario, centrada en un sistema de presupuesto proactivo basado en porcentajes. El objetivo es dar claridad y control sobre los flujos de dinero, tratando cada ingreso como un recurso que se distribuye sistemáticamente en "cubetas" (categorías) predefinidas.

---

## 🎯 Características Principales

- 📊 **Presupuesto basado en porcentajes** - Distribución automática de ingresos
- 🌳 **Categorías jerárquicas** - Organiza tu dinero en categorías y subcategorías (máximo 2 niveles)
- 💸 **Gestión de gastos** - Registra y rastrea todos tus gastos
- 🔄 **Transferencias entre categorías** - Mueve dinero entre cubetas
- 📈 **Movimientos y auditoría** - Historial completo de todas las transacciones
- 🔐 **Autenticación JWT** - Seguro con tokens de 24 horas
- 🚀 **Desplegado en producción** - API live en Railway

---

## 🏗️ Tecnologías

- **Backend**: Java 17, Spring Boot 3.2.0
- **Base de Datos**: PostgreSQL 17 (Railway) / PostgreSQL 16+ (local)
- **ORM**: JPA/Hibernate
- **Seguridad**: Spring Security con JWT (jjwt 0.12.5) + BCrypt
- **Build**: Maven
- **Contenedorización**: Docker (multi-stage)
- **Plataforma**: Railway
- **API Version**: v1 (`/api/v1/`)

---

## 📁 Estructura del Proyecto

```
NexusFi/backend/
├── docs/                  # Documentación completa del proyecto
│   ├── DATA_MODEL.md      # Modelo de datos detallado
│   ├── QUICK_REFERENCE.md # Guía rápida de referencia
│   ├── MVP.md             # Definición del MVP
│   └── LEARNING_NOTES.md  # Notas de aprendizaje
├── database/              # Scripts SQL
│   └── schema.sql         # Schema completo de la base de datos
├── postman/               # Colección Postman para testing
│   └── NexusFi_API_v1.postman_collection.json
├── src/                   # Código fuente
│   ├── main/
│   │   ├── java/          # Código Java
│   │   └── resources/     # Configuración y recursos
│   └── test/              # Tests
├── scripts/               # Scripts de desarrollo local
│   ├── start-dev.ps1/.cmd
│   ├── stop-dev.ps1/.cmd
│   ├── status-dev.ps1/.cmd
│   └── reset-dev-data.ps1/.cmd
├── Dockerfile             # Multi-stage Docker build
├── .dockerignore          # Exclusiones para Docker
├── mvnw / mvnw.cmd        # Maven wrapper
├── pom.xml                # Dependencias Maven
├── requirements.md        # Requisitos funcionales detallados
└── PROGRESS.md            # Progreso del desarrollo
```

---

## 🚀 Comenzando

### Prerrequisitos

- Java 17 o superior
- PostgreSQL 16 o superior
- Maven 3.8+ (o usar el Maven wrapper incluido)

### Instalación

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/williamsjmzhdz/NexusFi.git
   cd NexusFi/backend
   ```

2. **Crear la base de datos**

   ```bash
   createdb nexusfi
   psql -U postgres -d nexusfi -f database/schema.sql
   ```

3. **Configurar variables de entorno**

   ```bash
   # Windows PowerShell
   $env:DB_PASSWORD = "tu_password"
   
   # Linux/Mac
   export DB_PASSWORD=tu_password
   ```

4. **Iniciar localmente con un solo comando**

   En Windows PowerShell:

   ```powershell
   .\scripts\start-dev.ps1
   ```

   Si prefieres `.cmd`:

   ```powershell
   .\scripts\start-dev.cmd
   ```

   El script:
   - usa el perfil `dev`
   - se conecta a PostgreSQL local (`nexusfi`)
   - reutiliza `DB_PASSWORD` si ya existe
   - si no existe, te pide la contraseña una sola vez
   - si NexusFi ya está corriendo en `8080`, detecta la instancia y termina sin error

   Si quieres reiniciar la instancia local actual:

   ```powershell
   .\scripts\start-dev.ps1 -Restart
   ```

   Para detener la instancia local:

   ```powershell
   .\scripts\stop-dev.ps1
   ```

   Si prefieres `.cmd`:

   ```powershell
   .\scripts\stop-dev.cmd
   ```

   Para revisar el estado de la API local:

   ```powershell
   .\scripts\status-dev.ps1
   ```

   O con `.cmd`:

   ```powershell
   .\scripts\status-dev.cmd
   ```

   Para limpiar todos los datos locales y volver a correr la colección Postman desde cero:

   ```powershell
   .\scripts\reset-dev-data.ps1
   ```

   O con `.cmd`:

   ```powershell
   .\scripts\reset-dev-data.cmd
   ```

   Si quieres evitar la confirmación interactiva:

   ```powershell
   .\scripts\reset-dev-data.ps1 -Force -DbPassword tu_password
   ```

   Comando manual equivalente:

   ```powershell
   $env:DB_PASSWORD = "tu_password"
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

5. **Acceder a la API**

   Base URL local: `http://localhost:8080/api/v1`
   Base URL producción: `https://nexusfi-production.up.railway.app/api/v1`

   Primero registra un usuario:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"user@example.com","password":"password123","firstName":"John","lastName":"Doe"}'
   ```

6. **Verificar que arrancó correctamente**

   ```powershell
   Invoke-WebRequest http://localhost:8080/api/v1/auth/health -UseBasicParsing
   ```

   Debe responder `200 OK` con body `OK`.

7. **Flujo recomendado para probar Postman desde cero**

   ```powershell
   .\scripts\stop-dev.ps1
   .\scripts\reset-dev-data.ps1
   .\scripts\start-dev.ps1
   ```

   Después de eso, usa la colección Postman empezando por `Register User`.

---

## 📚 Documentación

- **[Requisitos Funcionales](requirements.md)** - Lógica funcional y operativa completa
- **[Modelo de Datos](docs/DATA_MODEL.md)** - Documentación técnica del modelo de datos
- **[Guía Rápida](docs/QUICK_REFERENCE.md)** - Referencia rápida de tablas, queries y conceptos
- **[Documentación del Data Model](docs/README_DATA_MODEL.md)** - Guía de inicio del modelo de datos

---

## 🔧 Desarrollo

### Estado del Proyecto

✅ **Backend Completo y Desplegado en Producción** ✅

- [x] Diseño del modelo de datos
- [x] Schema de base de datos
- [x] Entidades JPA
- [x] Repositorios Spring Data JPA
- [x] Servicios de negocio
- [x] API REST (35 endpoints)
- [x] Spring Security + JWT Authentication
- [x] Categorías jerárquicas (máximo 2 niveles)
- [x] Distribución recursiva de ingresos
- [x] Colección Postman (35 requests)
- [x] Docker containerization
- [x] Despliegue en Railway (producción)
- [ ] Frontend
- [ ] Tests unitarios e integración

### API Endpoints

Base URL local: `http://localhost:8080/api/v1`
Base URL producción: `https://nexusfi-production.up.railway.app/api/v1`

### Frontend: ¿usar API local o producción?

**Recomendación:** desarrolla el frontend contra la **API local (`dev`)** y usa la **API de producción** solo para smoke tests finales o demos.

#### Usa `dev` para desarrollo diario cuando:

- quieras datos de prueba sin afectar producción
- necesites reiniciar la base, registrar usuarios de prueba o experimentar libremente
- estés iterando rápido en UI, validaciones y flujos
- quieras depurar backend y frontend al mismo tiempo

#### Usa `prod` solo cuando:

- quieras confirmar que CORS, despliegue y variables reales están bien
- necesites revisar comportamiento final en Railway
- hagas una validación corta antes de publicar el frontend

#### Frontend en el mismo repo o en otro?

Para este proyecto, la opción más simple es **crear el frontend en otro repositorio** (por ejemplo `NexusFi-UI`) porque este repo ya está claramente organizado como backend Spring Boot.

Ventajas de separar:

- ciclos de despliegue independientes
- historial Git más limpio por stack
- configuración de build/deploy más simple para cada app
- menor riesgo de mezclar dependencias Java y Node en el mismo proyecto

Si estás trabajando solo y quieres máxima simplicidad local, también puedes usar un monorepo más adelante, pero **hoy la recomendación práctica es: backend en este repo, frontend en otro**.

#### Configuración sugerida para frontend local

- Backend local: `http://localhost:8080/api/v1`
- Frontend local: `http://localhost:3000`
- Producción backend: `https://nexusfi-production.up.railway.app/api/v1`

`CORS` ya permite `http://localhost:3000` por defecto, así que ese es el origen ideal para empezar tu UI.

| Resource | Endpoints | Descripción |
|----------|-----------|-------------|
| Auth | 2 | `register`, `login` |
| Categories | 9 | CRUD + tree + root + subcategories |
| Incomes | 3 | Record and query |
| Expenses | 4 | Record and query |
| Transfers | 4 | Execute zero-sum transfers |
| Movements | 4 | Read-only transaction history |

### Próximos Pasos

1. ~~Crear repositorios Spring Data JPA~~ ✅
2. ~~Implementar servicios de negocio~~ ✅
3. ~~Desarrollar controladores REST API~~ ✅
4. ~~Implementar Spring Security + JWT~~ ✅
5. ~~Implementar categorías jerárquicas~~ ✅
6. ~~Desplegar en producción (Railway)~~ ✅
7. Crear frontend (React + TypeScript)
8. Implementar tests unitarios e integración

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
Categorías Raíz (Nivel 1 - deben sumar 100%):
  - Gastos Fijos (60%) → $6,000
  - Ahorros (40%) → $4,000

Subcategorías de Gastos Fijos (Nivel 2 - pueden sumar ≤100%):
  - Renta (50%) → $3,000
  - Servicios (30%) → $1,800
  - [Resto 20%] → $1,200 queda en Gastos Fijos

Nota: Máximo 2 niveles. No se permiten sub-subcategorías.
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
