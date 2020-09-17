package ethical.hacker;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import simplebuilder.SimpleTreeViewBuilder;
import utils.CommonsFX;
import utils.ex.RunnableEx;

public class WebScannerApplication extends Application {
    private static final String TITLE = "title";
    @FXML
    private TextField urlField;
    @FXML
    private TextField cookieField;
    @FXML
    private SplitPane splitPane0;
    @FXML
    private ScrollPane scrollPane1;

    @FXML
    private ImageView image;
    @FXML
    private TreeView<SitePage> treeView3;
    private WhoIsScanner whoIsScanner = new WhoIsScanner();

    private ObservableSet<String> links = FXCollections.observableSet();

    public void initialize() {
        SimpleTreeViewBuilder.of(treeView3).onSelect(treeItem -> {
            if (treeItem == null) {
                return;
            }
            onSelectItem(treeItem, treeItem.getValue());
        });
    }

    public void onKeyReleasedUrlField(KeyEvent e) {
        RunnableEx.run(() -> {
            if (e.getCode() == KeyCode.ENTER) {
                addCookies();
                String text = urlField.getText();
                URL url = new URL(text);
                String sha256Hash = HashVerifier.getSha256Hash(url.getPath());
                Document evaluateURL = whoIsScanner.name(url.getHost() + sha256Hash).waitStr("Loading").subFolder(TITLE)
                        .evaluateURL(url.toString());
                List<String> urlLinks = WebsiteScanner.getLinks(url.toString(), evaluateURL);
                links.addAll(urlLinks);
                SitePage sitePage = new SitePage(url.toString());
                sitePage.setLinks(urlLinks);
                sitePage.setTitle(evaluateURL.select(TITLE).text());
                sitePage.setPrint(whoIsScanner.getPrint());
                treeView3.setRoot(toTreeItem(sitePage));
            }
        });

    }

    @Override
    public void start(Stage primaryStage) {
        CommonsFX.loadFXML("Web Scanner ", "WebScannerApplication.fxml", this, primaryStage);
    }

    private void addCookies() {
        String[] split = cookieField.getText().split("; ");
        for (String string3 : split) {
            String[] split2 = string3.split("=");
            whoIsScanner.cookie(split2[0], split2[1]);
        }
    }

    private void addItemChildren(SitePage sitePage, TreeItem<SitePage> treeItem) {
        treeItem.getChildren()
                .addAll(sitePage.getLinks().stream()
                        .filter(t -> !links.contains(t))
                        .map(l -> new TreeItem<>(new SitePage(l))).collect(Collectors.toList()));
    }

    private void onSelectItem(TreeItem<SitePage> treeItem, SitePage value) throws IOException {
        if (StringUtils.isBlank(value.getTitle())) {
            String text = value.getUrl();
            URL url = new URL(text);
            String sha256Hash = HashVerifier.getSha256Hash(url.getPath());
            Document evaluateURL = whoIsScanner.name(url.getHost() + sha256Hash).waitStr("Loading").subFolder(TITLE)
                    .evaluateURL(url.toString());
            List<String> urlLinks = WebsiteScanner.getLinks(url.toString(), evaluateURL);
            links.addAll(urlLinks);
            SitePage sitePage = value;
            sitePage.setPrint(whoIsScanner.getPrint());
            sitePage.setLinks(urlLinks);
            sitePage.setTitle(evaluateURL.select(TITLE).text());
            addItemChildren(sitePage, treeItem);
        }
        image.setImage(new Image(value.getPrint().toURI().toURL().toExternalForm()));
    }

    private TreeItem<SitePage> toTreeItem(SitePage sitePage) {
        TreeItem<SitePage> treeItem = new TreeItem<>(sitePage);
        addItemChildren(sitePage, treeItem);
        return treeItem;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
