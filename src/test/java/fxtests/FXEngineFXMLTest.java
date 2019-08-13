package fxtests;

import static utils.PredicateEx.makeTest;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import gaming.ex01.SnakeLauncher;
import gaming.ex04.TronLauncher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.util.WaitForAsyncUtils;
import schema.sngpc.FXMLCreator;
import utils.HasLogging;

public final class FXEngineFXMLTest {

    private static final Logger LOG = HasLogging.log();

    @Test
    public void test() {
        List<Class<? extends Application>> classes = Arrays.asList(
            cubesystem.SphereSystemApp.class,
//            fxpro.ch04.ReversiMain.class, fxpro.ch04.ReversiPieceTest.class, fxpro.ch07.Chart3dDemo.class,
//            fxpro.ch08.MediaPlayerExample.class, fxsamples.bounds.BoundsPlayground.class,
//            gaming.ex03.SlidingPuzzleLauncher.class, gaming.ex06.QuartoLauncher.class, gaming.ex07.MazeLauncher.class,
//            gaming.ex10.MinesweeperLauncher.class, gaming.ex11.DotsLauncher.class, gaming.ex13.SolitaireLauncher.class,
//            gaming.ex14.PacmanLauncher.class, gaming.ex15.NumberDisplayLauncher.class,
//            gaming.ex15.RubiksCubeLauncher.class, gaming.ex17.PuzzleLauncher.class, gaming.ex19.SudokuLauncher.class,
//            gaming.ex21.CatanApp.class, graphs.app.GraphModelLauncher.class, labyrinth.Labyrinth3D.class,
//            labyrinth.Labyrinth3DAntiAliasing.class, labyrinth.Labyrinth3DCollisions.class,
//            labyrinth.Labyrinth3DGhosts.class, labyrinth.Labyrinth3DKillerGhosts.class,
//            labyrinth.Labyrinth3DKillerGhostsAndBalls.class, labyrinth.Labyrinth3DMouseControl.class,
//            labyrinth.Labyrinth3DWallTexture.class, ml.PopulacionalPyramidExample.class, ml.TimelineExample.class,
//            ml.WordSearchApp.class, ml.WordSuggetionApp.class, ml.WorldMapExample.class, ml.WorldMapExample2.class,
//            ml.WorldMapExample3.class, ml.graph.Chart3dGraph.class,
            paintexp.PaintMain.class
//            ,paintexp.svgcreator.SVGCreator.class
        );
        List<Class<?>> testApplications = FXMLCreator.testApplications(classes, false);
        WaitForAsyncUtils.waitForFxEvents();
        if (!testApplications.isEmpty()) {
            LOG.error("classes {} with errors", testApplications);
        } else {
            LOG.info("All classes successfull");
        }
    }

//    @Test
    public void testAllClasses() {
        List<Class<? extends Application>> classes = getClasses();
        classes.remove(SnakeLauncher.class);
        classes.remove(TronLauncher.class);
        List<Class<?>> testApplications = FXMLCreator.testApplications(classes);
        WaitForAsyncUtils.waitForFxEvents();
        if (!testApplications.isEmpty()) {
            LOG.error("classes {} with errors", testApplications);
        } else {
            LOG.info("All classes successfull");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Class<? extends Application>> getClasses() {
        List<Class<? extends Application>> appClass = new ArrayList<>();
        try {
            List<String> asList = Arrays.asList("javafx.", "org.", "com.");
            ClassPath from = ClassPath.from(FXEngineFXMLTest.class.getClassLoader());
            ImmutableSet<ClassInfo> topLevelClasses = from.getTopLevelClasses();
            topLevelClasses.stream()
                .filter(e -> asList.stream().noneMatch(p -> e.getName().contains(p)))
                .filter(makeTest(e -> Application.class.isAssignableFrom(e.load())))
                .map(ClassInfo::load)
                .map(e -> (Class<? extends Application>) e)
                .forEach(appClass::add);
        } catch (Exception e) {
            LOG.error("", e);
        }
        return appClass;
    }

}
