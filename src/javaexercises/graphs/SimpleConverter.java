package javaexercises.graphs;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javafx.util.StringConverter;

class SimpleConverter<T> extends StringConverter<T> {
	Map<String, T> mapaLayout = new HashMap<>();
	private Function<T, String> func;

	public SimpleConverter(Function<T, String> func) {
		this.func = func;
	}

	@Override
	public String toString(T lay) {
		String simpleName = func.apply(lay);
		mapaLayout.put(simpleName, lay);
		return simpleName;
	}

	@Override
	public T fromString(String arg0) {
		return mapaLayout.get(arg0);
	}
}