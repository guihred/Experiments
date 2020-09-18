package graphs.app;

import static ml.data.JavaFileDependency.getJavaFileDependencies;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ml.data.JavaFileDependency;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public class PackageTopology extends BaseTopology {

    private static final Logger LOG = HasLogging.log();
    private String chosenPackageName;
    private ObservableList<String> packages = getJavaFileDependencies(null).stream().map(JavaFileDependency::getPackage)
            .distinct().collect(Collectors.toCollection(FXCollections::observableArrayList));

    public PackageTopology(@NamedArg("graph") Graph graph) {
        super(graph);
        packages.add("");
    }

    @Override
    public void execute() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();
        List<JavaFileDependency> allFileDependencies = JavaFileDependency.getAllFileDependencies();
        Map<String, Map<String, Long>> packageDependencyMap =
                createFileDependencyMap(allFileDependencies, chosenPackageName);
        List<String> collect = packageDependencyMap.entrySet().stream()
                .flatMap(e -> Stream.concat(Stream.of(e.getKey()), e.getValue().keySet().stream()))
                .distinct()
                .collect(Collectors.toList());
        for (String packageName : collect) {
            graph.getModel().addCell(packageName, CellType.RECTANGLE);
        }
        List<Cell> cells = graph.getModel().getAddedCells();
        for (Cell cell : cells) {
            String cellId = cell.getCellId();
            Map<String, Long> map = packageDependencyMap.getOrDefault(cellId, Collections.emptyMap());
            cell.setSelected(packageDependencyMap.containsKey(cellId));
            map.forEach((dep, weight) -> graph.getModel().addEdge(cellId, dep, weight.intValue()));
        }
        graph.endUpdate();
        LayerLayout.layoutInLayers(graph.getModel().getAllCells(), graph.getModel().getAllEdges(),
                graph.getScrollPane().getViewportBounds().getWidth() / 2, graph.getScrollPane().getHeight());

    }

    public ObservableList<String> getPackages() {
        return packages;
    }

    public void setChosenPackageName(String chosenPackageName) {
        this.chosenPackageName = chosenPackageName;
    }

    public static Map<String, Map<String, Long>> createFileDependencyMap(Collection<JavaFileDependency> javaFiles) {
        // List<String> classesNames =
        // javaFiles.stream().map(JavaFileDependency::getName).sorted()
        // .collect(Collectors.toList());
        return javaFiles.stream().collect(Collectors.toMap(JavaFileDependency::getFullName, k -> k.getClasses().stream()
                // .filter(classesNames::contains)
                .collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
    }

    public static Map<String, Map<String, Long>> createFileDependencyMap(Collection<JavaFileDependency> allFiles,
            String packName) {
        return allFiles.stream().filter(e -> StringUtils.isBlank(packName) || e.getPackage().equals(packName))
                .collect(Collectors.toMap(JavaFileDependency::getFullName, k -> convertToMap(allFiles, k)));
    }


    public static void printDependencyMap(Map<String, Map<String, Long>> packageDependencyMap) {
        List<String> packNames = packageDependencyMap.keySet().stream()
                .sorted(Comparator
                        .comparing(e -> packageDependencyMap.get(e).values().stream().mapToLong(l -> l).sum()))
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

    private static Map<String, Long> convertToMap(Collection<JavaFileDependency> allFiles, JavaFileDependency k) {
        return k.getClasses().stream().filter(e -> allFiles.stream().anyMatch(d -> Objects.equals(d.getName(), e)))
                .collect(Collectors.groupingBy(e -> getFullName(allFiles, e), Collectors.counting()));
    }

    private static String getFullName(Collection<JavaFileDependency> allFiles, String e) {
        return allFiles.stream().filter(d -> Objects.equals(d.getName(), e)).findFirst()
                .map(JavaFileDependency::getFullName).orElse(null);
    }

    private static String mapString(Object s, int l) {
        String format = "%" + l + "s";
        return String.format(format, s);
    }

}