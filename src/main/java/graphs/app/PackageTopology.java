package graphs.app;

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
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.CommonsFX;
import utils.HasLogging;

public class PackageTopology extends BaseTopology {

    private static final Logger LOG = HasLogging.log();
	private String chosenPackageName;

    public PackageTopology(Graph graph) {
        super(graph, "Package");
	}

	@Override
	public void execute() {
        chosenPackageName = null;
        List<JavaFileDependecy> javaFiles = createGraph();
        displayDialogForShortestPath(
                javaFiles.stream().map(JavaFileDependecy::getPackage).distinct().collect(Collectors.toList()));

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
        List<Edge> addedEdges = graph.getModel().getAddedEdges();
        LayerLayout.layoutInLayers(cells, addedEdges);
        graph.endUpdate();
        return javaFiles;
    }

    private void displayDialogForShortestPath(List<String> javaFiles) {
        Stage dialog = new Stage();
        dialog.setTitle("Chose Package to Display");
        dialog.setWidth(70);
        ChoiceBox<String> c1 = new ChoiceBox<>(FXCollections.observableArrayList(javaFiles));
        Button okButton = CommonsFX.newButton("OK", event -> {
            if (c1.getValue() != null) {
                chosenPackageName = c1.getValue();
                createGraph();
                dialog.close();
            }
        });
        VBox root = new VBox(new Text("Source"), c1, okButton);
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();
    }

    public static Map<String, Map<String, Long>> createFileDependencyMap(Collection<JavaFileDependecy> javaFiles) {
        List<String> classesNames = javaFiles.stream().map(JavaFileDependecy::getName)
                    .collect(Collectors.toList());
            Map<String, Map<String, Long>> packageDependencyMap = new HashMap<>();
        javaFiles.forEach(k -> packageDependencyMap.put(k.getName(), k.getClasses().stream()
                .filter(classesNames::contains).collect(Collectors.groupingBy(e -> e, Collectors.counting()))));
        return packageDependencyMap;
    }

    public static List<JavaFileDependecy> getJavaFileDependencies(String packName) {
        File file = new File("src");
        try (Stream<Path> walk = Files.walk(file.toPath(), 20)) {
            return walk.filter(e -> e.toFile().getName().endsWith(".java"))
                    .map(JavaFileDependecy::new)
                    .filter(e -> packName == null || e.getPackage().equals(packName))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOG.error("", e);
            return new ArrayList<>();
        }
    }

    public static void main(String[] args) {

        List<JavaFileDependecy> javaFiles = getJavaFileDependencies(null);
        Map<String, List<JavaFileDependecy>> filesByPackage = javaFiles.stream()
                .collect(Collectors.groupingBy(JavaFileDependecy::getPackage));
        filesByPackage.forEach((pack, files) -> {
            Map<String, Map<String, Long>> packageDependencyMap = createFileDependencyMap(files);
            printDependencyMap(packageDependencyMap);
        });

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
        LOG.trace("{}", table);
    }

    private static String mapString(Object s, int l) {
        String format = "%" + l + "s";
        return String.format(format, s);
    }

}