package graphs.app;

import static ml.data.JavaFileDependency.getAllFileDependencies;
import static utils.CommonsFX.newFastFilter;
import static utils.HibernateUtil.shutdown;
import static utils.ex.PredicateEx.makeTest;

import ethical.hacker.ssh.PrintTextStream;
import fxml.utils.FXMLCreatorHelper;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ml.data.JavaFileDependency;
import simplebuilder.SimpleDialogBuilder;
import simplebuilder.SimpleListViewBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

/**
 * Application for displaying all Java files present in the project, and
 * creating a way to run code
 * 
 * 
 * @author guigu
 * 
 */
public class AllApps extends Application {

    @FXML
    private ListView<String> stackParts;
    @FXML
    private TextField textField0;
    @FXML
    private TextArea textArea5;
    private PrintStream out;

    private ObservableList<String> applications = FXCollections.observableArrayList();

    /**
     * @return all applications developed
     */
    public ObservableList<String> getApplications() {
        return applications;
    }

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        RunnableEx.runNewThread(
                () -> getAllFileDependencies().stream()
                        .filter(makeTest(d -> d.getPublicStaticMethods().contains("main")))
                        .map(JavaFileDependency::getFullName).collect(Collectors.toList()),
                items -> CommonsFX.runInPlatform(() -> getApplications().setAll(items)));
        System.setOut(SupplierEx.remap(() -> new PrintTextStream(out, true, "UTF-8", textArea5.textProperty()),
                "ERROR CREATING STREAM"));
        SimpleListViewBuilder.of(stackParts).onDoubleClick(AllApps::invoke).multipleSelection()
                .items(newFastFilter(textField0, getApplications().filtered(e -> true)));
    }

    public void onActionToFXML() {
        RunnableEx.runNewThread(() -> {
            List<String> selectedItems = stackParts.getSelectionModel().getSelectedItems();
            for (String appClass : selectedItems) {
                Class<?> forName = Class.forName(appClass);

                if (Application.class.isAssignableFrom(forName)) {
                    FXMLCreatorHelper.testApplications(Arrays.asList(asAppClass(forName)), false);
                }
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        out = System.out;
        CommonsFX.loadFXML("All Apps", "AllApps.fxml", this, primaryStage);
        primaryStage.setOnCloseRequest(e -> {
            shutdown();
            System.exit(0);
        });
        primaryStage.showingProperty().addListener((ob, old, val) -> {
            if (!val) {
                System.setOut(out);
            }
        });
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Application> asAppClass(Class<?> appClass) {
        return (Class<? extends Application>) appClass;
    }

    private static void invoke(String className) {
        RunnableEx.run(() -> {
            Class<?> appClass = Class.forName(className);
            if (Application.class.isAssignableFrom(appClass)) {
                new SimpleDialogBuilder(true).show(asAppClass(appClass));
                return;
            }
            Object[] args = new Object[] { new String[0] };
            ClassReflectionUtils.invoke(null, appClass.getMethod("main", String[].class), args);
        });
    }

}
