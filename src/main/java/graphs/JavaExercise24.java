package graphs;

import java.util.Arrays;
import utils.HasLogging;

/**
 * 24. Hash Problem. An array of data contains not more than 500 positive
 * numbers of type double which are reasonably well scattered in the range 0 : 0
 * to 10000.0 and a program is required which will determine whether all the
 * numbers in the data are different. The program should inspect each number in
 * turn and if the number has been met before a variable duplicates should be
 * incremented by 1. In detail:
 * 
 * 1. Declare an array double[] table = new double[630]
 * 
 * 2. Set all 630 elements to -1.0d
 * 
 * 3. Assign the first number in data to a double variable x
 * 
 * 4. Assign to an int variable n thus: int n = (int)x % 630;
 * 
 * 5. Set: table[n] = x
 * 
 * 6. Assign the next number to x
 * 
 * 7. Repeat from 4 but note that for each x there are three possibilities:
 * 
 * a) table[n] is empt y (=-1.0) so fill with x
 * 
 * b) table[n] holds x so increment duplicates
 * 
 * c) table[n] holds another value x' which by bad luck is such that: (int)x' %
 * 630 == (int)x % 630
 * 
 * 8. In case 7c increment n and repeat from 7a.
 * 
 * Invent some suitable data. Just a few entries in an array data will suffice
 * for test purposes, for example: private static double[] data = {637.42d,
 * 6300.95d, 7.81d, 6300.95d, 712.72d, 4325.22d, 2.79d, 3125.77d, 813.02d,
 * 3125.77d, 6.42d, 1234.56d}; Write a program to process the data in the manner
 * outlined. This program should not incorporate any bug which may be present in
 * the above description!
 */
public final class JavaExercise24 {
	private static double[] data = { 637.42D, 6300.95D, 7.81D, 6300.95D, 712.72D, 4325.22D, 2.79D, 3125.77D, 813.02D,
			3125.77D, 6.42D, 1234.56D };

	private JavaExercise24() {
	}

    public static void countDuplicates() {
		double[] table = new double[630];
		Arrays.fill(table, -1);
		int duplicates = 0;
		for (int i = 0; i < data.length; i++) {
			double x = data[i];
			int n = (int) x % 630;
			if (table[n] == n) {
				n++;
			}
			if (table[n] == -1.0) {
				table[n] = x;
			} else if (table[n] == x) {
				duplicates++;
			}
		}
        HasLogging.log().info("{}", duplicates);

	}

}