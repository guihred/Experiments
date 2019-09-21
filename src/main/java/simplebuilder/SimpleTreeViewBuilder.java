package simplebuilder;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class SimpleTreeViewBuilder<T> extends SimpleRegionBuilder<TreeView<T>, SimpleTreeViewBuilder<T>> {
    private TreeView<T> treeView;
	private TreeItem<T> last;
    public SimpleTreeViewBuilder() {
        super(new TreeView<>());
        treeView = region;
    }

    public SimpleTreeViewBuilder<T> addItem(T value) {
        treeView.getRoot().getChildren().add(new TreeItem<>(value));
        return this;
    }

	public SimpleTreeViewBuilder<T> addItem(T value, List<T> children) {
        addToRoot(treeView, value, children);
	    return this;
	}

    @SuppressWarnings("unchecked")
    public SimpleTreeViewBuilder<T> addItem(T value, T... children) {
        TreeItem<T> e = new TreeItem<>(value);
		e.getChildren().addAll(Stream.of(children).map(TreeItem<T>::new).collect(Collectors.toList()));
        treeView.getRoot().getChildren().add(e);
        return this;
    }

    public TreeItem<T> addItemToLast(T value) {
		TreeItem<T> e = new TreeItem<>(value);
		last.getChildren().add(e);
		return e;
	}

	@Override
    public TreeView<T> build() {
        return treeView;
    }


    public SimpleTreeViewBuilder<T> editable(boolean value) {
        treeView.setEditable(value);
        return this;
    }

    public SimpleTreeViewBuilder<T> onSelect(Consumer<TreeItem<T>> consume) {
        treeView.getSelectionModel().selectedItemProperty().addListener((ob, old, n) -> consume.accept(n));
        return this;
    }

    public SimpleTreeViewBuilder<T> root(T value) {
        treeView.setRoot(new TreeItem<>(value));
		last = treeView.getRoot();
        return this;
    }
    public SimpleTreeViewBuilder<T> root(TreeItem<T> value) {
		treeView.setRoot(value);
		last = treeView.getRoot();
		return this;
	}

	public SimpleTreeViewBuilder<T> showRoot(boolean value) {
        treeView.setShowRoot(value);
        return this;
    }

    public static <S>void addToRoot(TreeView<S> treeView,S value, List<S> children) {
        TreeItem<S> e = new TreeItem<>(value);
	    e.getChildren().addAll(children.stream().map(TreeItem<S>::new).collect(Collectors.toList()));
	    treeView.getRoot().getChildren().add(e);
    }

}
