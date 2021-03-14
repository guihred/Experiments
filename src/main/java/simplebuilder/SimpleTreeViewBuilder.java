package simplebuilder;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import utils.ex.ConsumerEx;

public class SimpleTreeViewBuilder<T> extends SimpleRegionBuilder<TreeView<T>, SimpleTreeViewBuilder<T>> {

    public SimpleTreeViewBuilder() {
        super(new TreeView<>());
    }

    private SimpleTreeViewBuilder(TreeView<T> tree) {
        super(tree);
    }

    public SimpleTreeViewBuilder<T> cellFactory(BiConsumer<T, TreeCell<T>> value) {
        return cellFactory(newCellFactory(value));
    }

    public SimpleTreeViewBuilder<T> cellFactory(Callback<TreeView<T>, TreeCell<T>> value) {
        node.setCellFactory(value);
        return this;
    }


    public SimpleTreeViewBuilder<T> onSelect(ConsumerEx<TreeItem<T>> consume) {
        node.getSelectionModel().selectedItemProperty()
                .addListener((ob, old, n) -> ConsumerEx.accept(consume, n));
        return this;
    }

    public SimpleTreeViewBuilder<T> root(T value) {
        node.setRoot(new TreeItem<>(value));
        return this;
    }

    public static <S> void addToRoot(TreeView<S> treeView, S value, Collection<S> children) {
        TreeItem<S> e = new TreeItem<>(value);
        e.getChildren().addAll(children.stream().map(TreeItem<S>::new).collect(Collectors.toList()));
        treeView.getRoot().getChildren().add(e);
    }

    public static <C> Callback<TreeView<C>, TreeCell<C>> newCellFactory(BiConsumer<C, TreeCell<C>> value) {
        return p -> new TreeCell<C>() {
            @Override
            protected void updateItem(final C item, final boolean empty) {
                super.updateItem(item, empty);
                value.accept(getItem(), this);
            }
        };
    }

    public static <V>SimpleTreeViewBuilder<V> of(TreeView<V> treeView) {
        return new SimpleTreeViewBuilder<>(treeView);
    }

    public static <T> void onSelect(TreeView<T> treeView, Consumer<TreeItem<T>> consume) {
        treeView.getSelectionModel().selectedItemProperty().addListener((ob, old, n) -> consume.accept(n));
    }

}
