package simplebuilder;

@FunctionalInterface
public interface SimpleBuilder<T> {
	T build();
}
