package com.poolguard.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestController
@RequestMapping("/api/cameras/{cameraId}/frames")
public class CameraFrameController {
    private static final int MAX_FRAME_BYTES = 2 * 1024 * 1024;
    private static final Duration ONLINE_WINDOW = Duration.ofSeconds(5);

    private final CameraFrameStore store;
    private final byte[] expectedToken;

    public CameraFrameController(CameraFrameStore store,
                                 @Value("${poolguard.ingest-token:}") String token) {
        this.store = store;
        this.expectedToken = token.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping(consumes = "image/jpeg")
    public ResponseEntity<Void> receiveFrame(@PathVariable String cameraId,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                             @RequestBody byte[] jpeg) {
        requireValidCameraId(cameraId);
        requireToken(authorization);
        if (jpeg.length < 4 || jpeg.length > MAX_FRAME_BYTES
                || (jpeg[0] & 0xff) != 0xff || (jpeg[1] & 0xff) != 0xd8
                || (jpeg[jpeg.length - 2] & 0xff) != 0xff || (jpeg[jpeg.length - 1] & 0xff) != 0xd9) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Expected one JPEG frame of at most 2 MiB");
        }
        store.put(cameraId, jpeg, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    public Map<String, Object> latestMetadata(@PathVariable String cameraId,
                                              @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        requireValidCameraId(cameraId);
        requireToken(authorization);
        CameraFrameStore.CameraFrame frame = store.getLatest(cameraId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No camera frame received yet"));
        long ageMillis = Duration.between(frame.receivedAt(), Instant.now()).toMillis();
        return Map.of("cameraId", cameraId, "receivedAt", frame.receivedAt().toString(),
                "ageMillis", Math.max(0, ageMillis), "online", ageMillis <= ONLINE_WINDOW.toMillis());
    }

    @GetMapping(value = "/latest.jpg", produces = "image/jpeg")
    public ResponseEntity<byte[]> latestJpeg(@PathVariable String cameraId,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        requireValidCameraId(cameraId);
        requireToken(authorization);
        CameraFrameStore.CameraFrame frame = store.getLatest(cameraId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No camera frame received yet"));
        long ageMillis = Duration.between(frame.receivedAt(), Instant.now()).toMillis();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.noStore())
                .header("X-Frame-Received-At", frame.receivedAt().toString())
                .header("X-Camera-Online", Boolean.toString(ageMillis <= ONLINE_WINDOW.toMillis()))
                .body(frame.jpeg());
    }

    @GetMapping("/stream.mjpg")
    public ResponseEntity<StreamingResponseBody> liveStream(
            @PathVariable String cameraId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        requireValidCameraId(cameraId);
        requireToken(authorization);

        StreamingResponseBody stream = output -> {
            Instant lastSentAt = Instant.MIN;
            CameraFrameStore.CameraFrame lastFrame = null;
            while (!Thread.currentThread().isInterrupted()) {
                java.util.Optional<CameraFrameStore.CameraFrame> next;
                try {
                    next = store.awaitNext(cameraId, lastSentAt, 2_000);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (next.isPresent()) {
                    lastFrame = next.get();
                    lastSentAt = lastFrame.receivedAt();
                }
                if (lastFrame == null) {
                    continue;
                }

                byte[] jpeg = lastFrame.jpeg();
                output.write(("--frame\r\nContent-Type: image/jpeg\r\nContent-Length: "
                        + jpeg.length + "\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
                output.write(jpeg);
                output.write("\r\n".getBytes(StandardCharsets.US_ASCII));
                output.flush();
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("multipart/x-mixed-replace; boundary=frame"))
                .cacheControl(CacheControl.noStore())
                .body(stream);
    }

    private void requireToken(String authorization) {
        String supplied = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length()) : "";
        byte[] suppliedBytes = supplied.getBytes(StandardCharsets.UTF_8);
        if (expectedToken.length < 32 || !MessageDigest.isEqual(expectedToken, suppliedBytes)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Camera ingest is not configured or token is invalid");
        }
    }

    private void requireValidCameraId(String cameraId) {
        if (!cameraId.matches("[A-Za-z0-9._-]{1,80}")) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Invalid camera id");
        }
    }
}
