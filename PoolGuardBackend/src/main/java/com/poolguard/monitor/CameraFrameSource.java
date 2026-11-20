package com.poolguard.monitor;

import com.poolguard.camera.CameraClient;

public interface CameraFrameSource {
    void subscribe(FrameConsumer consumer);
    @FunctionalInterface interface FrameConsumer { void accept(CameraClient.Frame frame); }
}
