# Smart Pool Security — Backend

API REST desarrollada con Java 17 y Spring Boot. El código fuente, Maven y el esquema relacional están dentro de `backend/`. Este repositorio conserva en la raíz el README, los archivos de Git y el Dockerfile que usa Render. La interfaz web está en [Proyecto-Patrones-De-Software-Frontend](https://github.com/Cristian-jimenez-creator/Proyecto-Patrones-De-Software-Frontend), dentro de `frontend/`.

## Ejecutar localmente

Requiere Java 17 y Maven. Desde la raíz del repositorio:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

La aplicación local usa H2 y, por defecto, deja la autenticación desactivada para facilitar la demostración.

## API

- `GET /health`: estado del servicio.
- `GET /api/pools`, `/api/cameras`, `/api/alerts`, `/api/statistics` y `/api/users`: datos del panel.
- Las rutas de escritura y el inicio de sesión usan sesión y protección CSRF cuando `SMARTPOOL_AUTH_ENABLED=true`.
- `GET /api/alerts/stream`: eventos de alertas mediante SSE.

El esquema SQL de referencia está en `backend/modelo_piscina.sql`.

## Variables de entorno en Render

El perfil `prod` requiere `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` para PostgreSQL. Además:

- `SMARTPOOL_AUTH_ENABLED=true`
- `SMARTPOOL_AUTH_USERNAME` y `SMARTPOOL_AUTH_PASSWORD` (contraseña larga y privada)
- `SMARTPOOL_CORS_ALLOWED_ORIGINS`: origen exacto del frontend.

El Dockerfile de la raíz construye el código desde `backend/`. La configuración del servicio de Render existente no se cambia con esta reorganización. No publiques contraseñas ni la URL privada de conexión a PostgreSQL.

## Wiki y documentación del proyecto

- [Wiki del proyecto](https://github.com/Cristian-jimenez-creator/Proyecto-final-Patrones-De-Software/wiki)
- [API REST](https://github.com/Cristian-jimenez-creator/Proyecto-final-Patrones-De-Software/wiki/API-REST)
- [Arquitectura y estado](https://github.com/Cristian-jimenez-creator/Proyecto-final-Patrones-De-Software/wiki/Arquitectura)
- [Modelo de datos](https://github.com/Cristian-jimenez-creator/Proyecto-final-Patrones-De-Software/wiki/Modelo-de-datos)
- [Ejecución local](https://github.com/Cristian-jimenez-creator/Proyecto-final-Patrones-De-Software/wiki/Desarrollo-local)
- [Despliegue en Render](https://github.com/Cristian-jimenez-creator/Proyecto-final-Patrones-De-Software/wiki/Despliegue-en-Render)
- [Alcance y requisitos](https://github.com/Cristian-jimenez-creator/Proyecto-final-Patrones-De-Software/wiki/Alcance-y-requisitos)

## Alcance de la demo

El analizador actual es simulado; el backend no recibe video de cámaras ni detecta ahogamientos reales.
