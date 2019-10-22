package graphs;

public class NodeGraph {
	private NodeGraph left;
	private int val;
	private NodeGraph right;

	public NodeGraph(int n) {
		left = null;
		val = n;
		right = null;
	}

	public void put(int k) {
		if (k < val) {
			if (left == null) {
				left = new NodeGraph(k);
				return;
			}
			left.put(k);
		}
		if (k > val) {
			if (right == null) {
				right = new NodeGraph(k);
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