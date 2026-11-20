package com.poolguard.server;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CameraFrameStore {
    private final ConcurrentHashMap<String, CameraFrame> latestFrames = new ConcurrentHashMap<>();

    public synchronized void put(String cameraId, byte[] jpeg, Instant receivedAt) {
        latestFrames.put(cameraId, new CameraFrame(jpeg, receivedAt));
        notifyAll();
    }

    public Optional<CameraFrame> getLatest(String cameraId) {
        return Optional.ofNullable(latestFrames.get(cameraId));
    }

    public synchronized Optional<CameraFrame> awaitNext(String cameraId, Instant after, long timeoutMillis)
            throws InterruptedException {
        long deadline = System.nanoTime() + timeoutMillis * 1_000_000;
        while (true) {
            Optional<CameraFrame> latest = getLatest(cameraId);
            if (latest.isPresent() && latest.get().receivedAt().isAfter(after)) {
                return latest;
            }
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                return Optional.empty();
            }
            long waitMillis = Math.max(1, remainingNanos / 1_000_000);
            wait(waitMillis);
        }
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
