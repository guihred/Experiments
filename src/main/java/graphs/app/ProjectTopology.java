package graphs.app;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;
import ml.data.JavaFileDependency;

public class ProjectTopology extends BaseTopology {

    public ProjectTopology(@NamedArg("graph") Graph graph) {
        super(graph);
    }

    @Override
    public void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();

        Map<String, Map<String, Long>> packageDependencyMap = createProjectDependencyMap();

        Set<String> keySet = packageDependencyMap.keySet();
        for (String packageName : keySet) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
        }
        List<Cell> cells = graph.getModel().getAddedCells();
        for (Cell cell : cells) {
            String cellId = cell.getCellId();
            Map<String, Long> map = packageDependencyMap.get(cellId);
            map.forEach((dep, weight) -> graph.getModel().addEdge(cellId, dep, weight.intValue()));
        }
        graph.endUpdate();
        double w = graph.getScrollPane().getViewportBounds().getWidth() / 2;
        double h = graph.getScrollPane().getViewportBounds().getHeight();
        LayerLayout.layoutInLayers(graph.getModel().getAllCells(), graph.getModel().getAllEdges(), w, h);

    }

    public static Map<String, Map<String, Long>> createProjectDependencyMap() {
        List<JavaFileDependency> javaFileDependencies = JavaFileDependency.getAllFileDependencies();
        Map<String, List<JavaFileDependency>> filesByPackage = javaFileDependencies.stream()
            .collect(Collectors.groupingBy(JavaFileDependency::getPackage));

        Map<String, Map<String, Long>> packageDependencyMap = new HashMap<>();
        filesByPackage
            .forEach((k, v) -> packageDependencyMap.put(k, v.stream().flatMap(e -> e.getDependencies().stream())
                .filter(filesByPackage::containsKey).collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
        return packageDependencyMap;
    }

    public static void main(String[] args) {
        Map<String, Map<String, Long>> packageDependencyMap = createProjectDependencyMap();
        PackageTopology.printDependencyMap(packageDependencyMap);
    }

}