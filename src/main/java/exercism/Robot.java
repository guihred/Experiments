package exercism;

import java.util.Random;

/**
 * The Class Robot.
 * 
 * Manage robot factory settings.
 * 
 * When robots come off the factory floor, they have no name.
 * 
 * The first time you boot them up, a random name is generated in the format of
 * two uppercase letters followed by three digits, such as RX837 or BC811.
 * 
 * Every once in a while we need to reset a robot to its factory settings, which
 * means that their name gets wiped. The next time you ask, it will respond with
 * a new random name.
 * 
 * The names must be random: they should not follow a predictable sequence.
 * Random names means a risk of collisions. Your solution should not allow the
 * use of the same name twice when avoidable. In some exercism language tracks
 * there are tests to ensure that the same name is never used twice.
 */
public class Robot {

	/** The name. */
	private String name;

	/** The random. */
	private Random random = new Random();

	/**
	 * Instantiates a new robot.
	 */
	public Robot() {
		reset();
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Reset.
	 */
	public final void reset() {
		name = newLetter() + newLetter() + newNumber() + newNumber() + newNumber();
	}

	/**
	 * New letter.
	 *
	 * @return the string
	 */
	private String newLetter() {
		String[] a = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
		return a[random.nextInt(a.length)];
	}

	/**
	 * New number.
	 *
	 * @return the string
	 */
	private String newNumber() {
		String[] a = "0123456789".split("");
		return a[random.nextInt(a.length)];
	}
	
}