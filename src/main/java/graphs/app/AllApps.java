package graphs.app;

import static java.util.stream.Collectors.toCollection;
import static ml.data.JavaFileDependency.getAllFileDependencies;
import static utils.ClassReflectionUtils.getInstance;
import static utils.CommonsFX.newFastFilter;
import static utils.HibernateUtil.shutdown;
import static utils.ex.FunctionEx.apply;
import static utils.ex.PredicateEx.makeTest;
import static utils.ex.RunnableEx.make;
import static utils.ex.RunnableEx.run;

import ethical.hacker.ssh.PrintTextStream;
import java.io.PrintStream;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.JavaFileDependency;
import simplebuilder.SimpleListViewBuilder;
import utils.ExtractUtils;
import utils.ex.SupplierEx;

public class AllApps extends Application {

    @Override
    public void start(Stage primaryStage) {
        TextArea right = new TextArea("");
        right.setEditable(false);
        ExtractUtils.insertProxyConfig();
        primaryStage.setTitle("All Apps");
        ObservableList<Class<?>> items = getAllFileDependencies().stream().map(JavaFileDependency::getFullName)
                .map(a -> apply(Class::forName, a)).filter(Objects::nonNull)
                .filter(makeTest(e -> e.getMethod("main", String[].class) != null))
                .collect(toCollection(FXCollections::observableArrayList));
        PrintStream out = System.out;
        System.setOut(SupplierEx.remap(() -> new PrintTextStream(out, true, "UTF-8", right.textProperty()),
                "ERROR CREATING STREAM"));
        TextField resultsFilter = new TextField();
        ListView<Class<?>> build = new SimpleListViewBuilder<Class<?>>().onDoubleClick(AllApps::invoke)
                .items(newFastFilter(resultsFilter, items.filtered(e -> true))).build();
        ScrollPane right2 = new ScrollPane(right);
        right2.setPrefSize(100, 100);
        SplitPane center = new SplitPane(build, right);
        DoubleProperty positionProperty = center.getDividers().get(0).positionProperty();
        Scene value = new Scene(new VBox(new Text("Filter"), resultsFilter, center));
        right.prefWidthProperty().bind(positionProperty.add(-1).negate().multiply(value.getWidth()));
        primaryStage.setScene(value);
        primaryStage.setOnCloseRequest(e -> {
            shutdown();
            System.exit(0);
        });
        primaryStage.showingProperty().addListener((ob, old, val) -> {
            if (!val) {
                System.setOut(out);
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void invoke(Class<?> appClass) {
        if (Application.class.isAssignableFrom(appClass)) {
            Platform.runLater(make(() -> {
                Application a = (Application) getInstance(appClass);
                a.start(new Stage());
            }));
            return;
        }
        run(() -> appClass.getMethod("main", String[].class).invoke(null, new Object[] { new String[0] }));
    }
}
