package graphs.app;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;

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
        if (Platform.isFxApplicationThread()) {
            text2.setValue(text2.getValue()
                    + new String(b, off, len, StandardCharsets.UTF_8).replaceAll("[\\dm (\\w+)[0;\\d+m:", "$1"));
        }
    }
}