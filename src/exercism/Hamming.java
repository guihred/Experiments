package exercism;

class Hamming {

	public static int compute(String s1, String s2) {
		if (s1.length() != s2.length()) {
			throw new IllegalArgumentException();
		}
		int count = 0;
		for (int i = 0; i < s1.length(); i++) {
			char charAt = s1.charAt(i);
			char charAt2 = s2.charAt(i);
			if (charAt != charAt2) {
				count++;
			}
		}
		return count;
	}
}