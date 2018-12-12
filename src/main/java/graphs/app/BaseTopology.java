package graphs.app;

import graphs.entities.Graph;
import java.util.Objects;
import utils.HasLogging;

public abstract class BaseTopology implements HasLogging {

    private static final int N_LETTERS = 26;
    private static final char[] DIGITS = { ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	protected Graph graph;
    private final String name;

    private int size;

    public BaseTopology(Graph graph, String name) {
        this(graph, name, 0);
    }
	public BaseTopology(Graph graph, String name, int size) {
		this.graph = graph;
		this.name = name;
		this.size = size;
	}

	public BaseTopology(String name) {
		this.name = name;
	}

	public abstract void execute();

	public String getName() {
		return name;
	}

	public int getSize() {
        return size;
    }

	public void setSize(int size) {
        this.size = size;
    }

    public static String cellIdentifier(int n) {
		int i = -n;
		/* Use the faster version */
        final int maxBuf = 33;
        char[] buf = new char[maxBuf];
        int charPos = maxBuf - 1;
        while (i <= -N_LETTERS) {
            buf[charPos--] = DIGITS[-(i % N_LETTERS)];
            i = i / N_LETTERS;
		}
        buf[charPos] = DIGITS[-i];
        return new String(buf, charPos, maxBuf - charPos);
	}

    public static String identifier(int i) {
        if (i >= N_LETTERS) {
			return cellIdentifier(i + 1);
		}
		return Objects.toString((char) ('A' + i));

	}
}