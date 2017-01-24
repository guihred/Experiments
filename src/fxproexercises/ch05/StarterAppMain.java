package fxproexercises.ch05;

import java.util.Arrays;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 *
 * @author Note
 */
public class StarterAppMain extends Application {

    StarterAppModel model = new StarterAppModel();
    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) {
        stage = primaryStage;
        Scene scene = new Scene(new BorderPane(createTabs(), new VBox(createMenus(), createToolBar()), null, null, null));
//                .stylesheets(StarterAppMain.class.getResource("starterApp.css")
        //                        .toExternalForm())
        stage.setScene(scene);
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setTitle("Starter App");
        stage.show();
    }

    MenuBar createMenus() {
        final MenuItem newMenuItem = new MenuItem("New...", new ImageView(
                new Image("https://cdn0.iconfinder.com/data/icons/16x16-free-toolbar-icons/16/2.png")));
        newMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newMenuItem.setOnAction((ActionEvent e) -> {
            System.out.println(e.getEventType()
                    + " occurred on MenuItem New");
        });
        final Menu fileMenu = new Menu("File", null, newMenuItem,
                new MenuItem("Save")
        );
        MenuBar menuBar = new MenuBar(
                fileMenu,
                new Menu("Edit", null, new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste")));
        return menuBar;
    }

    Node createHtmlEditorDemoNode() {
        final BorderPane htmlEditorDemo;
        final HTMLEditor htmlEditor = new HTMLEditor();
        htmlEditor.setHtmlText("<p>Replace this text</p>");

        Button viewHtmlButton = new Button("View HTML");
        viewHtmlButton.setOnAction((ActionEvent e) -> {
            Popup alertPopup1 = createAlertPopup(htmlEditor.getHtmlText());
            alertPopup1.show(stage, (stage.getWidth() - alertPopup1.getWidth()) / 2 + stage.getX(), (stage.getHeight() - alertPopup1.getHeight()) / 2 + stage.getY());
        });

        htmlEditorDemo = new BorderPane(htmlEditor, null, null, viewHtmlButton, null);
        BorderPane.setAlignment(viewHtmlButton, Pos.CENTER);
        BorderPane.setMargin(viewHtmlButton, new Insets(10, 0, 10, 0));
        return htmlEditorDemo;
    }

    Popup createAlertPopup(String text) {

        Popup alertPopup = new Popup();

        Button okButton = new Button("OK");
        okButton.setOnAction((ActionEvent e) -> {
            alertPopup.hide();
        });
        final Rectangle rectangle = new Rectangle(300, 200, Color.LIGHTBLUE);
        rectangle.setArcWidth(20);
        rectangle.setArcHeight(20);
        rectangle.setStroke(Color.GRAY);
        rectangle.setStrokeWidth(2);

        final Label htmlLabel = new Label(text);
        htmlLabel.setWrapText(true);
        htmlLabel.setMaxWidth(280);
        htmlLabel.setMaxHeight(140);
        final BorderPane borderPane = new BorderPane(htmlLabel, null, null, okButton, null);

        alertPopup.getContent().add(new StackPane(rectangle, borderPane));

        BorderPane.setAlignment(okButton, Pos.CENTER);
        BorderPane.setMargin(okButton, new Insets(10, 0, 10, 0));
        return alertPopup;
    }

    ToolBar createToolBar() {
        final Button newButton = new Button(null, new ImageView(new Image("https://cdn0.iconfinder.com/data/icons/16x16-free-toolbar-icons/16/2.png")));
        newButton.setId("newButton");
        newButton.setTooltip(new Tooltip("New Document... Ctrl+N"));
        newButton.setOnAction((ActionEvent e) -> {
            System.out.println("New toolbar button clicked");
        });

        final Button editButton = new Button(null, new Circle(8, Color.GREEN));
        editButton.setId("editButton");

        final Button deleteButton = new Button(null, new Circle(8, Color.BLUE));
        deleteButton.setId("deleteButton");

        final ToggleButton boldButton = new ToggleButton(null, new Circle(8, Color.MAROON));
        boldButton.setId("boldButton");
        boldButton.setOnAction((ActionEvent e) -> {
            ToggleButton tb = (ToggleButton) e.getTarget();
            System.out.print(e.getEventType() + " occurred on ToggleButton "
                    + tb.getId());
            System.out.print(", and selectedProperty is: ");
            System.out.println(tb.selectedProperty().getValue());
        });
        final ToggleButton italicButton = new ToggleButton(null, new Circle(8, Color.YELLOW));
        italicButton.setId("italicButton");
        italicButton.setOnAction((ActionEvent e) -> {
            ToggleButton tb = (ToggleButton) e.getTarget();
            System.out.print(e.getEventType() + " occurred on ToggleButton "
                    + tb.getId());
            System.out.print(", and selectedProperty is: ");
            System.out.println(tb.selectedProperty().getValue());
        });

        final ToggleGroup alignToggleGroup = new ToggleGroup();
        final ToggleButton leftAlignButton = new ToggleButton(null, new Circle(8, Color.PURPLE));
        leftAlignButton.setId("leftAlignButton");
        leftAlignButton.setToggleGroup(alignToggleGroup);

        final ToggleButton centerAlignButton = new ToggleButton(null, new Circle(8, Color.ORANGE));
        centerAlignButton.setId("centerAlignButton");
        centerAlignButton.setToggleGroup(alignToggleGroup);

        final ToggleButton rightAlignButton = new ToggleButton(null,
                new Circle(8, Color.CYAN)
        );
        rightAlignButton.setId("rightAlignButton");
        rightAlignButton.setToggleGroup(alignToggleGroup);
        alignToggleGroup.selectToggle(alignToggleGroup.getToggles().get(0));
        alignToggleGroup.selectedToggleProperty().addListener((ov, oldValue, newValue) -> {
            ToggleButton tb = (ToggleButton) alignToggleGroup.getSelectedToggle();
            if (tb != null) {
                System.out.println(tb.getId() + " selected");
            }
        });

        ToolBar toolBar = new ToolBar(newButton, editButton, deleteButton,
                new Separator(Orientation.VERTICAL), boldButton, italicButton,
                new Separator(Orientation.VERTICAL), leftAlignButton, centerAlignButton, rightAlignButton);
        return toolBar;
    }

    TabPane createTabs() {

        final Tab tableView = new Tab("TableView", createTableDemoNode());
        tableView.setClosable(false);

        final Tab accordionTitledPane = new Tab("Accordion/TitledPane", createAccordionTitledDemoNode());
        accordionTitledPane.setClosable(false);

        final Tab splitPaneTreeListView = new Tab("SplitPane/TreeView/ListView", createSplitTreeListDemoNode());
        splitPaneTreeListView.setClosable(false);

        final Tab scrollMisc = new Tab("ScrollPane/Miscellaneous", createScrollMiscDemoNode());
        scrollMisc.setClosable(false);

        final Tab htmlEditor = new Tab("HTMLEditor", createHtmlEditorDemoNode());
        htmlEditor.setClosable(false);

        final WebView webView = new WebView();
        Tab webViewTab = new Tab("WebView", webView);
        webViewTab.setClosable(false);
        webViewTab.setOnSelectionChanged((Event evt) -> {
            String randomWebSite = model.getRandomWebSite();
            if (webViewTab.isSelected()) {
                webView.getEngine().load(randomWebSite);
                System.out.println("WebView tab is selected, loading: "
                        + randomWebSite);
            }
        });

        TabPane tabPane = new TabPane(tableView, accordionTitledPane, splitPaneTreeListView, scrollMisc, htmlEditor, webViewTab);
        return tabPane;
    }

    Node createTableDemoNode() {
        final TableColumn<Person, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameColumn.setPrefWidth(180);

        final TableColumn<Person, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameColumn.setPrefWidth(180);

        final TableColumn<Person, String> phoneNumberColumn = new TableColumn<>("Phone Number");
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneNumberColumn.setPrefWidth(150);

        TableView<Person> table = new TableView<>(model.getTeamMembers());
        table.getColumns().addAll(Arrays.asList(firstNameColumn, lastNameColumn, phoneNumberColumn));
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    Person selectedPerson = newValue;
                    System.out.println(selectedPerson + " chosen in TableView");
                });
        return table;
    }

    Node createAccordionTitledDemoNode() {
        TitledPane firstPane = new TitledPane("TitledPane A", new TextArea("TitledPane A content"));
        final TitledPane secondPane = new TitledPane("TitledPane B", new TextArea("TitledPane B content"));
        final TitledPane thirdPane = new TitledPane("TitledPane C", new TextArea("TitledPane C content"));
        Accordion accordion = new Accordion(firstPane, secondPane, thirdPane);
        accordion.setExpandedPane(firstPane);
        return accordion;
    }

    Node createSplitTreeListDemoNode() {
        final TreeItem<String> animalItem = new TreeItem<>("Animal");
        animalItem.getChildren().addAll(Arrays.asList(
                new TreeItem<String>("Lion"),
                new TreeItem<String>("Tiger"),
                new TreeItem<String>("Bear")));
        final TreeItem<String> mineralItem = new TreeItem<>("Mineral");
        mineralItem.getChildren().addAll(Arrays.asList(
                new TreeItem<String>("Copper"),
                new TreeItem<String>("Diamond"),
                new TreeItem<String>("Quartz")));
        final TreeItem<String> vegetableItem = new TreeItem<>("Vegetable");
        vegetableItem.getChildren().addAll(Arrays.asList(
                new TreeItem<String>("Arugula"),
                new TreeItem<String>("Broccoli"),
                new TreeItem<String>("Cabbage")));

        final TreeItem<String> rootItem = new TreeItem<>("Root");
        rootItem.getChildren().addAll(Arrays.asList(animalItem, mineralItem, vegetableItem));

        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setMinWidth(150);
        treeView.setShowRoot(false);
        treeView.setEditable(false);

        ListView<String> listView = new ListView<>(model.listViewItems);
        SplitPane splitPane = new SplitPane(treeView, listView);
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    TreeItem<String> treeItem = newValue;
                    if (newValue != null && treeItem.isLeaf()) {
                        model.listViewItems.clear();
                        for (int i = 1; i <= 10000; i++) {
                            model.listViewItems.add(treeItem.getValue() + " " + i);
                        }
                    }
                });
        return splitPane;
    }

    Node createScrollMiscDemoNode() {

        ChoiceBox<String> choiceBox = new ChoiceBox<>(model.choiceBoxItems);
        choiceBox.getSelectionModel().selectFirst();
        choiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    System.out.println(newValue + " chosen in ChoiceBox");
                });

        final TextField textField = new TextField();
        textField.setPromptText("Enter user name");
        textField.setPrefColumnCount(16);
        textField.textProperty().addListener((ov, oldValue, newValue) -> {
            System.out.println("TextField text is: " + textField.getText());
        });

        final PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefColumnCount(16);
        passwordField.focusedProperty().addListener((ov, oldValue, newValue) -> {
            if (!passwordField.isFocused()) {
                System.out.println("PasswordField text is: "
                        + passwordField.getText());
            }
        });

        final TextArea textArea = new TextArea();
        textArea.setPrefColumnCount(12);
        textArea.setPrefRowCount(4);
        textArea.focusedProperty().addListener((ov, oldValue, newValue) -> {
            if (!textArea.isFocused()) {
                System.out.println("TextArea text is: " + textArea.getText());
            }
        });

        Slider slider = new Slider();
        slider.setPrefWidth(200);
        slider.setMin(-1);
        slider.setMax(model.maxRpm);
        slider.valueProperty().bindBidirectional(model.rpm);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefWidth(200);
        progressIndicator.progressProperty().bind(model.rpm.divide(model.maxRpm));

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);
        progressBar.progressProperty().bind(model.kph.divide(model.maxKph));

        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setPrefWidth(200);
        scrollBar.setMin(-1);
        scrollBar.setMax(model.maxKph);
        scrollBar.valueProperty().bindBidirectional(model.kph);

        CheckBox checkBox = new CheckBox("CheckBox");
        checkBox.setOnAction((ActionEvent e) -> {
            System.out.print(e.getEventType() + " occurred on CheckBox");
            System.out.print(", and selectedProperty is: ");
            System.out.println(checkBox.selectedProperty().getValue());
        });

        final Hyperlink hyperlink = new Hyperlink("Hyperlink");
        hyperlink.setOnAction((ActionEvent e) -> {
            System.out.println(e.getEventType() + " occurred on Hyperlink");
        });

        final Button button = new Button("Button");
        button.setOnAction((ActionEvent e) -> {
            System.out.println(e.getEventType() + " occurred on Button");
        });

        final MenuItem menItemA = new MenuItem("MenuItem A");
        menItemA.setOnAction((ActionEvent e) -> {
            System.out.println(e.getEventType()
                    + " occurred on Menu Item A");
        });

        final MenuButton menuButton = new MenuButton("MenuButton", null, menItemA,
                new MenuItem("MenuItem B")
        );

        final ToggleGroup radioToggleGroup = new ToggleGroup();
        final RadioButton radioButton1 = new RadioButton("RadioButton1");
        radioButton1.setToggleGroup(radioToggleGroup);
        final RadioButton radioButton2 = new RadioButton("RadioButton2");
        radioButton2.setToggleGroup(radioToggleGroup);
        radioToggleGroup.selectToggle(radioToggleGroup.getToggles().get(0));
        radioToggleGroup.selectedToggleProperty().addListener((ov, oldValue, newValue) -> {
            RadioButton rb = (RadioButton) radioToggleGroup.getSelectedToggle();
            if (rb != null) {
                System.out.println(rb.getText() + " selected");
            }
        });

        final MenuItem menuItem = new MenuItem("MenuItem A");
        menuItem.setOnAction((ActionEvent e) -> {
            System.out.println(e.getEventType()
                    + " occurred on Menu Item A");
        });
        final SplitMenuButton splitMenu = new SplitMenuButton(menuItem,
                new MenuItem("MenuItem B"));
        splitMenu.setText("SplitMenuButton");
        splitMenu.setOnAction((ActionEvent e) -> {
            System.out.println(e.getEventType()
                    + " occurred on SplitMenuButton");
        });
        VBox variousControls = new VBox(20, button,
                checkBox,
                new HBox(10, radioButton1, radioButton2), hyperlink,
                choiceBox, menuButton, splitMenu,
                textField,
                passwordField,
                new HBox(10, new Label("TextArea:"), textArea),
                progressIndicator,
                slider,
                progressBar,
                scrollBar
        );
        variousControls.setPadding(new Insets(10, 10, 10, 10));

        final MenuItem menuItemA = new MenuItem("MenuItem A");
        menuItemA.setOnAction((ActionEvent e) -> {
            System.out.println(e.getEventType()
                    + " occurred on Menu Item A");
        });

        ContextMenu contextMenu = new ContextMenu(menuItemA, new MenuItem("MenuItem B"));

        ScrollPane scrollPane = new ScrollPane(variousControls);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setOnMousePressed((MouseEvent me) -> {
            if (me.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(stage, me.getScreenX(), me.getScreenY());
            }
        });
        return scrollPane;
    }
}
