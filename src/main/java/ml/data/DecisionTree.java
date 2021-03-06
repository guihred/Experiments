
package ml.data;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import utils.ResourceFXUtils;
import utils.ex.HasLogging;

public class DecisionTree {

    private static final double MIN_GAIN = 0.000;
    private static final Logger LOG = HasLogging.log();

    public static DecisionNode buildTree(DataframeML frame, String label, double minGain) {
        Question question = findBestSplit(frame, label);
        if (question == null || question.getInfoGain() <= minGain) {
            return new DecisionNode(label, frame);
        }
        DataframeML trueFrame = new DataframeML(frame).filter(question.getColName(), question);
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
        LOG.info("{}", entropy(dataframeML, labelHeader));

        DecisionNode buildTree = buildTree(dataframeML, labelHeader);

        Map<String, Object> row = dataframeML.rowMap(4);
        LOG.trace("{}", buildTree);
        Object predict = buildTree.predict(row);
        LOG.trace("{}", predict);
    }



    public static void main(String[] args) {
        testCatanDecisionTree();
    }

    public static void testCatanDecisionTree() {
        File csvFile = ResourceFXUtils.getOutFile("txt/catan_log.txt");
        DataframeML decisionsData = DataframeBuilder.build(csvFile);
        List<Object> list = decisionsData.list("ACTION");
        if (list != null) {
            list.add(list.remove(0));
            decisionsData.removeCol("WINNER", "PLAYER");
            DecisionNode buildTree = buildTree(decisionsData, "ACTION");
            LOG.trace("\n{}", buildTree);
            LOG.trace("{}", buildTree.size());
            LOG.trace("\n{}", buildTree.shuffle());
            LOG.trace("\n{}", buildTree.shuffle());
            LOG.trace("\n{}", buildTree.shuffle());
        }
    }

    private static DecisionNode buildTree(DataframeML frame, String label) {
        return buildTree(frame, label, MIN_GAIN);
    }

    private static double entropy(DataframeML dataframe, String header) {
        List<Object> list = dataframe.list(header);
        Map<Object, Long> valuesCount = list.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        Set<Entry<Object, Long>> entrySet = valuesCount.entrySet();
        double s = list.size();
        double sum = 0;
        for (Entry<Object, Long> entry : entrySet) {
            double p = entry.getValue() / s;
            sum -= p * Math.log(p);

        }
        return sum;
    }

    private static Question findBestSplit(DataframeML dataframe, String label) {
        Set<String> cols = new HashSet<>(dataframe.cols());
        cols.remove(label);
        double bestGain = 0.00;
        Question bestQuestion = null;
        final double currentUncertainty = entropy(dataframe, label);
        for (String col : cols) {
            Set<Object> values = dataframe.freeCategory(col);
            List<QuestionType> values2 = QuestionType.getMatches(dataframe.getFormat(col));
            Collections.shuffle(values2);
            for (QuestionType questionType : values2) {
                for (Object val : values) {
                    Question question = new Question(col, val, questionType);
                    DataframeML trueFrame = new DataframeML(dataframe).filter(col, question);
                    DataframeML falseFrame = new DataframeML(dataframe).filter(col, c -> !question.answer(c));
                    if (trueFrame.getSize() == 0 || falseFrame.getSize() == 0) {
                        continue;
                    }
                    double infoGain = currentUncertainty - infoGain(trueFrame, falseFrame, label);
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

    private static double infoGain(DataframeML left, DataframeML right, String labelHeader) {

        double sum = entropy(left, labelHeader);
        sum += entropy(right, labelHeader);
        return sum;
    }

    private static boolean isRedundantNode(DecisionNode trueTree, DecisionNode falseTree) {
        return trueTree.isLeaf() && falseTree.isLeaf() && trueTree.getResult().containsAll(falseTree.getResult())
                && falseTree.getResult().containsAll(trueTree.getResult());
    }

}
