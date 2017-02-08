package ml;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegressionModel {
	private int current = 0;

	public static void main(String[] args) {

		System.out.println(new RegressionModel().gerarNumeros());
	}

	public List<Double> gerarNumeros() {
		Random random = new Random();
		double a = (random.nextDouble() - .5) * 10;

		List<Double> collect = Stream.iterate(0.0, (i) -> a * current++ + (random.nextDouble() - .5) * 3.0).limit(20).collect(Collectors.toList());
		return collect;
	}

}

