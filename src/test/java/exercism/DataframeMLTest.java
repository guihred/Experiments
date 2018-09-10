package exercism;

import static crypt.FXTesting.measureTime;

import japstudy.JapaneseLessonReader;
import ml.DataframeBuilder;
import ml.DataframeML;
import org.junit.Test;


public class DataframeMLTest {

	@Test
    public void testJapaneseLessonReader() {
        measureTime("JapaneseLessonReader.getLessons", () -> JapaneseLessonReader.getLessons("jaftranscript.docx"));
    }

    //    @Test
    public void testTransformOneValue() {
        DataframeBuilder b = DataframeML.builder("california_housing_train.csv");
        DataframeML x = measureTime("DataframeML.build", () -> b.build());
        measureTime("DataframeML.describe", () -> x.describe());
        measureTime("DataframeML.toString", () -> x.toString());
        measureTime("DataframeML.cols", () -> x.cols());
        measureTime("DataframeML.correlation", () -> x.correlation());
        measureTime("DataframeML.histogram", () -> x.histogram("population", 10));
        measureTime("DataframeML.histogram", () -> x.trim("population", 10));
        measureTime("DataframeML.apply", () -> x.apply("population", s -> s));
	}
}
