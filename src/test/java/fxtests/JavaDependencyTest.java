package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import utils.HasLogging;

@SuppressWarnings("static-method")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaDependencyTest {
    private static final Logger LOG = HasLogging.log();

    @Test
    public void testAGetJavaMethods() {
        measureTime("JavaFileDependency.getPublicMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                List<String> tests = dependency.getPublicMethods();
                LOG.trace("{} ={}", dependency.getFullName(), tests);
            }
        });
    }

    @Test
    public void testBGraphMethodMap() {
        measureTime("JavaFileDependency.getInvocationsMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                dependency.setDependents(displayTestsToBeRun);
                Map<String, List<String>> tests = dependency.getPublicMethodsFullName();
                tests.forEach((k, v) -> LOG.trace("{} ={}", k, v.stream().collect(Collectors.joining("\n", "\n", ""))));

            }
        });
    }

    @Test
    public void testCInvocations() {
        measureTime("JavaFileDependency.getInvocationsMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                dependency.setDependents(displayTestsToBeRun);
                List<String> tests = dependency.getInvocationsMethods();
                LOG.trace("{} ={}", dependency.getFullName(), tests);
            }
        });
    }

    @Test
    public void testDMethodMap() {
        measureTime("JavaFileDependency.getInvocationsMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                Map<String, List<String>> tests = dependency.getPublicMethodsMap();
                tests.forEach((k, v) -> LOG.trace("{} ={}", dependency.getFullName(),
                    v.stream().collect(Collectors.joining("\n", "\n", ""))));

            }
        });
    }

    @Test
    public void testEJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun", () -> {
			List<String> asList = Arrays.asList("EditSongHelper", "FilesComparator", "MusicOrganizer",
					"ContestApplicationController", "ContestQuestionEditingDisplay", "IadesHelper", "ContestQuestion",
					"ContestQuestionAnswer", "ContestText", "VigenereXORCipher", "CandidatoDAO", "CandidatoHelper",
					"CommonCrawlerTask", "AlarmClock", "EthicalHackApp", "ImageCrackerApp", "NetworkInformationScanner",
					"PingTraceRoute", "PortScanner", "PortServices", "BogusPasswordAuthenticator",
					"CommonTestSupportUtils", "SSHClientUtils", "Chapter4", "Point", "Anagram", "FilesComparatorHelper",
					"Music", "PdfImage", "SongUtils", "UnRar", "WordService", "LeafFractalApp", "PongLauncher",
					"StageControlExample", "JavaFXBeanController", "ReversiPiece", "ReversiSquare",
					"TableVisualizationExampleApp", "TableVisualizationModel", "ResponsiveUIView",
					"ThreadInformationView", "BasicAudioPlayerWithControlLauncher", "PlayerControlView", "CubeNode",
					"AnchorCircle", "BackgroundProcesses", "GlobeSphereApp", "InlineModelViewer", "JewelViewer",
					"MoleculeSampleApp", "PhotoViewer", "PlayingAudio", "SimpleScene3D", "WorkingListsViews",
					"BoundsDisplay", "ShapePair", "FormValidation", "PersonTableController", "WorkingWithTableView",
					"SnakeLauncher", "SnakeModel", "SnakeSquare", "MemoryModel", "SlidingPuzzleModel",
					"SlidingPuzzleSquare", "TronModel", "TronSquare", "QuartoLauncher", "QuartoModel", "Maze3DSquare",
					"Dog", "Leopard", "PlatformMain", "SolitaireCard", "PacmanBall", "MadEdgeDistance", "PuzzlePiece",
					"Square2048Model", "CatanResource", "EdgeElement", "JavaExercise19", "Link", "CircleLayout",
					"GraphMain", "JavaFileDependency", "LayerSplitter", "RandomLayout", "EdgeDistancePack",
					"MouseGestures", "ImageLoading", "JapaneseAudio", "JapaneseLesson", "JapaneseLessonApplication",
					"JapaneseLessonAudioSplitDisplay", "JapaneseLessonDisplay", "JapaneseLessonDisplayer", "LessonPK",
					"Labyrinth2D", "Labyrinth3D", "Labyrinth3DCollisions", "Labyrinth3DKillerGhostsAndBalls",
					"Labyrinth3DWallTexture", "HeatGraphExample", "MapCallback", "PointsExample",
					"PopulacionalPyramidExample", "WordSearchApp", "WorldMapExample", "Country",
					"DataframeStatisticAccumulator", "DataframeUtils", "DecisionNode", "Question", "ColorPattern",
					"MapGraph", "WorldMapGraph2", "OthersTests", "StatsLogAccess", "PaintEditUtils", "PaintFileUtils",
					"SimplePixelReader", "LineTool", "PaintTools", "PencilTool", "PictureOption", "TextTool",
					"PdfController", "Speaker", "LeitorArquivos", "RosarioCommons", "ControllerCompiler",
					"FXMLCreatorHelper", "SimpleComboBoxBuilder", "SimpleListViewBuilder", "SimpleMenuBarBuilder",
					"BaseDAO", "BaseEntity", "CommonsFX", "HibernateUtil", "ImageFXUtils", "ImageTableCell",
					"PixelHelper", "RotateUtils", "StageHelper", "StringSigaUtils", "TaskProgressView",
					"ZoomableScrollPane");

            Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(asList, "fxtests");
            String tests = displayTestsToBeRun.stream().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

    @Test
    public void testFJavaCoverage() {

        measureTime("JavaFileDependency.javaCoverage", () -> {
            File csvFile = new File("target/site/jacoco/jacoco.csv");
            if (csvFile.exists()) {
                DataframeML b = DataframeBuilder.build(csvFile);
                b.filter("INSTRUCTION_COVERED", v -> ((Number) v).intValue() == 0);
                List<String> uncovered = b.list("CLASS");
                LOG.info("Uncovered classes ={}", uncovered);
                Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(uncovered, "fxtests");
                String tests = displayTestsToBeRun.stream().collect(Collectors.joining(",*", "*", ""));
                LOG.info("TestsToBeRun ={}", tests);
            }

        });
    }

}
