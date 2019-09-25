package graphs.app;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.HasLogging;

public class PackageTopology extends BaseTopology {

    private static final Logger LOG = HasLogging.log();
    private String chosenPackageName;
    private ObservableList<String> packages = getJavaFileDependencies(null).stream().map(JavaFileDependecy::getPackage)
        .distinct().collect(Collectors.toCollection(FXCollections::observableArrayList));

    public PackageTopology(@NamedArg("graph") Graph graph) {
        super(graph, "Package");
		packages.add("");
    }

    @Override
    public void execute() {
        createGraph();

    }

    public ObservableList<String> getPackages() {
        return packages;
    }

    public void setChosenPackageName(String chosenPackageName) {
        this.chosenPackageName = chosenPackageName;
    }

    private List<JavaFileDependecy> createGraph() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();
        List<JavaFileDependecy> javaFiles = getJavaFileDependencies(chosenPackageName);
        Map<String, Map<String, Long>> packageDependencyMap = createFileDependencyMap(javaFiles);
        for (String packageName : packageDependencyMap.keySet()) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
        }
        List<Cell> cells = graph.getModel().getAddedCells();
        for (Cell cell : cells) {
            String cellId = cell.getCellId();
            Map<String, Long> map = packageDependencyMap.get(cellId);
            map.forEach((dep, weight) -> graph.getModel().addEdge(cellId, dep, weight.intValue()));
        }
        graph.endUpdate();
		LayerLayout.layoutInLayers(graph.getModel().getAllCells(), graph.getModel().getAllEdges(),
				graph.getScrollPane().getViewportBounds().getWidth() / 2);
        return javaFiles;
    }

    public static Map<String, Map<String, Long>> createFileDependencyMap(Collection<JavaFileDependecy> javaFiles) {
        List<String> classesNames = javaFiles.stream().map(JavaFileDependecy::getName).sorted()
            .collect(Collectors.toList());
        return javaFiles.stream().collect(Collectors.toMap(JavaFileDependecy::getName, k -> k.getClasses().stream()
            .filter(classesNames::contains).collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
    }

    public static List<JavaFileDependecy> getJavaFileDependencies(String packName) {
        File file = new File("src");
        try (Stream<Path> walk = Files.walk(file.toPath(), 20)) {
            return walk.filter(e -> e.toFile().getName().endsWith(".java")).map(JavaFileDependecy::new)
					.filter(e -> StringUtils.isBlank(packName) || e.getPackage().equals(packName))
					.collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("", e);
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {

        List<JavaFileDependecy> javaFiles = getJavaFileDependencies(null);
        Map<String, Map<String, Long>> packageDependencyMap = createFileDependencyMap(javaFiles);
        printDependencyMap(packageDependencyMap);

    }

    public static void printDependencyMap(Map<String, Map<String, Long>> packageDependencyMap) {
        List<String> packNames = packageDependencyMap.keySet().stream()
            .sorted(Comparator.comparing(e -> packageDependencyMap.get(e).values().stream().mapToLong(l -> l).sum()))
            .collect(Collectors.toList());
        int maxLength = packNames.stream().mapToInt(String::length).max().orElse(0);

        String paddedNames = packNames.stream().map(e -> mapString(e, e.length() + 1))
            .collect(Collectors.joining("", "\n" + mapString("", maxLength), "\n"));
        StringBuilder table = new StringBuilder();
        table.append(paddedNames);
        for (String pack : packNames) {
            table.append(mapString(pack, maxLength));
            for (String string2 : packNames) {
                Long orDefault = packageDependencyMap.get(pack).getOrDefault(string2, 0L);
                table.append(mapString(pack.equals(string2) ? "-" : orDefault, string2.length() + 1));

            }
            table.append("\n");
        }
        LOG.info("{}", table);
    }

    private static String mapString(Object s, int l) {
        String format = "%" + l + "s";
        return String.format(format, s);
    }

}