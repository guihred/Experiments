package fxpro.ch05;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import utils.ex.HasLogging;

public abstract class TableVisualizationVar extends Application implements HasLogging {
    protected static final String MENU_ITEM_B = "MenuItem B";
    protected static final String MENU_ITEM_A = "MenuItem A";
    @FXML
    protected Tab tab1;
    @FXML
    protected TreeView<String> treeView24;
    @FXML
    protected WebView webView8;
    @FXML
    protected ProgressBar progressBar43;
    @FXML
    protected ScrollBar scrollBar44;
    @FXML
    protected ToggleGroup toggleGroup46;
    @FXML
    protected ToggleGroup toggleGroup14;
    @FXML
    protected PasswordField passwordField40;
    @FXML
    protected CheckBox checkBox34;
    @FXML
    protected ProgressIndicator progressIndicator41;
    @FXML
    protected ChoiceBox<String> choiceBox36;
    @FXML
    protected TableView<SimplePerson> tableView4;
    @FXML
    protected TextField textField39;
    @FXML
    protected HTMLEditor hTMLEditor27;
    @FXML
    protected TextArea textArea52;
    @FXML
    protected Slider slider42;
    @FXML
    protected ListView<String> listView25;

}
