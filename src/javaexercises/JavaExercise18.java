package javaexercises;

/**
 * 18. The Tree Sort Problem In the program outlined below, class Node is has
 * three data fields. The middle one, val , holds a value if type int . The
 * outer ones point to further nodes so that a tree structure is formed.The tree
 * is built up one node at a time. A new node is put on the tree in su h a way
 * that the tree is ordered. Thus 4 < 8 < 64. If the next new node is to
 * incorporate 32, it will hang from the left of the 64 node. Complete the
 * methods in class Node .
 */
public final class JavaExercise18 {

	private JavaExercise18() {
	}

	public static void main(String[] args) {
		Node tree = new Node(16);
		tree.put(8);
		tree.put(4);
		tree.put(64);
		tree.put(32);
		System.out.printf("Tree		elements:		%s%n", tree);
		System.out.printf("Element		sum:		%d%n", tree.sum());
	}

}

class Node {
	private Node left;
	private int val;
	private Node right;

	public Node(int n) {
		left = null;
		val = n;
		right = null;
	}

	public void put(int k) {
		if (k < val) {
			if (left == null) {
				left = new Node(k);
				return;
			}
			left.put(k);
		}
		if (k > val) {
			if (right == null) {
				right = new Node(k);
				return;
			}
			right.put(k);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (left != null) {
			sb.append(left);
			sb.append(",");
		}
		sb.append(val);
		if (right != null) {
			sb.append(",");
			sb.append(right);
		}
		return sb.toString();
	}

	public int sum() {
		int sum = val;
		if (left != null) {
			sum += left.sum();
		}
		if (right != null) {
			sum += right.sum();
		}

		return sum;
	}
}