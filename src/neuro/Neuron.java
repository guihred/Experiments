package neuro;

import java.util.Collection;
import java.util.Map;

public interface Neuron {
	// The current bias this neuron
	Double getBiasValue();

	int getId();

	// 'The current output this neuron
	Double getOutputValue();

	void setOutputValue(Double outputValue);

	// 'The current delta value this neuron
	Double getDeltaValue();

	// 'A list of neurons to which this neuron is connected
	Collection<Neuron> getForwardConnections();

	// 'Gets a list of neurons connected to this neuron
	Map<Neuron, Double> getInputs();

	// 'Gets or sets the strategy of this neuron
	NeuronStrategy getStrategy();

	// 'Method to update the output of a neuron
	void updateOutput();

	// 'Method to find new delta value
	void updateDelta(Double errorFactor);

	// 'Method to update free parameters
	void updateFreeParams();

	void setStrategy(NeuronStrategy strategy);

}