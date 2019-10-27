package extract;

import static utils.StringSigaUtils.codificar;

import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.SupplierEx;

public final class WikiImagesUtils {

    private static final Logger LOG = HasLogging.log();

    private WikiImagesUtils() {

    }

    public static ImageView convertToImage(String url) {
        if (url.startsWith("data:image/")) {
            BufferedImage image = decodeToImage(url);
            Image image2 = SwingFXUtils.toFXImage(image, null);
            ImageView imageView = new ImageView(image2);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("wiki");
            return imageView;
        }
        
        String host = url.startsWith("//") ? "https:" : "https://en.wikipedia.org";
        ImageView imageView = new ImageView(host + url);
        imageView.getStyleClass().add("wiki");
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public static void displayCountByExtension() {
        try (Stream<Path> find = Files.find(new File("").toPath(), 20, (a, b) -> !a.toFile().isDirectory())) {
            Map<String, Long> fileExtensionCount = find.collect(Collectors
                .groupingBy(e -> com.google.common.io.Files.getFileExtension(e.toString()), Collectors.counting()));
            fileExtensionCount.entrySet().stream()
                .sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed())
                .forEach(ex -> LOG.info("{}={}", ex.getKey(), ex.getValue()));
        } catch (Exception e) {
            LOG.error("", e);
        }
    }



    public static List<String> getImagens(String artista) {
        CrawlerTask.insertProxyConfig();
        LOG.info("SEARCHING FOR {}", artista);
        String encode = codificar(artista.replace(' ', '_'));
        String url = "https://en.wikipedia.org/wiki/" + encode;
        List<String> images = new ArrayList<>();
        CompletableFuture.supplyAsync(() -> readPage(url)).thenAccept(images::addAll);
        ForkJoinPool.commonPool().awaitQuiescence(90, TimeUnit.SECONDS);
        return images;
    }

    public static ObservableList<String> getImagensForked(String artista, ObservableList<String> images) {
        CrawlerTask.insertProxyConfig();
        LOG.info("SEARCHING FOR {}", artista);
        String encode = codificar(artista.replace(' ', '_'));
        String url = "https://en.wikipedia.org/wiki/" + encode;
        String url2 = "https://pt.wikipedia.org/wiki/" + encode;

        CompletableFuture.supplyAsync(() -> readPage(url)).thenAccept(images::addAll);
        CompletableFuture.supplyAsync(() -> readPage(url2)).thenAccept(images::addAll);
        ForkJoinPool.commonPool().awaitQuiescence(90, TimeUnit.SECONDS);
        return images;
    }

    private static BufferedImage decodeToImage(String imageString) {
        try {
            String replaceAll = imageString.replaceAll("data:image/gif;base64,", "");
            byte[] imageByte = Base64.getDecoder().decode(replaceAll);
            ImageReader next = ImageIO.getImageReadersByFormatName("gif").next();
            next.setInput(new ByteArrayImageInputStream(imageByte));
            return next.read(0);
        } catch (Exception e) {
            LOG.info("ERROR LOADING {}", imageString);
            LOG.info("ERROR", e);
            return null;
        }
    }

    private static Document getDocument(final String url) throws IOException {
        Connection connect = Jsoup.connect(url);

        if (!CrawlerTask.isNotProxied()) {
            connect.header("Proxy-Authorization", "Basic " + CrawlerTask.getEncodedAuthorization());
        }
        return connect
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:52.0) Gecko/20100101         Firefox/52.0").get();
    }

    private static List<String> readPage(String urlString) {
        return SupplierEx.remap(() -> {
            Document parse = getDocument(urlString);
            LOG.info("READING PAGE {}", urlString);
            Elements kun = parse.select("img");
            return kun.stream().map(e -> e.attr("src"))
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
        }, "ERROR Reading Page");
    }

}
