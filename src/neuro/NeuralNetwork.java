package neuro;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface NeuralNetwork {


//    'Method to train a network     
	void trainNetwork(Map<Double, Double> trainingData);
//    'This function can be used for connecting two neurons together 
    void connectNeurons(Neuron source , 
    		Neuron destination , Double weight );
//    'This function can be used for connecting 
//    'two neurons together with random weight 
    void connectNeurons(Neuron source ,
    		Neuron destination);
//    'This function can be used for connecting neurons 
//    'in two layers together with random weights 
    void connectLayers(NeuronLayer layer1 , 
    		NeuronLayer layer2 );
//    'This function can be used for connecting all 
//    'neurons in all layers together 
    void connectLayers();
//    'This function may be used for running the network 
    List<Double> runNetwork(List<Double> inputs ); 
//    'This function may be used to obtain the output list 
    List<Double> getOutput();
    Collection<NeuronLayer> getLayers() ;
//    'Gets the first (input) layer
    NeuronLayer getInputLayer() ;
//    'Gets the last (output) layer
    NeuronLayer getOutputLayer() ;
}