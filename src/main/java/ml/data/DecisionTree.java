package ml.data;

import java.util.*;
import ml.data.Question.QuestionType;
import org.slf4j.Logger;
import utils.HasLogging;

public class DecisionTree {

    private static final Logger LOG = HasLogging.log();

    public static DecisionNode buildTree(DataframeML frame, String label) {
        Question question = findBestSplit(frame, label);
        if (question == null || question.getInfoGain() == 0) {
            return new DecisionNode(label, frame);
        }
		DataframeML trueFrame = new DataframeML(frame).filter(question.getColName(), question::answer);
        DataframeML falseFrame = new DataframeML(frame).filter(question.getColName(), c -> !question.answer(c));
                
        DecisionNode trueTree = buildTree(trueFrame, label);
        DecisionNode falseTree = buildTree(falseFrame, label);
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
            QuestionType[] values2 = dataframe.getFormat(col) == String.class ? new QuestionType[] { QuestionType.EQ }
                : QuestionType.values();
            for (QuestionType questionType : values2) {
                for (Object val : values) {
                    Question question = new Question(col, val,questionType);
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
        DecisionNode buildTree = buildTree(build, "ACTION");
        LOG.info("{}", buildTree);
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
