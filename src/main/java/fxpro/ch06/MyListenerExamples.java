package fxpro.ch06;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import utils.HasLogging;

public class MyListenerExamples implements ListChangeListener<String>, HasLogging {

    @Override
    public void onChanged(Change<? extends String> change) {

        ObservableList<? extends String> list = change.getList();
        getLogger().trace("\tlist = {}", list);
        String prettyPrint = MyListenerMethodsExamples.prettyPrint(change);
        getLogger().trace(prettyPrint);
    }



}