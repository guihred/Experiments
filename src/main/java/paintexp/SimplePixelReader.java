package paintexp;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import org.lwjgl.BufferUtils;
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

		WritablePixelFormat<ByteBuffer> byteBgraInstance = PixelFormat.getByteBgraInstance();
		ByteBuffer buf = BufferUtils.createByteBuffer(w * h * 4);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				byteBgraInstance.setArgb(buf, x + i, y + j, scanlineStride, getArgb(x, y));
			}
		}
        buf.get(buffer, offset, scanlineStride);
    }

    @Override
    public void getPixels(int x, int y, int w, int h, WritablePixelFormat<IntBuffer> pixelformat, int[] buffer,
            int offset, int scanlineStride) {
		IntBuffer buf = BufferUtils.createIntBuffer(w * h);
		for (int i = x; i < x + w; i++) {
			for (int j = y; j < y + h; j++) {
				getPixelFormat().setArgb(buf, i, j, scanlineStride, getArgb(x, y));
			}
		}
		buf.get(buffer, offset, scanlineStride);
    }

    @Override
    public <T extends Buffer> void getPixels(final int x, final int y, final int w, final int h,
            final WritablePixelFormat<T> pixelformat, final T buffer, final int scanlineStride) {
		ByteBuffer buffer2 = (ByteBuffer) buffer;
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				byte b = (byte) (onlyColor.getBlue() * 255);
				byte r = (byte) (onlyColor.getRed() * 255);
				byte g = (byte) (onlyColor.getGreen() * 255);
				byte a = (byte) (onlyColor.getOpacity() * 255);
				int k = i * scanlineStride + j * 4;
				buffer2.put(k++, b);
				buffer2.put(k++, g);
				buffer2.put(k++, r);
				buffer2.put(k, a);
			}
		}
    }
}