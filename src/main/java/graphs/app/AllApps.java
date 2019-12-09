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

import java.util.Objects;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simplebuilder.SimpleListViewBuilder;
import utils.CrawlerTask;

public class AllApps extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        CrawlerTask.insertProxyConfig();
        primaryStage.setTitle("All Apps");
        ObservableList<Class<?>> items = getAllFileDependencies().stream().map(JavaFileDependency::getFullName)
            .map(a -> apply(Class::forName, a)).filter(Objects::nonNull)
            .filter(makeTest(e -> e.getMethod("main", String[].class) != null))
            .collect(toCollection(FXCollections::observableArrayList));
        TextField resultsFilter = new TextField();
        ListView<Class<?>> build = new SimpleListViewBuilder<Class<?>>().onDoubleClick(AllApps::invoke)
            .items(newFastFilter(resultsFilter, items.filtered(e -> true))).build();
        primaryStage.setScene(new Scene(new BorderPane(build, resultsFilter, null, null, null)));
        primaryStage.setOnCloseRequest(e -> shutdown());
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
