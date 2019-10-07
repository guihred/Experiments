package graphs.app;

import static graphs.app.JavaFileDependency.getJavaFileDependencies;

import graphs.entities.Cell;
import graphs.entities.CellType;
import graphs.entities.Graph;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import utils.HasLogging;

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
        createGraph();

    }

    public ObservableList<String> getPackages() {
        return packages;
    }

    public void setChosenPackageName(String chosenPackageName) {
        this.chosenPackageName = chosenPackageName;
    }

    private List<JavaFileDependency> createGraph() {
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();
        List<JavaFileDependency> javaFiles = getJavaFileDependencies(chosenPackageName);
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
				graph.getScrollPane().getViewportBounds().getWidth() / 2,
            graph.getScrollPane().getHeight());
        return javaFiles;
    }

    public static Map<String, Map<String, Long>> createFileDependencyMap(Collection<JavaFileDependency> javaFiles) {
        List<String> classesNames = javaFiles.stream().map(JavaFileDependency::getName).sorted()
            .collect(Collectors.toList());
        return javaFiles.stream().collect(Collectors.toMap(JavaFileDependency::getName, k -> k.getClasses().stream()
            .filter(classesNames::contains).collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
    }


    public static void main(String[] args) {

        List<JavaFileDependency> javaFiles = getJavaFileDependencies(null);
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