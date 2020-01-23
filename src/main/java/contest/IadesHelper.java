package contest;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static simplebuilder.SimpleDialogBuilder.bindWindow;
import static utils.CrawlerTask.executeRequest;
import static utils.StringSigaUtils.decodificar;
import static utils.StringSigaUtils.removerDiacritico;
import static utils.SupplierEx.getIgnore;
import static utils.SupplierEx.orElse;

import contest.db.ContestQuestion;
import contest.db.Organization;
import extract.PdfUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import simplebuilder.SimpleListViewBuilder;
import utils.*;

public final class IadesHelper {
    public static final Logger LOG = HasLogging.log();
    static final String QUADRIX_DOMAIN = "http://www.quadrix.org.br";

    private IadesHelper() {
    }

    public static void saveConcurso(Property<Concurso> concurso, SimpleListViewBuilder<String> listBuilder,
        String value) {
        if (value == null) {
            return;
        }
        Concurso value1 = concurso.getValue();
        ObservableList<Entry<String, String>> linksFound = value1.getLinksFound();
        String number = Objects.toString(value) + "";
        List<Entry<String, String>> collect = linksFound.stream()
            .filter(e -> e.getKey().contains("Provas") || e.getKey().contains(number))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).collect(Collectors.toList());
        File file = collect.stream().map(IadesHelper::getFilesFromPage).filter(e -> !e.isEmpty()).map(e -> e.get(0))
            .findFirst().orElse(null);
        if (file == null) {
            QuadrixCrawler.LOG.info("COULD NOT DOWNLOAD {}/{} - {}", collect, value1, value);
            return;
        }
        File file2 = getPDF(value, file);
    
        getContestQuestions(file2, Organization.QUADRIX, entities -> {
            saveQuadrixQuestions(concurso, value, linksFound, number, entities);
            Platform
                .runLater(() -> new ContestApplication(entities).start(bindWindow(new Stage(), listBuilder.build())));
        });
    }

    public static String addQuadrixDomain(String url) {
        if (url.startsWith("http")) {
            return url;
        }
        return IadesHelper.QUADRIX_DOMAIN + "/" + url;
    }

    static boolean hasTI(ObservableList<?> observableList) {
        List<String> keys = Arrays.asList("Informação", "Sistema", "Tecnologia", "Informática");
        return observableList.stream().map(Objects::toString)
            .anyMatch(e -> keys.stream().anyMatch(
                m -> containsIgnoreCase(e, m) || containsIgnoreCase(removerDiacritico(e), removerDiacritico(m))));
    }

    public static String addDomain(SimpleStringProperty domain, String l) {
        if (l.startsWith("http")) {
            return l;
        }
        return domain.get() + (!l.startsWith("/") ? "/" + l : l);
    }

    public static int containsNumber(String number, Entry<String, String> e) {
        if (containsIgnoreCase(e.getKey(), number)) {
            return 0;
        }
        if (Stream.of(number.split(" ")).filter(s -> s.length() > 2)
            .anyMatch(m -> containsIgnoreCase(e.getKey(), m))) {
            return 0;
        }
        if (number.startsWith("2") && containsIgnoreCase(e.getKey(), "médio")) {
            return 1;
        }
        if (number.startsWith("1") && containsIgnoreCase(e.getKey(), "superior")) {
            return 1;
        }
        if (containsIgnoreCase(e.getKey(), "definitivo")) {
            return 2;
        }
        return 5;
    }

    public static String getAnswers(ContestReader entities, List<String> linesRead, String findFirst) {
        int indexOf = linesRead.indexOf(findFirst);
        List<String> subList = linesRead.subList(indexOf, linesRead.size() - 1);
        List<String> answersList = subList.stream().filter(StringUtils::isNotBlank).filter(s -> s.matches("[\\sA-E#]+"))
            .collect(Collectors.toList());
        StringBuilder answers = new StringBuilder();
        for (String string : answersList) {
            String split = Stream.of(string.split("")).filter(s -> s.matches("[A-E#]")).collect(Collectors.joining());
            answers.append(split);
            if (answers.length() >= entities.getListQuestions().size()) {
                return answers.toString();
            }
        }
        return answers.toString();
    }

    @SafeVarargs
    public static ContestReader getContestQuestions(File file, Organization organization,
        Consumer<ContestReader>... r) {
        ContestReader instance = new ContestReader();
		LOG.info("READING {}", file);
        instance.readFile(file, organization);
        Stream.of(r).forEach(e -> e.accept(instance));
        return instance;
    }

    public static File getFileFromPage(String text, String url3) throws IOException {
        // PDFs are redirected to an html visualization page
        if (!text.endsWith(".pdf")) {
            return CrawlerTask.getFile(text, url3);
        }
        Response executeRequest = executeRequest(url3, QuadrixCrawler.cookies);
        String fileParameter = decodificar(executeRequest.url().getQuery().split("=")[1]);
        return SupplierEx
            .makeSupplier(() -> CrawlerTask.getFile(text, fileParameter), e -> QuadrixCrawler.LOG.info("{} Failed", fileParameter))
            .get();
    }

    public static List<File> getFilesFromPage(Entry<String, String> link) {
        String url = link.getValue();
        URL url2 = orElse(getIgnore(() -> new URL(url)), () -> new URL(IadesHelper.addQuadrixDomain(url)));
        Document document = SupplierEx.getIgnore(() -> CrawlerTask.getDocument(url2.toExternalForm(), QuadrixCrawler.cookies));
        if (document == null) {
            return Collections.emptyList();
        }
        Elements select = document.select("a");
        return select.stream().filter(e -> hasFileExtension(e.text()))
            .map(FunctionEx.ignore(e -> getFileFromPage(e.text(), IadesHelper.addQuadrixDomain(e.attr("href"))))).filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static Path getFirstPDF(File file, String number) throws IOException {
		try (Stream<Path> find = Files.find(file.toPath(), 3, (path, info) -> nameMatches(number, path))) {
            Optional<Path> findFirst = find.findFirst();
            if (findFirst.isPresent()) {
                return findFirst.get();

            }
        }

        try (Stream<Path> find = Files.find(file.toPath(), 3,
				(path, info) -> path.toFile().getName().endsWith(".pdf"))) {
            Optional<Path> findFirst = find.findFirst();
            if (findFirst.isPresent()) {
                return findFirst.get();

            }
            LOG.info("NO PDF found {} {}- {}", file, Arrays.asList(file.list()), number);
            return null;
        }
    }

    public static File getPDF(String number, File file) {
        if (file.getName().endsWith(".pdf")) {
            return file;
        }
        File file3 = new File(file.getParentFile(), file.getName().replaceAll("\\.\\w+", ""));

        return SupplierEx.get(() -> getFirstPDF(file3, number).toFile());
    }

    public static boolean hasFileExtension(String key) {
        return key.endsWith(".pdf") || key.endsWith(".zip") || key.endsWith(".rar");
    }

    public static boolean nameMatches(String number, Path path) {
        String fileName = path.toFile().getName();
        return (containsIgnoreCase(fileName, number)
            || fileName.matches(".*" + number.replaceAll(" ", ".*") + ".*")
            || Stream.of(number.split(" ")).filter(e -> e.length() > 2)
                .anyMatch(m -> containsIgnoreCase(fileName, m))
            || Stream.of(number.split(" ")).map(StringSigaUtils::removerDiacritico).filter(e -> e.length() > 2)
                .anyMatch(m -> containsIgnoreCase(fileName, m))
        ) && fileName.endsWith(".pdf");
    }

    public static void saveContestValues(Property<Concurso> concurso, String vaga, Node vagasView) {
        if (vaga == null) {
            return;
        }
        Concurso value = concurso.getValue();
        ObservableList<Entry<String, String>> linksFound = value.getLinksFound();
        String number = Objects.toString(vaga).replaceAll("\\D", "");
        Entry<String, String> orElse = linksFound.stream().filter(e -> e.getKey().contains("Provas"))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).findFirst().orElse(null);
        if (orElse == null) {
            LOG.info("NO LINK FOR Provas found {} - {}", vaga, value);
            return;
        }
        File file = CrawlerTask.extractURL(orElse.getValue());
        if (file == null) {
            LOG.info("COULD NOT DOWNLOAD {}/{} - {}", orElse, value, vaga);
            return;
        }
        File file2 = getPDF(number, file);
        getContestQuestions(file2, Organization.IADES,
            entities -> saveQuestions(concurso, vaga, linksFound, number, entities, vagasView));
    }

    public static void saveQuadrixQuestions(Property<Concurso> concurso, String vaga,
        ObservableList<Entry<String, String>> linksFound, String number, ContestReader entities) {
        entities.getContest().setName(concurso.getValue().getNome());
        entities.getContest().setJob(vaga);
        entities.saveAll();
        File gabaritoFile = linksFound.stream().filter(e -> e.getKey().contains("Gabarito"))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).map(IadesHelper::getFilesFromPage)
            .filter(l -> !l.isEmpty()).map(l -> l.get(0)).findFirst().orElse(null);
        if (gabaritoFile == null) {
            QuadrixCrawler.LOG.info("SEM gabarito {}", linksFound);
            return;
        }
        List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
            .collect(Collectors.toList());
        String[] split = Objects.toString(vaga, "").split("\\s*-\\s*");
        String cargo = split[split.length - 1].trim();
        Optional<String> findFirst = linesRead.stream()
            .filter(e -> e.contains(vaga) || e.contains(number) || containsIgnoreCase(e, cargo)).findFirst();
        if (!findFirst.isPresent()) {
            QuadrixCrawler.LOG.info("COULDN'T FIND \"{}\" \"{}\" - {}", vaga, gabaritoFile, linesRead);
            return;
        }
        String answers = getAnswers(entities, linesRead, findFirst.get());
        if (answers.length() != entities.getListQuestions().size()) {
            QuadrixCrawler.LOG.info("QUESTIONS DON'T MATCH {} {}", answers.length(), entities.getListQuestions().size());
            return;
        }
        ObservableList<ContestQuestion> listQuestions = entities.getListQuestions();
        for (int i = 0; i < listQuestions.size(); i++) {
            ContestQuestion contestQuestion = listQuestions.get(i);
            contestQuestion.setAnswer(answers.charAt(i));
        }
        entities.saveAll();
    }

    public static void saveQuestions(Property<Concurso> concurso, String vaga,
        ObservableList<Entry<String, String>> linksFound, String number, ContestReader entities, Node vagasView) {
        entities.getContest().setName(concurso.getValue().getNome());
        entities.getContest().setJob(vaga);
        entities.saveAll();
        Entry<String, String> gabarito = linksFound.stream().filter(e -> e.getKey().contains("Gabarito"))
            .sorted(Comparator.comparing(e -> containsNumber(number, e))).findFirst().orElse(null);
        if (gabarito == null) {
            LOG.info("SEM gabarito {}", linksFound);
            return;
        }
        File gabaritoFile = CrawlerTask.extractURL(gabarito.getValue());
        List<String> linesRead = PdfUtils.readFile(gabaritoFile).getPages().stream().flatMap(List<String>::stream)
            .collect(Collectors.toList());
        String[] split = Objects.toString(vaga, "").split("\\s*-\\s*");
        String cargo = split[split.length - 1].trim();
        Optional<String> findFirst = linesRead.stream()
            .filter(e -> e.contains(vaga) || e.contains(number) || containsIgnoreCase(e, cargo)).findFirst();
        if (!findFirst.isPresent()) {
            LOG.info("COULDN'T FIND \"{}\" \"{}\" - {}", vaga, gabarito.getKey(), linesRead);
            return;
        }
        String answers = getAnswers(entities, linesRead, findFirst.get());
        if (answers.length() != entities.getListQuestions().size()) {
            LOG.info("QUESTIONS DON'T MATCH {} {}", answers.length(), entities.getListQuestions().size());
            return;
        }
        ObservableList<ContestQuestion> listQuestions = entities.getListQuestions();
        for (int i = 0; i < listQuestions.size(); i++) {
            ContestQuestion contestQuestion = listQuestions.get(i);
            contestQuestion.setAnswer(answers.charAt(i));
        }
        entities.saveAll();
        Platform.runLater(
            () -> new ContestApplication(entities).start(bindWindow(new Stage(), vagasView)));
    }

}
