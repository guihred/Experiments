package graphs;

import org.slf4j.Logger;
import utils.HasLogging;

public final class JavaExercise19 {
	private static final Logger LOG = HasLogging.log();
	public static int el=0;

	private JavaExercise19() {
	}

    /**
	 * 19. What does this do? . The following is again a problem of analysing a
	 * complete java program and determining what it writes out without keying
	 * the program in and trying it.
	 */

	public static void testingJavaConcepts() {

		Jack ink = new Jack();
		fred(ink, 1000);
		LOG.info("Value is {}", JavaExercise19.el);
	}
	public static void testingJavaConcepts2() {

        Jack ink = new Jack();
        final int n = 1001;
        fred(ink, n);
        LOG.info("Value is {}", JavaExercise19.el);
    }

    private static void fred(JJ uk, int n) {
		int a = 10 * n;
		uk.upk();
		if (n > 1000) {
			JavaExercise19.el++;
			Jill ink = new Jill();
			fred(ink, n - 1000);
			a += n;
		}
		JavaExercise19.el += a;
	}

}

class Jack implements JJ {
	@Override
	public void upk() {
        JavaExercise19.el += 10;
	}
}
class Jill implements JJ {
	@Override
	public void upk() {
		Jack ink = new Jack();
		ink.upk();
        JavaExercise19.el += 100;
	}
}

@FunctionalInterface
interface JJ {
	void upk();
}
