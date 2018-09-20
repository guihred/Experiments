package graphs;

import org.slf4j.Logger;
import utils.HasLogging;

public final class JavaExercise19 {
    private static final Logger LOG = HasLogging.log(JavaExercise19.class);
    private JavaExercise19() {
	}

	/**
	 * 19. What does this do? . The following is again a problem of analysing a
	 * complete java program and determining what it writes out without keying
	 * the program in and trying it.
	 */

    public static void testingJavaConcepts() {

		Jack ink = new Jack();
		fred(ink, 3000);
        LOG.info("Value is {}", K.el);
	}
	private static void fred(JJ uk, int n) {
		int a = 10 * n;
		uk.upk();
		if (n > 1000) {
			K.el++;
			Jill ink = new Jill();
			fred(ink, n - 1000);
			a += n;
		}
		K.el += a;
	}

}

class Jack implements JJ {
	@Override
	public void upk() {
		K.el += 10;
	}
}
class Jill implements JJ {
	@Override
	public void upk() {
		Jack ink = new Jack();
		ink.upk();
		K.el += 200;
	}
}

interface JJ {
	void upk();
}

class K {
	public static int el;
}
