package graphs.app;

import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;

public class MethodsTopology extends BaseTopology {

    private String chosenPackageName;

    public MethodsTopology(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {
        createGraph();

    }

    public void setChosenPackageName(String chosenPackageName) {
        this.chosenPackageName = chosenPackageName;
    }

    private List<JavaFileDependency> createGraph() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();
        List<JavaFileDependency> javaFiles = JavaFileDependency.getJavaFileDependencies(chosenPackageName);
        List<Entry<String, List<String>>> packageDependencyMap = createFileDependencyMap(javaFiles);
        for (Entry<String, List<String>> packageName : packageDependencyMap) {
            graph.getModel().addCell(packageName.getKey(), CellType.RECTANGLE);
        }
        for (Entry<String, List<String>> cell : packageDependencyMap) {
            String cellId = cell.getKey();
            List<String> value = cell.getValue();
            for (String entry : value) {
                graph.getModel().addEdge(cellId, entry, 1);
            }
        }
        graph.endUpdate();
        ConcentricLayout.layoutConcentric(graph.getModel().getAllCells(), graph.getModel().getAllEdges(),
            Math.min(graph.getScrollPane().getViewportBounds().getWidth() / 4,
                graph.getScrollPane().getViewportBounds().getHeight() / 4));
        return javaFiles;
    }

    public static List<Entry<String, List<String>>> createFileDependencyMap(Collection<JavaFileDependency> javaFiles) {
        return javaFiles.stream().peek(e -> e.setDependents(javaFiles))
            .flatMap(e -> e.getPublicMethodsFullName().entrySet().stream()).collect(Collectors.toList());
    }


}