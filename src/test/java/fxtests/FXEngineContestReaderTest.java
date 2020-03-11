package fxtests;

import contest.*;
import contest.db.Organization;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import utils.HasLogging;
import utils.HibernateUtil;
import utils.ResourceFXUtils;

@SuppressWarnings("static-method")
public class FXEngineContestReaderTest extends AbstractTestExecution {

    private Logger log = HasLogging.log();

    private List<String> invalidFiles = new ArrayList<>();

    @After
    public void cleanUp() {
        HibernateUtil.setShutdownEnabled(true);
        HibernateUtil.shutdown();
    }

    @Before
    public void start() {
        ResourceFXUtils.initializeFX();
        HibernateUtil.getSessionFactory();
        HibernateUtil.setShutdownEnabled(false);
    }
    @Test
    public void testAllFiles() {
        List<File> listFiles = Arrays
            .asList(ResourceFXUtils.getOutFile().listFiles(f -> f.isDirectory() && f.getName().matches("\\d+")));
        for (File file : listFiles) {
            File firstPdf = ResourceFXUtils.getFirstPathByExtension(file, "pdf").toFile();
            IadesHelper.getContestQuestions(firstPdf, Organization.IADES,
                reader -> addToInvalidFiles(invalidFiles, firstPdf, reader));
        }
        displayResults(listFiles, invalidFiles);
    }

    public void testErrorFiles() {
        List<File> listFiles = invalidFiles.stream().map(ResourceFXUtils::getOutFile).collect(Collectors.toList());
        List<String> invalidFiles2 = new ArrayList<>();
        for (File file : listFiles) {
            IadesHelper.getContestQuestions(file, Organization.IADES,
                reader -> addToInvalidFiles(invalidFiles2, file, reader));
        }
        displayResults(listFiles, invalidFiles2);
    }

    @Test
    public void testIades() {
        IadesHelper.addDomain(new SimpleStringProperty(""), "");
        show(IadesCrawler.class);
        clickOn(lookupFirst(TreeView.class));
        type(KeyCode.SPACE);
        type(KeyCode.RIGHT);
        type(KeyCode.DOWN, 20);
        type(KeyCode.TAB);
        queryYellow();

        type(KeyCode.TAB);
        type(KeyCode.SPACE);
    }

    @Test
    public void testQuadrix() {
        show(QuadrixCrawler.class);
        clickOn(lookupFirst(TreeView.class));
        type(KeyCode.SPACE);
        type(KeyCode.RIGHT);
        type(KeyCode.DOWN, 8);

        type(KeyCode.DOWN, 10);
        type(KeyCode.TAB);
        queryYellow();
        type(KeyCode.TAB);
        type(KeyCode.SPACE);
        type(KeyCode.DOWN, 1);
        IadesHelper.addDomain(new SimpleStringProperty(""), "");
    }

    @Test
    public void verifyContestApplication() {
        show(ContestApplication.class);
        lookup(ListCell.class).stream().filter(e -> e.getItem() != null)
            .collect(Collectors.groupingBy(e -> e.getListView().getId())).values().forEach(cells -> {
                for (Pos pos : Arrays.asList(Pos.CENTER_RIGHT, Pos.BASELINE_LEFT, Pos.TOP_LEFT, Pos.CENTER_LEFT,
                    Pos.CENTER)) {
                    targetPos(pos);
                    doubleClickOn(randomItem(cells), MouseButton.PRIMARY);
                }
            });
        tryClickButtons();
    }

    @Test
    public void verifyContestQuestionEditingDisplay() {
        show(ContestQuestionEditingDisplay.class);
        clickButtonsWait();
    }

    private void addToInvalidFiles(List<String> invalidFiles1, File firstPdf, ContestReader i) {
        String absolutePath = ResourceFXUtils.getOutFile().getAbsolutePath() + "\\";
        if (!i.validate()) {
            log.error("ERROR IN FILE {}", firstPdf);
            invalidFiles1.add(firstPdf.getAbsolutePath().replace(absolutePath, ""));
        } else {
            log.info("VALID {}", firstPdf);
        }
    }

    private void displayResults(List<File> listFiles, List<String> invalidFiles1) {
        String invalidStr = invalidFiles1.stream().map(e -> e.replace("\\", "\\\\"))
            .collect(Collectors.joining("\",\"", "\"", "\""));
        log.info("INVALID {}/{}  {}", invalidFiles1.size(), listFiles.size(), invalidStr);
    }

    private void queryYellow() {
        Set<Node> queryAll = lookup(".amarelo").queryAll();
        if (queryAll.isEmpty()) {
            type(KeyCode.DOWN, nextInt(20));
            return;
        }
        for (Node node : queryAll) {
            tryClickOn(node);
            lookup(".amarelo").match(ListCell.class::isInstance).queryAllAs(ListCell.class)
                .forEach(ode -> tryClickOn(ode));
        }
    }

}