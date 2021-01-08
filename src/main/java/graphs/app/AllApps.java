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
import java.util.Objects;
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
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class AllApps extends Application {

    @FXML
    private ListView<Class<?>> stackParts;
    @FXML
    private TextField textField0;
    @FXML
    private TextArea textArea5;
    private PrintStream out;

    private ObservableList<Class<?>> applications = FXCollections.observableArrayList();
    public ObservableList<Class<?>> getApplications() {
        return applications;
    }

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        RunnableEx.runNewThread(() -> getAllFileDependencies().stream().map(JavaFileDependency::getFullName)
                .map(FunctionEx.ignore(Class::forName)).filter(Objects::nonNull)
                .filter(makeTest(e -> e.getMethod("main", String[].class) != null)).collect(Collectors.toList()),
                items -> CommonsFX.runInPlatform(() -> getApplications().setAll(items)));
        System.setOut(SupplierEx.remap(() -> new PrintTextStream(out, true, "UTF-8", textArea5.textProperty()),
                "ERROR CREATING STREAM"));
        SimpleListViewBuilder.of(stackParts).onDoubleClick(AllApps::invoke)
                .items(newFastFilter(textField0, getApplications().filtered(e -> true)));
    }

    public void onActionToFXML() {
        RunnableEx.runNewThread(() -> {
            List<Class<?>> selectedItems = stackParts.getSelectionModel().getSelectedItems();
            for (Class<?> appClass : selectedItems) {
                if (Application.class.isAssignableFrom(appClass)) {
                    FXMLCreatorHelper.testApplications(Arrays.asList(asAppClass(appClass)), false);
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

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Application> asAppClass(Class<?> appClass) {
        return (Class<? extends Application>) appClass;
    }

    private static void invoke(Class<?> appClass) {
        if (Application.class.isAssignableFrom(appClass)) {
            new SimpleDialogBuilder().show(asAppClass(appClass));
            return;
        }
        RunnableEx.run(() -> {
            Object[] args = new Object[] { new String[0] };
            ClassReflectionUtils.invoke(null, appClass.getMethod("main", String[].class), args);
        });
    }

}
