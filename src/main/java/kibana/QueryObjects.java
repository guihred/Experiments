package kibana;

import static javafx.collections.FXCollections.observableArrayList;
import static javafx.collections.FXCollections.synchronizedObservableList;

import java.util.Map;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TableView;

class QueryObjects {
    private final String query;

    private String queryFile;
    private String[] params;
    private String group = "";
    private TableView<Map<String, String>> table;

    private final ObservableList<Map<String, String>> items = synchronizedObservableList(observableArrayList());
    private final ObservableList<Series<Number, Number>> series = synchronizedObservableList(observableArrayList());

    private LineChart<Number, Number> lineChart;

    public QueryObjects(String query, String queryFile, LineChart<Number, Number> lineChart) {
        this.query = query;
        this.queryFile = queryFile;
        this.lineChart = lineChart;

    }

    public QueryObjects(String query, String queryFile, TableView<Map<String, String>> table, String[] params) {
        this.query = query;
        this.queryFile = queryFile;
        this.table = table;
        this.params = params;
    }

    public String getGroup() {
        return group;
    }

    public ObservableList<Map<String, String>> getItems() {
        return items;
    }

    public LineChart<Number, Number> getLineChart() {
        return lineChart;
    }

    public String[] getParams() {
        return params;
    }

    public String getQuery() {
        return query;
    }

    public String getQueryFile() {
        return queryFile;
    }

    public ObservableList<Series<Number, Number>> getSeries() {
        return series;
    }

    public TableView<Map<String, String>> getTable() {
        return table;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}