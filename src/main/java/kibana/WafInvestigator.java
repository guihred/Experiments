package kibana;

import static java.util.stream.Collectors.toList;
import static kibana.QueryObjects.DESTINATION_IP_QUERY;
import static kibana.QueryObjects.POLICY_NAME;
import static kibana.QueryObjects.SOURCE_IP_QUERY;
import static kibana.QueryObjects.USER_NAME;

import extract.web.WhoIsScanner;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ex.RunnableEx;

public class WafInvestigator extends PaloAltoInvestigator {

    private static final String KEY = "key";

    @Override
    public void initialize() {
        ExtractUtils.addAuthorizationConfig();
        thresholdText.textProperty()
                .bind(Bindings.createStringBinding(
                        () -> String.format(Locale.ENGLISH, "%s (%.2f)", "Threshold", threshold.getValue()),
                        threshold.valueProperty()));
        String docCount = "doc_count";
        WhoIsScanner whoIsScanner = new WhoIsScanner();
        QueryObjects configureTable =
                configureTable(DESTINATION_IP_QUERY, "topAttackerQuery.json", acessosSistemaTable, KEY, docCount)
                        .setMappedColumn("DNS", map -> whoIsScanner.reverseDNS(map.get("key")));
        configureTable(POLICY_NAME, "topAlertsDestinationQuery.json", consultasTable, KEY, docCount);
        RunnableEx.runNewThread(() -> configureTable.makeKibanaQuery(filter, days.getValue()));
        configureTable(SOURCE_IP_QUERY, "topAttackedQuery.json", ipsTable, KEY, docCount);
        configureTimeline(USER_NAME, TimelionApi.TIMELINE_USERNAME, timelineSourceIP, ipCombo);
        configureTable(USER_NAME, "topUsersQuery.json", pathsTable, KEY, docCount);
        QueryObjects.linkFilter(filterList, filter, this::addToFilter);
        splitPane0.setDividerPositions(1. / 10);
    }

    @Override
    public void makeAutomatedSearch() {
        RunnableEx.runNewThread(() -> {
            List<QueryObjects> queries =
                    queryList.stream().filter(q -> q.getLineChart() == null).collect(Collectors.toList());
            List<String> applicationList = getApplicationList();
            ConsultasHelper.automatedSearch(DESTINATION_IP_QUERY, queries, applicationList, progress.progressProperty(),
                    days.getValue(), filter, threshold.getValue());
        });
    }

    @Override
    public void start(final Stage primaryStage) {
        final int width = 800;
        CommonsFX.loadFXML("Waf Investigator", "PaloAltoInvestigator.fxml", this, primaryStage, width, width);
    }

    @Override
    protected List<String> getApplicationList() {
        if (!acessosSistemaTable.getSelectionModel().getSelectedItems().isEmpty()) {
            return acessosSistemaTable.getSelectionModel().getSelectedItems().stream().map(e -> e.get(KEY))
                    .collect(toList());
        }
        return APPLICATION_LIST;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
