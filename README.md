# Smart Pool Security — Backend

API REST desarrollada con Java 17 y Spring Boot. Contiene la lógica de negocio, autenticación, acceso a datos y conexión a H2 en local o PostgreSQL en Render. La interfaz web vive en el repositorio `Proyecto-Patrones-De-Software-Frontend` y no está incluida aquí.

## Ejecutar localmente

Requiere Java 17 y Maven. Desde la raíz:

```powershell
mvn spring-boot:run
```

La aplicación local usa H2 y, por defecto, deja la autenticación desactivada para facilitar la demostración.

## API

- `GET /health`: estado del servicio.
- `GET /api/pools`, `/api/cameras`, `/api/alerts`, `/api/statistics` y `/api/users`: datos del panel.
- Las rutas de escritura y el inicio de sesión usan sesión y protección CSRF cuando `SMARTPOOL_AUTH_ENABLED=true`.
- `GET /api/alerts/stream`: eventos de alertas mediante SSE.

## Variables de entorno en Render

El perfil `prod` requiere `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` para PostgreSQL. Además:

- `SMARTPOOL_AUTH_ENABLED=true`
- `SMARTPOOL_AUTH_USERNAME` y `SMARTPOOL_AUTH_PASSWORD` (contraseña larga y privada)
- `SMARTPOOL_CORS_ALLOWED_ORIGINS`: origen exacto del frontend, por ejemplo `https://NOMBRE-DEL-SITIO.onrender.com`.

No publiques contraseñas ni la URL privada de conexión a PostgreSQL.

## Alcance de la demo

El analizador actual es simulado; el backend no recibe video de cámaras ni detecta ahogamientos reales.
