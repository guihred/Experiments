package extract;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import javafx.scene.control.TableColumn;

public class QuickSortML {

	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static Comparator<Integer> getComparator(TableColumn<Integer, ?> col, Entry<String, Boolean> e) {
        Comparator<Integer> comparing =
                Comparator.comparing(m -> (Comparable) (col.getCellData(m) instanceof Comparable ? col.getCellData(m)
                        : Objects.toString(col.getCellData(m))));
        return e.getValue() ? comparing : comparing.reversed();
    }
    public static <T> boolean isSorted(List<T> a, Comparator<T> comp) {
        for (int i = 0; i < a.size() - 1; i++) {
            if (comp.compare(a.get(i), a.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }
    public static <T extends Comparable<T>> void sort(List<T> inputArr) {
		sort(inputArr, (i, j) -> {
			// DOES NOTHING
		}, T::compareTo);
	}
	public static <T extends Comparable<T>> void sort(List<T> inputArr, BiIntConsumer onSwap) {
		sort(inputArr, onSwap, T::compareTo);
	}

	public static <T> void sort(List<T> inputArr, BiIntConsumer onSwap, Comparator<T> compa) {

		if (inputArr == null || inputArr.isEmpty()) {
			return;
		}
		quickSort(0, inputArr.size() - 1, inputArr, onSwap, compa);
	}
	public static <T> void sort(List<T> inputArr, Comparator<T> compa) {
		sort(inputArr, (i, j) -> {
			// DOES NOTHING
		}, compa);
	}

	private static <T> void exchangeNumbers(int i, int j, List<T> array, BiIntConsumer onSwap) {
		T temp = array.get(i);
		array.set(i, array.get(j));
		array.set(j, temp);
		onSwap.consume(i, j);
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

    @FunctionalInterface
	public interface BiIntConsumer {
		void consume(int i, int j);
	}

}
