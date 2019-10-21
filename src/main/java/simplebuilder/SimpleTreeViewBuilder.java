package simplebuilder;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

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

}
