package com.poolguard.camera;

public final class SecuredCameraClientProxy implements CameraClient {
    private final CameraClient delegate;
    private final String requiredKey;
    private final String presentedKey;

    public SecuredCameraClientProxy(CameraClient delegate, String requiredKey, String presentedKey) {
        this.delegate = delegate;
        this.requiredKey = requiredKey;
        this.presentedKey = presentedKey;
    }

    @Override
    public void start() {
        if (requiredKey != null && !requiredKey.isBlank() && !requiredKey.equals(presentedKey)) {
            throw new SecurityException("Camera access denied for this session");
        }
        delegate.start();
    }

    @Override public void stop() { delegate.stop(); }
    @Override public void addFrameObserver(FrameObserver observer) { delegate.addFrameObserver(observer); }
    @Override public void removeFrameObserver(FrameObserver observer) { delegate.removeFrameObserver(observer); }
    @Override public boolean isRunning() { return delegate.isRunning(); }
}
