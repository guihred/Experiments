package graphs.app;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import simplebuilder.SimpleListViewBuilder;
import utils.*;

public class AllApps extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("All Apps");
        List<JavaFileDependency> allFileDependencies = JavaFileDependency.getAllFileDependencies();
        List<Class<?>> collect = allFileDependencies.stream().map(JavaFileDependency::getFullName)
                .map(FunctionEx.makeFunction(Class::forName)).filter(e -> e != null)
                .filter(PredicateEx.makeTest(e -> e.getMethod("main", String[].class) != null))
                .collect(Collectors.toList());
        ObservableList<Class<?>> items = FXCollections.observableArrayList(collect);
        TextField resultsFilter = new TextField();
        ListView<Class<?>> build = new SimpleListViewBuilder<Class<?>>()
                .onDoubleClick(ConsumerEx
                        .makeConsumer(e -> {
                            Method method = e.getMethod("main", String[].class);
                            method.invoke(null, "");
                        }))
                .items(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true))).build();
        primaryStage.setScene(new Scene(new BorderPane(build, resultsFilter, null, null, null)));
        primaryStage.setOnCloseRequest(e -> HibernateUtil.shutdown());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
