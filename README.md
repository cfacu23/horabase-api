# HoraBase API

HoraBase es el backend REST de una plataforma multi-comercio para gestionar
empleados, sectores, turnos, asistencias, incidencias, solicitudes y horas
extra. Está pensado para administradores web, empleados desde dispositivos
móviles y terminales físicas de marcación.

## Arquitectura y tecnologías

- Java 21 y Spring Boot 4.1.
- Spring MVC, Jakarta Validation y manejo global de errores.
- Spring Data JPA con PostgreSQL 16.
- Spring Security stateless con JWT HS256 y roles `ADMIN` / `EMPLOYEE`.
- Credenciales cifradas con `PasswordEncoder`; secretos de terminal con BCrypt.
- OpenAPI/Swagger UI.
- Maven Wrapper, Docker Compose y H2 únicamente para tests.

El código se organiza por módulo de negocio. Cada paquete contiene entidad,
repositorio, servicio, controlador y DTOs cuando corresponde. Todas las
operaciones administrativas validan el `businessId` del JWT y todos los
recursos relacionados se vuelven a comprobar contra el comercio.

## Requisitos

- JDK 21.
- Docker Desktop o una instancia PostgreSQL 16.
- PowerShell en Windows, o una shell POSIX en Linux/macOS.

## Inicio desde cero

1. Copiar `.env.example` a `.env` y sustituir todos los valores `replace-*`.
2. Generar `JWT_SECRET` en Base64 con al menos 32 bytes, por ejemplo:
   `openssl rand -base64 32`.
3. Iniciar PostgreSQL: `docker compose --env-file .env up -d`.
4. Exportar las variables del `.env` en la shell que ejecutará Java.
5. Ejecutar `./mvnw spring-boot:run` o, en Windows,
   `.\mvnw.cmd spring-boot:run`.
6. Comprobar `GET http://localhost:8080/api/health`.

Para crear el primer comercio y administrador, establecer temporalmente
`HORABASE_BOOTSTRAP_ENABLED=true` y completar las demás variables
`HORABASE_BOOTSTRAP_*`. El inicializador solo actúa cuando no existe ningún
comercio ni cuenta. Después del primer arranque debe volver a `false`.

La configuración mantiene `spring.jpa.hibernate.ddl-auto=update` durante esta
fase. Antes de producción se debe reemplazar por migraciones versionadas con
Flyway o Liquibase.

## Variables de entorno

| Variable | Propósito |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión PostgreSQL |
| `JWT_SECRET`, `JWT_ISSUER`, `JWT_EXPIRATION_MINUTES` | Firma y vigencia JWT |
| `ATTENDANCE_SHIFT_MATCH_WINDOW_MINUTES` | Ventana para asociar turno al fichaje |
| `PASSWORD_RESET_EXPIRATION_MINUTES` | Vigencia del token de recuperación |
| `PASSWORD_RESET_BASE_URL` | URL del futuro frontend de recuperación |
| `MAIL_FROM`, `SMTP_*` | Transporte de correo |
| `HORABASE_BOOTSTRAP_*` | Alta inicial opcional y de un solo uso |

No se deben versionar `.env`, contraseñas, API keys ni secretos reales.

## Autenticación

`POST /api/auth/login` recibe documento, contraseña y `businessId` opcional.
Cuando un documento existe en más de un comercio, `businessId` es obligatorio.
La respuesta incluye el Bearer token, expiración, rol, comercio, empleado y
`mustChangePassword`.

- `POST /api/auth/change-password`: valida la contraseña actual.
- `POST /api/auth/forgot-password`: respuesta neutra para evitar enumeración.
- `POST /api/auth/reset-password`: consume una sola vez un token con vencimiento.

Las credenciales desactivadas se rechazan incluso cuando el JWT todavía no
venció. Swagger permite autorizar llamadas con `Bearer <token>`.

## Terminales físicas

Un administrador registra dispositivos en
`/api/businesses/{businessId}/terminals`. El secreto solo se devuelve al crear
o rotar y en base de datos se conserva su hash BCrypt.

Los fichajes de terminal requieren:

- `X-Terminal-Id: <identificador>`
- `X-Terminal-Secret: <secreto>`

El filtro verifica dispositivo activo, comercio, secreto y coincidencia con el
`businessId` de la ruta; además actualiza `lastSeenAt`.

## Endpoints principales

Todos los endpoints `/api/businesses/{businessId}/...` requieren rol `ADMIN`
y pertenencia al comercio.

| Área | Endpoints |
| --- | --- |
| Comercios | `GET/PUT /api/businesses...` (solo el comercio del JWT) |
| Sectores | `/api/businesses/{businessId}/sectors` |
| Empleados | `/api/businesses/{businessId}/employees` |
| Perfil integral | `GET /api/businesses/{businessId}/employees/{employeeId}/profile` |
| Turnos/calendario | `/api/businesses/{businessId}/shifts?from=&to=&employeeId=&sectorId=` |
| Asistencias | `/api/businesses/{businessId}/attendances` y acciones de corrección/anulación |
| Terminal | `POST /api/terminal/businesses/{businessId}/check-in` y `check-out` |
| Incidencias | `/api/businesses/{businessId}/incidents` |
| Horas extra | `/api/businesses/{businessId}/overtime` y acciones `approve`, `reject`, `pay` |
| Solicitudes | `/api/businesses/{businessId}/requests` y acciones `approve`, `reject` |
| Dashboard | `GET /api/businesses/{businessId}/dashboard` |
| Empleado | `/api/me/profile`, `/calendar`, `/attendances`, `/incidents`, `/overtime`, `/requests` |

Los intervalos con hora usan ISO-8601 con offset (`OffsetDateTime`) y las fechas
sin hora usan `yyyy-MM-dd`. Las consultas de turnos detectan intersección con el
período, por lo que incluyen correctamente turnos nocturnos.

## Swagger y errores

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Los errores usan una respuesta compacta con `timestamp`, `status`, `error`,
`message`, `path` y, para validaciones, un mapa `errors`. Nunca se incluyen
stack traces en la respuesta.

## Tests y comandos Maven

```text
./mvnw test
./mvnw clean verify
./mvnw spring-boot:run
```

En Windows reemplazar `./mvnw` por `.\mvnw.cmd`. Los tests usan H2 en modo de
compatibilidad PostgreSQL y no requieren Docker.

## Flujo Git

`dev` es la rama de integración. Cada módulo se desarrolla en una rama
`feature/...`, se valida con tests y se integra en `dev`. `main` queda reservada
para versiones preparadas para publicación.

## Contrato para el futuro frontend

La experiencia administrador consumirá dashboard, empleados/perfiles,
calendario, asistencias, incidencias, solicitudes, horas extra, sectores y
terminales. La experiencia empleado debe usar exclusivamente `/api/me/**`, de
modo que el navegador nunca decide ni envía el identificador del empleado.
