package ml.data;

import java.util.*;
import ml.data.Question.QuestionType;
import org.slf4j.Logger;
import utils.HasLogging;

public class DecisionTree {

    private static final double MIN_GAIN = 0.005;
    private static final Logger LOG = HasLogging.log();

    public static DecisionNode buildTree(DataframeML frame, String label) {
        Question question = findBestSplit(frame, label);
        if (question == null || question.getInfoGain() <= MIN_GAIN) {
            return new DecisionNode(label, frame);
        }
        DataframeML trueFrame = new DataframeML(frame).filter(question.getColName(), question::answer);
        DataframeML falseFrame = new DataframeML(frame).filter(question.getColName(), c -> !question.answer(c));

        DecisionNode trueTree = buildTree(trueFrame, label);
        DecisionNode falseTree = buildTree(falseFrame, label);
        if (trueTree.isLeaf() && falseTree.isLeaf()) {
            if (trueTree.result.containsAll(falseTree.result) || falseTree.result.containsAll(trueTree.result)) {
                return new DecisionNode(label, frame);
            }
        }

        return new DecisionNode(question, trueTree, falseTree);

    }

    public static void executeSimpleTest() {
        DataframeML dataframeML = new DataframeML();
        dataframeML.addCols("Color", String.class);
        dataframeML.addCols("Diam", Integer.class);
        String labelHeader = "Label";
        dataframeML.addCols(labelHeader, String.class);
        dataframeML.addAll("Green", 3, "Apple");
        dataframeML.addAll("Yellow", 3, "Apple");
        dataframeML.addAll("Red", 1, "Grape");
        dataframeML.addAll("Red", 1, "Grape");
        dataframeML.addAll("Yellow", 3, "Lemon");
        LOG.info("{}", gini(dataframeML, labelHeader));

        DecisionNode buildTree = buildTree(dataframeML, labelHeader);
        Map<String, Object> row = dataframeML.rowMap(4);
        LOG.info("{}", buildTree);
        Object predict = buildTree.predict(row);
        LOG.info("{}", predict);
    }

    public static Question findBestSplit(DataframeML dataframe, String label) {
        Set<String> cols = new HashSet<>(dataframe.cols());
        cols.remove(label);
        double bestGain = 0.00;
        Question bestQuestion = null;
        double currentUncertainty = gini(dataframe, label);
        for (String col : cols) {
            Set<Object> values = dataframe.freeCategory(col);
            List<QuestionType> values2 = dataframe.getFormat(col) == String.class ? Arrays.asList(QuestionType.EQ)
                : Arrays.asList(QuestionType.GE, QuestionType.GT);
            Collections.shuffle(values2);
            for (QuestionType questionType : values2) {
                for (Object val : values) {
                    Question question = new Question(col, val, questionType);
                    DataframeML trueFrame = new DataframeML(dataframe).filter(col, question::answer);
                    DataframeML falseFrame = new DataframeML(dataframe).filter(col, c -> !question.answer(c));
                    if (trueFrame.getSize() == 0 || falseFrame.getSize() == 0) {
                        continue;
                    }
                    double infoGain = infoGain(trueFrame, falseFrame, currentUncertainty, label);
                    if (infoGain >= bestGain) {
                        bestGain = infoGain;
                        bestQuestion = question;
                        question.setInfoGain(infoGain);
                    }
                }
            }
        }
        return bestQuestion;
    }

    public static double gini(DataframeML dataframe, String header) {
        double size = dataframe.getSize();
        if (size <= 0) {
            return 0.;
        }
        if (dataframe.getFormat(header) == String.class) {
            Set<String> categorize = dataframe.categorize(header);
            Map<String, Long> histogram = dataframe.histogram(header);
            double impurity = 1.;
            for (Object cat : categorize) {
                double prob = histogram.get(cat) / size;
                impurity -= prob * prob;
            }
            return impurity;
        }
        Set<Object> categorize = dataframe.freeCategory(header);
        Map<Double, Long> histogram = dataframe.histogram(header, categorize.size());
        double impurity = 1.;
        for (Object cat : categorize) {
            double prob = histogram.get(cat) / size;
            impurity -= prob * prob;
        }
        return impurity;

    }

    public static double infoGain(DataframeML left, DataframeML right, double uncertainty, String labelHeader) {
        double size = left.getSize();
        double p = size / (size + right.getSize());
        return uncertainty - p * gini(left, labelHeader) - (1 - p) * gini(right, labelHeader);
    }

    public static void main(String[] args) {
        DataframeML build = DataframeML.builder("out/catan_log.txt").build();
        List<Object> list = build.list("ACTION");
        list.add(list.remove(0));
        build.removeCol("WINNER", "PLAYER");
        DecisionNode buildTree = buildTree(build, "ACTION");
        LOG.info("\n{}", buildTree);
        LOG.info("{}", buildTree.size());
        LOG.info("\n{}", buildTree.shuffle());
    }

    public static class DecisionNode {

        private Random random = new Random();

        private DecisionNode trueNode;
        private DecisionNode falseNode;
        private Question question;
        private List<String> result;

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
            return String.format("\n  if(%s){\n  %s\n  }else{\n  %s\n  }", question, trueBranch, falseBranch)
                .replaceAll("\n *\n", "\n").replaceAll("\n *\n", "\n");
        }
    }

}
