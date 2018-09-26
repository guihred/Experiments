package graphs;

public class Node {
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
}