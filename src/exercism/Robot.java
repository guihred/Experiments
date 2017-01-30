package exercism;

import java.util.Random;

class Robot{
	String name;

	public Robot() {
		reset();
	}
	String getName() {
		return name;
	}
	
	Random random = new Random();

	final void reset() {
		name = newLetter() + newLetter() + newNumber() + newNumber() + newNumber();
	}

	private String newLetter() {
		String[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");

		String string = a[random.nextInt(a.length)];
		return string;
	}

	private String newNumber() {
		String[] a = "0123456789".split("");

		String string = a[random.nextInt(a.length)];
		return string;
	}
	
}