package simplebuilder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import utils.ImageFXUtils;
import utils.StringSigaUtils;
import utils.ex.ConsumerEx;
import utils.ex.FunctionEx;

public class SimpleListViewBuilder<T> extends SimpleRegionBuilder<ListView<T>, SimpleListViewBuilder<T>> {

    public SimpleListViewBuilder() {
        super(new ListView<T>());
    }

    private SimpleListViewBuilder(ListView<T> o) {
        super(o);
    }

    public SimpleListViewBuilder<T> cellFactory(Callback<ListView<T>, ListCell<T>> value) {
        node.setCellFactory(value);
        return this;
    }

    public SimpleListViewBuilder<T> cellFactory(FunctionEx<T, String> func) {
        return cellFactory(newCellFactory((t, cell) -> cell.setText(FunctionEx.apply(func, t))));
    }

    public SimpleListViewBuilder<T> copiable() {
        onKeyReleased(ev -> copyContent(node, ev));
        return this;
    }

    public SimpleListViewBuilder<T> deletable() {
        onKey(KeyCode.DELETE, node.getItems()::remove);
        return this;
    }

    public SimpleListViewBuilder<T> fixedCellSize(double value) {
        node.setFixedCellSize(value);
        return this;
    }

    public SimpleListViewBuilder<T> items(final Collection<T> value) {
        node.setItems(FXCollections.observableArrayList(value));
        return this;
    }

    public SimpleListViewBuilder<T> items(final ObservableList<T> value) {
        node.setItems(value);
        return this;
    }

    public SimpleListViewBuilder<T> multipleSelection() {
        node.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        return this;
    }

    public SimpleListViewBuilder<T> onDoubleClick(final ConsumerEx<T> object) {
        onDoubleClick(node, object);
        return this;
    }

    public SimpleListViewBuilder<T> onKey(KeyCode code, ConsumerEx<T> object) {
        SimpleNodeBuilder.onKeyReleased(node, e -> {
            if (code == e.getCode()) {
                List<T> selectedItem = node.getSelectionModel().getSelectedItems();
                for (T t : selectedItem) {
                    ConsumerEx.accept(object, t);
                }
            }
        });

        return this;

    }

    public SimpleListViewBuilder<T> onSelect(final BiConsumer<T, T> value) {
        node.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
        return this;
    }

    public SimpleListViewBuilder<T> pasteable(FunctionEx<String, T> f) {
        SimpleNodeBuilder.onKeyReleased(node, e -> {
            if (KeyCode.V == e.getCode() && e.isControlDown()) {
                String string = Objects.toString(ImageFXUtils.getClipboardString(), "");
                for (String string2 : string.split("[\n,\t\r]+")) {
                    T apply = FunctionEx.apply(f, string2.trim());
                    if (apply != null && StringUtils.isNotBlank(Objects.toString(apply, ""))
                            && !node.getItems().contains(apply)) {
                        node.getItems().add(apply);
                    }
                }
            }
        });
        return this;
    }

    public ObservableList<T> selected() {
        return node.getSelectionModel().getSelectedItems();
    }

    public static <T> void copyContent(ListView<T> table, KeyEvent ev) {
        if (ev.isControlDown() && ev.getCode() == KeyCode.C) {
            List<Integer> selectedItems = table.getSelectionModel().getSelectedIndices().isEmpty()
                    ? IntStream.range(0, table.getItems().size()).boxed().collect(Collectors.toList())
                    : table.getSelectionModel().getSelectedIndices();
            String content = getSelectedContent(table, selectedItems);
            ImageFXUtils.setClipboardContent(content);
        }
    }

    public static <C> Callback<ListView<C>, ListCell<C>> newCellFactory(final BiConsumer<C, ListCell<C>> value) {
        return p -> new CustomListCell<>(value);
    }

    public static <C> Callback<ListView<C>, ListCell<C>> newCellFactory(final FunctionEx<C, String> func) {
        return newCellFactory((t, cell) -> cell.setText(FunctionEx.apply(func, t)));
    }

    public static <V> SimpleListViewBuilder<V> of(ListView<V> o) {
        return new SimpleListViewBuilder<>(o);
    }

    public static <C> void onDoubleClick(ListView<C> table2, final ConsumerEx<C> object) {
        table2.setOnMouseClicked(e -> {
            if (e.getClickCount() > 1) {
                ObservableList<C> selectedItem = table2.getSelectionModel().getSelectedItems();
                ConsumerEx.foreach(selectedItem, object);
            }
        });
        SimpleNodeBuilder.onKeyReleased(table2, e -> {
            if (e.getCode() == KeyCode.ENTER) {
                ObservableList<C> selectedItem = table2.getSelectionModel().getSelectedItems();
                ConsumerEx.foreach(selectedItem, object);
            }
        });
    }

    public static <T> void onSelect(ListView<T> table, final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
    }

    private static <T> CustomListCell<T> getCustomList(ListView<T> table) {
        Callback<ListView<T>, ListCell<T>> cellFactory = table.getCellFactory();
        ListCell<T> call2 = cellFactory.call(table);
        if (call2 instanceof CustomListCell) {
            return (CustomListCell<T>) call2;
        }
        table.setCellFactory(newCellFactory(StringSigaUtils::toStringSpecial));
        return (CustomListCell<T>) table.getCellFactory().call(table);
    }

    private static <T> String getSelectedContent(ListView<T> table, List<Integer> selectedItems) {
        if (table.getCellFactory() == null) {
            return selectedItems.stream().filter(l -> table.getItems().size() > l)
                    .map(l -> StringSigaUtils.toStringSpecial(table.getItems().get(l)))
                    .collect(Collectors.joining("\n"));
        }
        CustomListCell<T> call = getCustomList(table);
        return selectedItems.stream().map(l -> {
            T t = table.getItems().get(l);
            call.updateItem(t, false);
            return Objects.toString(call.getText());
        }).collect(Collectors.joining("\n"));
    }

    private static final class CustomListCell<C> extends ListCell<C> {
        private final BiConsumer<C, ListCell<C>> value;

        private CustomListCell(BiConsumer<C, ListCell<C>> value) {
            this.value = value;
        }

        @Override
        public void updateItem(final C item, final boolean empty) {
            super.updateItem(item, empty);
            value.accept(getItem(), this);
        }
    }

}