/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fxproexercises.ch06;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Note
 */
public class FXCollectionsExamples {

    public static void main(String[] args) {
        ObservableList<String> strings = FXCollections.observableArrayList();
        strings.addListener(new MyListenerExamples());
        System.out.println("Calling addAll(\"Zero\", \"One\", \"Two\", \"Three\"): ");
        strings.addAll("Zero", "One", "Two", "Three");
        System.out.println("Calling FXCollections.sort(strings): ");
        FXCollections.sort(strings);
        System.out.println("Calling set(1, \"Three_1\"): ");
        strings.set(1, "Three_1");
        System.out.println("Calling setAll(\"One_1\", \"Three_1\", \"Two_1\", \"Zero_1\"): ");
        strings.setAll("One_1", "Three_1", "Two_1", "Zero_1");
        System.out.println("Calling removeAll(\"One_1\", \"Two_1\", \"Zero_1\"): ");
        strings.removeAll("One_1", "Two_1", "Zero_1");
    }

}
class MyListenerExamples implements ListChangeListener<String> {

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
            final String kind
                    = change.wasPermutated() ? "permutted"
                            : change.wasReplaced() ? "replaced"
                                    : change.wasRemoved() ? "removed"
                                            : change.wasAdded() ? "added" : "none";
            sb.append("\t\tKind of change: ")
                    .append(kind)
                    .append("\n");
            sb.append("\t\tAffected range: [")
                    .append(change.getFrom())
                    .append(", ")
                    .append(change.getTo())
                    .append("]\n");
			if ("added".equals(kind) || "replaced".equals(kind)) {
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
}

