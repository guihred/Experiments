package ml;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import utils.SupplierEx;

public class RegressionModel {

    private static final int MAX_SIZE = 50;
    private int i;
    private List<Double> target;
    private List<Double> features;
    private double bestSlope;
    private double bestInitial;
    private Series<Number, Number> linearSeries;

    private Series<Number, Number> polinominalSeries;

    /**
     * Returns the {@code j}th regression coefficient.
     * 
     * @param j the index
     * @return the {@code j}th regression coefficient
     */

    @SuppressWarnings("unchecked")
    public ObservableList<Series<Number, Number>> createRandomSeries() {
        Series<Number, Number> series = new Series<>();
        series.setName("Numbers");
        i = 0;
        features = IntStream.range(0, MAX_SIZE).mapToDouble(e -> e).boxed().collect(Collectors.toList());
        i = 0;
        target = doubleStream().boxed().collect(Collectors.toList());
        target.sort(Comparator.comparing(e -> Math.signum(bestSlope) * e));
        i = 0;
        List<Data<Number, Number>> dataPoints = target.stream().map(this::mapToData).collect(Collectors.toList());
        series.setData(FXCollections.observableArrayList(dataPoints));
        return FXCollections.observableArrayList(series);
    }

    public Series<Number, Number> createSeries(String name, Collection<?> features1, Collection<?> target1) {
        features = features1.stream().map(Number.class::cast).filter(Objects::nonNull).map(Number::doubleValue)
            .limit(MAX_SIZE).collect(Collectors.toList());
        target = target1.stream().map(Number.class::cast).filter(Objects::nonNull).map(Number::doubleValue)
            .limit(MAX_SIZE).collect(Collectors.toList());
        i = 0;
        Series<Number, Number> series = new Series<>();
        series.setName(name);

        ObservableList<Data<Number, Number>> observableList = target.stream().map(this::mapToData)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        series.setData(observableList);
        linearRegression();
        return series;
    }

    public double getBestInitial() {
        return bestInitial;
    }

    public double getBestSlope() {
        return bestSlope;
    }

    public Series<Number, Number> getExpectedSeries() {
        return SupplierEx.orElse(linearSeries, () -> linearSeries = SupplierEx.get(() -> {
            Series<Number, Number> series = new Series<>();
            i = 0;
            series.setName("Linear");
            List<Data<Number, Number>> expectedPoints = IntStream.range(0, features.size())
                .mapToObj(j -> toData(features.get(j), bestInitial + bestSlope * features.get(j)))
                .collect(Collectors.toList());
            series.setData(FXCollections.observableArrayList(expectedPoints));
            return series;
        }));
    }

    public Series<Number, Number> getPolinominalSeries() {
        return SupplierEx.orElse(polinominalSeries, () -> polinominalSeries = SupplierEx.get(() -> {
            Series<Number, Number> series = new Series<>();
            i = 0;
            series.setName("Polinominal");
            double[] x = features.stream().mapToDouble(e -> e).toArray();
            double[] y = target.stream().mapToDouble(e -> e).toArray();

            DoubleUnaryOperator polynomialRegression = polynomialRegression(x, y, 3);

//            DoubleUnaryOperator polyRegression = polyRegression(x, y);
            List<Data<Number, Number>> expectedPoints = IntStream.range(0, features.size())
                .mapToObj(j -> toData(features.get(j), polynomialRegression.applyAsDouble(features.get(j))))
                .collect(Collectors.toList());
            series.setData(FXCollections.observableArrayList(expectedPoints));
            return series;
        }));
    }

    public void linearRegression() {
        double[] x = features.stream().mapToDouble(e -> e).toArray();
        double[] y = target.stream().mapToDouble(e -> e).toArray();
        if (x.length != y.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        int n = x.length;

        // first pass
        double sumx = 0.0, sumy = 0.0;
        for (int j = 0; j < n; j++) {
            sumx += x[j];
            sumy += y[j];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, xybar = 0.0;
        for (int j = 0; j < n; j++) {
            xxbar += (x[j] - xbar) * (x[j] - xbar);
            xybar += (x[j] - xbar) * (y[j] - ybar);
        }
        bestSlope = xybar / xxbar;
        bestInitial = ybar - bestSlope * xbar;
    }

    /**
     * Returns the coefficient of determination <em>R</em><sup>2</sup>.
     *
     * @return the coefficient of determination <em>R</em><sup>2</sup>, which is a
     *         real number between 0 and 1
     */

    public void setBestInitial(double bestInitial) {
        this.bestInitial = bestInitial;
    }

    public void setBestSlope(double bestSlope) {
        this.bestSlope = bestSlope;
    }

    private DoubleStream doubleStream() {
        bestSlope = (Math.random() - .5) * 10;
        bestInitial = (Math.random() - .5) * 10;
        i = 0;
        return DoubleStream.generate(this::random).limit(MAX_SIZE);
    }

    private Data<Number, Number> mapToData(double e) {
        Double xValue = features.get(i++);
        return new Data<>(xValue, e, 1);
    }

    private double random() {
        double e = (Math.random() - .5) * 5;
        return bestInitial + e + bestSlope * i++;
    }

    public static double beta(RealMatrix beta, int j) {
        // to make -0.0 print as 0.0
        if (Math.abs(beta.getEntry(j, 0)) < 1E-4) {
            return 0.0;
        }
        return beta.getEntry(j, 0);
    }

    public static DoubleUnaryOperator polynomialRegression(double[] x, double[] y, int d) {

        int n = x.length;
        RealMatrix matrixX = new Array2DRowRealMatrix();
        QRDecomposition qr = null;
        int degree = d;
        // in case Vandermonde matrix does not have full rank, reduce degree until it
        // does
        while (true) {

            // build Vandermonde matrix
            double[][] vandermonde = new double[n][degree + 1];
            for (int j = 0; j < n; j++) {
                for (int k = 0; k <= degree; k++) {
                    vandermonde[j][k] = Math.pow(x[j], k);
                }
            }
            matrixX = new Array2DRowRealMatrix(vandermonde);

            // find least squares solution
            qr = new QRDecomposition(matrixX);
            if (qr.getSolver().isNonSingular()) {
                break;
            }

            // decrease degree and try again
            degree--;
        }
        if (qr == null) {
            return null;
        }

        // create matrix from vector
        RealMatrix matrixY = new Array2DRowRealMatrix(y);

        // linear regression coefficients
        RealMatrix beta = qr.getSolver().solve(matrixY);

        // mean of y[] values
        totalVariation(y, n);

        // variation not accounted for
        final int degreeFinal = degree;
        return x0 -> predict(beta, x0, degreeFinal);

    }

    /**
     * Returns the expected response {@code y} given the value of the predictor
     * variable {@code x}.
     *
     * @param x the value of the predictor variable
     * @return the expected response {@code y} given the value of the predictor
     *         variable {@code x}
     */
    public static double predict(RealMatrix beta, double x, int degree) {
        // horner's method
        double y = 0.0;
        for (int j = degree; j >= 0; j--) {
            y = beta(beta, j) + x * y;
        }
        return y;
    }

    private static Data<Number, Number> toData(double e1, double e2) {
        return new Data<>(e1, e2, 1);
    }

    private static double totalVariation(double[] y, int n) {
        double sum = 0.0;
        for (int j = 0; j < n; j++) {
            sum += y[j];
        }
        double mean = sum / n;
        double sst = 0;
        // total variation to be accounted for
        for (int j = 0; j < n; j++) {
            double dev = y[j] - mean;
            sst += dev * dev;
        }
        return sst;
    }

}
