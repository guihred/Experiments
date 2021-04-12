package contest;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

import com.google.common.io.Files;
import extract.SongUtils;
import extract.web.PhantomJSUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TreeCell;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import utils.ExtractUtils;
import utils.FileTreeWalker;
import utils.ProjectProperties;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;
import utils.ex.RunnableEx;
import utils.ex.SupplierEx;

public class SanarHelper {
    private static final String EXTENSION_REGEX = "\\.\\w$";

    private static final String E_SANAR_FOLDER = "eSanar/";

    private static final Logger LOG = HasLogging.log();

    public static final String PREFIX = ProjectProperties.getField();

    public static final File consulta = new File("C:" + "\\Users" + "\\guigu" + "\\Documents" + "\\Carol" + "\\eSanar");

    public static final String E_SANAR_DOMAIN = ProjectProperties.getField();

    public static void adjustStyleClass(TreeCell<Entry<String, String>> cell, Entry<String, String> c) {
        String con2 = c.getValue();
        String con = c.getKey();
        cell.setText(c + "");
        cell.getStyleClass().removeAll(IadesHelper.AMARELO);
        String finalName = fixName(con).replaceAll(EXTENSION_REGEX, "");
        String out = finalName + con2.replaceAll(".+(\\.\\w+).*", "$1");
        File videoFileName = ResourceFXUtils.getOutFile(E_SANAR_FOLDER + out);
        if (videoFileName.exists() || FileTreeWalker.getFirstPathByExtension(consulta, out) != null) {
            cell.getStyleClass().add(IadesHelper.AMARELO);
        }
    }

    public static void concursoCellFactory(Concurso t, TableCell<Concurso, Object> u) {
        u.setText(t.getNome());
        u.getStyleClass().removeAll(IadesHelper.AMARELO, IadesHelper.VERMELHO);
        if (existCopy(t.getNome())) {
            u.getStyleClass().add(IadesHelper.AMARELO);
            return;
        }
        if (t.getVagas().isEmpty()) {
            u.getStyleClass().add(IadesHelper.VERMELHO);
            t.getVagas().addListener((Observable c) -> {
                List<?> vagasList1 = (List<?>) c;
                if (t.getNome().equals(u.getText()) && !vagasList1.isEmpty()) {
                    u.getStyleClass().remove(IadesHelper.VERMELHO);
                    if (existCopy(t.getNome())) {
                        u.getStyleClass().add(IadesHelper.AMARELO);
                    }
                }
            });
        }
    }

    public static Document downloadMainPage(String url1) {
        File outFile = ResourceFXUtils.getOutFile("farmacia.html");
        return SupplierEx.getFirst(() -> Jsoup.parse(outFile, StandardCharsets.UTF_8.toString()),
                () -> loadFile(url1, outFile));
    }

    public static DoubleProperty downloadVideo(String nome, String url) {
        if (existCopy(nome)) {
            LOG.info("FILE {} EXISTS", nome);
            return null;
        }
        File file = toVideoFileName(nome);
        LOG.info("DOWNLOADING {} ...", file.getName());
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0");

        headers.put("Accept", "*/*");
        headers.put("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Origin", E_SANAR_DOMAIN);
        headers.put("DNT", "1");
        headers.put("Connection", "keep-alive");
        headers.put("Referer", "https://www.e-sanar.com.br/aluno/area-do-aluno/assunto/292,preparatorio-para-farmacia"
                + "/59602,apresentacao-planner-de-estudo-para-concursos-em-farmacia.html");
        List<String> list = SupplierEx.get(() -> PhantomJSUtils.makeGet(url, headers));
        String downloadURL = list.stream().filter(e -> e.startsWith("https")).findFirst().orElse("");
        return SongUtils.downloadVideo(downloadURL, file);
    }

    public static boolean existCopy(String nome) {
        return toVideoFileName(nome).exists()
                || FileTreeWalker.getFirstPathByExtension(consulta, videoName(nome)) != null;
    }

    public static String fixName(String name) {
        return name.replaceAll("[\\?\\\\/]+| *Baixar arquivo *", "");
    }

    public static Map<String, String> getCookies() {
        Map<String, String> cookies = new HashMap<>();
        cookies.put("ESANARSESSID", "m5otcssorltp87a8s62fmoqtc5");
        cookies.put("intercom-id-sju7o7kl", "1d4fda3e-8f5f-4a98-b129-1f73eb81f9e5");
        cookies.put("intercom-session-sju7o7kl",
                "WlFGTXRkc1NlSWZxdGxXL0NmTlk5RmV1ZWVDOVVaWUE5aDNlbWYxNDU5eFl"
                        + "QQmg1QWl6VTk1SGxsNngvdXRlRi0tS1pNNDlhVWtiMndZRFFWazdKWkRBQT09"
                        + "--60174817d9b3174e8d0eccfa9f2b797787d1c209");
        cookies.put("muxData", "mux_viewer_id=e3480ca3-e1f1-44ee-b6b5-d8d7655e92c7&msn=0.12306064932172656");
        return cookies;
    }

    public static File getFilesFromPage(Entry<String, String> entry) {
        return SupplierEx.get(() -> {
            String name = entry.getKey();
            String text = fixName(name);
            String url3 = entry.getValue();
            String extensions = url3.replaceAll(".+(\\.\\w+).+", "$1");
            String key = E_SANAR_FOLDER + text + extensions;
            File outFile = ResourceFXUtils.getOutFile(key);
            if (outFile.exists()) {
                return outFile;
            }
            Path path;
            if ((path = FileTreeWalker.getFirstPathByExtension(consulta, text + extensions)) != null) {
                return path.toFile();
            }
            return ExtractUtils.getFile(key, url3);
        });
    }

    public static boolean isValidLink(int level, Map.Entry<String, String> t) {
        return level == 1 || containsIgnoreCase(t.getValue(), E_SANAR_DOMAIN + "/arquivos/esanar_assuntos/");
    }

    public static void main(String[] args) {
        organizeFolders();
    }

    public static void organizeFolders() {
        String url1 = PREFIX + ",preparatorio-para-farmacia.html";
        Document doc = downloadMainPage(url1);
        Elements select = doc.select("#accordion .panel-default");
        for (Element panel : select) {
            String folder = fixName(panel.select("h4").text());
            List<String> filesFound =
                    panel.select("tr").stream().map(tr -> fixName(tr.text())).collect(Collectors.toList());
            for (String name : filesFound) {
                for (String extension : Arrays.asList(".mp4", ".pdf")) {
                    String finalName = fixName(name).replaceAll(EXTENSION_REGEX, "");
                    String out = finalName + extension;

                    File fileFound = getFileFound(out);
                    RunnableEx.runIf(fileFound, f -> rename(folder, out, f));
                }
            }
        }
        
    }

    public static File toVideoFileName(String nome) {
        String out = videoName(nome);
        return ResourceFXUtils.getOutFile("videos/" + out);
    }

    public static void vagasStyleClass(ListCell<Entry<String, String>> cell, Entry<String, String> c) {
        String con = c.getKey();
        cell.setText(c + "");
        cell.getStyleClass().removeAll(IadesHelper.AMARELO);
        if (toVideoFileName(con).exists()) {
            cell.getStyleClass().add(IadesHelper.AMARELO);
        }
    }

    public static String videoName(String nome) {
        String finalName = fixName(nome).replaceAll(EXTENSION_REGEX, "");
        return finalName + ".mp4";
    }

    private static File getFileFound(String out) {
        File videoFileName = ResourceFXUtils.getOutFile("videos/" + out);

        if (videoFileName.exists()) {
            return videoFileName;
        }
        File pdfFileName = ResourceFXUtils.getOutFile(E_SANAR_FOLDER + out);

        if (pdfFileName.exists()) {
            return pdfFileName;
        }
        Path firstPathByExtension = FileTreeWalker.getFirstPathByExtension(consulta, out);
        if (firstPathByExtension != null) {
            return firstPathByExtension.toFile();
        }
        return null;
    }

    private static Document loadFile(String url1, File outFile) throws IOException {
        PhantomJSUtils phantomJSUtils = new PhantomJSUtils();
        Map<String, String> cookies = getCookies();
        Document render = phantomJSUtils.render(url1, cookies, 10);
        phantomJSUtils.quit();
        Document normalise = render.normalise();
        Files.write(normalise.toString(), outFile, StandardCharsets.UTF_8);
        return normalise;
    }

    private static void rename(String folder, String out, File f) throws IOException {
        File outFile2 = new File(consulta, folder.replaceAll(":", "_") + File.pathSeparator + out);
        if (!outFile2.getParentFile().exists()) {
            outFile2.getParentFile().mkdir();
        }
        if (!f.equals(outFile2)) {
            Files.move(f, outFile2);
            LOG.info("{}->{}", f, outFile2);
        }
    }

}
