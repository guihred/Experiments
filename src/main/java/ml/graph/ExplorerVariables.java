package ml.graph;

import java.util.Map.Entry;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ml.data.*;
import utils.CommonsFX;
import utils.ex.FunctionEx;

public abstract class ExplorerVariables extends Application {
    @FXML
    protected ComboBox<Entry<String, DataframeStatisticAccumulator>> headersCombo;
    @FXML
    protected ListView<Entry<String, DataframeStatisticAccumulator>> columnsList;
    @FXML
    protected AutocompleteField text;
    @FXML
    protected Button fillIP;
    @FXML
    protected ListView<Question> questionsList;
    @FXML
    protected ComboBox<QuestionType> questType;
    protected final ObjectProperty<DataframeML> dataframe = new SimpleObjectProperty<>();
    @FXML
    protected PaginatedTableView dataTable;
    @FXML
    protected TableView<XYChart.Data<String, Number>> histogram;
    @FXML
    protected ProgressIndicator progress;
    @FXML
    protected LineChart<Number, Number> lineChart;
    @FXML
    protected PieChart pieChart;
    @FXML
    protected BarChart<String, Number> barChart;

    public ExplorerVariables() {
        dataframe.addListener((ob, old, val) -> CommonsFX.runInPlatform(() -> {
            String fileName = FunctionEx.mapIf(getDataframe(), d -> d.getFile().getName(), "");
            ((Stage) questionsList.getScene().getWindow()).setTitle(String.format("Dataframe Explorer (%s)", fileName));
        }));
    }

    public DataframeML getDataframe() {
        return dataframe.get();
    }

    public void setDataframe(DataframeML dataframe) {
        this.dataframe.set(dataframe);
    }

}
