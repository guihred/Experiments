package sample.cubesystem;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@SuppressWarnings("serial")
public final class ElementWiseOp {
	enum Operation {
		NONE((a, b) -> a), 
		ADD((a, b) -> a + b), 
		SUB((a, b) -> a - b), 
		MUL((a, b) -> a * b), 
		DIV((a, b) -> a / b), 
		POW((a, b) -> Math.pow(a, b)), 
		MOD((a, b) -> a % b);

		private final transient BiFunction<Double, Double, Double> function;
		private Operation(BiFunction<Double, Double, Double> op) {
			function = op;
		}

		public Double apply(Double a, Double b) {
			return function.apply(a, b);
		}
	}

	private ElementWiseOp() {
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
		Stream.of(matr).map(Arrays::toString).forEach(System.out::println);
	}
	public static void main(String[] args) {
		printMatrix(scalarOp(Operation.MUL, new Double[][] {
				{ 1.0, 2.0, 3.0 }, 
				{ 4.0, 5.0, 6.0 }, 
				{ 7.0, 8.0, 9.0 }
		}, 3.0));

		printMatrix(matrOp(Operation.DIV, new Double[][] {
				{ 1.0, 2.0, 3.0 }, 
				{ 4.0, 5.0, 6.0 }, 
				{ 7.0, 8.0, 9.0 }
		}, new Double[][] {
				{ 1.0, 2.0}, 
				{ 3.0, 4.0} 
		}));
	}
}
