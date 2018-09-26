package simplebuilder;

import java.util.function.BiConsumer;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SimpleTableViewBuilder<T> extends SimpleRegionBuilder<TableView<T>, SimpleTableViewBuilder<T>> {

    private TableView<T> table;

    public SimpleTableViewBuilder() {
        super(new TableView<T>());
        table = node;
	}

    public SimpleTableViewBuilder<T> addColumn(String columnName, String propertyName) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(150);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(String columnName, String propertyName, double prefWidth) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(prefWidth);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> items(ObservableList<T> value) {
        table.setItems(value);
        return this;
    }

    public SimpleTableViewBuilder<T> onSelect(BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
        return this;
    }

}