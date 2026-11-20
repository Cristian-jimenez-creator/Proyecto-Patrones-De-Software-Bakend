package com.poolguard.camera;

import java.net.URI;

public final class CameraClientFactory {
    private CameraClientFactory() {
    }

    public static CameraClient create(URI streamUrl) {
        String scheme = streamUrl.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return new MjpegCameraClient(streamUrl);
        }
        throw new IllegalArgumentException("Unsupported camera protocol: " + scheme);
    }
}
