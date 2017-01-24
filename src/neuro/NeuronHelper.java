package neuro;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;

public class NeuronHelper {

	public void trains() {
		double i=0;
		
		ImmutableMap<Double, Double> trainingData = ImmutableMap.<Double, Double> builder()
				.put(++i, i*i)
				.put(++i, i*i)
				.put(++i, i*i)
				.build();
		System.out.println(trainingData);
		NeuralNetwork create = NetworkFactory.getInstance().create(3L, 3L, 3L);
		create.trainNetwork(trainingData);
		create.trainNetwork(trainingData);
		create.trainNetwork(trainingData);

		create.getLayers().forEach(l -> {
			l.getNeurons().forEach(n -> {
				n.getInputs().forEach((a, b) -> {
					System.out.println(a.getId() + "->\t" + b * n.getBiasValue() + "->\t" + n.getId());

				});
			});
		});

		System.out.println(create.runNetwork(new ArrayList<>(trainingData.keySet())));

	}

	public static void main(String[] args) {
		new NeuronHelper().trains();
	}
}