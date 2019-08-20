package exercism;

/**
 * Bob is a lackadaisical teenager. In conversation, his responses are very
 * limited.
 * 
 * Bob answers 'Sure.' if you ask him a question.
 * 
 * He answers 'Whoa, chill out!' if you yell at him.
 * 
 * He says 'Fine. Be that way!' if you address him without actually saying
 * anything.
 * 
 * He answers 'Whatever.' to anything else.
 */
public final class Bob {

    private Bob() {
    }

    public static String hey(String s) {
		if (s.matches("[\\s]*")) {
			return "Fine. Be that way!";
		}
		if (s.matches("[a-z0-9, ]*.[A-Z\\s!]{2,}[^a-z]*")) {
			return "Whoa, chill out!";
		}
		if (s.matches(".+\\?")) {
			return "Sure.";
		}

		return "Whatever.";
	}

}