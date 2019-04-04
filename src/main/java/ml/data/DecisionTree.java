
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
        if (isRedundantNode(trueTree, falseTree)) {
            return new DecisionNode(label, frame);
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

    private static boolean isRedundantNode(DecisionNode trueTree, DecisionNode falseTree) {
        return trueTree.isLeaf() && falseTree.isLeaf() && trueTree.result.containsAll(falseTree.result) && falseTree.result.containsAll(trueTree.result);
    }

}