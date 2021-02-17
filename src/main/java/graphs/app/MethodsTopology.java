package graphs.app;

import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;
import ml.data.JavaFileDependency;

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
            cell.getValue().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()))
                    .forEach((id, weight) -> graph.getModel().addEdge(cellId, id, weight.intValue()));
        }
        graph.endUpdate();
        double min = Math.max(graph.getScrollPane().getViewportBounds().getWidth(),
                graph.getScrollPane().getViewportBounds().getWidth() / 2 / graph.getScrollPane().getScaleValue());
        ConcentricLayout.layoutConcentric(graph.getModel().getAllCells(), graph.getModel().getAllEdges(), min);
        return javaFiles;
    }

    public static List<Entry<String, List<String>>> createFileDependencyMap(Collection<JavaFileDependency> javaFiles) {
        return javaFiles.stream().peek(e -> e.setDependents(javaFiles))
                .flatMap(e -> e.getPublicMethodsFullName().entrySet().stream()).collect(Collectors.toList());
    }

}