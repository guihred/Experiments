package log.analyze;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsLogAccess {
	public static final Logger LOGGER = LoggerFactory.getLogger(StatsLogAccess.class);

	public static void main(String[] args) throws IOException {
		statisticaDemoraArquivo();
	}

	public static void statisticaTamanhoArquivos1() throws IOException {
		List<Path> lista = Files.list(Paths.get("C:\\Users\\Note\\Documents\\Log"))
				.filter(p -> p.toFile().getName().startsWith("localhost_access_log")).collect(Collectors.toList());
		for (Path path : lista) {
			try {
				LongSummaryStatistics summaryStatistics = Files.lines(path).map(tentarFuncao(linha -> {
					if (linha == null || linha.isEmpty()) {
						return "0";
					}

					String[] a = linha.split(" ");
					return a[a.length - 1];
				})).map(tentarFuncao(a -> Long.parseLong(a))).mapToLong(a -> a != null ? a : 0).summaryStatistics();
				System.out.println(
						path.toString() + " = " + summaryStatistics.getAverage() + ",\t" + summaryStatistics.getMax()
								+ ",\t" + summaryStatistics.getMin() + ",\t" + summaryStatistics.getCount());

			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}

		// fo

	}

	public static void ajustarArquivos() throws IOException {

		Predicate<String> asPredicate = Pattern.compile("siga-dia.log.2016-\\d+-\\d+-\\d+").asPredicate();
		List<Path> lista = Files.list(Paths.get("C:\\tmp")).filter(p -> asPredicate.test(p.toFile().getName()))
				.collect(Collectors.toList());
		for (Path path : lista) {
			File file = new File(path.toFile().getName().replaceAll("siga-dia.log.2016-(\\d+)-(\\d+)-(\\d+)",
					"siga-dia.2016-$1-$2.log"));
			if (!file.exists()) {
				file.createNewFile();
			}
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));) {
				Files.lines(path).forEach(out::println);
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}

		// fo

	}

	public static void statisticaTamanhoArquivos() throws IOException {
		List<Path> lista = Files.list(Paths.get("C:\\Users\\Note\\Documents\\Log"))
				.filter(p -> p.toFile().getName().startsWith("localhost_access_log")).collect(Collectors.toList());
		try (PrintStream out = new PrintStream(new File("acessos.txt"));) {
			for (Path path : lista) {
				try {
					Map<String, Long> stats = Files.lines(path)
							.filter(l -> !l.endsWith("-") && !l.split(" ")[6].contains("resources"))
							.collect(Collectors.groupingBy(tentarFuncao(linha -> linha.split(" ")[6]),
									Collectors.counting()));
					out.println(path + "--------------------------");

					stats.entrySet().stream().sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed())
							.forEach(entry -> out.println(entry.getKey() + "  ,  " + entry.getValue()));
				} catch (Exception e) {
					LOGGER.error("", e);
				}

			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	public static void statisticaDemoraArquivo() throws IOException {
		List<Path> lista = Files.list(Paths.get("C:\\Users\\Note\\Documents\\Log"))
				.filter(p -> p.toFile().getName().startsWith("localhost_access_log")).collect(Collectors.toList());
		try (PrintStream out = new PrintStream(new File("acessos.txt"));) {
			for (Path path : lista) {
				try {
					Map<String, LongSummaryStatistics> stats = Files.lines(path)
							.filter(l -> !l.endsWith("-") && !l.split(" ")[6].contains("resources"))
							.collect(Collectors.groupingBy(tentarFuncao(linha -> {
								String string = linha.split(" ")[6];
								if (string.contains("?")) {
									return string.substring(0, string.indexOf("?"));
								}

								return string;
							}), Collectors.summarizingLong((ToLongFunction<String>) (String linha) -> {
								if (linha == null || linha.isEmpty()) {
									return 0L;
								}

								final String[] a = linha.split(" ");
								return Long.parseLong(a[a.length - 1]);
							})));
					out.println(path + "--------------------------");

					stats.entrySet().stream()
							.sorted(Comparator
									.comparing((Entry<String, LongSummaryStatistics> c) -> c.getValue().getAverage())
									.reversed())
							.forEach(entry -> out.println(entry.getKey() + "  ,  " + entry.getValue()));
				} catch (Exception e) {
					LOGGER.error("", e);
				}

			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	@FunctionalInterface
	protected interface ConsumerEx<T> {
		void accept(T t) throws Exception;
	}

	protected static <T> Consumer<T> tentar(ConsumerEx<T> t) {
		return f -> {
			try {
				t.accept(f);
			} catch (Exception e) {
				// Ignorar exceção
			}
		};

	}

	@FunctionalInterface
	protected interface FunctionEx<T, R> {
		R apply(T t) throws Exception;
	}

	protected static <T, R> Function<T, R> tentarFuncao(FunctionEx<T, R> t) {
		return f -> {
			try {
				return t.apply(f);
			} catch (Exception e) {
				return null;
			}
		};

	}
}
