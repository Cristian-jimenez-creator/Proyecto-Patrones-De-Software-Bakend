package com.poolguard.server;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CameraFrameStore {
    private final ConcurrentHashMap<String, CameraFrame> latestFrames = new ConcurrentHashMap<>();

    public void put(String cameraId, byte[] jpeg, Instant receivedAt) {
        latestFrames.put(cameraId, new CameraFrame(jpeg, receivedAt));
    }

    public Optional<CameraFrame> getLatest(String cameraId) {
        return Optional.ofNullable(latestFrames.get(cameraId));
    }

    public record CameraFrame(byte[] jpeg, Instant receivedAt) {
        public CameraFrame {
            jpeg = jpeg.clone();
        }

        @Override
        public byte[] jpeg() {
            return jpeg.clone();
        }
    }
}
