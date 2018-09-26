package ml;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

class RegressionModel {

    private static final int MAX_SIZE = 20;
    private double slope;
    private double initial;
    private int c;
	private List<Double> target;
	private List<Double> features;
    private double bestSlope;
    private double bestInitial;
	private double learningRate = 0.001;
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
        List<Data<Number, Number>> dataPoints = target.stream().map(this::mapToData)
                .collect(Collectors.toList());
        series.setData(FXCollections.observableArrayList(dataPoints));
        return FXCollections.observableArrayList(series);
    }

	@SuppressWarnings("unchecked")
    public ObservableList<Series<Number, Number>> createSeries(Collection<?> features1, Collection<?> target1) {
        Random random = new Random();
        slope = (random.nextDouble() - .5) * 10;
        initial = (random.nextDouble() - .5) * 10;

        bestSlope = (random.nextDouble() - .5) * 10;
        bestInitial = (random.nextDouble() - .5) * 10;

        features = features1.stream().map(Number.class::cast)
                .filter(Objects::nonNull)
                .map(Number::doubleValue)
                .limit(MAX_SIZE)
				.collect(Collectors.toList());
		target = target1.stream()
		        .map(Number.class::cast)
		        .filter(Objects::nonNull)
		        .map(Number::doubleValue)
                .limit(MAX_SIZE)
				.collect(Collectors.toList());
		c = 0;
        ObservableList<Data<Number, Number>> observableArrayList = FXCollections.observableArrayList();
        Series<Number, Number> series = new Series<>();
        series.setName("Numbers");
        series.setData(observableArrayList);

        target.stream().map(this::mapToData).forEach(observableArrayList::add);
		return FXCollections.observableArrayList(series);
	}
    public double getBestInitial() {
        return bestInitial;
    }

    public double getBestSlope() {
        return bestSlope;
    }

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
                    .map(Math::abs).sum();
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

    @SuppressWarnings("unchecked")
    public ObservableList<Series<Number, Number>> getExpectedSeries() {
        Series<Number, Number> series = new Series<>();
        c = 0;
        series.setName("Prediction");
        List<Data<Number, Number>> expectedPoints = IntStream.range(0, MAX_SIZE)
                .mapToObj(i -> toData(i, bestInitial + bestSlope * i)).collect(Collectors.toList());
        series.setData(FXCollections.observableArrayList(expectedPoints));
        return FXCollections.observableArrayList(series);
    }

    public double getInitial() {
        return initial;
    }

    public double getSlope() {
        return slope;
    }
    public void setBestInitial(double bestInitial) {
        this.bestInitial = bestInitial;
    }
    public void setBestSlope(double bestSlope) {
        this.bestSlope = bestSlope;
    }
    public void setInitial(double initial) {
        this.initial = initial;
    }
    public void setSlope(double slope) {
        this.slope = slope;
    }
    private DoubleStream doubleStream() {
        Random random = new Random();
        slope = (random.nextDouble() - .5) * 10;
        initial = (random.nextDouble() - .5) * 10;
        c = 0;
        return DoubleStream.generate(() -> random(random)).limit(MAX_SIZE);
    }
    private Data<Number, Number> mapToData(double e) {
		return new Data<>(features.get(c++), e);
    }
    private double random(Random random) {
        double e = (random.nextDouble() - .5) * 5;
        return initial + e + slope * c++;
    }
    private Data<Number, Number> toData(double e1, double e2) {
        return new Data<>(e1, e2);
    }

}
