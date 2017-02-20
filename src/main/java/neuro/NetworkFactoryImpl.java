package neuro;

import java.util.ArrayList;

class NetworkFactoryImpl implements NetworkFactory {

	@Override
	public NeuralNetwork create(Long... inputneurons) {
		NeuralNetworkImpl neuralNetwork = new NeuralNetworkImpl();
		neuralNetwork.setNeuronLayers(new ArrayList<NeuronLayer>());
		for (Long neuronNumber : inputneurons) {
			NeuronLayer neuronLayer = new NeuronLayer();
			neuronLayer.setNeurons(new ArrayList<Neuron>());
			NeuronStrategy strategy = new NeuronStrategyImpl();
			for (long i = 0; i < neuronNumber; i++) {
				neuronLayer.getNeurons().add(new NeuronImpl(strategy));
			}
			neuralNetwork.getNeuronLayers().add(neuronLayer);
		}
		neuralNetwork.connectLayers();

		return neuralNetwork;
	}

	@Override
	public NeuralNetwork createNetwork(Long inputneurons, Long outputneurons) {
		return create(inputneurons, inputneurons, outputneurons);

	}

}