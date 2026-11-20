package com.poolguard.camera;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MjpegCameraClient implements CameraClient {
    private final URI streamUrl;
    private final List<FrameObserver> observers = new CopyOnWriteArrayList<>();
    private volatile boolean running;
    private volatile HttpURLConnection connection;

    public MjpegCameraClient(URI streamUrl) {
        this.streamUrl = streamUrl;
    }

    @Override
    public void start() {
        if (running) return;
        running = true;
        Thread.ofVirtual().name("mjpeg-camera-reader").start(this::readStream);
    }

    @Override
    public void stop() {
        running = false;
        HttpURLConnection activeConnection = connection;
        if (activeConnection != null) activeConnection.disconnect();
    }

    @Override public void addFrameObserver(FrameObserver observer) { observers.add(observer); }
    @Override public void removeFrameObserver(FrameObserver observer) { observers.remove(observer); }
    @Override public boolean isRunning() { return running; }

    private void readStream() {
        while (running) {
            try {
                connection = (HttpURLConnection) streamUrl.toURL().openConnection();
                connection.setConnectTimeout(8_000);
                connection.setReadTimeout(0);
                connection.setRequestProperty("Accept", "multipart/x-mixed-replace");
                connection.connect();
                if (connection.getResponseCode() != 200) {
                    throw new IOException("Camera returned HTTP " + connection.getResponseCode());
                }
                try (InputStream input = connection.getInputStream()) {
                    extractJpegs(input);
                }
            } catch (IOException exception) {
                if (running) {
                    System.err.println("Camera connection failed; retrying: " + exception.getMessage());
                }
            } finally {
                HttpURLConnection activeConnection = connection;
                if (activeConnection != null) activeConnection.disconnect();
                connection = null;
            }

            if (running) {
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
        }
    }

    private void extractJpegs(InputStream input) throws IOException {
        ByteArrayOutputStream jpeg = null;
        int previous = -1;
        int current;
        while (running && (current = input.read()) != -1) {
            if (previous == 0xFF && current == 0xD8) {
                jpeg = new ByteArrayOutputStream();
                jpeg.write(0xFF);
                jpeg.write(0xD8);
            } else if (jpeg != null) {
                jpeg.write(current);
                if (previous == 0xFF && current == 0xD9) {
                    publish(jpeg.toByteArray());
                    jpeg = null;
                }
            }
            previous = current;
        }
    }

    private void publish(byte[] jpeg) {
        try {
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(jpeg));
            if (image == null) return;
            Frame frame = new Frame(image, jpeg, System.currentTimeMillis());
            observers.forEach(observer -> observer.onFrame(frame));
        } catch (IOException ignored) {
            // Invalid bytes are ignored; no image is fabricated.
        }
    }
}
