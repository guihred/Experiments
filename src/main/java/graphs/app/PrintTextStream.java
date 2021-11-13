package graphs.app;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javafx.beans.property.StringProperty;
import utils.CommonsFX;

public final class PrintTextStream extends PrintStream {
    private final StringProperty text2;

    public PrintTextStream(OutputStream out, boolean autoFlush, String encoding, StringProperty text2)
            throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        this.text2 = text2;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        super.write(b, off, len);
        CommonsFX.runInPlatform(() -> {
            text2.setValue(text2.getValue()
                    + new String(b, off, len, StandardCharsets.UTF_8).replaceAll("\\[34m (\\w+)\\[0;39m:", "$1"));
        });
    }
}