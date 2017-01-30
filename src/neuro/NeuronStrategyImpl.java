package neuro;

import java.util.Map;
import java.util.Map.Entry;

class NeuronStrategyImpl implements NeuronStrategy {
	private static final double LEARNING_RATE = 0.5;

	@Override
	public Double findDelta(Double output, Double errorFactor) {
		return output * (1 - output) * errorFactor;

	}

	@Override
	public Double activation(Double value) {
		return 1 / (1 + Math.exp(value * -1));
	}

	@Override
	public Double findNetValue(Map<Neuron, Double> inputs, Double bias) {
		return inputs.entrySet().stream().reduce(bias,
				(soma, nur) -> soma + nur.getValue() * nur.getKey().getOutputValue(), (a, b) -> a + b);
	}

	@Override
	public void updateWeights(Map<Neuron, Double> connections, Double delta) {
		for (Entry<Neuron, Double> entry : connections.entrySet()) {
			connections.replace(entry.getKey(), entry.getValue() + LEARNING_RATE * entry.getKey().getOutputValue() * delta);
		}
	}

	@Override
	public Double findNewBias(Double bias, Double delta) {
		return bias + LEARNING_RATE * 1 * delta;
	}
}