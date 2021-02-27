package kibana;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;
import simplebuilder.SimpleComboBoxBuilder;
import simplebuilder.SimpleListViewBuilder;
import simplebuilder.SimpleTableViewBuilder;
import utils.CommonsFX;
import utils.QuickSortML;
import utils.StringSigaUtils;
import utils.ex.RunnableEx;

public class CredentialInvestigator extends KibanaInvestigator {

    private static final String IP_REGEX = "\\d+\\.\\d+\\.\\d+\\.\\d+";
    @FXML
    private Pane pane;
    private ComboBox<String> indexCombo;

    @Override
    public void initialize() {
        commonTable.setItems(CommonsFX.newFastFilter(resultsFilter, items.filtered(e -> true)));
        commonTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        SimpleTableViewBuilder.of(commonTable).copiable().savable()
                .onSortClicked((c, a) -> QuickSortML.sortMapList(items, c, a));

        indexCombo = new SimpleComboBoxBuilder<>(Arrays.asList("inss-*-prod-*", "mte-log4j-prod-*")).select(0).build();
        pane.getChildren().add(2, indexCombo);
        SimpleListViewBuilder.of(filterList).multipleSelection().copiable().deletable()
                .pasteable(s -> StringSigaUtils.getMatches(s, "(" + IP_REGEX + "|\\d{11})"));
    }

    @Override
    protected Map<String, String> executeCall(String ip, DoubleProperty progress, List<String> cols) {
        Map<String, String> result = new LinkedHashMap<>();
        Integer d = days.getSelectionModel().getSelectedItem();
        String index = indexCombo.getSelectionModel().getSelectedItem();
        CommonsFX.update(progress, 0);
        RunnableEx.run(() -> {

            if (ip.matches(IP_REGEX)) {
                Map<String, String> geridCredencial = KibanaApi.getGeridCredencial(ip, index, d);
                result.put("IP", ip);
                geridCredencial.forEach((k, v) -> {
                    result.merge("Credencial", k, (u, m) -> u + "\n" + m);
                    result.merge("Message", v.trim(), (u, m) -> u + "\n" + m);
                });
            } else {
                Map<String, String> iPsByCredencial = KibanaApi.getIPsByCredencial(ip, index, d);
                result.put("Credencial", ip);
                iPsByCredencial.forEach((k, v) -> {
                    result.merge("IP", k, (u, m) -> u + "\n" + m);
                    result.merge("Message", v.trim(), (u, m) -> u + "\n" + m);
                });
            }
            result.put("Login", KibanaApi.getLoginTimeCredencial(ip, index, d));
            result.computeIfAbsent("Message",
                    s -> KibanaApi.getMessageList(ip, index, d).stream().findFirst().orElse(""));
        });
        CommonsFX.update(progress, 1);

        return result;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
