package others;

import java.util.Random;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public final class RandomHelloWorld {

	private static final Logger LOG = HasLogging.log();

    RandomHelloWorld() {
	}

    public static void displayHelloWorld() {
        // EQUIVALENT TO HELLO
        final String randomString = randomString(-229_985_452);
        // EQUIVALENT TO WORLD
        final String randomString2 = randomString(-147_909_649);
        LOG.info("{} {}", randomString, randomString2);
	}

    private static String randomString(int i) {
        StringBuilder sb = new StringBuilder();
        Random ran = new Random(i);
		while (true) {
            final int k = ran.nextInt(27);
			if (k == 0) {
				break;
			}
			sb.append((char) ('`' + k));
		}
		return sb.toString();
	}
}


