package graphs.app;

import static graphs.app.JavaFileDependency.getAllFileDependencies;
import static java.util.stream.Collectors.toCollection;
import static utils.ClassReflectionUtils.getInstance;
import static utils.CommonsFX.newFastFilter;
import static utils.FunctionEx.apply;
import static utils.HibernateUtil.shutdown;
import static utils.PredicateEx.makeTest;
import static utils.RunnableEx.make;
import static utils.RunnableEx.run;

import ethical.hacker.ssh.PrintTextStream;
import java.io.PrintStream;
import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simplebuilder.SimpleListViewBuilder;
import utils.CrawlerTask;

public class AllApps extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Text right = new Text("");
        CrawlerTask.insertProxyConfig();
        primaryStage.setTitle("All Apps");
        ObservableList<Class<?>> items = getAllFileDependencies().stream().map(JavaFileDependency::getFullName)
            .map(a -> apply(Class::forName, a)).filter(Objects::nonNull)
            .filter(makeTest(e -> e.getMethod("main", String[].class) != null))
            .collect(toCollection(FXCollections::observableArrayList));
        PrintStream out = System.out;
        System.setOut(new PrintTextStream(out, true, "UTF-8", right));
        TextField resultsFilter = new TextField();
        ListView<Class<?>> build = new SimpleListViewBuilder<Class<?>>().onDoubleClick(AllApps::invoke)
            .items(newFastFilter(resultsFilter, items.filtered(e -> true))).build();
        ScrollPane right2 = new ScrollPane(right);
        right2.setPrefSize(100, 100);
        right2.vmaxProperty().addListener(e -> right2.setVvalue(right2.getVmax()));

        primaryStage
            .setScene(new Scene(
                new BorderPane(new SplitPane(build, right2), new VBox(new Text("Filter"), resultsFilter), null, null,
                    null)));
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
