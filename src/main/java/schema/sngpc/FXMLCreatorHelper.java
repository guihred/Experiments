package schema.sngpc;

import static java.util.stream.Collectors.joining;
import static utils.RunnableEx.remap;
import static utils.TreeElement.compareTree;
import static utils.TreeElement.displayMissingElement;

import com.google.common.util.concurrent.Atomics;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import utils.*;

public final class FXMLCreatorHelper {
    private static final Logger LOG = HasLogging.log();

    private FXMLCreatorHelper() {
    }

    public static void createXMLFile(Parent node, File file) {
        new FXMLCreator().createFXMLFile(node, file);
    }

    public static Stage duplicateStage(File file, String title, double... size) {
        Stage primaryStage = new Stage();
        CommonsFX.loadFXML(title, file, primaryStage, size);
        return primaryStage;
    }

    public static void main(String[] argv) {
        List<Class<? extends Application>> classes = Arrays.asList(SngpcViewer.class);
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

            RunnableEx.make(() -> testSingleApp(class1, stages, close, differentTree), error -> {
                LOG.error("ERROR IN {} ", class1);
                LOG.error("", error);
                errorClasses.add(class1);
                if (close) {
                    Platform.runLater(() -> stages.forEach(Stage::close));
                }
            }).run();

        }
        return errorClasses;
    }

    private static void testSingleApp(Class<? extends Application> appClass, List<Stage> stages, boolean close,
        Collection<Class<? extends Application>> differentTree) {
        CrawlerTask.insertProxyConfig();
        LOG.info("INITIALIZING {}", appClass.getSimpleName());
        AtomicReference<Throwable> thrownEx = Atomics.newReference();
        AtomicReference<Stage> reference = Atomics.newReference();
        Platform.runLater(RunnableEx.make(() -> {
            Stage primaryStage = new Stage();
            stages.add(primaryStage);
            LOG.info("New instance {}", appClass.getSimpleName());
            Application a = ClassReflectionUtils.getInstance(appClass);
            primaryStage.setTitle(appClass.getSimpleName());
            LOG.info("Starting {}", appClass.getSimpleName());
            remap(() -> a.start(primaryStage), "ERROR IN " + appClass);
            primaryStage.toBack();
            reference.getAndSet(primaryStage);
        }, e -> thrownEx.set(e)));
        while (reference.get() == null) {
            // DOES NOTHING
            if (thrownEx.get() != null) {
                throw RunnableEx.newException("", thrownEx.get());
            }
        }
        LOG.info("Started {}", appClass.getSimpleName());
        Stage primaryStage = reference.get();
        reference.set(null);
        Platform.runLater(RunnableEx.make(() -> {
            LOG.info("CREATING {}.fxml", appClass.getSimpleName());
            File outFile = ResourceFXUtils.getOutFile(appClass.getSimpleName() + ".fxml");
            Scene scene = primaryStage.getScene();
            Parent root = scene.getRoot();
            root.getStylesheets().addAll(primaryStage.getScene().getStylesheets());
            createXMLFile(root, outFile);
            Stage duplicateStage = duplicateStage(outFile, primaryStage.getTitle(), scene.getWidth(),
                scene.getHeight());
            reference.set(duplicateStage);
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
        }, e -> thrownEx.set(e)));
        while (reference.get() == null) {
            if (thrownEx.get() != null) {
                throw RunnableEx.newException("", thrownEx.get());
            }
        }

        LOG.info("{} successfull", appClass.getSimpleName());
    }

}
