package simplebuilder;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class SimpleTableViewBuilder<T> extends SimpleRegionBuilder<TableView<T>, SimpleTableViewBuilder<T>> {

    private static final int COLUMN_DEFAULT_WIDTH = 150;
    private TableView<T> table;

    public SimpleTableViewBuilder() {
        super(new TableView<T>());
        table = node;
	}

	@SuppressWarnings("unchecked")
	public <V extends TableCell<T, Object>> SimpleTableViewBuilder<T> addColumn(final String columnName,
    		final BiConsumer<T, V> value) {
		final TableColumn<T, Object> column = new TableColumn<>(columnName);
		column.setCellFactory(p -> new CustomableTableCell<T, Object>() {
			@Override
            protected void setStyleable(final T auxMed) {
				value.accept(auxMed, (V) this);
			}

		});
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
    	table.getColumns().add(column);
    	return this;
    }

	public <V> SimpleTableViewBuilder<T> addColumn(final String columnName,
            final Callback<TableColumn<T, V>, TableCell<T, V>> value) {
        final TableColumn<T, V> column = new TableColumn<>(columnName);
        column.setCellFactory(value);
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final Function<T, String> propertyName) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(
                param -> new SimpleStringProperty(Objects.toString(propertyName.apply(param.getValue()))));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final String propertyName) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final String propertyName, final boolean editable) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        column.setEditable(editable);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final String propertyName,
            final Callback<TableColumn<T, String>, TableCell<T, String>> value) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setCellFactory(value);
        column.setPrefWidth(COLUMN_DEFAULT_WIDTH);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> addColumn(final String columnName, final String propertyName, final double prefWidth) {
        final TableColumn<T, String> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(prefWidth);
        table.getColumns().add(column);
        return this;
    }

    public SimpleTableViewBuilder<T> equalColumns() {
        ObservableList<TableColumn<T, ?>> columns = table.getColumns();

        columns.forEach(c -> c.prefWidthProperty().bind(table.prefWidthProperty().divide(columns.size())));
        return this;
    }

    public SimpleTableViewBuilder<T> items(final ObservableList<T> value) {
        table.setItems(value);
        return this;
    }

    public SimpleTableViewBuilder<T> onDoubleClick(final Consumer<T> object) {
        node.setOnMouseClicked(e->{
            if (e.getClickCount() > 1) {
                T selectedItem = table.getSelectionModel().getSelectedItem();
                object.accept(selectedItem);
            }
        });
        return this;
    }

    public SimpleTableViewBuilder<T> onSelect(final BiConsumer<T, T> value) {
        table.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> value.accept(oldValue, newValue));
        return this;
    }

	private abstract class CustomableTableCell<M, X> extends TableCell<M, X> {

		protected abstract void setStyleable(M auxMed);

        @Override
		protected void updateItem(final X item, final boolean empty) {
			super.updateItem(item, empty);
			int index = getIndex();
			int size = getTableView().getItems().size();
			if (index >= 0 && index < size) {
				M auxMed = getTableView().getItems().get(index);
				setStyleable(auxMed);
			}
		}
	}

}