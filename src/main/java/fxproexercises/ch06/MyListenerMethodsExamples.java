package fxproexercises.ch06;

import java.util.stream.Stream;

import javafx.collections.ListChangeListener;

class MyListenerMethodsExamples implements ListChangeListener<String> {
	
	@Override
	public void onChanged(Change<? extends String> change) {
		System.out.println("\tlist = " + change.getList());
		System.out.println(prettyPrint(change));
	}
	
	private String prettyPrint(Change<? extends String> change) {
		StringBuilder sb = new StringBuilder("\tChange event data:\n");
		int i = 0;
		while (change.next()) {
			sb.append("\t\tcursor = ")
			.append(i++)
			.append("\n");
            final String kind = getChangeType(change);
			sb.append("\t\tKind of change: ")
			.append(kind)
			.append("\n");
			sb.append("\t\tAffected range: [")
			.append(change.getFrom())
			.append(", ")
			.append(change.getTo())
			.append("]\n");
            if (Stream.of("added", "replaced").anyMatch(kind::equals)) {
				sb.append("\t\tAdded size: ")
				.append(change.getAddedSize())
				.append("\n");
				sb.append("\t\tAdded sublist: ")
				.append(change.getAddedSubList())
				.append("\n");
			}
			if ("removed".equals(kind) || "replaced".equals(kind)) {
				sb.append("\t\tRemoved size: ")
				.append(change.getRemovedSize())
				.append("\n");
				sb.append("\t\tRemoved: ")
				.append(change.getRemoved())
				.append("\n");
			}
			if ("permutted".equals(kind)) {
				StringBuilder permutationStringBuilder = new StringBuilder("[");
				for (int k = change.getFrom(); k < change.getTo(); k++) {
					permutationStringBuilder.append(k)
					.append("->")
					.append(change.getPermutation(k));
					if (k < change.getTo() - 1) {
						permutationStringBuilder.append(", ");
					}
				}
				permutationStringBuilder.append("]");
				String permutation = permutationStringBuilder.toString();
				sb.append("\t\tPermutation: ").append(permutation).append("\n");
			}
		}
		return sb.toString();
	}

    private String getChangeType(Change<? extends String> change) {
        if (change.wasPermutated()) {
            return "permutted";
        }
        if (change.wasReplaced()) {
            return "replaced";
        }
        if (change.wasRemoved()) {
            return "removed";
        }
        if (change.wasAdded()) {
            return "added";
        }
        return "none";
    }
}