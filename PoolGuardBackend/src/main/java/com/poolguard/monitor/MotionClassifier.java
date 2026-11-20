package com.poolguard.monitor;

import com.poolguard.camera.CameraClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/** Estimates scene motion from consecutive real camera frames. */
public final class MotionClassifier {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 48;
    private static final int PIXEL_DELTA = 22;
    private byte[] previous;

    public synchronized Result classify(CameraClient.Frame frame) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(frame.jpeg()));
            if (image == null) return new Result("RED", 0);

            byte[] current = sample(image);
            if (previous == null) {
                previous = current;
                return new Result("RED", 0);
            }

            int changed = 0;
            for (int i = 0; i < current.length; i++) {
                if (Math.abs((current[i] & 0xff) - (previous[i] & 0xff)) >= PIXEL_DELTA) changed++;
            }
            previous = current;

            double percent = changed * 100.0 / current.length;
            String state = percent < 0.8 ? "RED" : percent < 4.5 ? "YELLOW" : "GREEN";
            return new Result(state, Math.round(percent * 100.0) / 100.0);
        } catch (Exception ignored) {
            return new Result("RED", 0);
        }
    }

    private byte[] sample(BufferedImage image) {
        byte[] values = new byte[WIDTH * HEIGHT];
        for (int y = 0; y < HEIGHT; y++) {
            int sourceY = Math.min(image.getHeight() - 1, (y * image.getHeight()) / HEIGHT);
            for (int x = 0; x < WIDTH; x++) {
                int sourceX = Math.min(image.getWidth() - 1, (x * image.getWidth()) / WIDTH);
                int rgb = image.getRGB(sourceX, sourceY);
                int red = (rgb >>> 16) & 0xff;
                int green = (rgb >>> 8) & 0xff;
                int blue = rgb & 0xff;
                values[y * WIDTH + x] = (byte) ((red * 30 + green * 59 + blue * 11) / 100);
            }
        }
        return values;
    }

    public record Result(String state, double motionPercent) { }
}
