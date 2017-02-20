package neuro;

import java.util.Map;

public interface NeuronStrategy {

	Double findDelta(Double outputValue, Double errorFactor);

	void updateWeights(Map<Neuron, Double> inputs, Double deltaValue);

	Double findNewBias(Double biasValue, Double deltaValue);

	Double activation(Double netValue);

	Double findNetValue(Map<Neuron, Double> inputs, Double biasValue);

}