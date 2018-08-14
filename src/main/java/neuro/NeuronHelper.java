package neuro;

import java.util.ArrayList;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;

import simplebuilder.HasLogging;

public class NeuronHelper {

	public void trains() {
        Logger log = HasLogging.log();

        double i = 0;
		
		ImmutableMap<Double, Double> trainingData = ImmutableMap.<Double, Double> builder()
				.put(++i, i*i)
				.put(++i, i*i)
				.put(++i, i*i)
				.build();
        log.info("{}", trainingData);
		NeuralNetwork create = NetworkFactory.getInstance().create(3L, 3L, 3L);
		create.trainNetwork(trainingData);
		create.trainNetwork(trainingData);
		create.trainNetwork(trainingData);

		create.getLayers().forEach(l -> l.getNeurons().forEach(n -> n.getInputs().forEach(
                (a, b) -> log.info("{}->\t{}->\t{}", a.getId(), b * n.getBiasValue(), n.getId()))));

        log.info("{}", create.runNetwork(new ArrayList<>(trainingData.keySet())));

	}

	public static void main(String[] args) {
		new NeuronHelper().trains();
	}
}