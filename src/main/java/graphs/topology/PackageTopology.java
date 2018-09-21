package graphs.topology;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Edge;
import graphs.entities.Graph;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utils.HasLogging;

public class PackageTopology extends BaseTopology {

	public PackageTopology(Graph graph) {
        super(graph, "Package");
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
            e.toFile().getName().endsWith(".java"))
                    .map(JavaFileDependecy::new)
                    .filter(e -> e.getPackage().contains("graphs."))
                    .collect(Collectors.toList());

            List<String> collect3 = collect.stream().map(JavaFileDependecy::getName)
                    .collect(Collectors.toList());
            Map<String, Map<String, Long>> packageDependencyMap = new HashMap<>();
            collect.forEach(k -> packageDependencyMap.put(k.getName(), k.getClasses().stream()
                    .filter(collect3::contains).collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
            return packageDependencyMap;
        } catch (Exception e) {
            HasLogging.log().error("", e);
            return new HashMap<>();
        }
    }

    public static void main(String[] args) {

        Map<String, Map<String, Long>> packageDependencyMap = createPackageDependencyMap();
        printDependencyMap(packageDependencyMap);

    }

    public static void printDependencyMap(Map<String, Map<String, Long>> packageDependencyMap) {
        List<String> packNames = packageDependencyMap.keySet().stream()
                .sorted(Comparator
                        .comparing(e -> packageDependencyMap.get(e).values().stream().mapToLong(l -> l).sum()))
                .collect(Collectors.toList());
        int maxLength = packNames.stream().mapToInt(String::length).max().orElse(0);

        String paddedNames = packNames.stream().map(e -> mapString(e, maxLength))
                .collect(Collectors.joining("", "\n" + mapString("", maxLength), "\n"));
        StringBuilder table = new StringBuilder();
        table.append(paddedNames);
        for (String pack : packNames) {
            table.append(mapString(pack, maxLength));
            for (String string2 : packNames) {
                Long orDefault = packageDependencyMap.get(pack).getOrDefault(string2, 0L);
                table.append(mapString(pack.equals(string2) ? "-" : orDefault, maxLength));

            }
            table.append("\n");
        }
        HasLogging.log().info("{}", table);
    }

    private static String mapString(Object s, int l) {
        String format = "%" + l + "s";
        return String.format(format, s);
    }

}