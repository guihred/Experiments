package schema.sngpc;

import static java.util.stream.Collectors.joining;
import static others.TreeElement.compareTree;
import static others.TreeElement.displayMissingElement;
import static utils.ResourceFXUtils.convertToURL;
import static utils.RunnableEx.make;

import com.google.common.collect.ImmutableMap;
import contest.db.ContestApplication;
import java.io.File;
import java.util.*;
import java.util.function.Function;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.paint.*;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import utils.*;

public final class FXMLCreatorHelper {
    private static final Logger LOG = HasLogging.log();

    static final List<String> IGNORE = Arrays.asList("needsLayout", "layoutBounds", "baselineOffset",
        "localToParentTransform", "eventDispatcher", "skin", "background", "controlCssMetaData", "pseudoClassStates",
        "localToSceneTransform", "parentPopup", "cssMetaData", "classCssMetaData", "boundsInParent", "boundsInLocal",
        "scene", "childrenUnmodifiable", "styleableParent", "parent", "labelPadding");
    static final List<Class<?>> METHOD_CLASSES = Arrays.asList(EventHandler.class);
    static final List<Class<?>> ATTRIBUTE_CLASSES = Arrays.asList(Double.class, String.class, Color.class,
        LinearGradient.class, RadialGradient.class, Long.class, Integer.class, Boolean.class, Enum.class,
        KeyCombination.class);
    static final List<Class<?>> NECESSARY_REFERENCE = Arrays.asList(Control.class);
    static final List<Class<?>> REFERENCE_CLASSES = Arrays.asList(ToggleGroup.class, Image.class);
    static final List<Class<?>> NEW_TAG_CLASSES = Arrays.asList(ConstraintsBase.class, EventTarget.class,
        PathElement.class);
    static final List<Class<?>> CONDITIONAL_TAG_CLASSES = Arrays.asList(Insets.class, Font.class, Point3D.class,
        Material.class, PropertyValueFactory.class, ConstraintsBase.class, EventTarget.class, Effect.class,
        StringConverter.class, SelectionModel.class, Paint.class, Enum.class, Number.class);
    static final Map<String, Function<Collection<?>, String>> FORMAT_LIST = ImmutableMap
        .<String, Function<Collection<?>, String>>builder()
        .put("styleClass", l -> l.stream().map(Object::toString).collect(joining(" ")))
        .put("stylesheets", FXMLCreator::mapStylesheet)
        .build();

    static final Map<String, String> PROPERTY_REMAP = ImmutableMap.<String, String>builder()
        .put("gridpane-column", "GridPane.columnIndex").put("gridpane-row", "GridPane.rowIndex")
        .put("hbox-hgrow", "HBox.hgrow").put("vbox-vgrow", "VBox.vgrow").put("tilepane-alignment", "TilePane.alignment")
        .put("stackpane-alignment", "StackPane.alignment").put("pane-bottom-anchor", "AnchorPane.bottomAnchor")
        .put("pane-right-anchor", "AnchorPane.rightAnchor").put("pane-left-anchor", "AnchorPane.leftAnchor")
        .put("pane-top-anchor", "AnchorPane.topAnchor").put("borderpane-alignment", "BorderPane.alignment")
        .put("gridpane-halignment", "GridPane.halignment").put("gridpane-valignment", "GridPane.valignment")
        .put("gridpane-column-span", "GridPane.columnSpan").put("gridpane-row-span", "GridPane.rowSpan").build();
    private FXMLCreatorHelper() {
    }

    public static void createXMLFile(Parent node, File file) {
        new FXMLCreator().createFXMLFile(node, file);
    }

    public static Stage duplicate(File file, String title) {
        if (Platform.isFxApplicationThread()) {
            return duplicateStage(file, title);
        }
        ResourceFXUtils.initializeFX();
        SimpleObjectProperty<Stage> stage = new SimpleObjectProperty<>();
        Platform.runLater(() -> stage.set(duplicateStage(file, title)));
        while (stage.get() == null) {
//            DOES NOTHING
        }

        return stage.get();
    }

    public static void duplicate(String out) {
        if (Platform.isFxApplicationThread()) {
            duplicateStage(ResourceFXUtils.getOutFile(out));
        } else {
            ResourceFXUtils.initializeFX();
            Platform.runLater(() -> duplicateStage(ResourceFXUtils.getOutFile(out)));
        }
    }

    public static void duplicateStage(File file) {
        duplicate(file, file.getName());
    }

    public static Stage duplicateStage(File file, String title) {
        Stage primaryStage = new Stage();
        loadFXML(file, title, primaryStage);
        return primaryStage;
    }

    public static void loadFXML(File file, String title, Stage primaryStage, double... size) {
        try {
            Parent content = FXMLLoader.load(convertToURL(file));
            Scene scene = size.length == 2 ? new Scene(content, size[0], size[1]) : new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeIOException("ERROR in file " + file, e);
        }
    }

    public static void main(String[] argv) {
        List<Class<? extends Application>> classes = Arrays.asList(ContestApplication.class);
        testApplications(classes, false);
    }

    public static List<Class<?>> testApplications(List<Class<? extends Application>> asList) {
        return testApplications(asList, true);
    }

    public static List<Class<?>> testApplications(List<Class<? extends Application>> asList, boolean close) {
        return testApplications(asList, close, new ArrayList<>());
    }

    public static List<Class<?>> testApplications(List<Class<? extends Application>> asList, boolean close,
        List<Class<? extends Application>> differentTree) {
        ResourceFXUtils.initializeFX();
        List<Class<?>> errorClasses = new ArrayList<>();
        for (Class<? extends Application> class1 : asList) {
            List<Stage> stages = new ArrayList<>();

            Platform.runLater(RunnableEx.make(() -> testSingleApp(class1, stages, close, differentTree), error -> {
                LOG.error("ERROR IN {} ", class1);
                LOG.error("", error);
                errorClasses.add(class1);
                if (close) {
                    stages.forEach(Stage::close);
                }
            }));

        }
        return errorClasses;
    }


    private static void testSingleApp(Class<? extends Application> appClass, List<Stage> stages, boolean close,
        Collection<Class<? extends Application>> differentTree) {
        CrawlerTask.insertProxyConfig();
        LOG.info("INITIALIZING {}", appClass.getSimpleName());
        Application a = ClassReflectionUtils.getInstance(appClass);
        Stage primaryStage = new Stage();
        stages.add(primaryStage);
        primaryStage.setTitle(appClass.getSimpleName());
        make(() -> a.start(primaryStage), e -> throwException(appClass, e)).run();
        primaryStage.toBack();
        File outFile = ResourceFXUtils.getOutFile(appClass.getSimpleName() + ".fxml");
        Parent root = primaryStage.getScene().getRoot();
        root.getStylesheets().addAll(primaryStage.getScene().getStylesheets());
        LOG.info("CREATING {}.fxml", appClass.getSimpleName());
        createXMLFile(root, outFile);
        Stage duplicateStage = duplicateStage(outFile, primaryStage.getTitle());
        duplicateStage.toBack();
        stages.add(duplicateStage);
        if (close) {
            stages.forEach(Stage::close);
        }
        Parent root2 = duplicateStage.getScene().getRoot();
        if (!compareTree(root, root2)) {
            LOG.info("{} has different tree", appClass.getSimpleName());
            String displayMissingElement2 = displayMissingElement(root, root2).stream()
                .collect(joining("\n", "\n", ""));
            LOG.info("MISSING {}", displayMissingElement2);
            differentTree.add(appClass);
        }
        LOG.info("{} successfull", appClass.getSimpleName());
    }

    private static void throwException(Class<? extends Application> appClass, Throwable e) {
        throw new RuntimeIOException("ERROR IN " + appClass, e);
    }
}
