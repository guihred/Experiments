package javaexercises;

import simplebuilder.HasLogging;

public final class JavaExercise19 {
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
        HasLogging.log().info("Value is {}", K.k);
	}
	private static void fred(JJ uk, int n) {
		int a = 10 * n;
		uk.upk();
		if (n > 1000) {
			K.k++;
			Jill ink = new Jill();
			fred(ink, n - 1000);
			a += n;
		}
		K.k += a;
	}

}

class Jack extends JJ {
	@Override
	public void upk() {
		K.k += 10;
	}
}
class Jill extends JJ {
	@Override
	public void upk() {
		Jack ink = new Jack();
		ink.upk();
		K.k += 200;
	}
}

abstract class JJ {
	public abstract void upk();
}

class K {
	public static int k;
}
