package neuro;

import java.util.ArrayList;
import java.util.List;

public class NeuronLayer {

	private List<Neuron> neurons = new ArrayList<>();

	public List<Neuron> getNeurons() {
		return neurons;
	}

	public void setNeurons(List<Neuron> neurons) {
		this.neurons = neurons;
	}

}