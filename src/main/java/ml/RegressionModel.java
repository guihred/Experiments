package ml;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

class RegressionModel {

    private static final int MAX_SIZE = 20;
    double slope;
    double initial;

    private int c;

	private List<Double> target;
	private List<Double> features;

    double bestSlope;
    double bestInitial;
	public ObservableList<Series<Number, Number>> getExpectedSeries() {
        Series<Number, Number> series = new Series<>();
        c = 0;
        series.setName("Prediction");
        List<Data<Number, Number>> collect = IntStream.range(0, MAX_SIZE)
                .mapToObj(i -> toData(i, bestInitial + bestSlope * i)).collect(Collectors.toList());
        series.setData(FXCollections.observableArrayList(collect));
        // return FXCollections.observableArrayList(series); FIXME
        return FXCollections.observableArrayList();
    }
    @SuppressWarnings("unchecked")
	public ObservableList<Series<Number, Number>> createRandomSeries() {
        Series<Number, Number> series = new Series<>();
        series.setName("Numbers");
		c = 0;
		features = IntStream.range(0, MAX_SIZE).mapToDouble(e -> e).boxed().collect(Collectors.toList());
		c = 0;
        DoubleStream sorted = doubleStream();
		target = sorted.boxed().collect(Collectors.toList());
		target.sort(Comparator.comparing(e -> Math.signum(slope) * e));
		c = 0;
		List<Data<Number, Number>> collect = target.stream().map(this::mapToData)
                .collect(Collectors.toList());
        series.setData(FXCollections.observableArrayList(collect));
        return FXCollections.observableArrayList(series);
    }

	@SuppressWarnings("unchecked")
	public ObservableList<Series<Number, Number>> createSeries(List<?> features1, List<?> target1) {
        Random random = new Random();
        slope = (random.nextDouble() - .5) * 10;
        initial = (random.nextDouble() - .5) * 10;

        bestSlope = (random.nextDouble() - .5) * 10;
        bestInitial = (random.nextDouble() - .5) * 10;

		features = features1.stream().map(Number.class::cast).filter(e -> e != null).map(e -> e.doubleValue())

                .limit(MAX_SIZE)
				.collect(Collectors.toList());
		target = target1.stream().map(Number.class::cast).filter(e -> e != null).map(e -> e.doubleValue())
                .limit(MAX_SIZE)
				.collect(Collectors.toList());
		c = 0;
        ObservableList<Data<Number, Number>> observableArrayList = FXCollections.observableArrayList();
        Series<Number, Number> series = new Series<>();
        series.setName("Numbers");
        series.setData(observableArrayList);

        target.stream().map(this::mapToData).forEach(e -> observableArrayList.add(e));
		return FXCollections.observableArrayList(series);
	}
    private DoubleStream doubleStream() {
        Random random = new Random();
        slope = (random.nextDouble() - .5) * 10;
        initial = (random.nextDouble() - .5) * 10;
        c = 0;
        return DoubleStream.generate(() -> random(random)).limit(MAX_SIZE);
    }

    private double random(Random random) {
        double e = (random.nextDouble() - .5) * 5;
        return initial + e + slope * c++;
    }

    double learningRate = 0.001;

    @SuppressWarnings("unchecked")
    public ObservableList<Series<Number, Number>> getErrorSeries() {
        Series<Number, Number> series = new Series<>();
        Series<Number, Number> series2 = new Series<>();
        ObservableList<Data<Number, Number>> slopeList = FXCollections.observableArrayList();
        ObservableList<Data<Number, Number>> interceptList = FXCollections.observableArrayList();
        series.setData(slopeList);
        series2.setData(interceptList);
        series.setName("Slope Error");
        series2.setName("Intercept Error");
        bestSlope = 50;
        bestInitial = 0;
		for (int i = 0; i < 1000; i++) {
            c = 0;
			double adjust = target.stream().mapToDouble(e -> e)
					.map(y -> (-y + bestInitial + bestSlope * features.get(c++)) * features.get(c - 1))
                    .sum() * 2
					/ target.size()
                    * learningRate;
            bestSlope -= adjust;
            c = 0;
			target.stream().mapToDouble(e -> e).map(e -> -e + bestInitial + bestSlope * features.get(c++))
					.map(e -> Math.abs(e)).sum();
        }
		for (int i = 0; i < 5000; i++) {

            c = 0;
			double adjust = target.stream().mapToDouble(e -> e)
					.map(y -> -y + bestInitial + bestSlope * features.get(c++)).sum() * 2 / target.size()
					* learningRate;
            bestInitial -= adjust;
            c = 0;
			double loss = target.stream().mapToDouble(e -> e).map(e -> -e + bestInitial + bestSlope * features.get(c++))
                    .map(e -> e * e).sum();
			interceptList.add(toData(bestInitial, loss));

        }
        return FXCollections.observableArrayList(series2, series);
    }

    private Data<Number, Number> mapToData(double e) {
		return new Data<>(features.get(c++), e);
    }

    private Data<Number, Number> toData(double e1, double e2) {
        return new Data<>(e1, e2);
    }

}
