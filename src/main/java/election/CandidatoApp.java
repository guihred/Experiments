package election;

import static election.CandidatoHelper.*;
import static utils.CommonsFX.onCloseWindow;

import java.util.List;
import javafx.beans.Observable;
import javafx.stage.Stage;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.HibernateUtil;

public class CandidatoApp extends CandidatoAppVariables {

    public void initialize() {
        CrawlerTask.insertProxyConfig();
        List<String> relevantFields = getRelevantFields();
        for (String field : relevantFields) {
            SimpleTreeViewBuilder.addToRoot(treeView0, field, distinct(field));
        }
        CandidatoHelper.configTable(fotoUrl, cidade, eleito, nascimento, tableView2);
        tableView2.setItems(CommonsFX.newFastFilter(filter, candidates.filtered(e -> true)));
        column.addListener((ob, o, n) -> updateTable(first, maxResult.get(), n, pieGraph, candidates, fieldMap));
        maxResult
            .addListener((ob, o, n) -> updateTable(first, n.intValue(), column.get(), pieGraph, candidates, fieldMap));
        slider20.valueProperty().bindBidirectional(pieGraph.legendsRadiusProperty());
        treeView0.getSelectionModel().selectedItemProperty()
            .addListener((ob, o, newValue) -> onChangeElement(fieldMap, portChecks, newValue));
        bindTextToMap(text18, fieldMap);
        fieldMap.addListener(
            (Observable e) -> updateTable(first, maxResult.get(), column.get(), pieGraph, candidates, fieldMap));
        column.set(relevantFields.get(0));
    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Candidato App", "CandidatoApp.fxml", this, primaryStage);
        onCloseWindow(primaryStage, HibernateUtil::shutdown);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
