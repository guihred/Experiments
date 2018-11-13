package paintexp;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import utils.HasLogging;

public final class SimplePixelReader implements PixelReader {
    private final Color onlyColor;

    public SimplePixelReader(Color white) {
        onlyColor = white;
    }

    @Override
    public int getArgb(int x, int y) {
        int b = (int) (onlyColor.getBlue() * 255);
        int r = (int) (onlyColor.getRed() * 255);
        int g = (int) (onlyColor.getGreen() * 255);
        int a = (int) (onlyColor.getOpacity() * 255);
        return a << 24 + r << 16 + g << 8 + b;
    }

    @Override
    public Color getColor(int x, int y) {
        return onlyColor;
    }

    @Override
    public WritablePixelFormat<IntBuffer> getPixelFormat() {
        return PixelFormat.getIntArgbInstance();
    }

    @Override
    public void getPixels(int x, int y, int w, int h, WritablePixelFormat<ByteBuffer> pixelformat,
            byte[] buffer, int offset, int scanlineStride) {
        HasLogging.log().error("getPixels({}, {}, {}, {}, {},{}, {},{})", x, y, w, h, pixelformat, buffer, offset,
                scanlineStride);
        // TODO
    }

    @Override
    public void getPixels(int x, int y, int w, int h, WritablePixelFormat<IntBuffer> pixelformat, int[] buffer,
            int offset, int scanlineStride) {
        HasLogging.log().error("getPixels({}, {}, {}, {}, {},{}, {},{})", x, y, w, h, pixelformat, buffer, offset,
                scanlineStride);
        // TODO
    }

    @Override
    public <T extends Buffer> void getPixels(final int x, final int y, final int w, final int h,
            final WritablePixelFormat<T> pixelformat, final T buffer, final int scanlineStride) {
        byte[] array = (byte[]) buffer.array();
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                byte b = (byte) (onlyColor.getBlue() * 255);
                byte r = (byte) (onlyColor.getRed() * 255);
                byte g = (byte) (onlyColor.getGreen() * 255);
                byte a = (byte) (onlyColor.getOpacity() * 255);
                int k = 4 * (i * scanlineStride / 4 + j);
                array[k++] = b;
                array[k++] = g;
                array[k++] = r;
                array[k] = a;
            }
        }
    }
}