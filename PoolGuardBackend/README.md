# PoolGuard Camera Monitor

Aplicacion Java de escritorio para conectarse a una camara IP real, mostrar su stream MJPEG y enviar fotogramas reales a un servicio de analisis de riesgo. No contiene generadores de datos, estados aleatorios ni lecturas simuladas.

## Ejecutar

Requiere Java 21 o superior. Desde esta carpeta:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp out com.poolguard.app.PoolGuardApplication
```

La camara de la captura se configura por defecto como `http://192.168.101.11:8080/video`. Antes de ejecutar confirma que el computador esta en la misma red Wi-Fi y que la aplicacion IP Webcam tiene iniciado el servidor. Si la ruta de video de tu telefono es distinta, configura:

```powershell
$env:POOLGUARD_CAMERA_URL='http://192.168.101.11:8080/video'
```

El frontend incluye Dashboard, Pools, Cameras, Active Alerts, Incidents y Users. Estas pantallas no crean registros de prueba: se conectaran al backend real cuando se configure su URL.

```powershell
$env:POOLGUARD_BACKEND_URL='https://api.your-domain.example'
```

El analisis de peligro no se inventa. Para habilitarlo configura un servicio de IA real:

```powershell
$env:POOLGUARD_RISK_API='https://your-real-ai-service.example/api/v1/risk'
```

Por cada JPEG real el servicio debe devolver JSON con esta forma:

```json
{"level": 0, "confidence": 0.98, "reason": "No risk detected"}
```

`level` acepta `0` (normal), `1` (caution) o `2` (danger). Sin esa API la aplicacion conserva el estado **Waiting for real analysis**; no muestra verde como si no hubiera peligro.

De manera opcional se puede exigir una clave local antes de abrir la camara:

```powershell
$env:POOLGUARD_OPERATOR_KEY='your-secret'
$env:POOLGUARD_SESSION_KEY='your-secret'
```

## Patrones implementados y para que sirven

| Patron | Uso en PoolGuard | Para que sirve |
|---|---|---|
| State | `PoolSafetyState` | Cambia de forma controlada entre espera, verde, amarillo y rojo. |
| Strategy | `RiskAnalysisStrategy` | Permite cambiar el proveedor/modelo de IA sin reescribir la camara ni la interfaz. |
| Observer | `FrameObserver` y `SafetyObserver` | Actualiza video, alertas e historial cuando llega un fotograma o cambia el riesgo. |
| Factory Method | `CameraClientFactory` | Crea el cliente adecuado segun el protocolo de la camara. |
| Facade | `PoolMonitoringFacade` | Da un unico punto para iniciar/detener camara, analisis y notificaciones. |
| Singleton | `ApplicationContext` | Mantiene una configuracion consistente para toda la aplicacion. |
| Adapter | `CameraFrameSourceAdapter` | Convierte el cliente de camara en una fuente que consume el modulo de IA. |
| Proxy | `SecuredCameraClientProxy` | Aplica control de acceso antes de permitir abrir una camara. |

## Siguiente paso para nube

Este cliente se ejecuta cerca de la camara, dentro de la red local. El backend Java en la nube recibira los eventos y resultados de IA por HTTPS; la nube no puede abrir directamente una IP privada como `192.168.101.11`.
