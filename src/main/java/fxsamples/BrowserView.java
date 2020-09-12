package fxsamples;

import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Worker.State;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;

public class BrowserView extends Region {
    private static final String[] imageFiles =
            new String[] { "product.jpg", "blog.png", "documentation.png", "partners.png", "help.png" };
    private static final String[] captions = new String[] { "Products", "Blogs", "Documentation", "Partners", "Help" };
    private static final String[] urls = new String[] { "http://www.oracle.com/products/index.html",
            "http://blogs.oracle.com/", "http://docs.oracle.com/javase/index.html",
            "http://www.oracle.com/partners/index.html", ResourceFXUtils.toExternalForm("About.html") };
    private final HBox toolBar;
    private final Hyperlink[] hpls = new Hyperlink[captions.length];
    private final Image[] images = new Image[imageFiles.length];
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();
    private final Button toggleHelpTopics = new Button("Toggle Help Topics");
    private final WebView smallView = new WebView();
    private final ComboBox<String> comboBox = new ComboBox<>();
    private boolean needDocumentationButton;
    private final Stage stage;
    private final JavaApp javaApp = new JavaApp();

    public BrowserView(Stage stage) {
        this.stage = stage;
        getStyleClass().add("browser");
        for (int i = 0; i < captions.length; i++) {
            Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
            Image image = images[i] = new Image(ResourceFXUtils.toStream(imageFiles[i]));
            String url = urls[i];
            hpl.setGraphic(new ImageView(image));
            hpl.setOnAction(e -> {
                needDocumentationButton = "Help".equals(hpl.getText());
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
        toggleHelpTopics
                .setOnAction(t -> RunnableEx.run(() -> webEngine.executeScript("toggle_visibility('help_topics')")));
        smallView.setPrefSize(120, 80);
        webEngine.setCreatePopupHandler(config -> {
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
        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            toolBar.getChildren().remove(toggleHelpTopics);
            if (newState == State.SUCCEEDED) {
                JSObject win = (JSObject) webEngine.executeScript("window");
                win.setMember("app", getJavaApp());
                if (needDocumentationButton) {
                    toolBar.getChildren().add(toggleHelpTopics);
                }
            }
        });
        final ContextMenu cm = new ContextMenu();
        MenuItem cmItem1 = new MenuItem("Print");
        cm.getItems().add(cmItem1);
        toolBar.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                cm.show(toolBar, e.getScreenX(), e.getScreenY());
            }
        });
        cmItem1.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(stage)) {
                webEngine.print(job);
                job.endJob();
            }
        });
        webEngine.load("http://www.oracle.com/products/index.html");
        getChildren().add(toolBar);
        getChildren().add(browser);
    }

    public JavaApp getJavaApp() {
        return javaApp;
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
            getStage().close();
        }
    }
}