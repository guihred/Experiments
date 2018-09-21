package graphs.topology;

import graphs.entities.CellType;
import graphs.entities.Graph;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;
import utils.ResourceFXUtils;

public class WordTopology extends BaseTopology {


    public WordTopology(Graph graph) {
        super(graph, "Word");
	}


	@Override
    public void execute() {
        graph.getModel().clearSelected();
        graph.clean();
        graph.getModel().removeAllCells();
        graph.getModel().removeAllEdges();

        String[] words = getWords();

        Stream.of(words).sorted().forEach(a -> graph.getModel().addCell(a, CellType.CIRCLE));
        for (String v : words) {
            for (String w : words) {
                if (oneCharOff(w, v)) {
                    graph.getModel().addBiEdge(v, w, 1);
                }
            }
        }
        graph.endUpdate();
        ConcentricLayout.layoutConcentric(graph.getModel().getAllCells(), graph.getModel().getAllEdges());

	}

    private String[] getWords() {
        try (Stream<String> lines = Files.lines(ResourceFXUtils.toPath("alice.txt"))) {
            return lines.flatMap((String e) -> Stream.of(e.split("[^a-zA-Z]"))).filter(s -> s.length() == 4)
                    .map(String::toLowerCase).distinct().toArray(String[]::new);
        } catch (IOException e) {
            getLogger().error("", e);
        }

        return new String[] { "fine", "line", "mine", "nine", "pine", "vine", "wine", "wide", "wife", "wipe", "wire",
                "wind", "wing", "wink", "wins", "none", "gone", "note", "vote", "site", "nite", "bite" };
    }

    public static boolean oneCharOff(String word1, String word2) {
        if (word1.length() != word2.length()) {
            return false;
        }

        int diffs = 0;

        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i) && ++diffs > 1) {
                return false;
            }
        }

        return diffs == 1;
    }

}