package javaexercises;

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