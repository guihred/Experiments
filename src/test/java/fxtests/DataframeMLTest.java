package fxtests;

import static fxtests.FXTesting.measureTime;

import java.util.Map;
import ml.data.DataframeBuilder;
import ml.data.DataframeML;
import ml.data.DataframeStatisticAccumulator;
import ml.data.DataframeUtils;
import org.junit.Test;
import org.nd4j.linalg.io.Assert;


@SuppressWarnings("static-method")
public class DataframeMLTest {

    @Test
    public void testTransformOneValue() {
        DataframeBuilder b = DataframeBuilder.builder("california_housing_train.csv");
		DataframeML x = measureTime("DataframeML.build", () -> b.build());
		measureTime("DataframeML.describe", () -> DataframeUtils.describe(x));
		measureTime("DataframeML.toString", () -> DataframeUtils.toString(x));
        measureTime("DataframeML.cols", x::cols);
        measureTime("DataframeML.correlation", () -> DataframeUtils.displayCorrelation(x));
        measureTime("DataframeML.trim", () -> DataframeUtils.trim("population", 10, x));
        measureTime("DataframeML.apply", () -> x.apply("population", s -> s));
        measureTime("DataframeML.createNumberEntries",
				() -> DataframeStatisticAccumulator.createNumberEntries(x.getDataframe(), x.getSize(), "longitude",
						"latitude"));
        DataframeBuilder b2 = DataframeBuilder.builder("cities.csv");
        measureTime("DataframeML.displayStats", () -> DataframeUtils.displayStats(b2.build()));
		Map<Double, Long> histogram = measureTime("DataframeML.histogram",
				() -> DataframeUtils.histogram(x, "population", 10));
		Assert.notNull(histogram, "Must not be null");
	}
}
