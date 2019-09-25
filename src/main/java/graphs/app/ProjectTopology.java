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

public class ProjectTopology extends BaseTopology {

    public ProjectTopology(@NamedArg("graph") Graph graph) {
        super(graph, "Project");
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
        double w = graph.getScrollPane().getWidth() / 2;
        ConcentricLayout.layoutConcentric(graph.getModel().getAllCells(), graph.getModel().getAllEdges(), w);

    }

    public static void main(String[] args) {
        Map<String, Map<String, Long>> packageDependencyMap = createProjectDependencyMap();
        PackageTopology.printDependencyMap(packageDependencyMap);
    }

    private static Map<String, Map<String, Long>> createProjectDependencyMap() {
        List<JavaFileDependecy> javaFileDependencies = JavaFileDependecy.getAllFileDependencies();
        Map<String, List<JavaFileDependecy>> filesByPackage = javaFileDependencies.stream()
            .collect(Collectors.groupingBy(JavaFileDependecy::getPackage));

        Map<String, Map<String, Long>> packageDependencyMap = new HashMap<>();
        filesByPackage
            .forEach((k, v) -> packageDependencyMap.put(k, v.stream().flatMap(e -> e.getDependencies().stream())
                .filter(filesByPackage::containsKey).collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
        return packageDependencyMap;
    }

}