package fxtests;

import contest.db.ContestQuestionEditingDisplay;
import cubesystem.DeathStar;
import cubesystem.GolfBall;
import election.ElectionCrawlerApp;
import furigana.FuriganaCrawlerApp;
import fxpro.ch01.EarthriseChristmasApp;
import fxpro.ch01.HelloWorldApp;
import fxpro.ch02.CSSStylingExample;
import fxpro.ch04.CenterUsingBind;
import fxpro.ch04.CenterUsingStack;
import fxpro.ch05.TableVisualizationExampleApp;
import fxpro.ch07.Chart3dSampleApp;
import fxpro.ch07.LineChartExample;
import fxpro.earth.EarthCubeMain;
import fxsamples.*;
import fxsamples.bounds.BoundsPlayground;
import fxsamples.person.FormValidation;
import gaming.ex08.ArkanoidLauncher;
import gaming.ex11.DotsLauncher;
import gaming.ex16.MadMazeLauncher;
import graphs.app.GraphModelLauncher;
import ml.HeatGraphExample;
import ml.HistogramExample;
import ml.PieGraphExample;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import schema.sngpc.FXMLCreator;

public final class FXEngineFXMLTest {

    @Test
    public void test() throws Throwable {
        FXMLCreator.testApplications(BackgroundProcesses.class, PieGraphExample.class,
            BoundsPlayground.class, CenterUsingBind.class, CenterUsingStack.class, ChangingTextFonts.class,
            Chart3dSampleApp.class, ContestQuestionEditingDisplay.class, CSSStylingExample.class, Cubes3D.class,
            DeathStar.class, DotsLauncher.class, DrawingColors.class, DrawingLines.class, DrawingShape.class,
            DrawingText.class, EarthriseChristmasApp.class, ElectionCrawlerApp.class, FormValidation.class,
            FuriganaCrawlerApp.class, GlobeSphereApp.class, GolfBall.class, GraphModelLauncher.class,
            GridPaneForm.class, HeatGraphExample.class, HelloWorldApp.class, HistogramExample.class,
            EarthCubeMain.class, InlineModelViewer.class, LineChartExample.class, LineManipulator.class,
            LookNFeelChooser.class, TableVisualizationExampleApp.class, MadMazeLauncher.class, ArkanoidLauncher.class);
        WaitForAsyncUtils.waitForFxEvents();
    }


}
