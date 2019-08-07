package paintexp;

import static utils.PixelHelper.MAX_BYTE;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import utils.HasLogging;
import utils.PixelHelper;

public final class SimplePixelReader implements PixelReader {
    private Color onlyColor;

    public SimplePixelReader(Color white) {
        onlyColor = white;
    }

    @Override
    public int getArgb(int x, int y) {
		return PixelHelper.toArgb(onlyColor);

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
    public void getPixels(int x, int y, int w, int h, WritablePixelFormat<ByteBuffer> pixelformat, byte[] buffer,
            int offset, int scanlineStride) {
        HasLogging.log().error("getPixels({}, {}, {}, {}, {},{}, {},{})", x, y, w, h, pixelformat, buffer, offset,
                scanlineStride);

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
                byte b = (byte) (onlyColor.getBlue() * MAX_BYTE);
                byte r = (byte) (onlyColor.getRed() * MAX_BYTE);
                byte g = (byte) (onlyColor.getGreen() * MAX_BYTE);
                byte a = (byte) (onlyColor.getOpacity() * MAX_BYTE);
				int k = i * scanlineStride + j * 4;
				buffer[k++] = b;
				buffer[k++] = g;
				buffer[k++] = r;
				buffer[k] = a;
			}
		}
    }

    @Override
    public void getPixels(int x, int y, int w, int h, WritablePixelFormat<IntBuffer> pixelformat, int[] buffer,
            int offset, int scanlineStride) {
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				int k = i * scanlineStride + j;
				buffer[k] = getArgb(i, j);
			}
		}
    }

    @Override
    public <T extends Buffer> void getPixels(final int x, final int y, final int w, final int h,
            final WritablePixelFormat<T> pixelformat, final T buffer, final int scanlineStride) {
        ByteBuffer buffer2 = (ByteBuffer) buffer;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                byte b = (byte) (onlyColor.getBlue() * MAX_BYTE);
                byte r = (byte) (onlyColor.getRed() * MAX_BYTE);
                byte g = (byte) (onlyColor.getGreen() * MAX_BYTE);
                byte a = (byte) (onlyColor.getOpacity() * MAX_BYTE);
                int k = i * scanlineStride + j * 4;
                buffer2.put(k++, b);
                buffer2.put(k++, g);
                buffer2.put(k++, r);
                buffer2.put(k, a);
            }
        }
    }

    public void setColor(Color color) {
        onlyColor = color;
    }

    public static void paintColor(WritableImage image, Color backColor) {
        PixelReader reader = new SimplePixelReader(backColor);
        image.getPixelWriter().setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), reader, 0, 0);
    }
}