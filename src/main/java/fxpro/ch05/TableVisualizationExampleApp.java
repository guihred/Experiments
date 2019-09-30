package fxpro.ch05;

import static fxpro.ch05.TableVisualizationModel.*;

import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.slf4j.Logger;
import simplebuilder.*;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.ResourceFXUtils;

/**
 *
 * @author Note
 */
public class TableVisualizationExampleApp extends Application {

    private static final String MENU_ITEM_B = "MenuItem B";
    private static final String MENU_ITEM_A = "MenuItem A";
    private static final Logger LOG = HasLogging.log();

    private Stage stage;

    @Override
    public void start(final Stage primaryStage) {
        stage = primaryStage;
        VBox top = new VBox(createMenus(), createToolBar());
        BorderPane borderPane = new BorderPane(createTabs());
        borderPane.setTop(top);
        Scene scene = new Scene(borderPane);
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("starterApp.css"));
        stage.setScene(scene);
        final int WIDTH = 800;
        stage.setWidth(WIDTH);
        stage.setHeight(WIDTH);
        stage.setTitle("Table Visualization Example");
        stage.show();
    }

    private Node createHtmlEditorDemoNode() {
        final HTMLEditor htmlEditor = new HTMLEditor();
        htmlEditor.setHtmlText("<p>Replace this text</p>");

        Button viewHtmlButton = SimpleButtonBuilder.newButton("View HTML",
            e -> createAlertPopup(htmlEditor.getHtmlText()).show(stage));

        final BorderPane htmlEditorDemo = new BorderPane(htmlEditor, null, null, viewHtmlButton, null);
        BorderPane.setAlignment(viewHtmlButton, Pos.CENTER);
        BorderPane.setMargin(viewHtmlButton, new Insets(10, 0, 10, 0));
        return htmlEditorDemo;
    }

    private Node createScrollMiscDemoNode() {
        ChoiceBox<String> choiceBox = new ChoiceBox<>(CHOICE_BOX_ITEMS);
        choiceBox.getSelectionModel().selectFirst();
        choiceBox.getSelectionModel().selectedItemProperty()
            .addListener((ob, oldValue, newValue) -> LOG.info("{} chosen in ChoiceBox", newValue));
        final TextField textField = new TextField();
        textField.setPromptText("Enter user name");
        textField.setPrefColumnCount(16);
        textField.textProperty()
            .addListener((ov, oldValue, newValue) -> LOG.info("TextField text is: {}", textField.getText()));
        final PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefColumnCount(16);
        passwordField.focusedProperty().addListener((ov, oldValue, newValue) -> {
            if (!passwordField.isFocused()) {
                LOG.info("PasswordField text is: {}", passwordField.getText());
            }
        });
        final TextArea textArea = new TextArea();
        textArea.setPrefColumnCount(12);
        textArea.setPrefRowCount(4);
        textArea.focusedProperty().addListener((ov, oldValue, newValue) -> {
            if (!textArea.isFocused()) {
                LOG.info("TextArea text is: {}", textArea.getText());
            }
        });
        final int standardSize = 200;
        Slider slider = new SimpleSliderBuilder().prefWidth(standardSize).min(-1).max(MAX_RPM).bindBidirectional(RPM)
            .build();
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefWidth(standardSize);
        progressIndicator.progressProperty().bind(RPM.divide(MAX_RPM));
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(standardSize);
        progressBar.progressProperty().bind(KPH.divide(MAX_KPH));
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setPrefWidth(standardSize);
        scrollBar.setMin(-1);
        scrollBar.setMax(MAX_KPH);
        scrollBar.valueProperty().bindBidirectional(KPH);
        CheckBox checkBox = new CheckBox("CheckBox");
        checkBox.setOnAction(e -> {
            LOG.info("{} occurred on CheckBox", e.getEventType());
            logSelectedProperty(checkBox.selectedProperty().getValue());
        });
        final Hyperlink hyperlink = new Hyperlink("Hyperlink");
        hyperlink.setOnAction(e -> LOG.info("{} occurred on Hyperlink", e.getEventType()));
        final Button button = SimpleButtonBuilder.newButton("Button",
            e -> LOG.info("{} occurred on Button", e.getEventType()));
        final MenuItem menItemA = new MenuItem(MENU_ITEM_A);
        menItemA.setOnAction(e -> LOG.info("{} occurred on Menu Item A ", e.getEventType()));
        final MenuButton menuButton = new MenuButton("MenuButton");
        menuButton.getItems().addAll(menItemA, new MenuItem(MENU_ITEM_B));
        final SimpleToggleGroupBuilder radioToggleGroup = new SimpleToggleGroupBuilder().addRadioToggle("RadioButton1")
            .addRadioToggle("RadioButton2").select(0).onChange((ov, oldValue, newValue) -> {
                Labeled rb = (Labeled) newValue;
                if (rb != null) {
                    LOG.info("{} selected", rb.getText());
                }
            });
        final MenuItem menuItem = new MenuItem(MENU_ITEM_A);
        menuItem.setOnAction(e -> LOG.info("{} occurred on Menu Item A", e.getEventType()));
        final SplitMenuButton splitMenu = new SplitMenuButton(menuItem, new MenuItem(MENU_ITEM_B));
        splitMenu.setText("SplitMenuButton");
        splitMenu.setOnAction(e -> LOG.info("{} occurred on SplitMenuButton", e.getEventType()));
        VBox variousControls = new VBox(10, button, checkBox, new HBox(10, radioToggleGroup.getToggles()), hyperlink,
            choiceBox, menuButton, splitMenu, textField, passwordField, new HBox(10, new Label("TextArea:"), textArea),
            progressIndicator, slider, progressBar, scrollBar);
        variousControls.setPadding(new Insets(10, 10, 10, 10));
        final MenuItem menuItemA = new MenuItem(MENU_ITEM_A);
        menuItemA.setOnAction(e -> LOG.info("{} occurred on Menu Item A", e.getEventType()));
        ContextMenu contextMenu = new ContextMenu(menuItemA, new MenuItem(MENU_ITEM_B));
        ScrollPane scrollPane = new ScrollPane(variousControls);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setOnMousePressed(me -> {
            if (me.isSecondaryButtonDown()) {
                contextMenu.show(stage, me.getScreenX(), me.getScreenY());
            }
        });
        return scrollPane;
    }

    private TabPane createTabs() {
        final WebView webView = new WebView();
        return new SimpleTabPaneBuilder().addTab("TableView", createTableDemoNode())
            .addTab("Accordion/TitledPane", createAccordionTitledDemoNode())
            .addTab("SplitPane/TreeView/ListView", createSplitTreeListDemoNode())
            .addTab("ScrollPane/Miscellaneous", createScrollMiscDemoNode())
            .addTab("HTMLEditor", createHtmlEditorDemoNode()).addTab("WebView", webView, (tab, evt) -> {
                if (tab.isSelected()) {
                    String randomWebSite = getRandomWebSite();
                    webView.getEngine().load(randomWebSite);
                    LOG.info("WebView tab is selected, loading: {}", randomWebSite);
                }
            }).allClosable(false).build();
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
    }

    private static Node createAccordionTitledDemoNode() {
        TitledPane firstPane = new TitledPane("TitledPane A", new TextArea("TitledPane A content"));
        TitledPane secondPane = new TitledPane("TitledPane B", new TextArea("TitledPane B content"));
        TitledPane thirdPane = new TitledPane("TitledPane C", new TextArea("TitledPane C content"));
        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(firstPane, secondPane, thirdPane);
        accordion.setExpandedPane(firstPane);
        return accordion;
    }

    private static Popup createAlertPopup(String text) {

        Popup alertPopup = new Popup();

        Button okButton = SimpleButtonBuilder.newButton("OK", e -> alertPopup.hide());
        final Label htmlLabel = new Label(text);
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

    private static MenuBar createMenus() {
        return new SimpleMenuBarBuilder().addMenu("File")
            .addMenuItem("New...",
                new ImageView("https://cdn0.iconfinder.com/data/icons/16x16-free-toolbar-icons/16/2.png"), "Ctrl+N",
                e -> LOG.info("{} occurred on MenuItem New", e.getEventType()))
            .addMenuItem("Save").addMenu("Edit").addMenuItem("Cut").addMenuItem("Copy").addMenuItem("Paste").build();
    }

    private static Node createSplitTreeListDemoNode() {
        return new SplitPane(new SimpleTreeViewBuilder<String>().root("Root").addItem("Animal", "Lion", "Tiger", "Bear")
            .addItem("Vegetable", "Arugula", "Broccoli", "Cabbage").addItem("Mineral", "Copper", "Diamond", "Quartz")
            .editable(false).showRoot(false).onSelect(newValue -> {
                if (newValue != null && newValue.isLeaf()) {
                    updateList(newValue.getValue());
                }
            }).build(), new ListView<>(LIST_VIEW_ITEMS));
    }

    private static Node createTableDemoNode() {
        return new SimpleTableViewBuilder<SimplePerson>().items(getTeamMembers()).addColumn("First Name", "firstName")
            .addColumn("Last Name", "lastName").addColumn("Phone Number", "phone")
            .onSelect((old, newValue) -> LOG.info("{} chosen in TableView", newValue)).build();
    }

    private static ToolBar createToolBar() {
        final Button newButton = SimpleButtonBuilder.newButton(
            new ImageView("https://cdn0.iconfinder.com/data/icons/16x16-free-toolbar-icons/16/2.png"), "newButton",
            e -> LOG.info("New toolbar button clicked"));
        newButton.setTooltip(new Tooltip("New Document... Ctrl+N"));

        final Button editButton = SimpleButtonBuilder.newButton(new Circle(8, Color.GREEN), "editButton", null);

        final Button deleteButton = SimpleButtonBuilder.newButton(new Circle(8, Color.BLUE), "deleteButton", null);

        final ToggleButton boldButton = newToggleButton("boldButton", Color.MAROON);
        final ToggleButton italicButton = newToggleButton("italicButton", Color.YELLOW);

        List<Node> toggles = new SimpleToggleGroupBuilder().addToggle(new Circle(8, Color.PURPLE), "leftAlignButton")
            .addToggle(new Circle(8, Color.ORANGE), "centerAlignButton")
            .addToggle(new Circle(8, Color.CYAN), "rightAlignButton").select(0).onChange((ov, oldValue, newValue) -> {
                Node tb = (Node) newValue;
                if (tb != null) {
                    LOG.info("{} selected", tb.getId());
                }
            }).getTogglesAs(Node.class);

        ToolBar toolBar = new ToolBar(newButton, editButton, deleteButton, boldButton, italicButton);
        toolBar.getItems().addAll(toggles);

        return toolBar;
    }

    private static void logSelectedProperty(Boolean value) {
        LOG.info(", and selectedProperty is: {}", value);
    }

    private static ToggleButton newToggleButton(String id, Color yellow) {
        return CommonsFX.newToggleButton(id, new Circle(8, yellow), e -> {
            Toggle tb = (Toggle) e.getTarget();
            LOG.info("{} occurred on ToggleButton {}", e.getEventType(), tb);
            logSelectedProperty(tb.selectedProperty().getValue());
        });
    }
}
