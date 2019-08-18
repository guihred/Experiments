package fxtests;

import static fxtests.FXTesting.measureTime;

import graphs.GraphAlgorithms;
import graphs.Vertex;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

@SuppressWarnings("static-method")
public class GraphAlgorithmsTest {

    @Test
    public void testGraphAlgorithms() throws Exception {
        List<Vertex> vertices = measureTime("GraphAlgorithms.createGraph8",
                GraphAlgorithms::createGraph8);
        Map<Vertex, Integer> num = new HashMap<>();
        Map<Vertex, Integer> low = new HashMap<>();
        measureTime("Vertex.assignNum", () -> vertices.get(0).assignNum(num, 0));
        measureTime("Vertex.assignLow", () -> vertices.get(0).assignLow(num, low));
        measureTime("Vertex.kruskal", () -> Vertex.kruskal(vertices));
        measureTime("Vertex.prim", () -> Vertex.prim(vertices));
        measureTime("Vertex.sortTopology", () -> Vertex.sortTopology(vertices));
        measureTime("Vertex.chain", () -> Vertex.chain("A", "F", vertices));
        measureTime("Vertex.unweighted", () -> Vertex.unweighted(vertices));
        measureTime("Vertex.weightedNegative", () -> Vertex.weightedNegative(vertices));

        measureTime("GraphAlgorithms.createGraph1", GraphAlgorithms::createGraph1);
        measureTime("GraphAlgorithms.createGraph2", GraphAlgorithms::createGraph2);
        measureTime("GraphAlgorithms.createGraph3", GraphAlgorithms::createGraph3);
        measureTime("GraphAlgorithms.createGraph4", GraphAlgorithms::createGraph4);
        measureTime("GraphAlgorithms.createGraph5", GraphAlgorithms::createGraph5);
        measureTime("GraphAlgorithms.createGraph6", GraphAlgorithms::createGraph6);
        measureTime("GraphAlgorithms.createGraph7", GraphAlgorithms::createGraph7);
    }

}
