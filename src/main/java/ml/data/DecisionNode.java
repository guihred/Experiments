package ml.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DecisionNode {

    private Random random = new Random();

    private DecisionNode trueNode;
    private DecisionNode falseNode;
    private Question question;
    List<String> result;

    public DecisionNode(Question question, DecisionNode trueNode, DecisionNode falseNode) {
        this.question = question;
        this.trueNode = trueNode;
        this.falseNode = falseNode;
    }

    public DecisionNode(String label, DataframeML rows) {
        result = new ArrayList<>(rows.categorize(label));
    }

    public boolean isLeaf() {
        return question == null;
    }

    public Object predict(Map<String, Object> row) {
        if (isLeaf()) {
            return result.get(random.nextInt(result.size()));
        }
        boolean answer = question.answer(row.get(question.getColName()));
        return answer ? trueNode.predict(row) : falseNode.predict(row);
    }

    public DecisionNode shuffle() {
        if (isLeaf()) {
            return this;
        }
        boolean bBoolean = random.nextBoolean();
        DecisionNode b = bBoolean ? trueNode : falseNode;
        boolean aBoolean = random.nextBoolean();
        DecisionNode a = aBoolean ? b.trueNode : b.falseNode;
        if (a == null || a.isLeaf()) {
            return this;
        }
        if (random.nextBoolean()) {
            if (bBoolean) {
                trueNode = a;
                if (aBoolean) {
                    b.trueNode = a.trueNode;
                    a.trueNode = b;
                } else {
                    b.falseNode = a.trueNode;
                    a.trueNode = b;
                }
            } else {
                falseNode = a;
                if (aBoolean) {
                    b.trueNode = a.trueNode;
                    a.trueNode = b;
                } else {
                    b.falseNode = a.trueNode;
                    a.trueNode = b;
                }
            }
        }
        if (random.nextBoolean()) {
            trueNode.shuffle();
        }
        if (random.nextBoolean()) {
            falseNode.shuffle();
        }
        return this;
    }

    public int size() {
        if (isLeaf()) {
            return 1;
        }
        return Math.max(trueNode.size(), falseNode.size()) + 1;
    }

    @Override
    public String toString() {
        if (isLeaf()) {
            return "  " + result;
        }
        String trueBranch = trueNode.toString().replaceAll(" {2}( *)", "    $1");
        String falseBranch = falseNode.toString().replaceAll(" {2}( *)", "    $1");
        return String.format("%n  if(%s){%n  %s%n  }else{%n  %s%n  }", question, trueBranch, falseBranch)
            .replaceAll("\n *\n", "\n").replaceAll("\n *\n", "\n");
    }
}