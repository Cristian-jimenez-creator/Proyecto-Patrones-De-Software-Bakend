package com.poolguard.monitor;

import com.poolguard.camera.CameraClient;

public final class CameraFrameSourceAdapter implements CameraFrameSource {
    private final CameraClient camera;
    public CameraFrameSourceAdapter(CameraClient camera) { this.camera = camera; }
    @Override public void subscribe(FrameConsumer consumer) { camera.addFrameObserver(consumer::accept); }
}
