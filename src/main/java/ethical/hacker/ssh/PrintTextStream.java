package ethical.hacker.ssh;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import javafx.scene.text.Text;

final class PrintTextStream extends PrintStream {
    private final Text text2;

    PrintTextStream(OutputStream out, boolean autoFlush, String encoding, Text text2)
        throws UnsupportedEncodingException {
        super(out, autoFlush, encoding);
        this.text2 = text2;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        super.write(b, off, len);
        text2.setText(text2.getText() + new String(b, off, len, StandardCharsets.UTF_8));
    }
}