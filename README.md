# PoolGuard camera integration

This repository contains two separate programs:

- `PoolGuardServer/` is the HTTPS-facing API deployed to Render. It accepts JPEG frames from the local camera client and keeps only the most recent frame in memory.
- `PoolGuardBackend/` is the Java desktop client that reads the phone camera on the same Wi-Fi network and forwards up to four JPEG frames per second to the API.

## Render API service

Use the repository root as the service root and `Dockerfile` as the Dockerfile. Set this secret in the Render service:

- `POOLGUARD_INGEST_TOKEN`: a random secret of at least 32 characters.

The local camera client estimates motion from consecutive real JPEG frames and
publishes `RED`, `YELLOW`, or `GREEN` to `/api/cameras/{cameraId}/motion` using
the same bearer token. The ESP32 polls that protected endpoint and controls its
traffic-light LEDs and buzzer. `RED` means no visible movement, `YELLOW` means
moderate movement, and `GREEN` means active movement. The estimator compares
pixels in the whole image; it does not identify a specific person or mannequin.

Render provides `PORT`. The API exposes `GET /health`, `POST /api/cameras/{cameraId}/frames`, token-protected frame retrieval, and token-protected `POST`/`GET /api/cameras/{cameraId}/motion` for the latest movement state.

The server stores only the latest frame in memory. Frames are not written to disk or a database. If the service restarts or spins down, the client sends a new frame after reconnecting.

## Run the camera client on the local computer

The phone and computer running the client must be on the same Wi-Fi network. In IP Webcam, start the server and use the stream URL shown by the app. The supplied screenshot shows `http://192.168.101.11:8080/video`.

In PowerShell, set these values on the computer that can reach the phone:

```powershell
$env:POOLGUARD_CAMERA_URL = 'http://192.168.101.11:8080/video'
$env:POOLGUARD_BACKEND_URL = 'https://YOUR-RENDER-SERVICE.onrender.com'
$env:POOLGUARD_INGEST_TOKEN = 'same secret configured in Render'
$env:POOLGUARD_CAMERA_ID = 'phone-camera'
mvn -f PoolGuardBackend/pom.xml package
java -Djava.awt.headless=false -jar PoolGuardBackend/target/poolguard-backend-1.0.0.jar
```

Replace the Render hostname with the URL shown for the API service. Keep the token private. Do not put the phone's `192.168...` URL in Render; only the local client can reach that address.

The client forwards actual JPEG frames. It does not create detections. Risk analysis remains in `POOLGUARD_RISK_API`; without a configured real analyzer, the status stays in the waiting state.
