package others;

import difflib.DiffRow;
import difflib.DiffRowGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiffTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(DiffTest.class);

	private DiffTest() {
	}

	public static void main(String[] args) {
		List<String> original = fileToLines("adicionar-notificacao.html");
		List<String> revised = fileToLines("adicionar-notificacao2.html");

		// Compute diff. Get the Patch object. Patch is the container for
		// computed deltas.
		DiffRowGenerator generator = new DiffRowGenerator.Builder().build();

		List<DiffRow> rows = generator.generateDiffRows(original, revised);
        rows.forEach(s -> LOGGER.info("{}", s));

	}

	private static List<String> fileToLines(String string) {
        try (Stream<String> lines = Files.lines(Paths.get(string))) {
            return lines.collect(Collectors.toList());
		} catch (IOException e) {
			LOGGER.error("", e);
            return Collections.emptyList();
		}

	}
}
