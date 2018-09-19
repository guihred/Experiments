package exercism;

import static crypt.FXTesting.measureTime;

import java.io.File;
import ml.DataframeBuilder;
import ml.DataframeML;
import ml.Word2VecExample;
import org.junit.Test;
import simplebuilder.HasLogging;


public class DataframeMLTest implements HasLogging {



    @Test
    public void testWord2Vec() {
        File file = new File(Word2VecExample.PATH_TO_SAVE_MODEL_TXT);
        if (file.exists()) {
            boolean delete = file.delete();
            getLogger().info("File deleted {}", delete);
        }
        measureTime("Word2VecExample.createWord2Vec", Word2VecExample::createWord2Vec);
    }

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
