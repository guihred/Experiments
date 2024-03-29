package cubesystem;

import java.util.Arrays;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.ex.HasLogging;

public final class ElementWiseOp {
	private static final Logger LOGGER = HasLogging.log();

	private ElementWiseOp() {
	}

	public static Double[][] matrOp(Operation operation, Double[][] matr, Double[][] scalar) {
		Double[][] result = new Double[matr.length][Stream.of(matr).mapToInt(a -> a.length).max().getAsInt()];
		for (int i = 0; i < matr.length; i++) {
			for (int j = 0; j < matr[i].length; j++) {
				result[i][j] = operation.apply(matr[i][j],
						scalar[i % scalar.length][j
						                          % scalar[i % scalar.length].length]);
			}
		}
		return result;
	}

	public static void printMatrix(Double[][] matr) {

		Stream.of(matr).map(Arrays::toString).forEach(s -> LOGGER.info("{}", s));
	}

	public static Double[][] scalarOp(Operation operation, Double[][] matr, Double scalar) {
		Double[][] result = new Double[matr.length][matr[0].length];
		for (int i = 0; i < matr.length; i++) {
			for (int j = 0; j < matr[i].length; j++) {
				result[i][j] = operation.apply(matr[i][j], scalar);
			}
		}
		return result;
	}
	public enum Operation {
		NONE((a, b) -> a), 
		ADD((a, b) -> a + b), 
		SUB((a, b) -> a - b), 
		MUL((a, b) -> a * b), 
		DIV((a, b) -> a / b), 
		POW(Math::pow),
		MOD((a, b) -> a % b);

		private final transient DoubleBinaryOperator function;

		Operation(DoubleBinaryOperator op) {
			function = op;
		}

		public double apply(double a, double b) {
			return function.applyAsDouble(a, b);
		}
	}
}
