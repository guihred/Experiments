package simplebuilder;

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

    public SimpleMenuBarBuilder addCheckMenuItem(String text) {
        CheckMenuItem item = new CheckMenuItem(text);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addCheckMenuItem(String text, EventHandler<ActionEvent> action) {
        CheckMenuItem item = new CheckMenuItem(text);
        item.setOnAction(action);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addMenu(String text) {
        menu = new Menu(text);
        menuBar.getMenus().add(menu);
        return this;
    }

    public SimpleMenuBarBuilder addMenuItem(String text) {
        MenuItem item = new MenuItem(text);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addMenuItem(String text, EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(action);
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addMenuItem(String text, Node graphic,String combination, EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
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

    public SimpleMenuBarBuilder addMenuItem(String text, String combination, EventHandler<ActionEvent> action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(action);
        item.setAccelerator(KeyCombination.keyCombination(combination));
        if (subMenu != null) {
            subMenu.getItems().add(item);
        } else {
            menu.getItems().add(item);
        }
        return this;
    }

    public SimpleMenuBarBuilder addRadioMenuItem(String text) {

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

    public SimpleMenuBarBuilder addSubMenu(String text) {
        subMenu = new Menu(text);
        menu.getItems().add(subMenu);
        return this;
    }

    @Override
    public MenuBar build() {
        return menuBar;
    }

    public SimpleMenuBarBuilder endSubMenu() {
        subMenu = null;
        return this;
    }

}
