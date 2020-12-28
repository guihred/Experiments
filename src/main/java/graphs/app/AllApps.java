package graphs.app;

import static ml.data.JavaFileDependency.getAllFileDependencies;
import static utils.CommonsFX.newFastFilter;
import static utils.HibernateUtil.shutdown;
import static utils.ex.PredicateEx.makeTest;
import static utils.ex.RunnableEx.run;

import ethical.hacker.ssh.PrintTextStream;
import fxml.utils.FXMLCreatorHelper;
import java.io.PrintStream;
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
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ex.FunctionEx;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class AllApps extends Application {

    @FXML
    private ListView<Class<? extends Application>> stackParts;
    @FXML
    private TextField textField0;
    @FXML
    private TextArea textArea5;

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        ObservableList<Class<?>> applications = FXCollections.observableArrayList();
        RunnableEx.runNewThread(() -> getAllFileDependencies().stream().map(JavaFileDependency::getFullName)
                .map(FunctionEx.ignore(Class::forName)).filter(Objects::nonNull)
                .filter(makeTest(e -> e.getMethod("main", String[].class) != null)).collect(Collectors.toList()),
                items -> CommonsFX.runInPlatform(() -> applications.setAll(items)));
        System.setOut(SupplierEx.remap(() -> new PrintTextStream(System.out, true, "UTF-8", textArea5.textProperty()),
                "ERROR CREATING STREAM"));
        SimpleListViewBuilder.of(stackParts).onDoubleClick(AllApps::invoke)
                .items(newFastFilter(textField0, applications.filtered(e -> true)));
    }

    public void onActionToFXML() {
        RunnableEx.runNewThread(() -> {
            List<Class<? extends Application>> selectedItems = stackParts.getSelectionModel().getSelectedItems();
            FXMLCreatorHelper.testApplications(selectedItems, false);
        });
    }

    @Override
    public void start(Stage primaryStage) {
        PrintStream out = System.out;
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
    private static void invoke(Class<?> appClass) {
        if (Application.class.isAssignableFrom(appClass)) {
            new SimpleDialogBuilder().show((Class<? extends Application>) appClass);
            return;
        }
        run(() -> appClass.getMethod("main", String[].class).invoke(null, new Object[] { new String[0] }));
    }
}
