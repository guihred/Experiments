package audio.mp3;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.exception.RuntimeIOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import sun.misc.BASE64Decoder;
import utils.CrawlerTask;
import utils.HasLogging;

public final class GoogleImagesUtils {

    static final Logger LOGGER = HasLogging.log();

    private GoogleImagesUtils() {

    }

    public static ImageView convertToImage(String url) {
        if (url.startsWith("data:image/")) {
            BufferedImage image = decodeToImage(url);
            Image image2 = SwingFXUtils.toFXImage(image, null);
            ImageView imageView = new ImageView(image2);
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            return imageView;
        }
        
        String host = "https://www.google.com";
        ImageView imageView = new ImageView(host + url);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public static void displayCountByExtension() {
        try (Stream<Path> find = Files.find(new File("").toPath(), 20, (a, b) -> !a.toFile().isDirectory())) {
            Map<String, Long> collect = find.collect(Collectors
                .groupingBy(e -> com.google.common.io.Files.getFileExtension(e.toString()), Collectors.counting()));
            collect.entrySet().stream().sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed())
                .forEach(ex -> LOGGER.info("{}={}", ex.getKey(), ex.getValue()));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }



    static List<String> getImagens(String artista) {
        CrawlerTask.insertProxyConfig();
        LOGGER.info("SEARCHING FOR {}", artista);
        String encode = encode(artista);
        String url = "http://www.google.com/search?q=" + encode
            + "&client=firefox-b-d&source=lnms&tbm=isch&sa=X&ved=0ahUKEwiC07SYkLnhAhVdHbkGHTwpBKwQ_AUIDigB&biw=425&bih=942";
        List<String> images = new ArrayList<>();
        CompletableFuture.supplyAsync(() -> readPage(url)).thenAccept(images::addAll);
        ForkJoinPool.commonPool().awaitQuiescence(90, TimeUnit.SECONDS);
        return images;
    }

    private static BufferedImage decodeToImage(String imageString) {
        try {
            String replaceAll = imageString.replaceAll("data:image/gif;base64,", "");

            BASE64Decoder decoder = new BASE64Decoder();
            byte[] imageByte = decoder.decodeBuffer(replaceAll);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            BufferedImage image = ImageIO.read(bis);
            bis.close();
            return image;
        } catch (Exception e) {
            LOGGER.info("ERROR LOADING {}", imageString);
            LOGGER.trace("ERROR", e);
            return null;
        }
    }

    private static String encode(String artista) {
        try {
            return URLEncoder.encode(artista, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeIOException("ERROR Reading Page", e);
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
        try {
            Document parse = getDocument(urlString);
            LOGGER.info("READING PAGE {}", urlString);
            Elements kun = parse.select("img");

            return kun.stream().map(e -> e.attr("src")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeIOException("ERROR Reading Page", e);
        }
    }

}
