package election;

import static election.CandidatoHelper.bindTextToMap;
import static election.CandidatoHelper.distinct;
import static election.CandidatoHelper.onChangeElement;
import static election.CandidatoHelper.updateTable;
import static utils.CommonsFX.onCloseWindow;

import javafx.beans.Observable;
import javafx.beans.value.ObservableObjectValue;
import javafx.stage.Stage;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.HibernateUtil;
import utils.ex.RunnableEx;

public class CandidatoApp extends CandidatoAppVariables {

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        ObservableObjectValue<String> column = columnName.getSelectionModel().selectedItemProperty();
        RunnableEx.runNewThread(CandidatoHelper::getRelevantFields, relevantFields -> CommonsFX.runInPlatform(() -> {
            for (String field : relevantFields) {
                SimpleTreeViewBuilder.addToRoot(treeView0, field, distinct(field));
            }
            columnName.getSelectionModel().select(relevantFields.get(0));
        }));
        CandidatoHelper.configTable(fotoUrl, cidade, eleito, nascimento, tableView2);
        tableView2.setItems(CommonsFX.newFastFilter(filter, candidates.filtered(e -> true)));
        ObservableObjectValue<Integer> maxResult = maxResultCombo.getSelectionModel().selectedItemProperty();
        column.addListener((ob, o, n) -> updateTable(pagination, maxResult.get(), n, pieGraph, candidates, fieldMap));
        maxResult.addListener(
                (ob, o, n) -> updateTable(pagination, n.intValue(), column.get(), pieGraph, candidates, fieldMap));
        slider20.valueProperty().bindBidirectional(pieGraph.legendsRadiusProperty());
        treeView0.getSelectionModel().selectedItemProperty()
                .addListener((ob, o, newValue) -> onChangeElement(fieldMap, portChecks, newValue));
        bindTextToMap(text18, fieldMap);
        fieldMap.addListener((Observable e) -> updateTable(pagination, maxResult.get(), column.get(), pieGraph,
                candidates, fieldMap));
        pagination.currentPageIndexProperty().addListener((Observable e) -> updateTable(pagination, maxResult.get(),
                column.get(), pieGraph, candidates, fieldMap));
        CommonsFX.bind(slider20.widthProperty(), pieGraph.widthProperty());
        CommonsFX.bind(slider20.widthProperty(), pieGraph.heightProperty());
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
