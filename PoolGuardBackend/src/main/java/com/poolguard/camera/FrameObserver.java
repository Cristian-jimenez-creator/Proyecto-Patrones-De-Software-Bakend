package com.poolguard.camera;

@FunctionalInterface
public interface FrameObserver {
    void onFrame(CameraClient.Frame frame);
}
