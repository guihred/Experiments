package graphs.app;

import ethical.hacker.WebsiteScanner;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.List;
import java.util.Map.Entry;
import javafx.beans.NamedArg;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;

public class WebsiteTopology extends BaseTopology {

    private ObservableMap<String, List<String>> websiteRoutes;
    private final StringProperty websiteAddress = new SimpleStringProperty("https://pt.wikipedia.org");

    private WebsiteScanner websiteScanner = new WebsiteScanner();

    public WebsiteTopology(@NamedArg("graph") Graph graph) {
        super(graph);
        websiteAddress.addListener(o -> websiteRoutes = null);
    }

    @Override
    public synchronized void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();
        if (websiteRoutes == null) {
            websiteRoutes = websiteScanner.getLinkNetwork(websiteAddress.get(), this::execute);
        }

        for (String packageName : websiteScanner.allHosts()) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
        }

        List<Entry<String, List<String>>> values = websiteScanner.entrySet();
        for (int l = 0; l < values.size(); l++) {
            Entry<String, List<String>> hops = values.get(l);
            for (String hop : hops.getValue()) {
                graph.getModel().addEdge(hops.getKey(), hop, 1);
            }
        }
        graph.endUpdate();
        double min =
                Math.min(graph.getScrollPane().getViewportBounds().getWidth(), graph.getScrollPane().getWidth() / 2);
        ConcentricLayout.layoutConcentric(graph.getModel().getAllCells(), graph.getModel().getAllEdges(), min);
    }

    public StringProperty websiteProperty() {
        return websiteAddress;
    }

}