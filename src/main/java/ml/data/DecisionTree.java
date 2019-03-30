package ml.data;

import java.util.Map;
import java.util.Set;

public class DecisionTree {

	private void findBestSplit(DataframeML dataframe) {
		Set<String> cols = dataframe.cols();
		double bestGain = 0.;
		double bestQuestion = 0.;
		double currentUncenrtainty = gini(dataframe, "");
		int nFeatures = cols.size();
		for (String col : cols) {
			Set<Object> values = dataframe.freeCategory(col);
			for (Object val : values) {
				new Question(val);
				// TODO https://www.youtube.com/watch?v=LDRbO9a6XPU

			}

		}
		
	}


	public static double gini(DataframeML dataframe, String header) {
		double size = dataframe.getSize();
		if (size == 0) {
			return 1.;
		}
		Set<String> categorize = dataframe.categorize(header);
		Map<String, Long> histogram = dataframe.histogram(header);
		double impurity = 1.;
		for (String cat : categorize) {
			double prob = histogram.get(cat) / size;
			impurity -= prob * prob;
		}
		return impurity;
	}

}
