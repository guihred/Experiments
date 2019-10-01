package graphs.app;

import graphs.entities.Graph;
import java.util.Objects;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;

public abstract class BaseTopology implements EventTarget {

    private static final int N_LETTERS = 26;
    private static final char[] DIGITS = { ' ', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	protected Graph graph;
    private final String name;

    private int size;

    public BaseTopology(Graph graph) {
        this.graph = graph;
        name = getClass().getSimpleName().replaceAll("Topology", "");
        size = 0;
    }
	public BaseTopology(Graph graph, String name, int size) {
		this.graph = graph;
		this.name = name;
		this.size = size;
	}

    public BaseTopology(String name) {
		this.name = name;
	}

	@Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return null;
    }

	public abstract void execute();

	public Graph getGraph() {
        return graph;
    }

	public String getName() {
		return name;
	}

	public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return name;
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

    public static double rnd(double bound) {
        return Math.random() * bound - bound / 2;
    }

    public static double rndPositive(double bound) {
        return Math.random() * bound;
    }

    protected static double newAngle() {
        return Math.random() * 360 - 180;
    }
}