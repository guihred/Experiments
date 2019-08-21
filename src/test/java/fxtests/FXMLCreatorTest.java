package fxtests;

import static utils.PredicateEx.makeTest;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Application;
import org.junit.Test;
import org.slf4j.Logger;
import org.testfx.util.WaitForAsyncUtils;
import schema.sngpc.FXMLCreator;
import utils.HasLogging;

@SuppressWarnings("static-method")
public final class FXMLCreatorTest {

	private static final Logger LOG = HasLogging.log();

	@Test
	public void testAllClasses() {
		List<Class<? extends Application>> classes = getClasses(Application.class);
		testApplications(classes);
	}

	@Test
	public void testErrorClasses() {

		List<Class<? extends Application>> classes = Arrays.asList(election.HibernateCrawler.class,
				fxsamples.bounds.BoundsPlayground.class);
		testApplications(classes);
	}

	@Test
	public void testErrorClasses2() {

		List<Class<? extends Application>> classes = Arrays.asList(election.HibernateCrawler.class,
				ethical.hacker.EthicalHackApp.class, fxpro.ch01.AudioConfigLauncher.class, fxpro.ch04.ReversiMain.class,
				fxpro.ch05.TableVisualizationExampleApp.class, fxpro.ch07.AreaChartExample.class,
				fxpro.ch07.BarChartExample.class, fxpro.ch07.BubbleChartExample.class,
				fxpro.ch07.LineChartExample.class, fxpro.ch07.PieChartExample.class,
				fxpro.ch07.ScatterChartExample.class, fxpro.ch07.ScatterChartWithFillExample.class,
				fxpro.ch08.BasicAudioPlayerWithControlLauncher.class, fxsamples.PlayingAudio.class,
				fxsamples.bounds.BoundsPlayground.class, fxsamples.person.PersonTableController.class,
				fxsamples.person.WorkingWithTableView.class, gaming.ex07.MazeLauncher.class, gaming.ex21.CatanApp.class,
				graphs.app.GraphModelLauncher.class, japstudy.JapaneseLessonApplication.class,
				ml.HeatGraphExample.class, ml.HistogramExample.class, ml.MultilineExample.class, ml.PointsExample.class,
				ml.RegressionChartExample.class, ml.TimelineExample.class, paintexp.PaintMain.class,
				pdfreader.PdfReader.class, schema.sngpc.SngpcViewer.class);
		testApplications(classes);
	}

	private static String classNames(List<Class<?>> testApplications) {
		return testApplications.stream().map(e -> e.getName() + ".class").collect(Collectors.joining(","));
	}

	@SuppressWarnings("unchecked")
	private static <T> List<Class<? extends T>> getClasses(Class<T> cl) {
		List<Class<? extends T>> appClass = new ArrayList<>();
		List<String> excludePackages = Arrays.asList("javafx.", "org.", "com.");
		try {
			ClassPath.from(FXMLCreatorTest.class.getClassLoader()).getTopLevelClasses().stream()
					.filter(e -> excludePackages.stream().noneMatch(p -> e.getName().contains(p)))
					.filter(makeTest(e -> cl.isAssignableFrom(e.load()))).map(ClassInfo::load)
					.map(e -> (Class<? extends T>) e).forEach(appClass::add);
		} catch (Exception e) {
			LOG.error("", e);
		}
		return appClass;
	}

	private static void testApplications(List<Class<? extends Application>> classes) {
		List<Class<?>> testApplications = FXMLCreator.testApplications(classes);
		WaitForAsyncUtils.waitForFxEvents();
		if (!testApplications.isEmpty()) {
			LOG.error("classes {}/{} got errors", testApplications.size(), classes.size());
			LOG.error("classes {} with errors", classNames(testApplications));
		} else {
			LOG.info("All classes successfull");
		}
	}

}
