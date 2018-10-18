package ex.j8;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import utils.HasLogging;

public final class Chapter3 {
	private static final Logger LOGGER = HasLogging.log();

    private Chapter3() {
	}

    public static Integer getRich(List<Integer> l, int low, int high) {
        LOGGER.trace("{},{}", low, high);
		if (low + 1 == high) {
			int i = l.get(high) - l.get(low);
            LOGGER.trace("{}", i);
			return i;
		}
		int mid = (low + high) / 2;
		int x = getRich(l, low, mid);
		int y = getRich(l, mid, high);
		int z = highest(l, mid, high) - lowest(l, low, mid);

        int i = Integer.max(Integer.max(x, y), z);
        LOGGER.trace("{}", i);
		return i;
	}

	public static void main(String[] args) {
		Integer rich = getRich(Arrays.asList(20, 66, 12, 48, 38, 38, 20, 65, 54), 0, 8);
        LOGGER.trace("{}", rich);
	}

	private static int highest(List<Integer> l, int i, int high) {
		return l.get(i) > l.get(high) ? l.get(i) : l.get(high);

	}

	private static int lowest(List<Integer> l, int i, int high) {
		return l.get(i) < l.get(high) ? l.get(i) : l.get(high);
	}

}
