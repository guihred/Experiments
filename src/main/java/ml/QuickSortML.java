package ml;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import utils.HasLogging;

public class QuickSortML {

	@FunctionalInterface
	public interface BiIntConsumer {
		void consume(int i, int j);
	}

	public static <T extends Comparable<T>> void sort(List<T> inputArr) {
		sort(inputArr, (i, j) -> {
			// DOES NOTHING
		}, T::compareTo);
	}
	public static <T extends Comparable<T>> void sort(List<T> inputArr, BiIntConsumer onSwap) {
		sort(inputArr, onSwap, T::compareTo);
	}

	public static <T> void sort(List<T> inputArr, Comparator<T> compa) {
		sort(inputArr, (i, j) -> {
			// DOES NOTHING
		}, compa);
	}
	public static <T> void sort(List<T> inputArr, BiIntConsumer onSwap, Comparator<T> compa) {

		if (inputArr == null || inputArr.isEmpty()) {
			return;
		}
		quickSort(0, inputArr.size() - 1, inputArr, onSwap, compa);
	}

	private static <T> void quickSort(int lowerIndex, int higherIndex, List<T> array,
			BiIntConsumer onSwap, Comparator<T> compa) {

		int i = lowerIndex;
		int j = higherIndex;
		// calculate pivot number, I am taking pivot as middle index number
		T pivot = array.get(lowerIndex + (higherIndex - lowerIndex) / 2);
		// Divide into two arrays
		while (i <= j) {
			/**
			 * In each iteration, we will identify a number from left side which is greater
			 * then the pivot value, and also we will identify a number from right side
			 * which is less then the pivot value. Once the search is done, then we exchange
			 * both numbers.
			 */
			while (compa.compare(array.get(i), pivot) < 0) {
				i++;
			}
			while (compa.compare(array.get(j), pivot) > 0) {
				j--;
			}
			if (i <= j) {
				exchangeNumbers(i, j, array, onSwap);
				// move index to next position on both sides
				i++;
				j--;
			}
		}
		// call quickSort() method recursively
		if (lowerIndex < j) {
			quickSort(lowerIndex, j, array, onSwap, compa);
		}
		if (i < higherIndex) {
			quickSort(i, higherIndex, array, onSwap, compa);
		}
	}

	private static <T> void exchangeNumbers(int i, int j, List<T> array, BiIntConsumer onSwap) {
		T temp = array.get(i);
		array.set(i, array.get(j));
		array.set(j, temp);
		onSwap.consume(i, j);
	}

    public static void main(String[] a) {

		List<Integer> input = Arrays.asList(24, 2, 45, 20, 56, 75, 2, 56, 99, 53, 12);

		Comparator<Integer> c = Integer::compareTo;
		QuickSortML.sort(input, c.reversed());
		for (Integer i : input) {
            HasLogging.log().info("{}", i);
		}
	}

}
