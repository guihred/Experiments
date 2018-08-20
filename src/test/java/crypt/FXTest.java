package crypt;

import contest.db.ContestApplication;
import contest.db.ContestQuestionEditingDisplay;
import election.experiment.ElectionCrawlerApp;
import furigana.experiment.FuriganaCrawlerApp;
import fxproexercises.ch01.AudioConfigLauncher;
import fxproexercises.ch01.EarthriseChristmasApp;
import fxproexercises.ch01.HelloWorldApp;
import fxproexercises.ch02.CSSStylingExample;
import fxproexercises.ch02.PathTransitionExample;
import fxproexercises.ch02.PendulumAnimationLauncher;
import fxproexercises.ch02.PongLauncher;
import fxproexercises.ch02.StageControlExample;
import fxproexercises.ch04.CenterUsingBind;
import fxproexercises.ch04.CenterUsingStack;
import fxproexercises.ch04.ReversiMain;
import fxproexercises.ch04.ReversiPieceTest;
import fxproexercises.ch04.ReversiSquareTest;
import fxproexercises.ch06.ResponsiveUIApp;
import fxproexercises.ch06.TaskProgressApp;
import fxproexercises.ch06.ThreadInformationApp;
import fxproexercises.ch07.AreaChartExample;
import fxproexercises.ch07.BarChartExample;
import fxproexercises.ch07.BubbleChartExample;
import fxproexercises.ch07.Chart3dDemo;
import fxproexercises.ch07.Chart3dSampleApp;
import fxproexercises.ch07.LineChartExample;
import fxproexercises.ch07.PieChartExample;
import fxproexercises.ch07.ScatterChartWithFillExample;
import fxproexercises.ch08.MediaPlayerExample;
import fxproexercises.ch08.SimpleAudioPlayerLauncher;
import fxproexercises.ch10.VanishingCirclesApp;
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
import javaexercises.graphs.GraphModelLauncher;
import ml.*;
import mp3Audio.OrganizadorMusicas;
import org.junit.Test;
import rosario.RosarioComparadorArquivos;
import sample.cubesystem.DeathStar;
import sample.cubesystem.GolfBall;
import sample.cubesystem.SphereSystemApp;
import simplebuilder.HasLogging;
import xylophone.XylophoneApp;

public final class FXTest implements HasLogging {
    @Test
    public void test() throws Throwable {
        FXTesting.testApps(AreaChartExample.class, AudioConfigLauncher.class, BackgroundProcesses.class,
                BarChartExample.class, BoundsPlayground.class, BubbleChartExample.class, CenterUsingBind.class,
                CenterUsingStack.class, ChangingTextFonts.class, Chart3dDemo.class, Chart3dGraph.class,
                Chart3dSampleApp.class, ContestQuestionEditingDisplay.class, CSSStylingExample.class, Cubes3D.class,
                DeathStar.class, DotsLauncher.class, DrawingColors.class, DrawingLines.class, DrawingShape.class,
                DrawingText.class, EarthriseChristmasApp.class, ElectionCrawlerApp.class, FormValidation.class,
                FuriganaCrawlerApp.class, GlobeSphereApp.class, GolfBall.class, GraphModelLauncher.class,
                GridPaneForm.class, HeatGraphExample.class, HelloWorldApp.class, HistogramExample.class,
                InlineModelViewer.class, LineChartExample.class, LineManipulator.class, LookNFeelChooser.class,
                MadMazeLauncher.class, ArkanoidLauncher.class);
    }

    @Test
    public void test2() throws Throwable {
        FXTesting.testApps(MapGraph.class, Maze3DLauncher.class,
                MazeLauncher.class, MediaPlayerExample.class, MemoryLauncher.class, MinesweeperLauncher.class,
                MoleculeSampleApp.class, MultilineExample.class, NumberDisplayLauncher.class, NumberPad.class,
                OrganizadorMusicas.class, PacmanLauncher.class, PathTransitionExample.class,
                PendulumAnimationLauncher.class, PersonTableController.class, PhotoViewer.class, 
                PieChartExample.class, PlatformMain.class, PlayingAudio.class, PointsExample.class, PongLauncher.class,
                PopulacionalPyramidExample.class, PuzzleLauncher.class, QuartoLauncher.class, RaspiCycle.class,
                RegressionChartExample.class, ResponsiveUIApp.class, ReversiMain.class, ReversiPieceTest.class,
                ReversiSquareTest.class, RosarioComparadorArquivos.class, RubiksCubeLauncher.class, SandboxFX.class);
    }

    @Test
    public void test3() throws Throwable {
        FXTesting.testApps(ContestApplication.class,
                AnimationExample.class, XylophoneApp.class, ScatterChartWithFillExample.class, Shapes3DTexture.class,
                Simple3DBoxApp.class, SimpleAudioPlayerLauncher.class, SimpleScene3D.class, SnakeLauncher.class,
                SlidingPuzzleLauncher.class, SolitaireLauncher.class, SphereSystemApp.class, WorkingWithTableView.class,
                Square2048Launcher.class, StageControlExample.class, TaskProgressApp.class, TetrisLauncher.class,
                TheMenuGrid.class, ThreadInformationApp.class, TimelineExample.class, TriangleMeshes.class,
                TronLauncher.class, VanishingCirclesApp.class, WordSearchApp.class, WordSuggetionApp.class,
                WorkingListsViews.class, WorldMapExample.class, WorldMapExample2.class, WorldMapExample3.class);

    }
}
