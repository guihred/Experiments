package fxpro.ch06;

import javafx.collections.MapChangeListener;
import org.slf4j.Logger;
import utils.ex.HasLogging;

class MyListenerMapExamples implements MapChangeListener<String, Integer> {

	private static final Logger LOG = HasLogging.log();
	
    @Override
	public void onChanged(Change<? extends String, ? extends Integer> change) {
        LOG.trace("\tmap = {}", change.getMap());
        String prettyPrint = prettyPrint(change);
        LOG.trace(prettyPrint);
	}

    private static String prettyPrint(Change<? extends String, ? extends Integer> change) {
        StringBuilder sb = new StringBuilder("\tChange event data:\n");
        sb.append("\t\tWas added: ").append(change.wasAdded()).append("\n");
        sb.append("\t\tWas removed: ").append(change.wasRemoved()).append("\n");
        sb.append("\t\tKey: ").append(change.getKey()).append("\n");
        sb.append("\t\tValue added: ").append(change.getValueAdded()).append("\n");
        sb.append("\t\tValue removed: ").append(change.getValueRemoved()).append("\n");
        return sb.toString();
    }
}