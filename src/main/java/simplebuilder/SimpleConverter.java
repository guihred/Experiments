package simplebuilder;

import static utils.ClassReflectionUtils.invoke;
import static utils.FunctionEx.makeFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javafx.beans.NamedArg;
import javafx.util.StringConverter;

public class SimpleConverter<T> extends StringConverter<T> {
    private Function<T, String> func;
    private Map<String, T> mapaLayout = new HashMap<>();
    private String name;

    public SimpleConverter() {
        this.func = f -> Objects.toString(f, "");
    }

    public SimpleConverter(Function<T, String> func) {
        this.func = func;
    }

    public SimpleConverter(@NamedArg("name") String name) {
        this.name = name;
        this.func = makeFunction(f -> Objects.toString(invoke(f, name), ""));
        if (name != null && name.contains(".")) {
            String[] split = name.split("\\.");
            this.func = makeFunction(f -> Objects.toString(invoke(invoke(f, split[0]), split[1]), ""));
        }

    }

    @Override
    public T fromString(String arg0) {
        return mapaLayout.get(arg0);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString(T lay) {
        String simpleName = func.apply(lay);
        mapaLayout.put(simpleName, lay);
        return simpleName;
    }
}