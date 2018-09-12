package fxproexercises.ch06;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import simplebuilder.HasLogging;

public class MyListenerExamples implements ListChangeListener<String>, HasLogging {

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
            final String kind = MyListenerMethodsExamples.getChangeType(change);
            MyListenerMethodsExamples.appendKindOfChange(change, sb, kind);
        }
        return sb.toString();
    }

}