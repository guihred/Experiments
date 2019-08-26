package schema.sngpc;

import static java.util.stream.Collectors.joining;
import static others.TreeElement.compareTree;
import static others.TreeElement.displayMissingElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.assertj.core.api.exception.RuntimeIOException;
import org.slf4j.Logger;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public final class FXMLCreatorHelper {
    private static final Logger LOG = HasLogging.log();

    private FXMLCreatorHelper() {
    }

    public static void createXMLFile(Parent node, File file) {
        new FXMLCreator().createFXMLFile(node, file);
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
        duplicateStage(file, file.getName());
    }

    public static Stage duplicateStage(File file, String title) {
        Stage primaryStage = new Stage();
        try {
            Parent content = FXMLLoader.load(file.toURI().toURL());
            Scene scene = new Scene(content);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeIOException("ERROR in file " + file, e);
        }
        return primaryStage;
    }

    public static void main(String[] argv) {
        List<Class<? extends Application>> classes = Arrays.asList(fxsamples.bounds.BoundsPlayground.class);
        testApplications(classes, false);
//        for (Class<? extends Application> class1 : asList) 
//            duplicate(class1.getSimpleName() + ".fxml");
//        }
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

    public static void testSingleApp(Class<? extends Application> appClass, List<Stage> stages, boolean close,
        Collection<Class<? extends Application>> differentTree) throws Exception {
        CrawlerTask.insertProxyConfig();
        LOG.info("INITIALIZING {}", appClass.getSimpleName());
        Application a = appClass.newInstance();
        Stage primaryStage = new Stage();
        stages.add(primaryStage);
        primaryStage.setTitle(appClass.getSimpleName());
        a.start(primaryStage);
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
}
