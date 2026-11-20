package com.poolguard.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@RestController
@RequestMapping("/api/cameras/{cameraId}/motion")
public class MotionStateController {
    private final byte[] expectedToken;
    private final ConcurrentHashMap<String, MotionState> states = new ConcurrentHashMap<>();

    public MotionStateController(@Value("${poolguard.ingest-token:}") String token) {
        this.expectedToken = token.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping
    public ResponseEntity<Void> update(@PathVariable String cameraId,
                                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                       @RequestBody MotionUpdate update) {
        requireAccess(cameraId, authorization);
        if (update == null || update.state() == null
                || !update.state().matches("RED|YELLOW|GREEN")
                || !Double.isFinite(update.motionPercent())
                || update.motionPercent() < 0 || update.motionPercent() > 100) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Invalid motion state");
        }
        states.put(cameraId, new MotionState(update.state(), update.motionPercent(), Instant.now()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public Map<String, Object> current(@PathVariable String cameraId,
                                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        requireAccess(cameraId, authorization);
        MotionState state = states.getOrDefault(cameraId, new MotionState("RED", 0, Instant.EPOCH));
        long ageMillis = state.updatedAt().equals(Instant.EPOCH)
                ? Long.MAX_VALUE : Math.max(0, Duration.between(state.updatedAt(), Instant.now()).toMillis());
        return Map.of("state", state.state(), "motionPercent", state.motionPercent(),
                "updatedAt", state.updatedAt().toString(), "ageMillis", ageMillis);
    }

    private void requireAccess(String cameraId, String authorization) {
        if (!cameraId.matches("[A-Za-z0-9._-]{1,80}")) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY, "Invalid camera id");
        }
        String supplied = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length()) : "";
        if (expectedToken.length < 32 || !MessageDigest.isEqual(expectedToken,
                supplied.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(UNAUTHORIZED, "Motion API token is invalid");
        }
    }

    public record MotionUpdate(String state, double motionPercent) { }
    private record MotionState(String state, double motionPercent, Instant updatedAt) { }
}
