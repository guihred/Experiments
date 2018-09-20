package graphs.app;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javafx.util.StringConverter;

class SimpleConverter<T> extends StringConverter<T> {
	private Function<T, String> func;
	private Map<String, T> mapaLayout = new HashMap<>();

	public SimpleConverter(Function<T, String> func) {
		this.func = func;
	}

	@Override
	public T fromString(String arg0) {
		return mapaLayout.get(arg0);
	}

	@Override
	public String toString(T lay) {
		String simpleName = func.apply(lay);
		mapaLayout.put(simpleName, lay);
		return simpleName;
	}
}