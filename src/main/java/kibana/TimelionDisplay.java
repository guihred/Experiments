package kibana;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import simplebuilder.ListHelper;
import simplebuilder.SimpleButtonBuilder;
import simplebuilder.SimpleComboBoxBuilder;

public class TimelionDisplay extends Application {

    private Map<String, String> filter = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        Tab content = getTimelineGraph("Timeline Usuários", TimelionApi.TIMELINE_USERS, "mdc.uid.keyword");
        Tab content2 = getTimelineGraph("Timeline Endereço de Origem", TimelionApi.TIMELINE_IPS, "clientip.keyword");
        TabPane tabPane = new TabPane(content, content2);
        tabPane.setSide(Side.LEFT);
        primaryStage.setTitle("Timelion Display");
        primaryStage.setScene(new Scene(tabPane));
        primaryStage.show();
    }

    private Tab getTimelineGraph(String text, String timelineUsers, String keyword) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public Number fromString(String string) {
                return LocalDateTime.parse(string).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }

            @Override
            public String toString(Number object) {
                return Instant.ofEpochMilli(object.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .toString();
            }
        });
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, new NumberAxis());
        ObservableList<Series<Number, Number>> timelionFullScan = FXCollections.observableArrayList();

        ObservableList<String> mapping = ListHelper.mapping(timelionFullScan, Series<Number, Number>::getName);

        FilteredList<Series<Number, Number>> filtered = timelionFullScan.filtered(e -> true);
        lineChart.setData(filtered);
        lineChart.setCreateSymbols(false);
        lineChart.setTitle(text);
        lineChart.setAnimated(false);
        lineChart.setVerticalZeroLineVisible(false);
        mapping.add(0, "");
        ComboBox<String> comboBox = new SimpleComboBoxBuilder<String>().items(mapping).onChange(
                (old, val) -> filtered.setPredicate(e -> StringUtils.isBlank(val) || Objects.equals(e.getName(), val)))
                .build();
        HBox content = new HBox(new VBox(new Text(keyword),
                SimpleButtonBuilder.newButton("Update",
                        e -> TimelionApi.timelionScan(timelionFullScan, timelineUsers, filter, "now-d")),
                comboBox), lineChart);
        HBox.setHgrow(lineChart, Priority.ALWAYS);
        return new Tab(text, content);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
