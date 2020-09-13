package graphs.app;

import static java.util.stream.Collectors.toCollection;
import static utils.CommonsFX.newFastFilter;
import static utils.ex.PredicateEx.makeTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.CoverageUtils;
import simplebuilder.SimpleListViewBuilder;
import utils.ClassReflectionUtils;
import utils.ExtractUtils;

public class AllNodes extends Application {

    @Override
    public void start(Stage primaryStage) {
        Map<Class<?>, Node> hashMap = new HashMap<>();

        Text right = new Text("");
        ExtractUtils.insertProxyConfig();
        primaryStage.setTitle("All Nodes");
        ObservableList<Class<?>> items = CoverageUtils.getClasses(Node.class, Arrays.asList("com.")).stream()
                .filter(Objects::nonNull)
                .filter(e -> !Cell.class.isAssignableFrom(e))
                .filter(makeTest(ClassReflectionUtils::isClassPublic))
            .collect(toCollection(FXCollections::observableArrayList));
        TextField resultsFilter = new TextField();
        ScrollPane right2 = new ScrollPane(right);
        ListView<Class<?>> build = new SimpleListViewBuilder<Class<?>>()
                .onSelect((old, t) -> right2.setContent(hashMap.computeIfAbsent(t, AllNodes::createInstance)))
            .items(newFastFilter(resultsFilter, items.filtered(e -> true))).build();
        right2.setPrefSize(100, 100);
        right2.vmaxProperty().addListener(e -> right2.setVvalue(right2.getVmax()));

        primaryStage
            .setScene(new Scene(
                        new VBox(new Text("Filter"), resultsFilter, new SplitPane(build, right2))));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Node createInstance(Class<?> appClass) {
        Node instanceNull = (Node) ClassReflectionUtils.getInstanceNull(appClass);
        if (instanceNull instanceof Pane) {
            ((Pane) instanceNull).getChildren().addAll(new Text("1"),new Text("2"),new Text("3"));
        }

        return instanceNull;
    }
}
