package exercise;

import static language.FXTesting.measureTime;

import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import org.junit.Test;
import utils.HasLogging;


public class DataframeMLTest implements HasLogging {





    @Test
    public void testTransformOneValue() {
        DataframeBuilder b = DataframeML.builder("california_housing_train.csv");
        DataframeML x = measureTime("DataframeML.build", b::build);
        measureTime("DataframeML.describe", x::describe);
        measureTime("DataframeML.toString", x::toString);
        measureTime("DataframeML.cols", x::cols);
        measureTime("DataframeML.correlation", x::correlation);
        measureTime("DataframeML.histogram", () -> x.histogram("population", 10));
        measureTime("DataframeML.histogram", () -> x.trim("population", 10));
        measureTime("DataframeML.apply", () -> x.apply("population", s -> s));
	}
}
