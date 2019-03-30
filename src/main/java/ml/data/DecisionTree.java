package ml.data;

import java.util.*;
import org.slf4j.Logger;
import utils.HasLogging;

public class DecisionTree {

    private static final Logger LOG = HasLogging.log();

    public static DecisionNode buildTree(DataframeML frame, String label) {
        Question question = findBestSplit(frame, label);
        if (question == null || question.getInfoGain() == 0) {
            return new DecisionNode(label, frame);
        }
        DataframeML trueFrame = new DataframeML(frame).filterString(question.getColName(), c -> question.answer(c));
        DataframeML falseFrame = new DataframeML(frame).filterString(question.getColName(), c -> !question.answer(c));
                
        DecisionNode trueTree = buildTree(trueFrame, label);
        DecisionNode falseTree = buildTree(falseFrame, label);
        return new DecisionNode(question, trueTree, falseTree);
        
    }

    public static Question findBestSplit(DataframeML dataframe, String label) {
        Set<String> cols = new HashSet<>(dataframe.cols());
        cols.remove(label);
        double bestGain = 0.00;
        Question bestQuestion = null;
        double currentUncenrtainty = gini(dataframe, label);
        for (String col : cols) {
            Set<Object> values = dataframe.freeCategory(col);
            for (Object val : values) {
                Question question = new Question(col, val);
                DataframeML trueFrame = new DataframeML(dataframe).filterString(col, c -> question.answer(c));
                DataframeML falseFrame = new DataframeML(dataframe).filterString(col, c -> !question.answer(c));
                if (trueFrame.getSize() == 0 || falseFrame.getSize() == 0) {
                    continue;
                }
                double infoGain = infoGain(trueFrame, falseFrame, currentUncenrtainty, label);
                if (infoGain >= bestGain) {
                    bestGain = infoGain;
                    bestQuestion = question;
                    question.setInfoGain(infoGain);
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
        Set<Object> categorize = dataframe.freeCategory(header);
        Map<String, Long> histogram = dataframe.histogram(header);
        double impurity = 1.;
        for (Object cat : categorize) {
            double prob = histogram.get(Objects.toString(cat)) / size;
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
        DataframeML dataframeML = new DataframeML();
        dataframeML.addCols("Color", String.class);
        dataframeML.addCols("Diam", Integer.class);
        dataframeML.addCols("Label", String.class);
        dataframeML.addAll("Green", 3, "Apple");
        dataframeML.addAll("Yellow", 3, "Apple");
        dataframeML.addAll("Red", 1, "Grape");
        dataframeML.addAll("Red", 1, "Grape");
        dataframeML.addAll("Yellow", 3, "Lemon");
        LOG.info("{}", gini(dataframeML, "Label"));

        DecisionNode buildTree = buildTree(dataframeML, "Label");
        Map<String, Object> row = dataframeML.rowMap(4);
        LOG.info("{}", buildTree);
        Object predict = buildTree.predict(row);
        LOG.info("{}", predict);

    }

    public static class DecisionNode {

        private Random random;

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
            random = new Random();
        }

        public Object predict(Map<String, Object> row) {
            if (question == null) {
                return result.get(random.nextInt(result.size()));
            }
            boolean answer = question.answer(row.get(question.getColName()));
            return answer ? trueNode.predict(row) : falseNode.predict(row);
        }

        @Override
        public String toString() {
            if (question == null) {
                return "> " + result;
            }
            String trueBranch = trueNode.toString().replaceAll("-(-*)", "--$1");
            String falseBranch = falseNode.toString().replaceAll("-(-*)", "--$1");
            return String.format("(%s)\n-%s\n- else %s ", question, trueBranch, falseBranch);
        }

    }

}
