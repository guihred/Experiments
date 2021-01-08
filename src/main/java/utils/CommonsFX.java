package utils;

import static utils.ResourceFXUtils.convertToURL;
import static utils.ResourceFXUtils.toExternalForm;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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

    public static void addProgress(Property<Number> progress, double value) {
        RunnableEx.runIf(progress,
                p -> CommonsFX.runInPlatformSync(() -> p.setValue(p.getValue().doubleValue() + value)));
    }

    public static <T> void bind(ObservableValue<T> source, Property<T> target) {
        source.addListener((ob, old, val) -> runInPlatform(() -> target.setValue(val)));
        target.setValue(source.getValue());
    }

    public static <T> void bindBidirectional(Property<T> prop1, Property<T> prop2) {
        prop1.addListener((ob, old, val) -> prop2.setValue(val));
        prop2.addListener((ob, old, val) -> prop1.setValue(val));
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

    public static <T> FilteredList<T> newFastFilter(TextField filterField, FilteredList<T> filteredData) {
        filterField.textProperty()
                .addListener((o, old,
                        value) -> RunnableEx.run(() -> filteredData.setPredicate(row -> StringUtils.isBlank(value)
                                || StringUtils.containsIgnoreCase(row.toString(), value)
                                || PredicateEx.test(s -> s.matches(value), row.toString()))));
        return filteredData;
    }

    public static <T> FilteredList<T> newFastFilter(TextField filterField, ObservableList<T> data) {
        FilteredList<T> filteredData = data.filtered(e -> true);
        filterField.textProperty()
                .addListener((o, old,
                        value) -> RunnableEx.run(() -> filteredData.setPredicate(row -> StringUtils.isBlank(value)
                                || StringUtils.containsIgnoreCase(row.toString(), value)
                                || PredicateEx.test(s -> s.matches(value), row.toString())
                                || PredicateEx.test(s -> StringSigaUtils.anyMatches(s, value), row.toString()))));
        return filteredData;
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

    public static <T> T runInPlatformSync(SupplierEx<T> run) {
        AtomicReference<T> a = new AtomicReference<>();
        AtomicReference<Throwable> ex = new AtomicReference<>();
        Platform.runLater(() -> a.set(SupplierEx.makeSupplier(run, ex::set).get()));
        while (a.get() == null && ex.get() == null) {
            // DOES NOTHING
        }
        return a.get();
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
