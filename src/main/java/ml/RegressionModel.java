package ml;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class RegressionModel {
	@GuardedBy("this")
	private int current;

	public static void main(String[] args) {

		RegressionModel regressionModel = new RegressionModel();
		for (int i = 0; i < 4000; i++) {
			new Thread(() -> {
				System.out.println(regressionModel.getNext());
			}).start();
		}

	}

	public synchronized int getNext() {
		return ++current;
	}
	public synchronized List<Double> gerarNumeros() {
		Random random = new Random();
		double a = (random.nextDouble() - .5) * 10;
		return Stream.iterate(0.0, i -> a * current++ + (random.nextDouble() - .5) * 3.0).limit(20)
				.collect(Collectors.toList());
	}

}

