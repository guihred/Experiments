package fxtests;

import ethical.hacker.CoverageUtils;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import ml.data.*;
import ml.graph.DataframeExplorer;
import org.junit.Test;
import org.nd4j.linalg.io.Assert;
import utils.FileTreeWalker;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class DataframeMLTest extends AbstractTestExecution {

    @Test
    public void testCoverageFile() {
        File csvFile = CoverageUtils.getCoverageFile();
        if (!csvFile.exists()) {
            return;
        }

        DataframeML b = DataframeBuilder.build(csvFile);
        b.filter("INSTRUCTION_COVERED", v -> ((Number) v).intValue() == 0);
        DataframeUtils.describe(b);
    }

    @Test
    public void testDataframeExplorer() {
        DataframeExplorer show = show(DataframeExplorer.class);
        File csvFile = FileTreeWalker.getRandomPathByExtension(ResourceFXUtils.getOutFile(), ".csv").toFile();
        if (!csvFile.exists()) {
            return;
        }
        show.readDataframe(csvFile, 1000);
        show.save(ResourceFXUtils.getOutFile("csv/" + csvFile.getName()));
        lookup(ComboBox.class).forEach(c -> selectComboItems(c, 2));
        lookup(TextField.class).forEach(e -> {
            tryClickOn(e);
            write(nextInt(10) + "");
        });
        List<Button> lookupList = lookupList(Button.class);
        Button button = lookupList.get(3);
        tryClickOn(button);
        tryClickOn(button);
        tryClickOn(button);
        List<KeyCode> codes = Arrays.asList(KeyCode.DELETE, KeyCode.SUBTRACT);
        lookup(ListView.class).forEach(listView -> {
            from(listView).lookup(ListCell.class::isInstance).queryAll().stream().limit(3).forEach(t -> {
                tryClickOn(t);
                type(randomItem(codes));
            });
        });
    }

    @Test
    public void testExcelData() {
        Path firstPathByExtension =
                FileTreeWalker.getRandomPathByExtension(ResourceFXUtils.getOutFile().getParentFile(), ".xlsx");
        DataframeML readExcel = measureTime("ExcelDataReader.readExcel",
                () -> ExcelDataReader.readExcel(firstPathByExtension.toFile()));
        measureTime("DataframeUtils.toString", () -> DataframeUtils.toString(readExcel));
    }

    @Test
    public void testMakeStats() {
        Path randomPathByExtension = FileTreeWalker.getRandomPathByExtension(ResourceFXUtils.getOutFile(), ".csv");
        measureTime("DataframeUtils.makeStats",
                () -> DataframeBuilder.builder(randomPathByExtension.toFile()).makeStats());
    }

    @Test
    public void testMapping() {
        measureTime("Mapping.getMethods", () -> Mapping.getMethods());
    }

    @Test
    public void testMappingExecution() {
        DataframeExplorer show = show(DataframeExplorer.class);
        File csvFile = FileTreeWalker.getRandomPathByExtension(ResourceFXUtils.getOutFile(), ".csv").toFile();
        if (!csvFile.exists()) {
            return;
        }
        show.readDataframe(csvFile, 1000);
        holding(KeyCode.CONTROL, () -> {
            from(lookupFirst(ListView.class)).lookup(ListCell.class::isInstance).queryAll().stream().limit(2)
                    .forEach(this::tryClickOn);
        });
        testMappingMenu();
    }

    @Test
    public void testNotExists() {
        File csvFile = ResourceFXUtils.getOutFile("notExists");
        DataframeML b = DataframeBuilder.build(csvFile);
        DataframeUtils.describe(b);
    }

    @Test
    public void testTableTabs() {
        DataframeExplorer show = show(DataframeExplorer.class);
        File csvFile = FileTreeWalker.getRandomPathByExtension(ResourceFXUtils.getOutFile(), ".csv").toFile();
        if (!csvFile.exists()) {
            return;
        }
        show.readDataframe(csvFile, 1000);
        holding(KeyCode.CONTROL, () -> from(lookupFirst(ListView.class)).lookup(ListCell.class::isInstance).queryAll()
                .stream().limit(2).forEach(this::tryClickOn));
        Set<Node> queryAll2 = lookup(".tab").queryAll();
        for (Node node : queryAll2) {
            tryClickOn(node);
            Set<Node> queryAs =
                    lookup(".tab-content-area").queryAll().stream().filter(Node::isVisible).collect(Collectors.toSet());
            List<KeyCode> asList = Arrays.asList(KeyCode.S, KeyCode.C, KeyCode.SUBTRACT, KeyCode.ADD, KeyCode.A);
            from(queryAs).lookup(TableRow.class::isInstance).queryAll().stream().filter(Node::isVisible).limit(2)
                    .forEachOrdered(t -> {
                        tryClickOn(t);
                        press(KeyCode.CONTROL);
                        type(randomItem(asList));
                        release(KeyCode.CONTROL);
                        type(KeyCode.ENTER);
                        press(KeyCode.ALT);
                        type(KeyCode.S);
                        release(KeyCode.ALT);
                    });

        }

    }

    @Test
    public void testTransformOneValue() {
        DataframeBuilder b = DataframeBuilder.builder("california_housing_train.csv");
        DataframeML x = measureTime("DataframeML.build", () -> b.build());
        measureTime("DataframeML.describe", () -> DataframeUtils.describe(x));
        measureTime("DataframeML.toString", () -> DataframeUtils.toString(x));
        measureTime("DataframeML.cols", x::cols);
        measureTime("DataframeML.correlation", () -> DataframeUtils.displayCorrelation(x));
        measureTime("DataframeML.trim", () -> DataframeUtils.trim("population", 10, x));
        measureTime("DataframeML.apply", () -> x.apply("population", s -> s));
        measureTime("DataframeML.createNumberEntries", () -> DataframeStatisticAccumulator
                .createNumberEntries(x.getDataframe(), x.getSize(), "longitude", "latitude"));
        DataframeBuilder b2 = DataframeBuilder.builder("cities.csv");
        measureTime("DataframeML.displayStats", () -> DataframeUtils.displayStats(b2.build()));
        Map<Double, Long> histogram =
                measureTime("DataframeML.histogram", () -> DataframeUtils.histogram(x, "population", 10));
        Assert.notNull(histogram, "Must not be null");
    }

    private void testMappingMenu() {
        MenuItem menuItem = lookupFirst(ListView.class).getContextMenu().getItems().get(1);
        interactNoWait(menuItem::fire);
        ComboBox<?> methods = lookup("#methodCombo").queryAs(ComboBox.class);
        selectComboItems(methods, nextInt(methods.getItems().size()));
        clickOn("Add");
    }
}
