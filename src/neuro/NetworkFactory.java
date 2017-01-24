package neuro;

public abstract class NetworkFactory {
	static NetworkFactory getInstance() {
		return new NetworkFactoryImpl();
	}

	abstract NeuralNetwork create(Long... inputneurons);

	abstract NeuralNetwork createNetwork(Long inputneurons, Long outputneurons);
}