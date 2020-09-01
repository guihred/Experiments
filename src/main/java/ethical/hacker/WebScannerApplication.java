package ethical.hacker;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import simplebuilder.SimpleTreeViewBuilder;
import utils.RunnableEx;

public class WebScannerApplication extends Application {
    private static final String TITLE = "title";
    private WhoIsScanner whoIsScanner = new WhoIsScanner();

    @Override
    public void start(Stage primaryStage) {
        ImageView image = new ImageView();
        HBox.setHgrow(image, Priority.ALWAYS);
        Pane root = new VBox();
        TreeView<SitePage> treeView = new SimpleTreeViewBuilder<SitePage>().onSelect(treeItem -> {
            SitePage value = treeItem.getValue();
            if (StringUtils.isBlank(value.title)) {
                String text = value.url;
                URL url = new URL(text);
                String sha256Hash = HashVerifier.getSha256Hash(url.getPath());
                Document evaluateURL = whoIsScanner.name(url.getHost() + sha256Hash).waitStr("Loading").subFolder(TITLE)
                        .evaluateURL(url.toString());
                List<String> links = WebsiteScanner.getLinks(url.toString(), evaluateURL);
                SitePage sitePage = value;
                sitePage.print = whoIsScanner.getPrint();
                sitePage.links = links;
                sitePage.title = evaluateURL.select(TITLE).text();
                treeItem.getChildren().addAll(
                        sitePage.links.stream().map(l -> new TreeItem<>(new SitePage(l))).collect(Collectors.toList()));
            }
            image.setImage(new Image(value.print.toURI().toURL().toExternalForm()));
        }).build();

        TextField urlField = new TextField();
        urlField.setOnKeyReleased(e -> RunnableEx.run(() -> {
            if (e.getCode() == KeyCode.ENTER) {
                String text = urlField.getText();
                URL url = new URL(text);
                String sha256Hash = HashVerifier.getSha256Hash(url.getPath());
                Document evaluateURL = whoIsScanner.name(url.getHost() + sha256Hash).waitStr("Loading").subFolder(TITLE)
                        .evaluateURL(url.toString());
                List<String> links = WebsiteScanner.getLinks(url.toString(), evaluateURL);
                SitePage sitePage = new SitePage(url.toString());
                sitePage.links = links;
                sitePage.title = evaluateURL.select(TITLE).text();
                sitePage.print = whoIsScanner.getPrint();
                treeView.setRoot(toTreeItem(sitePage));
            }
        }));
        String cookies = "rdtrk=%7B%22id%22%3A%22f7f77868-5d52-4f51-a585-1c436b401ce9%22%7D; "
                + "darkmode=false; _gcl_au=1.1.470272904.1598049562; "
                + "__cfduid=dc00783bb0c6a7dc937faed557994bff31598261000; "
                + "caelum.login.token=10ca04b164aff2456b311b0e2180aa70f93adeb8fd01487f6150e381776b1e48; "
                + "alura.userId=456133; " + "gnarus.menuClosed=true; " + "JSESSIONID=9265D15CFA663B40708873B4FA2AA77D";

        String[] split = cookies.split("; ");
        for (String string3 : split) {
            String[] split2 = string3.split("=");
            whoIsScanner.cookie(split2[0], split2[1]);
        }

        root.getChildren().add(urlField);
        root.getChildren().add(treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);
        ScrollPane scrollPane = new ScrollPane(image);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        primaryStage.setTitle("Web Scanner");
        SplitPane root2 = new SplitPane(root, scrollPane);
        primaryStage.setScene(new Scene(root2));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static TreeItem<SitePage> toTreeItem(SitePage sitePage) {
        TreeItem<SitePage> treeItem = new TreeItem<>(sitePage);
        treeItem.getChildren()
                .addAll(sitePage.links.stream().map(l -> new TreeItem<>(new SitePage(l))).collect(Collectors.toList()));
        return treeItem;
    }

    static class SitePage {
        String title;
        final String url;
        List<String> links;
        File print;
        public SitePage(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return Stream.of(title, url).filter(StringUtils::isNotBlank).collect(Collectors.joining(" "));
        }
    }
}
