package ex.j8;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import utils.ResourceFXUtils;
import utils.RunnableEx;

public class WebViewSample extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Web View Sample");
        Scene scene = new Scene(new BrowserView(stage), 900, 600, Color.web("#666970"));
        stage.setScene(scene);
        scene.getStylesheets().add(ResourceFXUtils.toExternalForm("BrowserToolbar.css"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class BrowserView extends Region {
    private static final String[] imageFiles =
            new String[] { "product.jpg", "blog.png", "documentation.png", "partners.png", "help.png" };
    private static final String[] captions = new String[] { "Products", "Blogs", "Documentation", "Partners", "Help" };
    private static final String[] urls = new String[] { "http://www.oracle.com/products/index.html",
            "http://blogs.oracle.com/", "http://docs.oracle.com/javase/index.html",
            "http://www.oracle.com/partners/index.html", ResourceFXUtils.toExternalForm("About.html") };
    private final HBox toolBar;
    final ImageView selectedImage = new ImageView();
    final Hyperlink[] hpls = new Hyperlink[captions.length];
    final Image[] images = new Image[imageFiles.length];
    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();
    final Button toggleHelpTopics = new Button("Toggle Help Topics");
    final WebView smallView = new WebView();
    final ComboBox<String> comboBox = new ComboBox<>();
    private boolean needDocumentationButton = false;
    private final Stage stage;

    public BrowserView(final Stage stage) {
        this.stage = stage;
        getStyleClass().add("browser");
        for (int i = 0; i < captions.length; i++) {
            Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
            Image image = images[i] = new Image(ResourceFXUtils.toStream(imageFiles[i]));
            hpl.setGraphic(new ImageView(image));
            final String url = urls[i];
            final boolean addButton = hpl.getText().equals("Help");
            hpl.setOnAction((ActionEvent e) -> {
                needDocumentationButton = addButton;
                webEngine.load(url);
            });
        }
        comboBox.setPrefWidth(60);
        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().add(comboBox);
        toolBar.getChildren().addAll(hpls);
        toolBar.getChildren().add(createSpacer());
        toggleHelpTopics.setOnAction(
                (ActionEvent t) -> RunnableEx.run(() -> webEngine.executeScript("toggle_visibility('help_topics')")));
        smallView.setPrefSize(120, 80);
        webEngine.setCreatePopupHandler((PopupFeatures config) -> {
            smallView.setFontScale(0.8);
            if (!toolBar.getChildren().contains(smallView)) {
                toolBar.getChildren().add(smallView);
            }
            return smallView.getEngine();
        });
        final WebHistory history = webEngine.getHistory();
        history.getEntries().addListener((Change<? extends Entry> c) -> {
            c.next();
            c.getRemoved().stream().forEach(e -> comboBox.getItems().remove(e.getUrl()));
            c.getAddedSubList().stream().forEach(e -> comboBox.getItems().add(e.getUrl()));
        });
        comboBox.setOnAction(ev -> {
            int offset = comboBox.getSelectionModel().getSelectedIndex() - history.getCurrentIndex();
            history.go(offset);
        });
        webEngine.getLoadWorker().stateProperty()
                .addListener((ObservableValue<? extends State> ov, State oldState, State newState) -> {
                    toolBar.getChildren().remove(toggleHelpTopics);
                    if (newState == State.SUCCEEDED) {
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("app", new JavaApp());
                        if (needDocumentationButton) {
                            toolBar.getChildren().add(toggleHelpTopics);
                        }
                    }
                });
        final ContextMenu cm = new ContextMenu();
        MenuItem cmItem1 = new MenuItem("Print");
        cm.getItems().add(cmItem1);
        toolBar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent e) -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                cm.show(toolBar, e.getScreenX(), e.getScreenY());
            }
        });
        cmItem1.setOnAction((ActionEvent e) -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                boolean showPrintDialog = job.showPrintDialog(stage);
                if (showPrintDialog) {
                    System.out.println("WHAT??");
                    webEngine.print(job);
                    job.endJob();
                }
            }
        });
        webEngine.load("http://www.oracle.com/products/index.html");
        getChildren().add(toolBar);
        getChildren().add(browser);
    }

    public Stage getStage() {
        return stage;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 600;
    }

    @Override
    protected double computePrefWidth(double height) {
        return 900;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser, 0, 0, w, h - tbHeight, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0, h - tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    private static Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public class JavaApp {
        public void exit() {
            Platform.exit();
        }
    }
}