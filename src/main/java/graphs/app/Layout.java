package graphs.app;

@FunctionalInterface
public interface Layout {
	void execute();

	default String getName() {
		return getClass().getSimpleName().replace("Layout", "");
	}
}