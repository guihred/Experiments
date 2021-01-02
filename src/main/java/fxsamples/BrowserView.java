package fxsamples;

import java.util.Arrays;
import java.util.Objects;
import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebHistory.Entry;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import simplebuilder.SimpleDialogBuilder;
import utils.ClassReflectionUtils;
import utils.CommonsFX;
import utils.ExtractUtils;
import utils.ResourceFXUtils;
import utils.ex.RunnableEx;

public class BrowserView extends Pane {
    private static final int PREF_WIDTH = 900;
    private static final int PREF_HEIGHT = 600;
    private static final String[] captions = new String[] { "Products", "Blogs", "Documentation", "Partners", "Help" };
    private static final String[] urls = new String[] { "http://www.oracle.com/products/index.html",
            "http://blogs.oracle.com/", "http://docs.oracle.com/javase/index.html",
            "http://www.oracle.com/partners/index.html", ResourceFXUtils.toExternalForm("About.html") };
    @FXML
    private HBox toolBar;
    @FXML
    private WebView browser;
    private final WebEngine webEngine;
    @FXML
    private Button toggleHelpTopics;
    private final WebView smallView = new WebView();
    private boolean needDocumentationButton;
    private final JavaApp javaApp = new JavaApp();

    @FXML
    private ContextMenu cm;
    @FXML
    private ComboBox<String> comboBox0;

    private WebHistory history;

    public BrowserView() {
        ExtractUtils.insertProxyConfig();
        CommonsFX.loadRoot("BrowserView.fxml", this);
        webEngine = browser.getEngine();
        webEngine.setCreatePopupHandler(config -> {
            if (!toolBar.getChildren().contains(smallView)) {
                toolBar.getChildren().add(smallView);
            }
            return smallView.getEngine();
        });
        history = webEngine.getHistory();
        history.getEntries().addListener((Change<? extends Entry> c) -> {
            c.next();
            c.getRemoved().stream().forEach(e -> comboBox0.getItems().remove(e.getUrl()));
            c.getAddedSubList().stream().forEach(e -> comboBox0.getItems().add(e.getUrl()));
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
        toolBar.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                cm.show(toolBar, e.getScreenX(), e.getScreenY());
            }
        });
        webEngine.load("http://www.oracle.com/products/index.html");
    }

    public JavaApp getJavaApp() {
        return javaApp;
    }

    public void onActionComboBox0() {
        int offset = comboBox0.getSelectionModel().getSelectedIndex() - history.getCurrentIndex() - 1;
        history.go(offset);
    }

    public void onActionHyperlink1(ActionEvent e) {
        String fieldValue = Objects.toString(ClassReflectionUtils.invoke(e.getSource(), "getText"), "");
        needDocumentationButton = "Help".equals(fieldValue);
        int indexOf = Arrays.asList(captions).indexOf(fieldValue);
        webEngine.load(urls[indexOf]);
    }

    public void onActionMenuItem12() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(getScene().getWindow())) {
            webEngine.print(job);
            job.endJob();
        }
    }

    public void onActionToggleHelp() {
        RunnableEx.run(() -> webEngine.executeScript("toggle_visibility('help_topics')"));
    }

    @Override
    protected double computePrefHeight(double width) {
        return PREF_HEIGHT;
    }

    @Override
    protected double computePrefWidth(double height) {
        return PREF_WIDTH;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser, 0, 0, w, h - tbHeight, 0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0, h - tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
    }

    public class JavaApp {
        public void exit() {
            SimpleDialogBuilder.closeStage(browser);
        }
    }
}