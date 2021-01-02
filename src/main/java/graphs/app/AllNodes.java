package graphs.app;

import fxml.utils.FXMLCreatorHelper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Cell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ml.data.CoverageUtils;
import simplebuilder.SimpleListViewBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.ex.PredicateEx;
import utils.ex.RunnableEx;

public class AllNodes extends Application {

    @FXML
    private ScrollPane scrollPane4;
    @FXML
    private TextField textField1;
    @FXML
    private ListView<Class<? extends Node>> build;

    public void initialize() {
        Map<Class<?>, Node> nodeMap = new HashMap<>();
        ExtractUtils.insertProxyConfig();
        ObservableList<Class<? extends Node>> items = FXCollections.observableArrayList();
        RunnableEx.runNewThread(() -> CoverageUtils.getClasses(Node.class, Arrays.asList("com.")).stream()
                .filter(Objects::nonNull).filter(e -> !Cell.class.isAssignableFrom(e))
                .filter(PredicateEx.makeTest(ClassReflectionUtils::isClassPublic))
                .collect(Collectors.toCollection(() -> items)));
        ScrollPane right2 = scrollPane4;
        SimpleListViewBuilder.of(build)
                .onSelect((old, t) -> right2.setContent(nodeMap.computeIfAbsent(t, AllNodes::createInstance)))
                .items(CommonsFX.newFastFilter(textField1, items.filtered(e -> true))).build();
        right2.setPrefSize(100, 100);
        right2.vmaxProperty().addListener(e -> right2.setVvalue(right2.getVmax()));
    }

    public void onActionToFXML() {
        RunnableEx.runNewThread(() -> {
            Node instance = scrollPane4.getContent();
            Parent node = instance instanceof Parent ? (Parent) instance
                    : CommonsFX.runInPlatformSync(() -> new VBox(instance));

            FXMLCreatorHelper.createXMLFile(node,
                    ResourceFXUtils.getOutFile("fxml/" + instance.getClass().getSimpleName() + ".fxml"),
                    Arrays.asList("children", "transforms"));
        });
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("All Nodes", "AllNodes.fxml", this, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static Node createInstance(Class<?> appClass) {
        Node instanceNull = (Node) ClassReflectionUtils.getInstanceNull(appClass);
        if (instanceNull instanceof Pane) {
            ((Pane) instanceNull).getChildren().addAll(new Text("1"), new Text("2"), new Text("3"));
        }
        return instanceNull;
    }
}
