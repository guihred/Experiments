package exercism;

class Bob {

	public String hey(String s) {
		if (s.matches(".+\\?")) {
			return "Sure.";
		}
		if (s.matches(".[A-Z\\s!]+[^a-z]*")) {
			return "Whoa, chill out!";
		}
		if (s.matches("[ ]*")) {
			return "Fine. Be that way!";
		}

		return "Whatever.";
	}

}