package contest;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static simplebuilder.SimpleDialogBuilder.bindWindow;
import static utils.RunnableEx.run;
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
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import utils.*;

public final class QuadrixHelper {

    private static final Logger LOG = HasLogging.log();
    public static final String QUADRIX_DOMAIN = "http://www.quadrix.org.br";
    private static final Map<String, String> COOKIES = new HashMap<>();
    private static final List<String> LINK_KEYWORDS = Arrays.asList("aplicada", "Gabarito Definitivo", "Caderno");

    private static final List<String> IT_KEYWORDS = Arrays.asList("Informação", "Sistema", "Tecnologia", "Informática");

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
                    if (hasTI(observableList)) {
                        cell.getStyleClass().add("amarelo");
                    }
                }
            });
        } else if (hasTI(con.getVagas())) {
            cell.getStyleClass().add("amarelo");
        }
    }

    public static String addQuadrixDomain(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        return QUADRIX_DOMAIN + "/" + url;
    }

    public static void findVagas(Collection<Map.Entry<String, String>> linksFound, Concurso e2,
        ObservableList<Concurso> concursos) {
        runNewThread(() -> {
            if (e2.getVagas().isEmpty()) {
                List<Entry<String, String>> collect = linksFound.stream()
                    .filter(t -> containsIgnoreCase(t.getKey(), "Resultado")).collect(toList());
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
        return ExtractUtils.getDocument(url2.toExternalForm(), COOKIES);
    }

    public static File getFileFromPage(String text, String url3) throws IOException {
        // PDFs are redirected to an html visualization page
        if (!text.endsWith(".pdf")) {
            return ExtractUtils.getFile(text, url3);
        }
        String fileParameter = decodificar(ExtractUtils.executeRequest(url3, COOKIES).url().getQuery().split("=")[1]);
        return SupplierEx
            .makeSupplier(() -> ExtractUtils.getFile(text, fileParameter), e -> LOG.info("{} Failed", fileParameter))
            .get();
    }

    public static List<File> getFilesFromPage(Entry<String, String> link) {
        String url = link.getValue();
        URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(addQuadrixDomain(url)));
        Document document = SupplierEx.getIgnore(() -> ExtractUtils.getDocument(url2.toExternalForm(), COOKIES));
        if (document == null) {
            return Collections.emptyList();
        }
        return document.select("a").stream().filter(e -> IadesHelper.hasFileExtension(e.text()))
            .map(FunctionEx.ignore(e -> getFileFromPage(e.text(), addQuadrixDomain(e.attr("href")))))
            .filter(Objects::nonNull).collect(toList());
    }

    public static List<Map.Entry<String, String>> getLinks(Document doc, Map.Entry<String, String> url,
        Property<String> domain, int level, ObservableList<Concurso> concursos, Set<String> links) {
        List<SimpleEntry<String, String>> allLinks = doc.select("a").stream()
            .map(l -> new AbstractMap.SimpleEntry<>(l.text(), IadesHelper.addDomain(domain, l.attr("href"))))
            .filter(t -> !"#".equals(t.getValue()) && isNotBlank(t.getKey())).filter(t -> links.add(t.getValue()))
            .collect(toList());
        List<Map.Entry<String, String>> linksFound = allLinks.stream().filter(t -> isValidLink(level, t)).distinct()
            .collect(toList());
        if (level == 2) {
            run(() -> getFilesFromPage(url));
        }
        if (level == 1 && !linksFound.isEmpty()) {
            Concurso e2 = new Concurso();
            e2.setUrl(url.getValue());
            String key = url.getKey();
            String[] split = key.split("-");
            e2.setNome(split[0]);
            e2.setLinksFound(linksFound);
            linksFound.stream()
                .filter(e -> containsIgnoreCase(e.getKey(), "Caderno de prova -") || e.getKey().matches("\\d+ *- *.+"))
                .map(s -> s.getKey().split("- *")[1]).forEach(e -> {
                    if (!e2.getVagas().contains(e)) {
                        e2.getVagas().add(e);
                    }
                });
            findVagas(linksFound, e2, concursos);

            concursos.add(e2);
        }
        return linksFound;
    }

    public static boolean hasTI(ObservableList<?> observableList) {
        return observableList.stream().map(Objects::toString).anyMatch(e -> IT_KEYWORDS.stream()
            .anyMatch(m -> containsIgnoreCase(e, m) || containsIgnoreCase(removerDiacritico(e), removerDiacritico(m))));
    }

    public static boolean isValidLink(int level, Map.Entry<String, String> t) {
        return level == 0 && containsIgnoreCase(t.getKey(), "•") || level == 1
            || LINK_KEYWORDS.stream().anyMatch(e -> containsIgnoreCase(t.getKey(), e));
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
            .sorted(Comparator.comparing(e -> IadesHelper.containsNumber(number, e))).collect(toList());
        File file = collect.stream().map(QuadrixHelper::getFilesFromPage).filter(e -> !e.isEmpty()).map(e -> e.get(0))
            .findFirst().orElse(null);
        if (file == null) {
            LOG.info("COULD NOT DOWNLOAD {}/{} - {}", collect, value1, value);
            return;
        }
        File file2 = IadesHelper.getPDF(value, file);

        IadesHelper.getContestQuestions(file2, Organization.QUADRIX, entities -> {
            saveQuadrixQuestions(concurso, value, linksFound, number, entities);
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
            LOG.info("SEM gabarito {}", linksFound);
            return;
        }
        List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
            .collect(toList());
        String[] split = Objects.toString(vaga, "").split("\\s*-\\s*");
        String cargo = split[split.length - 1].trim();
        Optional<String> findFirst = linesRead.stream()
            .filter(e -> e.contains(vaga) || e.contains(number) || containsIgnoreCase(e, cargo)).findFirst();
        if (!findFirst.isPresent()) {
            LOG.info("COULDN'T FIND \"{}\" \"{}\" - {}", vaga, gabaritoFile, linesRead);
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
        getFilesFromPage(resultado).forEach(ConsumerEx.ignore(f -> extrairVagas(e2, f)));
    }

}