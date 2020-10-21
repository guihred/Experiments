package election;

import java.time.LocalDate;
import java.util.Set;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import ml.graph.PieGraph;

public abstract class CandidatoAppVariables extends Application {
    @FXML
    protected Text text18;
    @FXML
    protected TableColumn<Candidato, String> fotoUrl;
    @FXML
    protected TableColumn<Candidato, Boolean> eleito;
    @FXML
    protected Slider slider20;
    @FXML
    protected TreeView<String> treeView0;
    @FXML
    protected TextField filter;
    @FXML
    protected TableColumn<Candidato, LocalDate> nascimento;
    @FXML
    protected TableView<Candidato> tableView2;
    @FXML
    protected TableColumn<Candidato, Cidade> cidade;
    @FXML
    protected PieGraph pieGraph;
    @FXML
    protected ComboBox<Integer> maxResultCombo;
    @FXML
    protected ComboBox<String> columnName;
    @FXML
    protected Pagination pagination;
    @FXML
    protected ObservableList<Candidato> candidates;
    @FXML
    protected ObservableMap<String, Set<String>> fieldMap;
    @FXML
    protected ObservableMap<String, CheckBox> portChecks;

}
