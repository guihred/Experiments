package japstudy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CompareAnswers {
	private CompareAnswers() {
	}
	public static double compare(String x, String y) {

		String s = Objects.toString(x, "");
		String s2 = Objects.toString(y, "");

        List<String> set = bigrams(s);
        List<String> set2 = bigrams(s2);
        int nx = set.size();
        int ny = set2.size();
        set2.removeAll(set);

		if (nx + ny == 0) {
			return 1;
		}

        return 1. - 2. * set2.size() / (nx + ny);
	}

	private static List<String> bigrams(String s) {
		List<String> characters = s.chars().mapToObj(i -> Character.toString((char) i)).collect(Collectors.toList());
		List<String> bigrams = new ArrayList<>();
		for (int j = 0; j < characters.size() - 1; j++) {
			String string = characters.get(j);
			String string2 = characters.get(j + 1);
			bigrams.add(string + string2);
		}
		return bigrams;
	}

}
