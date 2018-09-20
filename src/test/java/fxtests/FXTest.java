package fxtests;

import audio.mp3.OrganizadorMusicas;
import contest.db.ContestApplication;
import contest.db.ContestQuestionEditingDisplay;
import cubesystem.DeathStar;
import cubesystem.GolfBall;
import cubesystem.SphereSystemApp;
import election.ElectionCrawlerApp;
import furigana.FuriganaCrawlerApp;
import fxpro.ch01.AudioConfigLauncher;
import fxpro.ch01.EarthriseChristmasApp;
import fxpro.ch01.HelloWorldApp;
import fxpro.ch02.*;
import fxpro.ch04.*;
import fxpro.ch05.TableVisualizationExampleApp;
import fxpro.ch06.ResponsiveUIApp;
import fxpro.ch06.TaskProgressApp;
import fxpro.ch06.ThreadInformationApp;
import fxpro.ch07.*;
import fxpro.ch08.BasicAudioClipExample;
import fxpro.ch08.BasicAudioPlayerWithControlLauncher;
import fxpro.ch08.MediaPlayerExample;
import fxpro.ch08.SimpleAudioPlayerLauncher;
import fxpro.ch10.VanishingCirclesApp;
import fxpro.earth.EarthCubeMain;
import fxsamples.*;
import gaming.ex01.SnakeLauncher;
import gaming.ex02.MemoryLauncher;
import gaming.ex03.SlidingPuzzleLauncher;
import gaming.ex04.TronLauncher;
import gaming.ex05.TetrisLauncher;
import gaming.ex06.MoleculeSampleApp;
import gaming.ex06.QuartoLauncher;
import gaming.ex07.MazeLauncher;
import gaming.ex08.ArkanoidLauncher;
import gaming.ex09.Maze3DLauncher;
import gaming.ex10.MinesweeperLauncher;
import gaming.ex11.DotsLauncher;
import gaming.ex12.PlatformMain;
import gaming.ex13.SolitaireLauncher;
import gaming.ex14.PacmanLauncher;
import gaming.ex15.NumberDisplayLauncher;
import gaming.ex15.RubiksCubeLauncher;
import gaming.ex16.MadMazeLauncher;
import gaming.ex17.PuzzleLauncher;
import gaming.ex18.Square2048Launcher;
import gaming.ex19.SudokuLauncher;
import graphs.app.GraphModelLauncher;
import labyrinth.*;
import language.FXTesting;
import ml.*;
import org.junit.Test;
import physics.Physics;
import rosario.RosarioComparadorArquivos;
import utils.HasLogging;
import xylophone.XylophoneApp;

public final class FXTest implements HasLogging {
    @Test
    public void test() throws Throwable {
        FXTesting.testApps(AreaChartExample.class, BackgroundProcesses.class, PieGraphExample.class,
                BarChartExample.class, BoundsPlayground.class, BubbleChartExample.class, CenterUsingBind.class,
                CenterUsingStack.class, ChangingTextFonts.class, Chart3dDemo.class, Chart3dGraph.class,
                Chart3dSampleApp.class, ContestQuestionEditingDisplay.class, CSSStylingExample.class, Cubes3D.class,
                DeathStar.class, DotsLauncher.class, DrawingColors.class, DrawingLines.class, DrawingShape.class,
                DrawingText.class, EarthriseChristmasApp.class, ElectionCrawlerApp.class, FormValidation.class,
                FuriganaCrawlerApp.class, GlobeSphereApp.class, GolfBall.class, GraphModelLauncher.class,
                GridPaneForm.class, HeatGraphExample.class, HelloWorldApp.class, HistogramExample.class,
                EarthCubeMain.class, InlineModelViewer.class, LineChartExample.class, LineManipulator.class,
                LookNFeelChooser.class, TableVisualizationExampleApp.class, MadMazeLauncher.class,
                ArkanoidLauncher.class);
    }

    @Test
    public void test2() throws Throwable {
        FXTesting.testApps(MapGraph.class, Maze3DLauncher.class,
                MazeLauncher.class, MediaPlayerExample.class, MemoryLauncher.class, MinesweeperLauncher.class,
                MoleculeSampleApp.class, MultilineExample.class, NumberDisplayLauncher.class, NumberPad.class,
                OrganizadorMusicas.class, PacmanLauncher.class, PathTransitionExample.class,
                PendulumAnimationLauncher.class, PersonTableController.class, PhotoViewer.class, 
                PieChartExample.class, PlatformMain.class, PointsExample.class, PongLauncher.class,
                PopulacionalPyramidExample.class, PuzzleLauncher.class, QuartoLauncher.class, RaspiCycle.class,
                RegressionChartExample.class, ResponsiveUIApp.class, ReversiMain.class, ReversiPieceTest.class,
                ReversiSquareTest.class, RosarioComparadorArquivos.class, RubiksCubeLauncher.class, SandboxFX.class,
                AlignUsingStackAndTile.class, ScatterChartExample.class, Shapes3DApp.class);
    }

    @Test
    public void test5() throws Throwable {
        FXTesting.testApps(BasicAudioPlayerWithControlLauncher.class, BasicAudioClipExample.class,
                AudioConfigLauncher.class, SimpleAudioPlayerLauncher.class, PlayingAudio.class, Physics.class,
                JewelViewer.class);
    }

    @Test
    public void test3() throws Throwable {
        FXTesting.testApps(ContestApplication.class, AnimationExample.class, XylophoneApp.class,
                ScatterChartWithFillExample.class, Shapes3DTexture.class, Simple3DBoxApp.class,
                SimpleScene3D.class, SnakeLauncher.class, SlidingPuzzleLauncher.class, SudokuLauncher.class,
                SolitaireLauncher.class, SphereSystemApp.class, WorkingWithTableView.class, Square2048Launcher.class,
                StageControlExample.class, TaskProgressApp.class, TetrisLauncher.class, TheMenuGrid.class,
                ThreadInformationApp.class, TimelineExample.class, TriangleMeshes.class, TronLauncher.class,
                VanishingCirclesApp.class, WordSearchApp.class, WordSuggetionApp.class, WorkingListsViews.class,
                WorldMapExample.class, WorldMapExample2.class, WorldMapExample3.class);
    }

    @Test
    public void test4() throws Throwable {
        FXTesting.testApps(Labyrinth2D.class, Labyrinth3D.class, Labyrinth3DAntiAliasing.class,
                Labyrinth3DCollisions.class, Labyrinth3DGhosts.class, Labyrinth3DKillerGhosts.class,
                Labyrinth3DKillerGhostsAndBalls.class, Labyrinth3DMouseControl.class, Labyrinth3DWallTexture.class);
    }
}
