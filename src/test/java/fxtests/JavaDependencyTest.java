package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.app.JavaFileDependency;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import utils.HasLogging;

@SuppressWarnings("static-method")
public class JavaDependencyTest {
    private static final Logger LOG = HasLogging.log();

    @Test
    public void testJavaDependency() {

        measureTime("JavaFileDependency.displayTestsToBeRun", () -> {
            Set<String> displayTestsToBeRun = JavaFileDependency
                .displayTestsToBeRun(Arrays.asList("MusicHandler", "Concurso", "ContestApplicationController",
                    "IadesCrawler", "IadesHelper", "QuestionPosition", "VigenereXORCipher", "Cidade",
                    "CrawlerCandidates2018Task", "CrawlerCompleteCandidateTask", "EthicalHackApp", "TracerouteScanner",
                    "SSHSessionApp", "FilesComparatorHelper", "Music", "PdfImage", "PdfUtils", "WordService",
                    "FernFractal", "FractalApp", "LeafFractalApp", "OrganicTreeFractal", "PolygonFractal",
                    "ShellFractal", "SnowflakeFractal", "TreeFractal", "CrawlerFuriganaTask", "PongLauncher",
                    "TableVisualizationExampleApp", "PlayerControlView", "SongModel", "DraggingRectangle",
                    "BoundsPlayground", "FormValidation", "SnakeLauncher", "SnakeModel", "SnakeSquare",
                    "SlidingPuzzleModel", "TronLauncher", "TronModel", "TronSquare", "TetrisLauncher", "MazeSquare",
                    "DotsHelper", "DotsModel", "DotsSquare", "GhostColor", "PacmanGhost", "PacmanModel",
                    "RubiksCubeFaces", "RubiksCubeLauncher", "RubiksKeyboard", "RubiksModel", "RubiksPiece",
                    "MadEdgeDistance", "MadTopology", "MadTriangle", "PuzzlePath", "SudokuModel", "CatanCard",
                    "CatanModel", "CatanResource", "Deal", "ResourceType", "Terrain", "UserChart", "GraphAlgorithms",
                    "Vertex", "CircleLayout", "ConcentricLayout", "JavaFileDependency", "LayerLayout", "LayerSplitter",
                    "RandomLayout", "Linha", "CommomLabyrinth", "GhostGenerator", "Labyrinth2D", "Labyrinth3D",
                    "Labyrinth3DAntiAliasing", "Labyrinth3DCollisions", "Labyrinth3DKillerGhosts",
                    "MovimentacaoAleatoria", "HeatGraphExample", "HistogramExample", "MultilineExample",
                    "PieGraphExample", "PointsExample", "PopulacionalPyramidExample", "RegressionChartExample",
                    "WorldMapExample2", "WorldMapExample3", "DataframeBuilder", "DataframeML",
                    "DataframeStatisticAccumulator", "HistogramGraph", "MultiLineGraph", "PieGraph",
                    "PopulacionalGraph", "TimelineGraph"));
            String tests = displayTestsToBeRun.stream().collect(Collectors.joining(",*"));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

}
