package com.poolguard.camera;

import java.awt.image.BufferedImage;

public interface CameraClient {
    void start();
    void stop();
    void addFrameObserver(FrameObserver observer);
    void removeFrameObserver(FrameObserver observer);
    boolean isRunning();
    record Frame(BufferedImage image, byte[] jpeg, long capturedAtMillis) { }
}
