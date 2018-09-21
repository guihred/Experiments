package graphs.topology;

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
import utils.HasLogging;

public class ProjectTopology extends BaseTopology {

	public ProjectTopology(Graph graph) {
        super(graph, "Project");
	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();


        Map<String, Map<String, Long>> packageDependencyMap = createPackageDependencyMap();

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
        LayerLayout.layoutInLayers(cells, addedEdges);
        graph.endUpdate();

    }


    private static Map<String, Map<String, Long>> createPackageDependencyMap() {
        File file = new File("src");
        try (Stream<Path> walk = Files.walk(file.toPath(), 20)) {
            List<JavaFileDependecy> collect = walk.filter(e ->
            //            !e.toFile().getAbsolutePath().contains("test") && 
            e.toFile().getName().endsWith(".java"))
                    .map(JavaFileDependecy::new).collect(Collectors.toList());

            Map<String, List<JavaFileDependecy>> collect2 = collect.stream()
                    .collect(Collectors.groupingBy(JavaFileDependecy::getPackage));

            Map<String, Map<String, Long>> packageDependencyMap = new HashMap<>();

            collect2.forEach((k, v) -> packageDependencyMap.put(k,
                    v.stream().flatMap(e -> e.getDependencies().stream()).filter(collect2::containsKey)
                            .collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
            return packageDependencyMap;
        } catch (Exception e) {
            HasLogging.log().error("", e);
            return new HashMap<>();
        }
    }

    public static void main(String[] args) {
        Map<String, Map<String, Long>> packageDependencyMap = createPackageDependencyMap();
        PackageTopology.printDependencyMap(packageDependencyMap);
    }

}