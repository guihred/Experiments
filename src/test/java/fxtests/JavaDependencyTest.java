package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
                LOG.info("{} ={}", dependency.getFullName(), tests);
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
                tests.forEach((k, v) -> LOG.info("{} ={}", k, v.stream().collect(Collectors.joining("\n", "\n", ""))));

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
                LOG.info("{} ={}", dependency.getFullName(), tests);
            }
        });
    }

    @Test
    public void testDMethodMap() {
        measureTime("JavaFileDependency.getInvocationsMethods", () -> {
            List<JavaFileDependency> displayTestsToBeRun = JavaFileDependency.getAllFileDependencies();
            for (JavaFileDependency dependency : displayTestsToBeRun) {
                Map<String, List<String>> tests = dependency.getPublicMethodsMap();
                tests.forEach((k, v) -> LOG.info("{} ={}", dependency.getFullName(),
                    v.stream().collect(Collectors.joining("\n", "\n", ""))));

            }
        });
    }

    @Test
    public void testEJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun", () -> {
            Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(Arrays.asList(
					"CatanApp", "MovimentacaoAleatoria", "GhostGenerator", "Chapter1$Collection2Impl",
					"HeatGraphExample", "PacmanGhost$GhostDirection", "ConcentricLayout", "JavaFileDependency",
					"DecisionNode", "LineTool", "CircleLayout", "PaintImageUtils", "SprayTool$1", "VigenereXORCipher",
					"CustomLayout", "PolygonTool", "RandomLayout", "RunnableEx", "GabrielTopology", "EchoShell",
					"TextTool", "ColorChooser", "RegressionChartExample", "PopulacionalGraph", "FormValidation",
					"FXMLCreatorHelper", "LeitorArquivos", "Labyrinth3DMouseControl", "DataframeML", "PieGraph",
					"ContestApplication", "PacmanModel", "EditSongHelper", "HibernateUtil", "PaintModel", "CatanModel",
					"PaintHelper", "PaintMain", "RosarioExperiment", "ContestQuestion", "CurveTool", "EdgeCatan",
					"BaseTopology", "WorldMapExample", "Deal", "CatanLogger", "Village", "PictureTool", "EdgeElement",
					"Labyrinth3DKillerGhostsAndBalls", "Port", "PieGraphExample", "NameServerLookup", "PdfUtils",
					"StageHelper", "TetrisModel", "MultiLineGraph", "CrawlerTask", "Labyrinth3DWallTexture",
					"PlayerControlView$StatusListener", "PrintImageLocations", "PongLauncher", "CatanCard",
					"PacmanGhost$GhostStatus", "LayerSplitter", "Labyrinth3DGhosts", "Terrain", "WandTool",
					"Chapter1$Collection2", "CommonsFX", "RandomTopology", "Maze3DSquare", "EyedropTool", "AlarmClock",
					"MusicHandler", "PolygonFractal", "RosarioCommons$3", "RosarioCommons$1", "RosarioCommons$2",
					"WorldMapExample3", "TimelineGraph", "WorldMapExample2", "Vertex", "SngpcViewer", "QuartoLauncher",
					"ProjectTopology", "Labyrinth2D", "ExtraPoint", "SnakeLauncher", "PaintTool", "ProcessScan",
					"IadesHelper", "HistogramExample", "City", "CandidatoApp", "PackageTopology", "RectangleTool",
					"PacmanGhost", "ContestQuestionEditingDisplay", "RosarioCommons$CustomableTableCell", "SudokuModel",
					"BlurTool", "MazeSquare", "SnowflakeFractal", "ConsoleUtils", "Labyrinth3DCollisions",
					"ContestReader$ReaderState", "Chapter1", "PortScanner", "BoundsPlayground", "TetrisLauncher",
					"AreaTool$SelectOption", "SongUtils", "SelectRectTool", "CandidatoHelper",
					"Labyrinth3DKillerGhosts", "DecisionTree", "ContestReader", "OrganicTreeFractal", "LayerLayout",
					"ResponsiveUIView", "GraphModelLauncher", "PaintTools", "RosarioCommons", "CommomLabyrinth",
					"Combination", "Labyrinth3D", "MinesweeperModel", "WorldMapGraph", "WordTopology", "FXMLCreator",
					"RectBuilder", "HeatGraph", "Thief", "Labyrinth3DAntiAliasing", "Chart3dGraph", "PaintToolHelper",
					"TreeFractal", "PointGraph", "BasicAudioPlayerWithControlLauncher", "DataframeUtils",
					"PdfController", "Music", "XMLExtractor", "RoundMazeModel", "ContestApplicationController",
					"PlayerColor", "PacmanModel$1", "DelaunayTopology", "DataframeBuilder", "TracerouteScanner",
					"EditSongController", "PaintViewUtils", "PlayerControlView$CurrentTimeListener", "SolitaireModel",
					"PdfUtils$1", "ContestQuestionAnswer", "DotsModel", "HistogramGraph", "GraphAlgorithms",
					"MadTopology", "ResourceType", "TronLauncher", "MusicReader", "PlayerControlView", "MadTriangle",
					"Square2048Model", "PaintEditUtils", "EraserTool", "GraphModel", "CircleTopology", "CatanResource",
					"PointsExample", "FernFractal", "PencilTool", "SelectFreeTool", "ImageCrackerApp", "MusicOrganizer",
					"PaintController", "BucketTool", "PingTraceRoute", "WorldMapGraph2", "PhotoViewer",
					"GraphModelAlgorithms", "PopulacionalPyramidExample", "FilesComparator", "PhotoViewer$1",
					"AreaTool", "EthicalHackController", "SettlePoint", "FXMLConstants", "ShellFractal", "BrushTool",
					"ImageLoader", "DrawOnPoint", "TreeTopology", "Road", "AutoCompleteTextField", "DragContext",
					"UserChart", "TimelineExample", "BorderTool", "SlidingPuzzleModel", "Linha", "SprayTool",
					"NetworkTopology", "PaintFileUtils", "TableVisualizationExampleApp", "EllipseTool", "SVGCreator",
					"RotateTool", "ExcelService", "GhostColor", "MultilineExample"), "fxtests");
            String tests = displayTestsToBeRun.stream().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

}
