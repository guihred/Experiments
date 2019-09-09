package gaming.ex21;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.PixelHelper;

public class PixelChanger implements PixelReader {

    private static final Logger LOG = HasLogging.log();
    private PixelReader reader;
    private Map<Integer, Integer> replace = new HashMap<>();

    public PixelChanger(PixelReader reader) {
        this.reader = reader;

    }

    @Override
    public int getArgb(int x, int y) {
        int argb = reader.getArgb(x, y);

        return replace.getOrDefault(argb, argb);
    }

    @Override
    public Color getColor(int x, int y) {
        return PixelHelper.asColor(getArgb(x, y));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public PixelFormat getPixelFormat() {
        return reader.getPixelFormat();
    }

    @Override
    public void getPixels(int x, int y, int w, int h, WritablePixelFormat<ByteBuffer> pixelformat, byte[] buffer,
            int offset, int scanlineStride) {
        reader.getPixels(x, y, w, h, pixelformat, buffer, offset, scanlineStride);
    }

    @Override
    public void getPixels(int x, int y, int w, int h, WritablePixelFormat<IntBuffer> pixelformat, int[] buffer,
            int offset, int scanlineStride) {
        reader.getPixels(x, y, w, h, pixelformat, buffer, offset, scanlineStride);
    }

    @Override
    public <T extends Buffer> void getPixels(int x, int y, int w, int h, WritablePixelFormat<T> pixelformat, T buffer,
            int scanlineStride) {
        ByteBuffer buffer2 = (ByteBuffer) buffer;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int argb = getArgb(x + i, y + j);
                byte a = (byte) PixelHelper.getByte(argb, 3);
                byte r = (byte) PixelHelper.getByte(argb, 2);
                byte g = (byte) PixelHelper.getByte(argb, 1);
                byte b = (byte) (PixelHelper.getByte(argb, 0) & 0xFF);
                int k = i * 4 + j * scanlineStride;
                try {
                    buffer2.put(k++, b);
                    buffer2.put(k++, g);
                    buffer2.put(k++, r);
                    buffer2.put(k, a);
                } catch (Exception e) {
                    LOG.error("ERROR GETTING PIXELS", e);
                }
            }
        }

    }

    public void put(Color key, Color value) {
        replace.put(PixelHelper.toArgb(key), PixelHelper.toArgb(value));
    }

}
