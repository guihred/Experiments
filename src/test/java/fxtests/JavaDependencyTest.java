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
			Set<String> displayTestsToBeRun = JavaFileDependency.displayTestsToBeRun(Arrays.asList("EditSongHelper",
					"FilesComparator", "MusicOrganizer", "ContestApplicationController", "VigenereXORCipher",
					"CandidatoHelper", "CrawlerCandidates2018Task", "AlarmClock", "EthicalHackApp", "ImageCrackerApp",
					"NetworkInformationScanner", "PingTraceRoute", "PortScanner", "PortServices", "ProcessScan",
					"TracerouteScanner", "BaseTestSupport", "CommandExecutionHelper", "CommonTestSupportUtils",
					"SSHSessionApp", "Chapter4", "Ch4", "Point", "Rectangle", "Shape", "FilesComparatorHelper", "Music",
					"PdfImage", "SongUtils", "UnRar", "WikiImagesUtils", "WordService", "FernFractal", "LeafFractalApp",
					"OrganicTreeFractal", "PolygonFractal", "ShellFractal", "SnowflakeFractal", "TreeFractal",
					"TableVisualizationExampleApp", "TableVisualizationModel", "ResponsiveUIView",
					"BasicAudioPlayerWithControlLauncher", "AnchorCircle", "JewelViewer", "LookNFeelChooser",
					"MoleculeSampleApp", "BoundsPlayground", "FormValidation", "SlidingPuzzleModel", "DotsModel",
					"Leopard", "MadCell", "MadEdge", "MadEdgeDistance", "MadTopology", "MadTriangle", "CatanCard",
					"CatanLogger", "CatanModel", "CatanResource", "EdgeElement", "GraphAlgorithms", "JavaExercise19",
					"LayerLayout", "LayerSplitter", "RandomLayout", "EdgeDistancePack", "Linha", "ImageLoading",
					"JapaneseAudio", "CommomLabyrinth", "Labyrinth2D", "Labyrinth3D", "HeatGraphExample",
					"HistogramExample", "MapCallback", "MultilineExample", "PieGraphExample", "PointsExample",
					"PopulacionalPyramidExample", "RegressionChartExample", "TimelineExample", "WorldMapExample",
					"WorldMapExample2", "WorldMapExample3", "DataframeBuilder", "DataframeML", "DataframeUtils",
					"DecisionNode", "DecisionTree", "Chart3dGraph", "ColorPattern", "HeatGraph", "HistogramGraph",
					"MapGraph", "MultiLineGraph", "PieGraph", "PointGraph", "PopulacionalGraph", "TimelineGraph",
					"WorldMapGraph", "WorldMapGraph2", "EWSTest", "StatsLogAccess", "ColorChooser",
					"ColorChooserController", "PaintFileUtils", "SimplePixelReader", "PictureOption", "Speaker",
					"LeitorArquivos", "FXMLCreator", "SimpleListViewBuilder", "SimplePaneBuilder", "SimplePathBuilder",
					"SimpleToggleGroupBuilder", "CommonsFX", "CrawlerTask", "ImageFXUtils", "MouseInScreenHandler",
					"StringSigaUtils", "SupplierEx", "ZoomableScrollPane"));
			String tests = displayTestsToBeRun.stream().collect(Collectors.joining(",*"));
			LOG.info("TestsToBeRun ={}", tests);
		});
	}

}
