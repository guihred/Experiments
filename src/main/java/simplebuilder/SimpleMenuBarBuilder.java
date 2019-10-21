package simplebuilder;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;

public class SimpleMenuBarBuilder implements SimpleBuilder<MenuBar> {
    private MenuBar menuBar = new MenuBar();
    private Menu menu;
    private Menu subMenu;
    private ToggleGroup toggleGroup;

    public SimpleMenuBarBuilder addCheckMenuItem(final String text) {
        CheckMenuItem item = new CheckMenuItem(text);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }


    public SimpleMenuBarBuilder addMenu(final String text) {
        menu = new Menu(text);
        menuBar.getMenus().add(menu);
        return this;
    }

    public SimpleMenuBarBuilder addMenuItem(final String text) {
        MenuItem item = new MenuItem(text);
		item.setId(text);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addMenuItem(final String text, final EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
		item.setId(text);
        item.setOnAction(action);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

	public SimpleMenuBarBuilder addMenuItem(final String text, final EventHandler<ActionEvent> action,
			final ObservableValue<? extends Boolean> disabled) {
		MenuItem item = new MenuItem(text);
		item.setId(text);
		item.setOnAction(action);
		if (subMenu != null) {
			subMenu.getItems().add(item);
		} else {
			menu.getItems().add(item);
		}
		item.disableProperty().bind(disabled);
		return this;
	}

    public SimpleMenuBarBuilder addMenuItem(final String text, final Node graphic, final String combination,
        final EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
		item.setId(text);
        item.setOnAction(action);
        item.setGraphic(graphic);
        item.setAccelerator(KeyCombination.keyCombination(combination));
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addMenuItem(final String text, final String combination,
        final EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
		item.setId(text);
        item.setOnAction(action);
        item.setAccelerator(KeyCombination.keyCombination(combination));
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addMenuItem(final String text, final String combination,
			final EventHandler<ActionEvent> action, final ObservableValue<? extends Boolean> disabled) {
		MenuItem item = new MenuItem(text);
		item.setId(text);
		item.setOnAction(action);
		item.setAccelerator(KeyCombination.keyCombination(combination));
		if (subMenu != null) {
			subMenu.getItems().add(item);
		} else {
			menu.getItems().add(item);
		}
		item.disableProperty().bind(disabled);
		return this;
	}


    public SimpleMenuBarBuilder addRadioMenuItem(final String text) {
        if (toggleGroup == null) {
            toggleGroup = new ToggleGroup();
        }
        RadioMenuItem item = new RadioMenuItem(text);
        item.setToggleGroup(toggleGroup);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addSeparator() {
        menu.getItems().add(new SeparatorMenuItem());
        return this;
    }

    public SimpleMenuBarBuilder addSubMenu(final String text) {
        subMenu = new Menu(text);
        menu.getItems().add(subMenu);
        return this;
    }

    @Override
    public MenuBar build() {
        return menuBar;
    }


}
