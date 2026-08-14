# HoraBase

HoraBase es una aplicación web multi-comercio para gestionar personas, turnos y asistencia. Incluye un panel administrativo, un portal móvil para empleados y un modo terminal (kiosk) para registrar entradas y salidas.

## Arquitectura

- Backend: Java 21, Spring Boot 4, Spring Security, JWT y JPA.
- Base de datos: PostgreSQL 16. H2 se usa únicamente en tests unitarios/de integración.
- Frontend: React 19, TypeScript, Vite y CSS responsive.
- Ejecución: Docker Compose con PostgreSQL, API y Nginx para el frontend.
- API: REST documentada con OpenAPI/Swagger.

La API es stateless. Los endpoints administrativos verifican rol `ADMIN` y el `businessId` firmado en el JWT; los empleados consumen `/api/me/**`, sin poder elegir otro empleado o comercio. Las terminales usan credenciales propias cuyo secreto solo se muestra al crear o rotar el dispositivo.

## Requisitos

Para la opción recomendada solo se necesita Docker Desktop con Compose. Para desarrollo sin contenedores se requiere además:

- JDK 21;
- Node.js 22 o 24 y npm;
- PostgreSQL 16;
- PowerShell 5.1+ en Windows para el E2E incluido.

## Inicio rápido con Docker

1. Copiar `.env.example` como `.env`.
2. Reemplazar `DB_PASSWORD` y generar `JWT_SECRET` en Base64 con al menos 32 bytes:

   ```bash
   openssl rand -base64 32
   ```

3. Para una base nueva, cambiar temporalmente `HORABASE_BOOTSTRAP_ENABLED=true`.
4. Levantar todo:

   ```bash
   docker compose up --build -d
   docker compose ps
   ```

5. Abrir:

   - Aplicación: `http://localhost:3000`
   - Kiosk: `http://localhost:3000/kiosk`
   - API: `http://localhost:8080`
   - Salud: `http://localhost:8080/api/health`
   - Swagger: `http://localhost:8080/swagger-ui.html` cuando `SWAGGER_ENABLED=true`

Después del primer arranque exitoso, volver a dejar `HORABASE_BOOTSTRAP_ENABLED=false`. El inicializador también se protege comprobando que la base no contenga comercios ni cuentas.

Para detener servicios sin borrar datos:

```bash
docker compose down
```

No usar `docker compose down -v` salvo que se quiera eliminar deliberadamente toda la base local.

## Demo opcional

`.env.example` contiene datos de ejemplo seguros para desarrollo:

- Comercio: `HoraBase Demo`
- Documento administrador: `45678901`
- Contraseña temporal: `AdminDemo123!`

La demo solo se crea si se activa `HORABASE_BOOTSTRAP_ENABLED=true` sobre una base vacía. En el primer login se exige reemplazar la contraseña temporal. Nunca debe habilitarse el bootstrap ni conservarse esa contraseña en producción.

Para poblar y comprobar automáticamente sector, empleado, turno, terminal, asistencia, incidencia, horas extra, solicitud y aislamiento multi-comercio sobre un entorno demo descartable:

```powershell
.\scripts\e2e.ps1
```

El E2E cambia las contraseñas demo a valores de prueba indicados en sus parámetros y crea registros; no debe ejecutarse sobre datos productivos.

## Desarrollo local

### Base y backend

Configurar las variables de `.env` en la shell o IDE. Si PostgreSQL ya usa el puerto 5432, establecer por ejemplo `POSTGRES_PORT=55432` y ajustar `DB_URL=jdbc:postgresql://localhost:55432/horabase`.

Para levantar solo PostgreSQL:

```bash
docker compose up -d postgres
```

Ejecutar la API:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux/macOS: `./mvnw spring-boot:run`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Vite abre `http://localhost:5173` y redirige `/api` y `/v3` a `VITE_DEV_API_URL` (por defecto `http://localhost:8080`). En la imagen Docker, Nginx usa el backend interno y el navegador conserva un único origen.

## Variables de entorno

| Variable | Uso |
| --- | --- |
| `DB_NAME`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL |
| `POSTGRES_PORT`, `API_PORT`, `FRONTEND_PORT` | Puertos publicados por Compose |
| `JWT_SECRET`, `JWT_ISSUER`, `JWT_EXPIRATION_MINUTES` | Firma y vigencia de tokens |
| `CORS_ALLOWED_ORIGINS` | Lista exacta de orígenes separados por coma |
| `SWAGGER_ENABLED` | Habilita documentación en el perfil `prod` |
| `HORABASE_BOOTSTRAP_*` | Comercio y admin iniciales, solo base vacía |
| `ATTENDANCE_SHIFT_MATCH_WINDOW_MINUTES` | Ventana de asociación automática de turnos |
| `PASSWORD_RESET_*`, `MAIL_FROM`, `SMTP_*` | Recuperación y correo |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring; Compose usa `prod` |

No se versionan `.env`, secretos, contraseñas reales ni archivos de configuración local. En producción, `CORS_ALLOWED_ORIGINS` debe contener dominios HTTPS explícitos; no se recomienda `*`.

## Login y roles

`POST /api/auth/login` recibe documento, contraseña y un `businessId` opcional. Si el mismo documento existe en más de un comercio, el identificador del comercio pasa a ser obligatorio.

- `ADMIN`: dashboard, empleados, perfiles, sectores, calendario/turnos, asistencias, incidencias, horas extra, solicitudes y terminales del propio comercio.
- `EMPLOYEE`: perfil, calendario, asistencias, incidencias, horas extra y solicitudes propias mediante `/api/me/**`.
- Terminal: no usa JWT; requiere `X-Terminal-Id` y `X-Terminal-Secret` en `/api/terminal/businesses/{businessId}/check-in|check-out`.

Una cuenta con `mustChangePassword=true` solo puede invocar el cambio de contraseña hasta completar ese paso. Las cuentas, comercios o terminales inactivas se rechazan incluso si una credencial anterior todavía existe.

## Kiosk

La ruta pública es `/kiosk`. El engranaje permite guardar en el almacenamiento local del dispositivo:

- ID del comercio;
- identificador de terminal;
- secreto generado por el administrador.

El secreto no vuelve a mostrarse en el panel administrativo. Si se pierde o compromete, debe rotarse; el valor anterior queda invalidado. El kiosk solo permite identificar al empleado y marcar entrada/salida, sin exponer navegación administrativa.

## Endpoints principales

| Área | Ruta |
| --- | --- |
| Autenticación | `/api/auth/login`, `/change-password`, `/forgot-password`, `/reset-password` |
| Dashboard | `/api/businesses/{businessId}/dashboard` |
| Empleados y perfiles | `/api/businesses/{businessId}/employees` |
| Sectores | `/api/businesses/{businessId}/sectors` |
| Turnos | `/api/businesses/{businessId}/shifts` |
| Asistencias | `/api/businesses/{businessId}/attendances` |
| Incidencias | `/api/businesses/{businessId}/incidents` |
| Horas extra | `/api/businesses/{businessId}/overtime` |
| Solicitudes admin | `/api/businesses/{businessId}/requests` |
| Portal empleado | `/api/me/profile`, `/calendar`, `/attendances`, `/incidents`, `/overtime`, `/requests` |
| Terminales | `/api/businesses/{businessId}/terminals` |
| Fichaje kiosk | `/api/terminal/businesses/{businessId}/check-in`, `/check-out` |

Las horas se transmiten como ISO-8601 con offset (`OffsetDateTime`) y las fechas como `yyyy-MM-dd`.

## Tests y validación

Backend completo:

```powershell
.\mvnw.cmd clean verify
```

Frontend:

```bash
cd frontend
npm run lint
npm run test
npm run build
```

Compose:

```bash
docker compose --env-file .env.example config
docker compose up --build -d
```

Los tests cubren autenticación, JWT, contraseña inicial, roles, acceso cross-business, terminal, doble entrada, salida sin entrada, empleados, turnos, solicitudes, horas extra, dashboard y respuestas de error.

## Estructura

```text
src/main/java/com/horabase/api/   Backend por módulos de negocio
src/main/resources/              Configuración Spring
src/test/                        Tests backend
frontend/src/                    Aplicación React
scripts/e2e.ps1                  Flujo real contra PostgreSQL/API
compose.yaml                     Stack completo
Dockerfile                       Imagen backend
frontend/Dockerfile              Imagen frontend
```

## Deployment

1. Usar contraseñas aleatorias y un `JWT_SECRET` exclusivo del entorno.
2. Configurar HTTPS en el proxy o plataforma y orígenes CORS exactos.
3. Mantener `HORABASE_BOOTSTRAP_ENABLED=false` y `SWAGGER_ENABLED=false`.
4. Usar PostgreSQL administrado o respaldar el volumen periódicamente.
5. Inyectar variables desde el gestor de secretos de la plataforma.
6. Ejecutar los builds y tests antes de desplegar.

Durante esta etapa Hibernate mantiene el esquema con `ddl-auto=update`, apropiado para el MVP y arranques limpios de Compose. Antes de una migración productiva con datos críticos se deben introducir migraciones versionadas (Flyway/Liquibase) y probar el plan de rollback.

## Troubleshooting

- Puerto ocupado: cambiar `POSTGRES_PORT`, `API_PORT` o `FRONTEND_PORT` en `.env`; no detener servicios ajenos.
- API no inicia: comprobar `docker compose logs backend`, la salud de PostgreSQL y que `JWT_SECRET` sea Base64 de 32 bytes o más.
- Login inicial no funciona: el bootstrap solo actúa sobre una base vacía; revisar `docker compose logs backend`.
- Swagger devuelve 404: establecer `SWAGGER_ENABLED=true` y recrear backend.
- Frontend no llega a la API: en desarrollo revisar `VITE_DEV_API_URL`; en Docker comprobar la salud de `backend` y `frontend`.
- Terminal rechazada: verificar comercio, identificador, secreto y que tanto comercio como terminal estén activos.
