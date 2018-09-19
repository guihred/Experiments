package javaexercises.graphs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.paint.Color;
import simplebuilder.HasLogging;

public class ProjectTopology extends BaseTopology {

	public ProjectTopology(int size, Graph graph) {
        super(graph, "Project", size);
	}

	@Override
	public void execute() {
		graph.clean();
		graph.getModel().removeAllCells();
		graph.getModel().removeAllEdges();

        double bound = 800;

        Map<String, Map<String, Long>> packageDependencyMap = createPackageDependencyMap();

        Set<String> keySet = packageDependencyMap.keySet();

        for (String packageName : keySet) {
            graph.getModel().addCell(packageName, CellType.CIRCLE);
		}
		List<Cell> cells = graph.getModel().getAddedCells();
		for (Cell cell : cells) {
            String cellId = cell.getCellId();
            Map<String, Long> map = packageDependencyMap.get(cellId);
            map.forEach((dep, weight) -> graph.getModel().addEdge(cellId, dep, weight.intValue()));
		}
        List<Edge> addedEdges = graph.getModel().getAddedEdges();
        GraphModelAlgorithms.coloring(cells, addedEdges);
        Map<Color, List<Cell>> collect = cells.stream().collect(Collectors.groupingBy(Cell::getColor));

        List<List<Cell>> collect2 = collect.entrySet().stream()
                .sorted(Comparator.comparing(e -> sortByEdgesNumber(e, addedEdges)))
                .map(Entry<Color, List<Cell>>::getValue).collect(Collectors.toList());
        double layerHeight = bound / (collect2.size() + 1);
        double y = layerHeight;
        for (List<Cell> list : collect2) {
            double xStep = bound / (list.size() + 1);
            double x = xStep;
            for (Cell cell : list) {
                cell.relocate(x, y);
                x += xStep;
            }
            y += layerHeight;
        }
        cells.forEach(e -> e.setColor(null));
		graph.endUpdate();

	}

    private long sortByEdgesNumber(Entry<Color, List<Cell>> e, List<Edge> allEdges) {
        return e.getValue().stream().mapToLong(c -> GraphModelAlgorithms.edgesNumber(c, allEdges)).sum();
    }

    private static Map<String, Map<String, Long>> createPackageDependencyMap() {
        File file = new File("src");
        try (Stream<Path> walk = Files.walk(file.toPath(), 20)) {
            List<JavaFileDependecy> collect = walk.filter(e ->

            !e.toFile().getAbsolutePath().contains("test") && e.toFile().getName().endsWith(".java"))
                    .map(JavaFileDependecy::new).collect(Collectors.toList());

            Map<String, List<JavaFileDependecy>> collect2 = collect.stream()
                    .collect(Collectors.groupingBy(JavaFileDependecy::getPackage));

            Map<String, Map<String, Long>> packageDependencyMap = new HashMap<>();

            collect2.forEach((k, v) -> packageDependencyMap.put(k,
                    v.stream().flatMap(e -> e.getDependencies().stream()).filter(e -> collect2.containsKey(e))
                            .collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
            return packageDependencyMap;
        } catch (Exception e) {
            HasLogging.log().error("", e);
            return new HashMap<>();
        }
    }

    static class JavaFileDependecy implements HasLogging {
        private static final String IMPORT_REGEX = "import ([\\w\\.]+)\\.\\w+;|import static ([\\w\\.]+)\\.\\w+\\.\\w+;";
        private static final String PACKAGE_REGEX = "package ([\\w\\.]+);";
        private Path javaPath;
        private List<String> dependencies;
        private String name;
        private String packageName;

        public JavaFileDependecy(Path javaPath) {
            this.javaPath = javaPath;
        }

        public List<String> getDependencies() {
            if (dependencies == null) {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8);) {
                    dependencies = lines.filter(e -> e.matches(IMPORT_REGEX))
                            .map(e -> e.replaceAll(IMPORT_REGEX, "$1$2"))
                            .distinct()
                            .collect(Collectors.toList());
                } catch (IOException e) {
                    getLogger().error("", e);
                }
            }
            return dependencies;
        }

        public String getName() {
            if (name == null) {
                name = javaPath.toFile().getName();
            }
            return name;
        }

        public String getPackage() {
            if (packageName == null) {
                try (Stream<String> lines = Files.lines(javaPath, StandardCharsets.UTF_8);) {
                    packageName = lines.filter(e -> e.matches(PACKAGE_REGEX))
                            .map(e -> e.replaceAll(PACKAGE_REGEX, "$1")).findFirst().orElse("");
                } catch (IOException e) {
                    getLogger().error("", e);
                }
            }
            return packageName;
        }

        @Override
        public String toString() {
            return getPackage() + "." + getName() + " " + getDependencies();
        }
    }

}