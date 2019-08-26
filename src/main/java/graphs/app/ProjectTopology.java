package graphs.app;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Edge;
import graphs.entities.Graph;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.NamedArg;
import org.slf4j.Logger;
import utils.HasLogging;

public class ProjectTopology extends BaseTopology {

    private static final Logger LOG = HasLogging.log();

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
        List<Edge> addedEdges = graph.getModel().getAddedEdges();
        LayerLayout.displayInLayers(cells, addedEdges);
        graph.endUpdate();

    }

    public static void main(String[] args) {
        Map<String, Map<String, Long>> packageDependencyMap = createProjectDependencyMap();
        PackageTopology.printDependencyMap(packageDependencyMap);
    }

    private static Map<String, Map<String, Long>> createProjectDependencyMap() {
        File file = new File("src");
        try (Stream<Path> walk = Files.walk(file.toPath(), 20)) {
            List<JavaFileDependecy> javaFileDependencies = walk.filter(e -> e.toFile().getName().endsWith(".java"))
                .map(JavaFileDependecy::new).collect(Collectors.toList());

            Map<String, List<JavaFileDependecy>> filesByPackage = javaFileDependencies.stream()
                .collect(Collectors.groupingBy(JavaFileDependecy::getPackage));

            Map<String, Map<String, Long>> packageDependencyMap = new HashMap<>();

            filesByPackage.forEach((k, v) -> packageDependencyMap.put(k,
                v.stream().flatMap(e -> e.getDependencies().stream()).filter(filesByPackage::containsKey)
                    .collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
            return packageDependencyMap;
        } catch (Exception e) {
            LOG.error("", e);
            return new HashMap<>();
        }
    }

}