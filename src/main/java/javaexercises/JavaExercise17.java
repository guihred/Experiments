package javaexercises;

import org.slf4j.Logger;
import simplebuilder.HasLogging;

/**
 * 17. Reversing a List. Consider a class Link whose definition begins thus: Any
 * given Link can point to a sequence of others to form a list. Incorporate into
 * Link a method public Link reverse() which returns a new list which is the
 * reverse of that which begins with the given Link . It may be profitable for
 * the reverse method to make use of a put method which places a new element at
 * the end of an existing list. Write a convincing test program to exercise the
 * reverse method.
 */
public final class JavaExercise17 {

    private static final Logger LOGGER = HasLogging.log();
	private JavaExercise17() {
	}

	public static void main(String[] args) {

		Link link = new Link(3);
		link.put(4).put(6).put(7);
        LOGGER.info("{}", link);
		Link reverse = link.reverse();
        LOGGER.info("{}", reverse);
	}

}
