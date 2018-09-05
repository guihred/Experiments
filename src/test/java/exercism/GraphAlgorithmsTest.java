package exercism;

import crypt.FXTesting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javaexercises.GraphAlgorithms;
import javaexercises.Vertex;
import org.junit.Test;

public class GraphAlgorithmsTest {
    @Test
    public void testGraphAlgorithms() throws Exception {
        List<Vertex> vertices = FXTesting.measureTime("GraphAlgorithms.createGraph8",
                GraphAlgorithms::createGraph8);
        Map<Vertex, Integer> num = new HashMap<>();
        Map<Vertex, Integer> low = new HashMap<>();
        FXTesting.measureTime("Vertex.assignNum", () -> vertices.get(0).assignNum(num, 0));
        FXTesting.measureTime("Vertex.assignLow", () -> vertices.get(0).assignLow(num, low));
        FXTesting.measureTime("Vertex.kruskal", () -> Vertex.kruskal(vertices));
        FXTesting.measureTime("Vertex.prim", () -> Vertex.prim(vertices));
        FXTesting.measureTime("Vertex.sortTopology", () -> Vertex.sortTopology(vertices));

    }

}
