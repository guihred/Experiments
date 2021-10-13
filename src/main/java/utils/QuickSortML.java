package utils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.control.TableColumn;
import org.apache.commons.lang3.StringUtils;

public final class QuickSortML {
    private QuickSortML() {
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Comparator<Integer> getComparator(TableColumn<Integer, ?> col, Boolean ascending) {
        Comparator<Integer> comparing = Comparator.comparing(m -> {
            Object cellData = col.getCellData(m);
            return (Comparable) (cellData instanceof Number ? ((Number) cellData).doubleValue()
                    : Objects.toString(cellData));
        });
        return ascending ? comparing : comparing.reversed();
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

    public static <T> void sortMapList(List<Map<String, T>> ipItems2, String col, Boolean ascending) {
        Comparator<Map<String, T>> comparing = Comparator.comparing(m -> {
            String string = Objects.toString(m.get(col), "");
            if (StringUtils.isNumeric(string)) {
                return String.format("%09d", Long.valueOf(string));
            }
            List<String> fileSizes = StringSigaUtils.matches(string, "([\\d\\.]+ ?[MKGT]?B)");
            if (!fileSizes.isEmpty()) {
                return String.format("%15d", StringSigaUtils.strToFileSize(fileSizes.get(0)));
            }
            return string;
        });
        ipItems2.sort(ascending ? comparing : comparing.reversed());
    }

    private static <T> void exchangeNumbers(int i, int j, List<T> array, BiIntConsumer onSwap) {
        T temp = array.get(i);
        array.set(i, array.get(j));
        array.set(j, temp);
        onSwap.consume(i, j);
    }

    private static <T> void quickSort(int lowerIndex, int higherIndex, List<T> array, BiIntConsumer onSwap,
            Comparator<T> compa) {
        Comparator<T> nullsLast = Comparator.nullsLast(compa);
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
            while (nullsLast.compare(array.get(i), pivot) < 0) {
                i++;
            }
            while (nullsLast.compare(array.get(j), pivot) > 0) {
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
}
