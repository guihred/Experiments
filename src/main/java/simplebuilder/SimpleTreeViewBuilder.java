package simplebuilder;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class SimpleTreeViewBuilder<T> extends SimpleRegionBuilder<TreeView<T>, SimpleTreeViewBuilder<T>> {
    private TreeView<T> treeView;

    public SimpleTreeViewBuilder() {
        super(new TreeView<>());
        treeView = region;
    }

    @Override
    public TreeView<T> build() {
        return treeView;
    }

    public SimpleTreeViewBuilder<T> cellFactory(BiConsumer<T, TreeCell<T>> value) {
		return cellFactory(newCellFactory(value));
    }

	public SimpleTreeViewBuilder<T> cellFactory(Callback<TreeView<T>, TreeCell<T>> value) {
    	treeView.setCellFactory(value);
    	return this;
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
        return this;
    }

	public static <S> void addToRoot(TreeView<S> treeView, S value, Collection<S> children) {
        TreeItem<S> e = new TreeItem<>(value);
        e.getChildren().addAll(children.stream().map(TreeItem<S>::new).collect(Collectors.toList()));
        treeView.getRoot().getChildren().add(e);
    }

	public static <C, V extends TreeCell<C>> Callback<TreeView<C>, TreeCell<C>> newCellFactory(
			final BiConsumer<C, V> value) {
		return p -> new CustomableTreeCell<C>() {
			@Override
			@SuppressWarnings("unchecked")
			protected void setVisual(C auxMed) {
				value.accept(auxMed, (V) this);
			}

		};
	}

	public abstract static class CustomableTreeCell<M> extends TreeCell<M> {

		protected abstract void setVisual(M auxMed);

		@Override
		protected void updateItem(final M item, final boolean empty) {
			super.updateItem(item, empty);
			setVisual(getItem());
		}
	}

}
