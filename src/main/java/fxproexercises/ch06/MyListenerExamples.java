package fxproexercises.ch06;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import simplebuilder.HasLogging;

class MyListenerExamples implements ListChangeListener<String>, HasLogging {

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
            sb.append("\t\tcursor = ")
                    .append(i++)
                    .append("\n");
            final String kind = getTypeOfChange(change);
            MyListenerMethodsExamples.appendKindOfChange(change, sb, kind);
        }
        return sb.toString();
    }

    private String getTypeOfChange(Change<? extends String> change) {
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