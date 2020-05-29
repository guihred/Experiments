package extract;

import static utils.StringSigaUtils.codificar;

import com.twelvemonkeys.imageio.stream.ByteArrayImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.ObservableList;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.HasLogging;
import utils.RunnableEx;
import utils.SupplierEx;

public final class WikiImagesUtils {

    private static final Logger LOG = HasLogging.log();

    private WikiImagesUtils() {

    }

    public static void displayCountByExtension() {
        RunnableEx.run(() -> {
            try (Stream<Path> find = Files.find(new File("").toPath(), 20, (a, b) -> !a.toFile().isDirectory())) {
                Map<String, Long> fileExtensionCount = find.collect(Collectors
                    .groupingBy(e -> com.google.common.io.Files.getFileExtension(e.toString()), Collectors.counting()));
                fileExtensionCount.entrySet().stream()
                    .sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed())
                    .forEach(ex -> LOG.info("{}={}", ex.getKey(), ex.getValue()));
            }
        });
    }


    public static ObservableList<String> getImagensForked(String artista, ObservableList<String> images) {
        ExtractUtils.insertProxyConfig();
        LOG.info("SEARCHING FOR {}", artista);
        String encode = codificar(artista.replace(' ', '_'));
        String url = "https://en.wikipedia.org/wiki/" + encode;
        String url2 = "https://pt.wikipedia.org/wiki/" + encode;
        images.addAll(SupplierEx.getIgnore(() -> readPage(url), Collections.emptyList()));
        images.addAll(SupplierEx.getIgnore(() -> readPage(url2), Collections.emptyList()));
        return images;
    }

    static BufferedImage decodeToImage(String imageString) {
        return SupplierEx.get(() -> {
            String formatName = imageString.replaceAll("data:image/(\\w+);.*", "$1");
            String replaceAll = imageString.replaceAll("data:image/" + formatName + ";base64,", "");
            byte[] imageByte = Base64.getDecoder().decode(replaceAll);
            ImageReader next = ImageIO.getImageReadersByFormatName(formatName).next();
            next.setInput(new ByteArrayImageInputStream(imageByte));
            return next.read(0);
        });
    }


    private static List<String> readPage(String urlString) {
        return SupplierEx.remap(() -> {
            Document parse = ExtractUtils.getDocument(urlString);
            LOG.info("READING PAGE {}", urlString);
            Elements kun = parse.select("img");
            return kun.stream().map(e -> e.attr("src")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        }, "ERROR Reading Page");
    }

}
