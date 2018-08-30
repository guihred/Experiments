package neuro;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;

class NeuralNetworkImpl implements NeuralNetwork {

	private List<NeuronLayer> neuronLayers;

	@Override
	public void trainNetwork(Map<Double, Double> trainingData) {
		int i =0;
		for (Entry<Double, Double> inputI : trainingData.entrySet()) {
			if (i < getInputLayer().getNeurons().size()) {
				Neuron someNeuron = getInputLayer().getNeurons().get(i);
				someNeuron.setOutputValue(inputI.getKey());
				i++;
			}
		}
//		'Step1: Find the output of hidden layer 
//		'neurons and output layer neurons

		for (NeuronLayer nl : getNeuronLayers()) {
			for (Neuron someNeuron : nl.getNeurons()) {
				someNeuron.updateOutput();
			}
		}
		// 'Step2: Finding Delta
		//
		// '2.1) Find the delta (error rate) of output layer

		i = 0;
		for (Entry<Double, Double> outputI : trainingData.entrySet()) {
			Neuron someNeuron;
			if (i < getOutputLayer().getNeurons().size()) {
				someNeuron = getOutputLayer().getNeurons().get(i);
				someNeuron.updateDelta(outputI.getValue() - someNeuron.getOutputValue());
				i++;
			}
		}
		// '2.2) Calculate delta of all the hidden layers, backwards

		for (int j = getNeuronLayers().size() - 2; j > 0; j--) {
			NeuronLayer currentLayer = getNeuronLayers().get(j);
			for (Neuron someNeuron : currentLayer.getNeurons()) {
				Double errorFactor = 0D;
				Collection<Neuron> forwardConnections = someNeuron.getForwardConnections();
				for (Neuron connectedNeuron : forwardConnections) {
					// 'Sum up all the delta * weight
					errorFactor += connectedNeuron.getDeltaValue() * connectedNeuron.getInputs().get(someNeuron);

				}
				someNeuron.updateDelta(errorFactor);
			}
		}
		for (int j = 1; j < getNeuronLayers().size(); j++) {
			NeuronLayer neuronLayer = getNeuronLayers().get(j);
			List<Neuron> b = neuronLayer.getNeurons();
			for (Neuron someNeuron : b) {
				someNeuron.updateFreeParams();
			}
		}
		
		
	}

	@Override
	public void connectNeurons(Neuron source, Neuron destination, Double weight) {
		destination.getInputs().put(source, weight);
		source.getForwardConnections().add(destination);
	}

	@Override
	public void connectNeurons(Neuron source, Neuron destination) {
		connectNeurons(source, destination, new Random().nextDouble());
	}

	@Override
	public void connectLayers(NeuronLayer layer1, NeuronLayer layer2) {
		List<Neuron> a = layer1.getNeurons();
		List<Neuron> b = layer2.getNeurons();
		for (Neuron inputNeuron : a) {
			for (Neuron targetNeuron : b) {
				connectNeurons(inputNeuron, targetNeuron);
			}
		}
	}

	@Override
	public void connectLayers() {
		for (int i = 1; i < getNeuronLayers().size(); i++) {
			connectLayers(getNeuronLayers().get(i - 1), getNeuronLayers().get(i));
		}

	}

	@Override
	public List<Double> runNetwork(List<Double> inputs) {
		int i=0;
		for (Neuron someNeuron : getInputLayer().getNeurons()) {
			if (i < getInputLayer().getNeurons().size()) {
                //                someNeuron = getInputLayer().getNeurons().get(i);
				Double inputValue = inputs.get(i);
                someNeuron.setOutputValue(inputValue);
			}
		}
//		'Step1: Find the output of each hidden neuron layer
		for (NeuronLayer neuronLayer : getNeuronLayers()) {
			for (Neuron someNeuron : neuronLayer.getNeurons()) {
				someNeuron.updateOutput();
			}
		}
		
		return getOutput();
	}

	@Override
	public List<Double> getOutput() { 
        return getOutputLayer().getNeurons().stream().map(Neuron::getOutputValue)
				.collect(Collectors.toList());
	}

	@Override
	public Collection<NeuronLayer> getLayers() {
		return getNeuronLayers();
	}

	@Override
	public NeuronLayer getInputLayer() {
		return getNeuronLayers().get(0);
	}

	@Override
	public NeuronLayer getOutputLayer() {
		return getNeuronLayers().get(getNeuronLayers().size() - 1);
	}

	public List<NeuronLayer> getNeuronLayers() {
		return neuronLayers;
	}

	public void setNeuronLayers(List<NeuronLayer> neuronLayers) {
		this.neuronLayers = neuronLayers;
	}

}