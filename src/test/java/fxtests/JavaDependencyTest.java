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
                "MovimentacaoAleatoria", "GhostGenerator", "ConcentricLayout", "JavaFileDependency", "LineTool",
                "Chapter4", "CircleLayout", "PaintImageUtils", "CustomLayout", "PolygonTool", "RandomLayout",
                "TextTool", "Labyrinth3DMouseControl", "PaintModel", "CurveTool", "BaseTopology", "PictureTool",
                "Labyrinth3DKillerGhostsAndBalls", "StageHelper", "Chapter4", "Labyrinth3DWallTexture",
                "PlayerControlView", "LayerSplitter", "Labyrinth3DGhosts", "WandTool", "RandomTopology", "MusicHandler",
                "ProjectTopology", "Labyrinth2D", "SnakeLauncher", "PaintTool", "PackageTopology", "RectangleTool",
                "SudokuModel", "BlurTool", "ConsoleUtils", "Labyrinth3DCollisions", "AreaTool",
                "Labyrinth3DKillerGhosts", "LayerLayout", "GraphModelLauncher", "CommomLabyrinth", "Labyrinth3D",
                "RectBuilder", "Labyrinth3DAntiAliasing", "TracerouteScanner", "PaintViewUtils", "PlayerControlView",
                "DotsModel", "TronLauncher", "PlayerControlView", "PaintEditUtils", "EraserTool", "GraphModel",
                "CircleTopology", "PencilTool", "PaintController", "BucketTool", "GraphModelAlgorithms", "AreaTool",
                "EthicalHackController", "BrushTool", "DrawOnPoint", "UserChart", "BorderTool", "SlidingPuzzleModel",
                "SprayTool", "NetworkTopology", "PaintFileUtils", "EllipseTool", "SVGCreator"), "fxtests");
            String tests = displayTestsToBeRun.stream().collect(Collectors.joining(",*", "*", ""));
            LOG.info("TestsToBeRun ={}", tests);
        });
    }

}
