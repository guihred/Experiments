package utils;

import static utils.ResourceFXUtils.convertToURL;
import static utils.ResourceFXUtils.toExternalForm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import utils.ex.PredicateEx;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public final class CommonsFX {

    public static final String FXML_DIR = "fxml/";
    private static final String CSS_DIR = "css/";

    private CommonsFX() {
    }

    public static void addCSS(Scene value, String css) {
        value.getStylesheets().add(toExternalForm(CSS_DIR + css));
    }

    public static boolean containsMouse(Node node, MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        for (Node parent = node; event.getSource() != parent.getParent(); parent = parent.getParent()) {
            x -= parent.getParent().getBoundsInParent().getMinX();
            y -= parent.getParent().getBoundsInParent().getMinY();
        }
        return node.getBoundsInParent().contains(x, y);
    }

    public static Node[] createField(String nome, StringProperty propriedade) {
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(propriedade);
        return new Node[] { new Label(nome), textField };
    }

    public static List<Color> generateRandomColors(final int size) {
        final int maxByte = 255;
        int max = 256;
        List<Color> availableColors = new ArrayList<>();
        int cubicRoot = Integer.max((int) Math.ceil(Math.pow(size, 1.0 / 3.0)), 2);
        for (int i = 0; i < cubicRoot * cubicRoot * cubicRoot; i++) {
            Color rgb = Color.rgb(Math.abs(maxByte - i / cubicRoot / cubicRoot % cubicRoot * max / cubicRoot) % max,
                    Math.abs(maxByte - i / cubicRoot % cubicRoot * max / cubicRoot) % max,
                    Math.abs(maxByte - i % cubicRoot * max / cubicRoot) % max);

            availableColors.add(rgb);
        }
        Collections.shuffle(availableColors);
        return availableColors;
    }
    public static void initializeFX() {
        Platform.setImplicitExit(false);
        new JFXPanel().toString();
    }

    public static void loadFXML(String title, File file, Stage primaryStage, double... size) {
        RunnableEx.remap(() -> {
            Parent content = FXMLLoader.load(convertToURL(file));
            Scene scene = size.length == 2 ? new Scene(content, size[0], size[1]) : new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        }, "ERROR in file " + file);
    }

    public static void loadFXML(String title, String file, Object controller, Stage primaryStage, double... size) {
        loadFXML(title, ResourceFXUtils.toFile(FXML_DIR + file), controller, primaryStage, size);
    }

    public static void loadFXML(String title, String file, Stage primaryStage, double... size) {
        loadFXML(title, ResourceFXUtils.toFile(FXML_DIR + file), primaryStage, size);
    }

    public static Parent loadParent(File file, Object controller) {
        return SupplierEx.remap(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(convertToURL(file));
            fxmlLoader.setController(controller);
            return fxmlLoader.load();
        }, "ERROR IN " + file);
    }

    public static Parent loadParent(String file, Object controller) {
        return loadParent(ResourceFXUtils.toFile(FXML_DIR + file), controller);
    }

    public static void loadRoot(String arquivo, Object root) {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceFXUtils.toURL(FXML_DIR + arquivo));
        fxmlLoader.setRoot(root);
        fxmlLoader.setController(root);
        RunnableEx.remap(fxmlLoader::load, "ERROR LOADING " + arquivo);
    }

    public static void loadRoot(String arquivo, Object root, Object controller) {
        FXMLLoader fxmlLoader = new FXMLLoader(ResourceFXUtils.toURL(FXML_DIR + arquivo));
        fxmlLoader.setRoot(root);
        fxmlLoader.setController(controller);
        RunnableEx.remap(fxmlLoader::load, "ERROR LOADING " + arquivo);
    }

    public static CheckBox newCheck(final String name, final BooleanProperty showWeight) {
        CheckBox checkBox = new CheckBox(name);
        checkBox.setSelected(showWeight.get());
        showWeight.bind(checkBox.selectedProperty());
        return checkBox;
    }

    public static CheckBox newCheckBox(final String text, final boolean disabled) {
        CheckBox build = new CheckBox(text);
        build.setDisable(disabled);
        return build;
    }

    public static <T> FilteredList<T> newFastFilter(TextField filterField, FilteredList<T> filteredData) {
        filterField.textProperty()
                .addListener((o, old,
                        value) -> filteredData.setPredicate(row -> StringUtils.isBlank(value)
                                || StringUtils.containsIgnoreCase(row.toString(), value)
                                || PredicateEx.test(s -> s.matches(value), row.toString())));
        return filteredData;
    }

    public static TextField newTextField(final String text, final int prefColumnCount) {
        TextField textField = new TextField(text);
        textField.setPrefColumnCount(prefColumnCount);
        return textField;
    }

    public static void onCloseWindow(Stage stage, RunnableEx run) {
        stage.showingProperty().addListener((ob, old, val) -> {
            if (!val) {
                RunnableEx.run(run);
            }
        });

    }

    public static void runInPlatform(RunnableEx run) {
        Platform.runLater(RunnableEx.make(run));
    }

    public static void runInPlatformSync(RunnableEx run) {
        AtomicBoolean a = new AtomicBoolean(false);
        Platform.runLater(() -> {
            RunnableEx.run(run);
            a.set(true);
        });
        while (!a.get()) {
            // DOES NOTHING
        }
    }

    public static void update(Property<Number> progress, double value) {
        if (progress != null) {
            CommonsFX.runInPlatform(() -> progress.setValue(value));
        }
    }

    private static void loadFXML(String title, File file, Object controller, Stage primaryStage, double... size) {
        RunnableEx.remap(() -> {
            Parent content = loadParent(file, controller);
            Scene scene = size.length == 2 ? new Scene(content, size[0], size[1], true) : new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        }, "ERROR in file " + file);
    }

}
