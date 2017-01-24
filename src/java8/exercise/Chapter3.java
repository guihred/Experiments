package java8.exercise;

import java.util.Arrays;
import java.util.List;

public class Chapter3 {

	private static Integer getRich(List<Integer> l, int low, int high) {
		System.out.println(low + "," + high);
		if (low + 1 == high) {
			int i = l.get(high) - l.get(low);
			System.out.println(i);
			return i;
		}
		int mid = (low + high) / 2;
		int x = getRich(l, low, mid);
		int y = getRich(l, mid, high);
		int z = highest(l, mid, high) - lowest(l, low, mid);

		int i = x > y && x > z ? x : y > z ? y : z;
		System.out.println(i);
		return i;
	}

	private static int highest(List<Integer> l, int i, int high) {
		return l.get(i) > l.get(high) ? l.get(i) : l.get(high);

	}

	private static int lowest(List<Integer> l, int i, int high) {
		return l.get(i) < l.get(high) ? l.get(i) : l.get(high);
	}

	public static void main(String[] args) {
		Integer rich = getRich(Arrays.asList(20, 66, 12, 48, 38, 38, 20, 65, 54), 0, 8);
		System.out.println(rich);

	}

}
