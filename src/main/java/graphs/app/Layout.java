package graphs.app;

import graphs.entities.Graph;
import javafx.beans.NamedArg;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;

public abstract class Layout implements EventTarget {
	protected final Graph graph;

    public Layout(@NamedArg("graph") Graph graph) {
        this.graph = graph;
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return null;
    }

    public final Graph getGraph() {
        return graph;
    }

    public String getName() {
        return getClass().getSimpleName().replace("Layout", "");
    }

    abstract void execute();
}