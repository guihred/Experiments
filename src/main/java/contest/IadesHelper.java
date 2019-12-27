package contest;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static simplebuilder.SimpleDialogBuilder.bindWindow;

import contest.db.ContestQuestion;
import extract.PdfUtils;
import extract.UnRar;
import extract.UnZip;
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
import org.slf4j.Logger;
import utils.CrawlerTask;
import utils.HasLogging;
import utils.ResourceFXUtils;
import utils.SupplierEx;

public final class IadesHelper {
    private static final Logger LOG = HasLogging.log();

    private IadesHelper() {
    }

    public static String addDomain(SimpleStringProperty domain, String l) {
        return l.startsWith("/") ? domain.get() + l : l;
    }

    public static int containsNumber(String number, Entry<String, String> e) {
        if (e.getKey().contains(number)) {
            return 0;
        }
        if (number.startsWith("2") && StringUtils.containsIgnoreCase(e.getKey(), "médio")) {
            return 1;
        }
        if (number.startsWith("1") && StringUtils.containsIgnoreCase(e.getKey(), "superior")) {
            return 1;
        }
        if (StringUtils.containsIgnoreCase(e.getKey(), "definitivo")) {
            return 2;
        }
        return 5;
    }

    public static File extractURL(String url) {
        return SupplierEx.get(() -> {
            String file = new URL(url).getFile();
            String[] split = file.split("/");
            String out = split[split.length - 1];
            return extractURL(out, url);
        });
    }

    public static File extractURL(String name, String url) {
        return SupplierEx.get(() -> {
            File outFile = ResourceFXUtils.getOutFile(name);
            CrawlerTask.copy(url, outFile);
            if (url.endsWith(".zip")) {
                UnZip.extractZippedFiles(outFile);
            }
            if (url.endsWith(".rar")) {
                UnRar.extractRarFiles(outFile);
            }
            LOG.info("FILE {} SAVED", name);
            return outFile;
        });
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
    public static ContestReader getContestQuestions(File file, Consumer<ContestReader>... r) {
        ContestReader instance = new ContestReader();
		LOG.info("READING {}", file);
        instance.readFile(file);
        Stream.of(r).forEach(e -> e.accept(instance));
        return instance;
    }

    public static ContestReader getContestQuestions(File file, Runnable... r) {
        ContestReader instance = new ContestReader();
        new Thread(() -> {
            instance.readFile(file);
            Stream.of(r).forEach(Runnable::run);
        }).start();
        return instance;
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

    public static boolean nameMatches(String number, Path path) {
        return path.toFile().getName().contains(number) && path.toFile().getName().endsWith(".pdf");
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
        File file = extractURL(orElse.getValue());
        if (file == null) {
            LOG.info("COULD NOT DOWNLOAD {}/{} - {}", orElse, value, vaga);
            return;
        }
        File file2 = getPDF(number, file);
        getContestQuestions(file2, entities -> saveQuestions(concurso, vaga, linksFound, number, entities, vagasView));
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
        File gabaritoFile = extractURL(gabarito.getValue());
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
