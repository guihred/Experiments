package fxproexercises.ch06;

import java.util.stream.Stream;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import simplebuilder.HasLogging;

class MyListenerMethodsExamples implements ListChangeListener<String>, HasLogging {
	
	private static final String REPLACED = "replaced";

    @Override
	public void onChanged(Change<? extends String> change) {
        ObservableList<? extends String> list = change.getList();
        getLogger().info("\tlist = {}", list);
        String prettyPrint = prettyPrint(change);
        getLogger().info(prettyPrint);
	}
	
	private String prettyPrint(Change<? extends String> change) {
		StringBuilder sb = new StringBuilder("\tChange event data:\n");
		int i = 0;
		while (change.next()) {
            sb.append("\t\tcursor = ").append(i++).append("\n");
            final String kind = getChangeType(change);
            appendKindOfChange(change, sb, kind);
		}
		return sb.toString();
	}

    public static void appendKindOfChange(Change<? extends String> change, StringBuilder sb, final String kind) {
        sb.append("\t\tKind of change: ")
        .append(kind)
        .append("\n");
        sb.append("\t\tAffected range: [")
        .append(change.getFrom())
        .append(", ")
        .append(change.getTo())
        .append("]\n");
        if (Stream.of("added", REPLACED).anyMatch(kind::equals)) {
        	sb.append("\t\tAdded size: ")
        	.append(change.getAddedSize())
        	.append("\n");
        	sb.append("\t\tAdded sublist: ")
        	.append(change.getAddedSubList())
        	.append("\n");
        }
        if ("removed".equals(kind) || REPLACED.equals(kind)) {
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

    public static String getChangeType(Change<? extends String> change) {
        if (change.wasPermutated()) {
            return "permutted";
        }
        if (change.wasReplaced()) {
            return REPLACED;
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