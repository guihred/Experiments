package others;

import difflib.DiffRow;
import difflib.DiffRowGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiffTest {
	public static final Logger LOGGER = LoggerFactory.getLogger(DiffTest.class);

	private DiffTest() {
	}

	public static void main(String[] args) {
		List<String> original = fileToLines("adicionar-notificacao.html");
		List<String> revised = fileToLines("adicionar-notificacao2.html");

		// Compute diff. Get the Patch object. Patch is the container for
		// computed deltas.
		// Patch patch = DiffUtils.diff(original, revised);
		//
		//
		// for (Delta delta : patch.getDeltas()) {
		// System.out.println(delta);
		// }
		DiffRowGenerator generator = new DiffRowGenerator.Builder().build();

		List<DiffRow> rows = generator.generateDiffRows(original, revised);
		rows.forEach(System.out::println);

	}

	private static List<String> fileToLines(String string) {
		try {
			return Files.lines(Paths.get(string)).collect(Collectors.toList());
		} catch (IOException e) {
			LOGGER.error("", e);
			return null;
		}

	}
}
