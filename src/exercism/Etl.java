package exercism;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class Etl {

	public Map<String, Integer> transform(Map<Integer, List<String>> old) {
		Map<String, Integer> collect = old.entrySet().stream()
				.flatMap(e -> e.getValue().stream().collect(Collectors.toMap(String::toLowerCase, a -> e.getKey())).entrySet().stream())
				.collect(Collectors.toMap(Entry<String, Integer>::getKey, Entry<String, Integer>::getValue));

		return collect;
	}

}