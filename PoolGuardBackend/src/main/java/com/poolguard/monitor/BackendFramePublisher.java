package com.poolguard.monitor;

import com.poolguard.camera.CameraClient;
import com.poolguard.camera.FrameObserver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** Sends up to four real camera frames per second to the configured HTTPS API. */
public final class BackendFramePublisher implements FrameObserver {
    private static final long MIN_UPLOAD_INTERVAL_MILLIS = 250;
    private final URI endpoint;
    private final String token;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();
    private final AtomicLong lastUploadAt = new AtomicLong();
    private final AtomicBoolean uploadInFlight = new AtomicBoolean();
    private final URI motionEndpoint;
    private final MotionClassifier motionClassifier = new MotionClassifier();
    private final AtomicLong lastMotionPublishAt = new AtomicLong();
    private volatile String lastMotionState = "";
    private final AtomicBoolean motionUploadInFlight = new AtomicBoolean();

    public BackendFramePublisher(URI backendUrl, String token, String cameraId) {
        if (backendUrl != null && !usesHttpsOrLoopback(backendUrl)) {
            throw new IllegalArgumentException("POOLGUARD_BACKEND_URL must use HTTPS outside the local computer");
        }
        this.endpoint = backendUrl == null ? null
                : backendUrl.resolve("/api/cameras/" + cameraId + "/frames");
        this.motionEndpoint = backendUrl == null ? null
                : backendUrl.resolve("/api/cameras/" + cameraId + "/motion");
        this.token = token;
    }

    private boolean usesHttpsOrLoopback(URI uri) {
        return "https".equalsIgnoreCase(uri.getScheme())
                || ("http".equalsIgnoreCase(uri.getScheme())
                    && ("localhost".equalsIgnoreCase(uri.getHost())
                        || "127.0.0.1".equals(uri.getHost())
                        || "::1".equals(uri.getHost())));
    }

    @Override
    public void onFrame(CameraClient.Frame frame) {
        if (motionEndpoint != null && token != null && !token.isBlank()) {
            publishMotion(motionClassifier.classify(frame));
        }
        if (endpoint == null || token == null || token.isBlank()) return;
        if (!uploadInFlight.compareAndSet(false, true)) return;

        long now = System.currentTimeMillis();
        long previous = lastUploadAt.get();
        if (now - previous < MIN_UPLOAD_INTERVAL_MILLIS
                || !lastUploadAt.compareAndSet(previous, now)) {
            uploadInFlight.set(false);
            return;
        }

        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "image/jpeg")
                .header("X-Captured-At", Long.toString(frame.capturedAtMillis()))
                .POST(HttpRequest.BodyPublishers.ofByteArray(frame.jpeg()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .whenComplete((response, failure) -> {
                    uploadInFlight.set(false);
                    if (failure != null) {
                        System.err.println("Camera frame upload failed: " + failure.getMessage());
                    } else if (response.statusCode() / 100 != 2) {
                        System.err.println("Camera frame upload returned HTTP " + response.statusCode());
                    }
                });
    }

    private void publishMotion(MotionClassifier.Result result) {
        long now = System.currentTimeMillis();
        if (result.state().equals(lastMotionState) && now - lastMotionPublishAt.get() < 1_000) return;
        if (!motionUploadInFlight.compareAndSet(false, true)) return;

        lastMotionState = result.state();
        lastMotionPublishAt.set(now);
        String json = "{\"state\":\"" + result.state() + "\",\"motionPercent\":"
                + result.motionPercent() + "}";
        HttpRequest request = HttpRequest.newBuilder(motionEndpoint)
                .timeout(Duration.ofSeconds(8))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .whenComplete((response, failure) -> {
                    motionUploadInFlight.set(false);
                    if (failure != null) {
                        System.err.println("Motion state upload failed: " + failure.getMessage());
                    } else if (response.statusCode() / 100 != 2) {
                        System.err.println("Motion state upload returned HTTP " + response.statusCode());
                    }
                });
    }
}
