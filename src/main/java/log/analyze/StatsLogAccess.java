package log.analyze;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import utils.FunctionEx;
import utils.HasLogging;
import utils.ResourceFXUtils;

public class StatsLogAccess {
    private static final String LOG_PREFIX = "localhost_access_log";
    private static final String LOG_DIRECTORY = "C:\\Users\\Note\\Documents\\Log";
    private static final Logger LOGGER = HasLogging.log();

    public static void ajustarArquivos() {

		Predicate<String> asPredicate = Pattern.compile("siga-dia.log.2016-\\d+-\\d+-\\d+").asPredicate();
        try (Stream<Path> list = Files.list(Paths.get("C:\\tmp"))) {
            List<Path> lista = list.filter(p -> asPredicate.test(p.toFile().getName())).collect(Collectors.toList());
            for (Path path : lista) {
                File file = new File(path.toFile().getName().replaceAll("siga-dia.log.2016-(\\d+)-(\\d+)-(\\d+)",
                        "siga-dia.2016-$1-$2.log"));
                if (!file.exists()) {
                    boolean a = file.createNewFile();
					LOGGER.trace("file created {}", a);
                }
                printLines(path, file);
            }
        } catch (Exception e) {
			LOGGER.error("", e);
        }


	}

    public static void main(String[] args) {
		try {
			statisticaDemoraArquivo();
        } catch (Exception e) {
			LOGGER.error("", e);
		}
	}

	public static void statisticaDemoraArquivo() {
        try (Stream<Path> list = Files.list(Paths.get(LOG_DIRECTORY));
                PrintStream out = new PrintStream(new FileOutputStream(ResourceFXUtils.toFile("acessos.txt")), true,
                        StandardCharsets.UTF_8.name())) {
            List<Path> lista = list.filter(p -> p.toFile().getName().startsWith(LOG_PREFIX))
                    .collect(Collectors.toList());
			for (Path path : lista) {
                printDemoraArquivo(out, path);
	
			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	
	}

    public static void statisticaTamanhoArquivos() {
        try (Stream<Path> list = Files.list(Paths.get(LOG_DIRECTORY));
                PrintStream out = new PrintStream(new FileOutputStream(ResourceFXUtils.toFile("acessos.txt")), true,
                        StandardCharsets.UTF_8.name())) {
            List<Path> lista = list.filter(p -> p.toFile().getName().startsWith(LOG_PREFIX))
                    .collect(Collectors.toList());
			for (Path path : lista) {
				tryGetMeanSize(out, path);

			}
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

    public static void statisticaTamanhoArquivos1() {
        try (Stream<Path> list = Files.list(Paths.get(LOG_DIRECTORY))) {
            List<Path> lista = list.filter(p -> p.toFile().getName().startsWith(LOG_PREFIX))
                    .collect(Collectors.toList());
            for (Path path : lista) {
                printSummary(path);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

	}

    private static void printDemoraArquivo(PrintStream out, Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            Map<String, LongSummaryStatistics> stats = lines
                    .filter(l -> !l.endsWith("-") && !l.split(" ")[6].contains("resources"))
                    .collect(Collectors.groupingBy(FunctionEx.makeFunction(linha -> {
                        String string = linha.split(" ")[6];
                        if (string.contains("?")) {
                            return string.substring(0, string.indexOf('?'));
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

            stats.entrySet().stream().sorted(Comparator
                    .comparing((Entry<String, LongSummaryStatistics> c) -> c.getValue().getAverage()).reversed())
                    .forEach(entry -> out.println(entry.getKey() + "  ,  " + entry.getValue()));
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static void printLines(Path path, File file) {
        try (PrintWriter out = new PrintWriter(file, StandardCharsets.UTF_8.displayName());
                Stream<String> lines = Files.lines(path)) {
            lines.forEach(out::println);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    private static void printSummary(Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            LongSummaryStatistics summary = lines.map(FunctionEx.makeFunction(linha -> {
        		if (linha == null || linha.isEmpty()) {
        			return "0";
        		}

        		String[] a = linha.split(" ");
        		return a[a.length - 1];
            })).map(FunctionEx.makeFunction(Long::parseLong)).mapToLong(a -> a != null ? a : 0).summaryStatistics();
            LOGGER.info("{} = {},\t{},\t{},\t{}", path, summary.getAverage(), summary.getMax(), summary.getMin(),
                    summary.getCount());

        } catch (Exception e) {
        	LOGGER.error("", e);
        }
    }

	private static void tryGetMeanSize(PrintStream out, Path path) {
        try (Stream<String> lines = Files.lines(path)) {
            Map<String, Long> stats = lines
					.filter(l -> !l.endsWith("-") && !l.split(" ")[6].contains("resources"))
                    .collect(Collectors.groupingBy(FunctionEx.makeFunction(linha -> linha.split(" ")[6]),
                            Collectors.counting()));
			out.println(path + "--------------------------");

			stats.entrySet().stream().sorted(Comparator.comparing(Entry<String, Long>::getValue).reversed())
					.forEach(entry -> out.println(entry.getKey() + "  ,  " + entry.getValue()));
		} catch (Exception e) {
			LOGGER.error("", e);
		}
	}

}
