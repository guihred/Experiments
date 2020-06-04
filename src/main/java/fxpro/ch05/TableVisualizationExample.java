package fxpro.ch05;

import static fxpro.ch05.TableVisualizationModel.*;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import simplebuilder.SimpleButtonBuilder;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.RunnableEx;

public class TableVisualizationExample extends TableVisualizationVar {

    protected ContextMenu contextMenu;

    public void initialize() {
        ExtractUtils.insertProxyConfig();
        listView25.setItems(LIST_VIEW_ITEMS);
        tableView4.setItems(getTeamMembers());
        tableView4.getSelectionModel().selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> getLogger().info("{} chosen in TableView", newValue));
        treeView24.getSelectionModel().selectedItemProperty().addListener((ob, old, value) -> {
            if (value != null && value.isLeaf()) {
                updateList(value.getValue());
            }
        });
        toggleGroup14.selectedToggleProperty().addListener((observable, oldValue, newValue) -> RunnableEx
                .runIf((Node) newValue, tb -> getLogger().info("{} selected", tb.getId())));
        choiceBox36.getSelectionModel().selectedItemProperty()
            .addListener((ob, oldValue, newValue) -> getLogger().info("{} chosen in ChoiceBox", newValue));
        textField39.textProperty()
            .addListener((ov, oldValue, newValue) -> getLogger().info("TextField text is: {}", textField39.getText()));
        passwordField40.focusedProperty().addListener((ov, oldValue, newValue) -> {
            if (!passwordField40.isFocused()) {
                getLogger().info("PasswordField text is: {}", passwordField40.getText());
            }
        });
        textArea52.focusedProperty().addListener((ov, oldValue, newValue) -> {
            if (!textArea52.isFocused()) {
                getLogger().info("TextArea text is: {}", textArea52.getText());
            }
        });
        slider42.valueProperty().bindBidirectional(RPM);
        progressIndicator41.progressProperty().bind(RPM.divide(MAX_RPM));
        progressBar43.progressProperty().bind(KPH.divide(MAX_KPH));
        scrollBar44.valueProperty().bindBidirectional(KPH);
        toggleGroup46.selectedToggleProperty().addListener((ov, oldValue, newValue) -> RunnableEx
                .runIf((Labeled) newValue, rb -> getLogger().info("{} selected", rb.getText())));
        MenuItem menuItemA = new MenuItem(MENU_ITEM_A);
        menuItemA.setOnAction(e -> getLogger().info("{} occurred on Menu Item A", e.getEventType()));
        contextMenu = new ContextMenu(menuItemA, new MenuItem(MENU_ITEM_B));
    }

    public void onActionBoldButton(ActionEvent e) {
        Toggle tb = (Toggle) e.getTarget();
        getLogger().info("{} occurred on ToggleButton {}", e.getEventType(), tb);
        logSelectedProperty(tb.selectedProperty().getValue());
    }

    public void onActionButton(ActionEvent e) {
        getLogger().info("{} occurred on Button", e.getEventType());
    }

    public void onActionCheckBox34(ActionEvent e) {
        getLogger().info("{} occurred on CheckBox", e.getEventType());
        logSelectedProperty(checkBox34.selectedProperty().getValue());
    }

    public void onActionHyperlink35(ActionEvent e) {
        getLogger().info("{} occurred on Hyperlink", e.getEventType());
    }

    public void onActionMenuItem49(ActionEvent e) {
        getLogger().info("{} occurred on Menu Item A ", e.getEventType());
    }

    public void onActionMenuItem50(ActionEvent e) {
        getLogger().info("{} occurred on Menu Item A", e.getEventType());
    }

    public void onActionNew28(ActionEvent e) {
        getLogger().info("{} occurred on MenuItem New", e.getEventType());
    }

    public void onActionNewButton() {
        getLogger().info("New toolbar button clicked");
    }

    public void onActionSplitMenuButton38(ActionEvent e) {
        getLogger().info("{} occurred on SplitMenuButton", e.getEventType());
    }

    public void onActionViewHTML() {
        TableVisualizationExample.createAlertPopup(hTMLEditor27.getHtmlText())
            .show(hTMLEditor27.getScene().getWindow());
    }

    public void onMousePressedScrollPane7(MouseEvent me) {
        if (me.isSecondaryButtonDown()) {

            contextMenu.show(hTMLEditor27.getScene().getWindow(), me.getScreenX(), me.getScreenY());
        }
    }

    public void onSelectionChangedTab1() {
        if (tab1.isSelected()) {
            String randomWebSite = getRandomWebSite();
            webView8.getEngine().load(randomWebSite);
            getLogger().info("WebView tab is selected, loading: {}", randomWebSite);
        }
    }

    @Override
	public void start(Stage primaryStage) {
        final int width = 800;
        CommonsFX.loadFXML("Table Visualization Example", "TableVisualizationExampleApp.fxml", this, primaryStage,
            width, width);
        CommonsFX.addCSS(primaryStage.getScene(), "starterApp.css");
    }

    private void logSelectedProperty(Boolean value) {
        getLogger().info(", and selectedProperty is: {}", value);
    }

    public static Popup createAlertPopup(String text) {

        Popup alertPopup = new Popup();

        Button okButton = SimpleButtonBuilder.newButton("OK", e -> alertPopup.hide());
        Label htmlLabel = new Label(text);
        htmlLabel.setWrapText(true);
        final int MAX_WIDTH = 280;
        htmlLabel.setMaxWidth(MAX_WIDTH);
        final int MAX_HEIGHT = 140;
        htmlLabel.setMaxHeight(MAX_HEIGHT);
        final BorderPane borderPane = new BorderPane(htmlLabel, null, null, okButton, null);

        StackPane pane = new StackPane(borderPane);
        alertPopup.getContent().add(pane);
        pane.getStyleClass().add("cool-popup");
        BorderPane.setAlignment(okButton, Pos.CENTER);
        BorderPane.setMargin(okButton, new Insets(10, 0, 10, 0));
        return alertPopup;
    }

    public static void main(String[] args) {

        launch(args);
    }
}
