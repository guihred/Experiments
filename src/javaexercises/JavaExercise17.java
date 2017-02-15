package javaexercises;

/**
 * 17. Reversing a List. Consider a class Link whose definition begins thus: Any
 * given Link can point to a sequence of others to form a list. Incorporate into
 * Link a method public Link reverse() which returns a new list which is the
 * reverse of that which begins with the given Link . It may be profitable for
 * the reverse method to make use of a put method which places a new element at
 * the end of an existing list. Write a convincing test program to exercise the
 * reverse method.
 */
public final class JavaExercise17 {

	private JavaExercise17() {
	}

	public static void main(String[] args) {

		Link link = new Link(3);
		link.put(4).put(6).put(7);
		System.out.println(link);
		Link reverse = link.reverse();
		System.out.println(reverse);
	}

}

class Link {
	private int val;
	private Link next;

	public Link(int n) {
		val = n;
		next = null;
	}

	public Link put(int n) {
		if (next == null) {
			next = new Link(n);
			return next;
		}

		for (Link link = this; link != null; link = link.next) {
			if (link.next == null) {
				return link.put(n);
			}
		}
		return next;
	}

	public Link reverse() {
		Link link2 = new Link(val);
		for (Link link = next; link != null; link = link.next) {
			Link link3 = new Link(link.val);
			link3.next = link2;
			if (link.next == null) {
				return link3;
			}
			link2 = link3;

		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		for (Link link = this; link != null; link = link.next) {
			sb.append(link.val);
			if (link.next != null) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
