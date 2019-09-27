package schema.sngpc;

import static java.util.stream.Collectors.joining;
import static utils.ResourceFXUtils.getOutFile;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.ConstraintsBase;
import javafx.scene.paint.*;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

public final class FXMLConstants {
	public static final Map<String, String> PROPERTY_REMAP = ImmutableMap.<String, String>builder()
        .put("gridpane-column", "GridPane.columnIndex").put("gridpane-row", "GridPane.rowIndex")
        .put("hbox-hgrow", "HBox.hgrow").put("vbox-vgrow", "VBox.vgrow").put("tilepane-alignment", "TilePane.alignment")
        .put("stackpane-alignment", "StackPane.alignment").put("pane-bottom-anchor", "AnchorPane.bottomAnchor")
        .put("pane-right-anchor", "AnchorPane.rightAnchor").put("pane-left-anchor", "AnchorPane.leftAnchor")
        .put("pane-top-anchor", "AnchorPane.topAnchor").put("borderpane-alignment", "BorderPane.alignment")
        .put("gridpane-halignment", "GridPane.halignment").put("gridpane-valignment", "GridPane.valignment")
        .put("gridpane-column-span", "GridPane.columnSpan").put("gridpane-row-span", "GridPane.rowSpan").build();
	public static final List<Class<?>> CONDITIONAL_TAG_CLASSES = Arrays.asList(Insets.class, Font.class, Point3D.class,
        Material.class, PropertyValueFactory.class, ConstraintsBase.class, EventTarget.class, Effect.class,
        StringConverter.class, SelectionModel.class, Paint.class, Enum.class, Number.class);
	public static final Map<String, Function<Collection<?>, String>> FORMAT_LIST = ImmutableMap
        .<String, Function<Collection<?>, String>>builder()
        .put("styleClass", l -> l.stream().map(Object::toString).collect(joining(" ")))
        .put("stylesheets", FXMLConstants::mapStylesheet).build();
	public static final List<Class<?>> NEW_TAG_CLASSES = Arrays.asList(ConstraintsBase.class, EventTarget.class,
        PathElement.class);
	public static final List<Class<?>> REFERENCE_CLASSES = Arrays.asList(ToggleGroup.class, Image.class);
	public static final List<Class<?>> NECESSARY_REFERENCE = Arrays.asList(Control.class, Text.class);
	public static final List<Class<?>> ATTRIBUTE_CLASSES = Arrays.asList(Double.class, String.class, Color.class,
        LinearGradient.class, RadialGradient.class, Long.class, Integer.class, Boolean.class, Enum.class,
        KeyCombination.class);
	public static final List<Class<?>> METHOD_CLASSES = Arrays.asList(EventHandler.class);
	public static final List<String> IGNORE = Arrays.asList("needsLayout", "layoutBounds", "baselineOffset",
        "localToParentTransform", "eventDispatcher", "skin", "background", "controlCssMetaData", "pseudoClassStates",
        "localToSceneTransform", "parentPopup", "cssMetaData", "classCssMetaData", "boundsInParent", "boundsInLocal",
        "scene", "childrenUnmodifiable", "styleableParent", "parent", "labelPadding");
    private FXMLConstants() {
    }

	private static String mapStylesheet(Collection<?> l) {
        String s = "file:/" + getOutFile().getParentFile().toString().replaceAll("\\\\", "/");
        String sd = "file:/" + new File("src/main/resources/").getAbsolutePath().replaceAll("\\\\", "/");
        return l.stream().map(st -> st.toString().replaceAll(s + "|" + sd, "@")).distinct().collect(joining(", "));
    }

}
