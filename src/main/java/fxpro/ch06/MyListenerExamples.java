package fxpro.ch06;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import utils.HasLogging;

public class MyListenerExamples implements ListChangeListener<String> {

    private static final Logger LOG = HasLogging.log();

    @Override
    public void onChanged(Change<? extends String> change) {

        ObservableList<? extends String> list = change.getList();
        LOG.trace("\tlist = {}", list);
        String prettyPrint = MyListenerMethodsExamples.prettyPrint(change);
        LOG.trace(prettyPrint);
    }

}