package others;

import java.util.Random;
import simplebuilder.HasLogging;

public final class RandomHelloWorld {

	private RandomHelloWorld() {
	}

	public static void main(String[] args) {
        // EQUIVALENT TO HELLO
        String randomString = randomString(-229985452);
        // EQUIVALENT TO WORLD
        String randomString2 = randomString(-147909649);
        HasLogging.log().info("{} {}", randomString, randomString2);
	}

	public static String randomString(int i) {
		Random ran = new Random(i);
		StringBuilder sb = new StringBuilder();
		while (true) {
			int k = ran.nextInt(27);
			if (k == 0) {
				break;
			}
			sb.append((char) ('`' + k));
		}
		return sb.toString();
	}
}


