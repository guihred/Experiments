package contest;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static simplebuilder.SimpleDialogBuilder.bindWindow;
import static utils.CrawlerTask.executeRequest;
import static utils.CrawlerTask.getDocument;
import static utils.CrawlerTask.getFile;
import static utils.RunnableEx.runNewThread;
import static utils.StringSigaUtils.decodificar;
import static utils.StringSigaUtils.removerDiacritico;
import static utils.SupplierEx.getIgnore;
import static utils.SupplierEx.orElse;

import contest.db.Organization;
import extract.PdfUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.*;

public final class QuadrixHelper {


    public static final String QUADRIX_DOMAIN = "http://www.quadrix.org.br";
    private static final Map<String, String> COOKIES = new HashMap<>();
    private static final List<String> keywords = Arrays.asList("aplicada", "Gabarito Definitivo", "Caderno");

    private QuadrixHelper() {
    }

    public static void addClasses(Concurso con, TableCell<Concurso, Object> cell) {
        cell.setText(con.getNome());
        cell.getStyleClass().removeAll("amarelo", "vermelho");
        if (con.getVagas().isEmpty()) {
            cell.getStyleClass().add("vermelho");
            con.getVagas().addListener((Observable c) -> {
                ObservableList<?> observableList = (ObservableList<?>) c;
                if (con.getNome().equals(cell.getText()) && !observableList.isEmpty()) {
                    cell.getStyleClass().remove("vermelho");
                    if (QuadrixHelper.hasTI(observableList)) {
                        cell.getStyleClass().add("amarelo");
                    }
                }
            });
        } else if (QuadrixHelper.hasTI(con.getVagas())) {
            cell.getStyleClass().add("amarelo");
        }
    }

    public static String addQuadrixDomain(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        return QuadrixHelper.QUADRIX_DOMAIN + "/" + url;
    }

    public static void findVagas(Collection<Map.Entry<String, String>> linksFound, Concurso e2,
        ObservableList<Concurso> concursos) {
        runNewThread(() -> {
            if (e2.getVagas().isEmpty()) {
                List<Entry<String, String>> collect = linksFound.stream()
                    .filter(t -> containsIgnoreCase(t.getKey(), "Resultado")).collect(Collectors.toList());
                for (Entry<String, String> entry : collect) {
                    getVagas(e2, entry);
                    if (!e2.getVagas().isEmpty()) {
                        break;
                    }
                }
                if (e2.getVagas().isEmpty()) {
                    Platform.runLater(() -> concursos.remove(e2));
                }
            }
        });
    }

    public static Document getDocumentCookies(URL url2) throws IOException {
        return CrawlerTask.getDocument(url2.toExternalForm(), COOKIES);
    }

    public static File getFileFromPage(String text, String url3) throws IOException {
        // PDFs are redirected to an html visualization page
        if (!text.endsWith(".pdf")) {
            return getFile(text, url3);
        }
        Response executeRequest = executeRequest(url3, QuadrixHelper.COOKIES);
        String fileParameter = decodificar(executeRequest.url().getQuery().split("=")[1]);
        return SupplierEx
            .makeSupplier(() -> getFile(text, fileParameter), e -> HasLogging.log().info("{} Failed", fileParameter))
            .get();
    }

    public static List<File> getFilesFromPage(Entry<String, String> link) {
        String url = link.getValue();
        URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(QuadrixHelper.addQuadrixDomain(url)));
        Document document = SupplierEx.getIgnore(() -> getDocument(url2.toExternalForm(), QuadrixHelper.COOKIES));
        if (document == null) {
            return Collections.emptyList();
        }
        Elements select = document.select("a");
        return select.stream().filter(e -> IadesHelper.hasFileExtension(e.text()))
            .map(FunctionEx.ignore(e -> getFileFromPage(e.text(), QuadrixHelper.addQuadrixDomain(e.attr("href")))))
            .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static boolean hasTI(ObservableList<?> observableList) {
        List<String> keys = Arrays.asList("Informação", "Sistema", "Tecnologia", "Informática");
        return observableList.stream().map(Objects::toString).anyMatch(e -> keys.stream()
            .anyMatch(m -> containsIgnoreCase(e, m) || containsIgnoreCase(removerDiacritico(e), removerDiacritico(m))));
    }

    public static boolean isValidLink(int level, SimpleEntry<String, String> t) {
        return level == 0 && containsIgnoreCase(t.getKey(), "•") || level == 1
            || keywords.stream().anyMatch(e -> containsIgnoreCase(t.getKey(), e));
    }

    public static void saveConcurso(Property<Concurso> concurso, ListView<String> listBuilder, String value) {
        if (value == null) {
            return;
        }
        Concurso value1 = concurso.getValue();
        ObservableList<Entry<String, String>> linksFound = value1.getLinksFound();
        String number = Objects.toString(value) + "";
        List<Entry<String, String>> collect = linksFound.stream()
            .filter(e -> e.getKey().contains("Provas") || e.getKey().contains(number))
            .sorted(Comparator.comparing(e -> IadesHelper.containsNumber(number, e))).collect(Collectors.toList());
        File file = collect.stream().map(QuadrixHelper::getFilesFromPage).filter(e -> !e.isEmpty()).map(e -> e.get(0))
            .findFirst().orElse(null);
        if (file == null) {
            IadesHelper.LOG.info("COULD NOT DOWNLOAD {}/{} - {}", collect, value1, value);
            return;
        }
        File file2 = IadesHelper.getPDF(value, file);

        IadesHelper.getContestQuestions(file2, Organization.QUADRIX, entities -> {
            QuadrixHelper.saveQuadrixQuestions(concurso, value, linksFound, number, entities);
            Platform.runLater(() -> new ContestApplication(entities).start(bindWindow(new Stage(), listBuilder)));
        });
    }

    public static void saveQuadrixQuestions(Property<Concurso> concurso, String vaga,
        ObservableList<Entry<String, String>> linksFound, String number, ContestReader entities) {
        entities.getContest().setName(concurso.getValue().getNome());
        entities.getContest().setJob(vaga);
        entities.saveAll();
        File gabaritoFile = linksFound.stream().filter(e -> e.getKey().contains("Gabarito"))
            .sorted(Comparator.comparing(e -> IadesHelper.containsNumber(number, e)))
            .map(QuadrixHelper::getFilesFromPage).filter(l -> !l.isEmpty()).map(l -> l.get(0)).findFirst().orElse(null);
        if (gabaritoFile == null) {
            IadesHelper.LOG.info("SEM gabarito {}", linksFound);
            return;
        }
        List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
            .collect(Collectors.toList());
        String[] split = Objects.toString(vaga, "").split("\\s*-\\s*");
        String cargo = split[split.length - 1].trim();
        Optional<String> findFirst = linesRead.stream()
            .filter(e -> e.contains(vaga) || e.contains(number) || containsIgnoreCase(e, cargo)).findFirst();
        if (!findFirst.isPresent()) {
            IadesHelper.LOG.info("COULDN'T FIND \"{}\" \"{}\" - {}", vaga, gabaritoFile, linesRead);
            return;
        }
        IadesHelper.saveAnswers(entities, linesRead, findFirst.get());
    }

    private static void addVaga(Concurso e2, String vaga) {
        if (isNotBlank(vaga) && !vaga.matches("\\d+:") && !e2.getVagas().contains(vaga)) {
            Platform.runLater(() -> {
                if (!e2.getVagas().contains(vaga)) {
                    e2.getVagas().add(vaga);
                }
            });
        }
    }

    private static void extrairVagas(Concurso e2, File f) {
        for (String string : PdfUtils.getAllLines(f)) {
            if (string.matches("(?i).+\\(código \\d+\\).*")) {
                String split = string.split(" *\\(")[0];
                addVaga(e2, split);
                continue;
            }
            String[] split = string.split(" ");
            String regex = "\\d{3}\\.\\d+\\/\\d";
            List<String> asList = Arrays.asList(split);
            Optional<String> findFirst = asList.stream().filter(e -> e.matches(regex)).findFirst();
            if (findFirst.isPresent()) {
                int indexOf = asList.indexOf(findFirst.get());
                String collect = Stream.of(split).skip(1).limit(indexOf - 1L).collect(Collectors.joining(" "));
                String vaga = collect.split(" *-")[0];
                if (!vaga.matches(".*" + regex + ".*")) {
                    addVaga(e2, vaga);
                }
            }
        }
        RunnableEx.run(() -> Files.deleteIfExists(f.toPath()));
    }

    private static void getVagas(Concurso e2, Entry<String, String> resultado) {
        QuadrixHelper.getFilesFromPage(resultado).forEach(ConsumerEx.ignore(f -> extrairVagas(e2, f)));
    }

}
