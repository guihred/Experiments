package neuro;

import java.util.*;

public class NeuronImpl implements Neuron {
	private NeuronStrategy strategy;
	private static int count = 0;
	private int id = 0;
	private double biasValue = new Random().nextDouble();
	private double outputValue;
	private double deltaValue;
	private Map<Neuron, Double> neuronConnections = new HashMap<>();
	private Collection<Neuron> forwardConnections = new ArrayList<>();

	public NeuronImpl() {
		id = ++count;
	}

	public NeuronImpl(NeuronStrategy strategy) {
		id = ++count;
		this.strategy = strategy;
	}

	@Override
	public Double getBiasValue() {
		return biasValue;
	}

	@Override
	public Double getOutputValue() {
		return outputValue;
	}

	@Override
	public Double getDeltaValue() {
		return deltaValue;
	}

	@Override
	public Collection<Neuron> getForwardConnections() {
		return forwardConnections;
	}

	@Override
	public Map<Neuron, Double> getInputs() {
		return neuronConnections;
	}

	@Override
	public NeuronStrategy getStrategy() {
		return strategy;
	}

	@Override
	public void updateOutput() {
		double netValue = strategy.findNetValue(neuronConnections, biasValue);
		outputValue = strategy.activation(netValue);
	}

	@Override
	public String toString() {
		return "Neuron [id=" + id + ", biasValue=" + biasValue + ", outputValue=" + outputValue + ", deltaValue=" + deltaValue
				+ ", neuronConnections="
				+ neuronConnections.values() + "]";
	}

	@Override
	public void updateDelta(Double errorFactor) {
		deltaValue = strategy.findDelta(outputValue, errorFactor);
	}

	@Override
	public void updateFreeParams() {
		biasValue = strategy.findNewBias(biasValue, deltaValue);
		strategy.updateWeights(getInputs(), deltaValue);

	}

	@Override
	public void setOutputValue(Double outputValue) {
		this.outputValue = outputValue;
	}

	@Override
	public void setStrategy(NeuronStrategy strategy) {
		this.strategy = strategy;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
