package others;

import static com.google.common.collect.ImmutableMap.of;

import java.util.Arrays;
import java.util.Collections;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.complex.Complex;

import simplebuilder.HasLogging;

public class OthersTests {
	private static final class EanFactorReducer implements IntBinaryOperator {
		private int factor = 1;

		@Override
		public int applyAsInt(int sum, int i) {
			factor = factor == 1 ? 3 : 1;
			return sum + i * factor;
		}
	}

	enum Estado {
		ERROR(null),
		CLOSED(null),
		LAST_ACK(of("RCV_ACK", Estado.CLOSED)),
		CLOSE_WAIT(of("APP_CLOSE", LAST_ACK)),
		TIME_WAIT(of("APP_TIMEOUT", CLOSED)),
		CLOSING(of("RCV_ACK", TIME_WAIT)),
		FIN_WAIT_2(of("RCV_FIN", TIME_WAIT)),
		FIN_WAIT_1(of("RCV_FIN", CLOSING, "RCV_FIN_ACK", TIME_WAIT, "RCV_ACK", FIN_WAIT_2)),
		ESTABLISHED(of("APP_CLOSE", FIN_WAIT_1, "RCV_FIN", CLOSE_WAIT)),
		SYN_RCVD(of("APP_CLOSE", Estado.FIN_WAIT_1, "RCV_ACK", ESTABLISHED)),
		SYN_SENT(of("RCV_SYN", SYN_RCVD, "RCV_SYN_ACK", ESTABLISHED, "APP_CLOSE", CLOSED)),
		LISTEN(of("RCV_SYN", Estado.SYN_RCVD, "APP_SEND", Estado.SYN_SENT, "APP_CLOSE", Estado.CLOSED));
		private Map<String, Estado> map;

		Estado(Map<String, Estado> map) {
			this.map = map;
		}

		public Map<String, Estado> getMap() {
			if (map == null && this == CLOSED) {
				map = of("APP_PASSIVE_OPEN", Estado.LISTEN, "APP_ACTIVE_OPEN", Estado.SYN_SENT);
			}
			return map;
		}

	}

	public static Estado getStateMachine(Estado estado, List<String> eventList) {

		return eventList.stream().sequential().reduce(estado, (e, s) -> e.getMap().getOrDefault(s, Estado.ERROR), (e, f) -> e);
	}

	public static Complex p(Complex t, Complex a, Complex b, Complex c) {

		Complex c1 = a.subtract(b.multiply(2)).add(c).multiply(t.multiply(t));
		Complex c2 = b.subtract(a).multiply(t.multiply(2));
		return c1.add(c2).add(a);
	}

	public static String shorterReverseLonger(String a, String b) {
		return a.length() < b.length() ? a + reverse(b) + a : b + reverse(a) + b;
	}

	public static void main(String[] args) {
		Complex p = p(new Complex(1.0 / 2.0), new Complex(-3, -3), new Complex(-1, 1), new Complex(-9, -5));
        HasLogging.log().info("{}", p);

        //		System.out.println(getStateMachine(Estado.CLOSED, Arrays.asList("APP_PASSIVE_OPEN", "RCV_SYN", "RCV_ACK", "APP_CLOSE", "APP_SEND")));
	}

	public static int[] unique(int[] integers) {
		return Arrays.stream(integers).distinct().toArray();
	}

	public static String reverse(String a) {
		List<String> asList = Arrays.asList(a.split(""));
		Collections.reverse(asList);
		return asList.stream().collect(Collectors.joining());
	}

	public int squareDigits(int n) {
		return Integer.valueOf(String.valueOf(n).chars().mapToObj(Character::getNumericValue).map(i -> String.valueOf(i * i))
				.collect(Collectors.joining()));
	}

	public static boolean validate(final String eanCode) {

		int checksum = eanCode.chars().limit(eanCode.length() - 1L).map(i -> Character.getNumericValue((char) i))
				.reduce(0, new EanFactorReducer());
		checksum = (10 - checksum % 10) % 10;
		return checksum == Character.getNumericValue(eanCode.charAt(eanCode.length() - 1));
	}

	public static int[] minMax(int[] arr) {
		IntSummaryStatistics s = Arrays.stream(arr).summaryStatistics();
		return new int[] { s.getMin(), s.getMax() };
	}

	public static String nth(int n) {
		return String.format("%.2f", IntStream.range(0, n).mapToDouble(j -> 1.0 / (3 * j + 1)).sum());
	}

}
