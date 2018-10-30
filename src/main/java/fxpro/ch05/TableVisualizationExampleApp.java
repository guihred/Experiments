package fxpro.ch05;

import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
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
import simplebuilder.SimpleRectangleBuilder;
import simplebuilder.SimpleTabPaneBuilder;
import simplebuilder.SimpleTableViewBuilder;
import simplebuilder.SimpleToggleGroupBuilder;
import utils.CommonsFX;
import utils.CrawlerTask;
import utils.HasLogging;

/**
 *
 * @author Note
 */
public class TableVisualizationExampleApp extends Application implements HasLogging {

	private static final String MENU_ITEM_B = "MenuItem B";
    private static final String MENU_ITEM_A = "MenuItem A";
    private Stage stage;

	@Override
	public void start(final Stage primaryStage) {
		stage = primaryStage;
		VBox top = new VBox(createMenus(), createToolBar());
		BorderPane borderPane = new BorderPane(createTabs());
		borderPane.setTop(top);
		Scene scene = new Scene(borderPane);
		// .stylesheets(StarterAppMain.class.getResource("starterApp.css")
		// .toExternalForm())
		stage.setScene(scene);
		stage.setWidth(800);
		stage.setHeight(600);
        stage.setTitle("Table Visualization Example");
		stage.show();
	}

	private Node createAccordionTitledDemoNode() {
		TitledPane firstPane = new TitledPane("TitledPane A", new TextArea("TitledPane A content"));
		final TitledPane secondPane = new TitledPane("TitledPane B", new TextArea("TitledPane B content"));
		final TitledPane thirdPane = new TitledPane("TitledPane C", new TextArea("TitledPane C content"));
		Accordion accordion = new Accordion();
		accordion.getPanes().addAll(firstPane, secondPane, thirdPane);
		accordion.setExpandedPane(firstPane);
		return accordion;
	}

    private Popup createAlertPopup(String text) {

		Popup alertPopup = new Popup();

		Button okButton = new Button("OK");
		okButton.setOnAction(e -> alertPopup.hide());
		final Label htmlLabel = new Label(text);
		htmlLabel.setWrapText(true);
		htmlLabel.setMaxWidth(280);
		htmlLabel.setMaxHeight(140);
		final BorderPane borderPane = new BorderPane(htmlLabel, null, null, okButton, null);

        alertPopup.getContent()
                .add(new StackPane(new SimpleRectangleBuilder().width(300).height(200).fill(Color.LIGHTBLUE)
				.arcWidth(20).arcHeight(20).stroke(Color.GRAY).strokeWidth(2).build(), borderPane));

		BorderPane.setAlignment(okButton, Pos.CENTER);
		BorderPane.setMargin(okButton, new Insets(10, 0, 10, 0));
		return alertPopup;
	}

    private Node createHtmlEditorDemoNode() {
		final HTMLEditor htmlEditor = new HTMLEditor();
		htmlEditor.setHtmlText("<p>Replace this text</p>");

		Button viewHtmlButton = new Button("View HTML");
        viewHtmlButton.setOnAction(e -> {
			Popup alertPopup1 = createAlertPopup(htmlEditor.getHtmlText());
			alertPopup1.show(stage, (stage.getWidth() - alertPopup1.getWidth()) / 2 + stage.getX(),
					(stage.getHeight() - alertPopup1.getHeight()) / 2 + stage.getY());
		});

        final BorderPane htmlEditorDemo = new BorderPane(htmlEditor, null, null, viewHtmlButton, null);
		BorderPane.setAlignment(viewHtmlButton, Pos.CENTER);
		BorderPane.setMargin(viewHtmlButton, new Insets(10, 0, 10, 0));
		return htmlEditorDemo;
	}

    private MenuBar createMenus() {

        final MenuItem newMenuItem = new MenuItem("New...",
                new ImageView("https://cdn0.iconfinder.com/data/icons/16x16-free-toolbar-icons/16/2.png"));
		newMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newMenuItem.setOnAction(e -> getLogger().info("{} occurred on MenuItem New", e.getEventType()));

        final Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(newMenuItem, new MenuItem("Save"));
		MenuBar menuBar = new MenuBar();

		Menu editMenu = new Menu("Edit");
		editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"));
		menuBar.getMenus().addAll(fileMenu, editMenu);
		return menuBar;
	}

    @SuppressWarnings("unchecked")
    private Node createScrollMiscDemoNode() {
		ChoiceBox<String> choiceBox = new ChoiceBox<>(TableVisualizationModel.CHOICE_BOX_ITEMS);
		choiceBox.getSelectionModel().selectFirst();
        choiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> getLogger().info("{} chosen in ChoiceBox", newValue));
		final TextField textField = new TextField();
		textField.setPromptText("Enter user name");
		textField.setPrefColumnCount(16);
        textField.textProperty()
                .addListener((ov, oldValue, newValue) -> getLogger().info("TextField text is: {}" , textField.getText()));
		final PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter password");
		passwordField.setPrefColumnCount(16);
		passwordField.focusedProperty().addListener((ov, oldValue, newValue) -> {
			if (!passwordField.isFocused()) {
                getLogger().info("PasswordField text is: {}", passwordField.getText());
			}
		});
		final TextArea textArea = new TextArea();
		textArea.setPrefColumnCount(12);
		textArea.setPrefRowCount(4);
		textArea.focusedProperty().addListener((ov, oldValue, newValue) -> {
			if (!textArea.isFocused()) {
                getLogger().info("TextArea text is: {}", textArea.getText());
			}
		});
		Slider slider = new Slider();
		slider.setPrefWidth(200);
		slider.setMin(-1);
		slider.setMax(TableVisualizationModel.MAX_RPM);
		slider.valueProperty().bindBidirectional(TableVisualizationModel.RPM);
		ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setPrefWidth(200);
		progressIndicator.progressProperty().bind(TableVisualizationModel.RPM.divide(TableVisualizationModel.MAX_RPM));
		ProgressBar progressBar = new ProgressBar();
		progressBar.setPrefWidth(200);
		progressBar.progressProperty().bind(TableVisualizationModel.KPH.divide(TableVisualizationModel.MAX_KPH));
		ScrollBar scrollBar = new ScrollBar();
		scrollBar.setPrefWidth(200);
		scrollBar.setMin(-1);
		scrollBar.setMax(TableVisualizationModel.MAX_KPH);
		scrollBar.valueProperty().bindBidirectional(TableVisualizationModel.KPH);
		CheckBox checkBox = new CheckBox("CheckBox");
        checkBox.setOnAction(e -> {
            getLogger().info("{} occurred on CheckBox", e.getEventType());
            logSelectedProperty(checkBox.selectedProperty().getValue());
		});
		final Hyperlink hyperlink = new Hyperlink("Hyperlink");
        hyperlink.setOnAction(e -> getLogger().info("{} occurred on Hyperlink", e.getEventType()));
		final Button button = new Button("Button");
        button.setOnAction(e -> getLogger().info("{} occurred on Button", e.getEventType()));
		final MenuItem menItemA = new MenuItem(MENU_ITEM_A);
        menItemA.setOnAction(e -> getLogger().info("{} occurred on Menu Item A ", e.getEventType()));
		final MenuButton menuButton = new MenuButton("MenuButton");
		menuButton.getItems().addAll(menItemA, new MenuItem(MENU_ITEM_B));
        final ToggleGroup radioToggleGroup = new SimpleToggleGroupBuilder()
                .addRadioToggle("RadioButton1")
                .addRadioToggle("RadioButton2")
                .select(0)
                .onChange((ov, oldValue, newValue) -> {
                    Labeled rb = (Labeled) newValue;
                    if (rb != null) {
                        getLogger().info("{} selected", rb.getText());
                    }
                }).build();
		final MenuItem menuItem = new MenuItem(MENU_ITEM_A);
        menuItem.setOnAction(e -> getLogger().info("{} occurred on Menu Item A", e.getEventType()));
		final SplitMenuButton splitMenu = new SplitMenuButton(menuItem, new MenuItem(MENU_ITEM_B));
		splitMenu.setText("SplitMenuButton");
        splitMenu.setOnAction(e -> getLogger().info("{} occurred on SplitMenuButton", e.getEventType()));
        HBox hBox = new HBox(10, ((List<? extends Node>) radioToggleGroup.getToggles()).toArray(new Node[0]));
        VBox variousControls = new VBox(20, button, checkBox,
                hBox, hyperlink,
                choiceBox, menuButton, splitMenu, textField, passwordField,
                new HBox(10, new Label("TextArea:"), textArea), progressIndicator, slider, progressBar, scrollBar);
		variousControls.setPadding(new Insets(10, 10, 10, 10));
		final MenuItem menuItemA = new MenuItem(MENU_ITEM_A);
        menuItemA.setOnAction(e -> getLogger().info("{} occurred on Menu Item A", e.getEventType()));
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

    private Node createSplitTreeListDemoNode() {
		final TreeItem<String> animalItem = new TreeItem<>("Animal");
		animalItem.getChildren()
				.addAll(Arrays.asList(new TreeItem<>("Lion"), new TreeItem<>("Tiger"), new TreeItem<>("Bear")));
		final TreeItem<String> mineralItem = new TreeItem<>("Mineral");
		mineralItem.getChildren()
				.addAll(Arrays.asList(new TreeItem<>("Copper"), new TreeItem<>("Diamond"), new TreeItem<>("Quartz")));
		final TreeItem<String> vegetableItem = new TreeItem<>("Vegetable");
		vegetableItem.getChildren().addAll(
				Arrays.asList(new TreeItem<>("Arugula"), new TreeItem<>("Broccoli"), new TreeItem<>("Cabbage")));

		final TreeItem<String> rootItem = new TreeItem<>("Root");
		rootItem.getChildren().addAll(Arrays.asList(animalItem, mineralItem, vegetableItem));

		TreeView<String> treeView = new TreeView<>(rootItem);
		treeView.setMinWidth(150);
		treeView.setShowRoot(false);
		treeView.setEditable(false);

		ListView<String> listView = new ListView<>(TableVisualizationModel.LIST_VIEW_ITEMS);
		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(treeView, listView);
		treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			TreeItem<String> treeItem = newValue;
			if (newValue != null && treeItem.isLeaf()) {
				TableVisualizationModel.LIST_VIEW_ITEMS.clear();
				for (int i = 1; i <= 10000; i++) {
					TableVisualizationModel.LIST_VIEW_ITEMS.add(treeItem.getValue() + " " + i);
				}
			}
		});
		return splitPane;
	}

    private Node createTableDemoNode() {
        return new SimpleTableViewBuilder<Person>()
                .items(TableVisualizationModel.getTeamMembers())
                .addColumn("First Name", "firstName")
                .addColumn("Last Name", "lastName")
                .addColumn("Phone Number", "phone")
                .onSelect((old,newValue)->getLogger().info("{} chosen in TableView", newValue))
                .build();
	}

    private TabPane createTabs() {
        final WebView webView = new WebView();
        return new SimpleTabPaneBuilder().addTab("TableView", createTableDemoNode())
                .addTab("Accordion/TitledPane", createAccordionTitledDemoNode())
                .addTab("SplitPane/TreeView/ListView", createSplitTreeListDemoNode())
                .addTab("ScrollPane/Miscellaneous", createScrollMiscDemoNode())
                .addTab("HTMLEditor", createHtmlEditorDemoNode())
                .addTab("WebView", webView, (tab, evt) -> {
                    String randomWebSite = TableVisualizationModel.getRandomWebSite();
                    if (tab.isSelected()) {
                        webView.getEngine().load(randomWebSite);
                        getLogger().info("WebView tab is selected, loading: {}", randomWebSite);
                    }
                })
                .allClosable(false)
                .build();
	}

    @SuppressWarnings("unchecked")
    private ToolBar createToolBar() {
        final Button newButton = CommonsFX.newButton(
                new ImageView("https://cdn0.iconfinder.com/data/icons/16x16-free-toolbar-icons/16/2.png"), "newButton",
                e -> getLogger().info("New toolbar button clicked"));
        newButton.setTooltip(new Tooltip("New Document... Ctrl+N"));

        final Button editButton = CommonsFX.newButton(new Circle(8, Color.GREEN), "editButton", null);

        final Button deleteButton = CommonsFX.newButton(new Circle(8, Color.BLUE), "deleteButton", null);

        final ToggleButton boldButton = newToggleButton("boldButton", Color.MAROON);
        final ToggleButton italicButton = newToggleButton("italicButton", Color.YELLOW);

        final ToggleGroup alignToggleGroup = new SimpleToggleGroupBuilder()
                .addToggle(new Circle(8, Color.PURPLE), "leftAlignButton")
                .addToggle(new Circle(8, Color.ORANGE), "centerAlignButton")
                .addToggle(new Circle(8, Color.CYAN), "rightAlignButton")
                .select(0)
                .onChange((ov, oldValue, newValue) -> {
                    Node tb = (Node) newValue;
                    if (tb != null) {
                        getLogger().info("{} selected", tb.getId());
                    }
                }).build();

        ToolBar toolBar = new ToolBar(newButton, editButton, deleteButton, boldButton, italicButton);
        toolBar.getItems().addAll((List<? extends Node>) alignToggleGroup.getToggles());

        return toolBar;
	}

    private void logSelectedProperty(Boolean value) {
        getLogger().info(", and selectedProperty is: {}", value);
    }

    private ToggleButton newToggleButton(String id, Color yellow) {
        return CommonsFX.newToggleButton(id, new Circle(8, yellow), e -> {
            Toggle tb = (Toggle) e.getTarget();
            getLogger().info("{} occurred on ToggleButton {}", e.getEventType(), tb);
            logSelectedProperty(tb.selectedProperty().getValue());
		});
    }

    public static void main(String[] args) {
        CrawlerTask.insertProxyConfig();
        launch(args);
	}
}
