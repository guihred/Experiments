package simplebuilder;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class SimpleTreeViewBuilder<T> extends SimpleRegionBuilder<TreeView<T>, SimpleTreeViewBuilder<T>> {
    private TreeView<T> treeView;

    public SimpleTreeViewBuilder() {
        super(new TreeView<>());
        treeView = region;
    }

    public SimpleTreeViewBuilder<T> addItem(T value) {
        treeView.getRoot().getChildren().add(new TreeItem<>(value));
        return this;
    }

    @SuppressWarnings("unchecked")
    public SimpleTreeViewBuilder<T> addItem(T value, T... children) {
        TreeItem<T> e = new TreeItem<>(value);
        e.getChildren().addAll(Stream.of(children).map(TreeItem::new).collect(Collectors.toList()));
        treeView.getRoot().getChildren().add(e);
        return this;
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

    public SimpleTreeViewBuilder<T> showRoot(boolean value) {
        treeView.setShowRoot(value);
        return this;
    }

}
