package neuro;

public interface NetworkFactory {
	static NetworkFactory getInstance() {
		return new NetworkFactoryImpl();
	}

	NeuralNetwork create(Long... inputneurons);

	NeuralNetwork createNetwork(Long inputneurons, Long outputneurons);
}