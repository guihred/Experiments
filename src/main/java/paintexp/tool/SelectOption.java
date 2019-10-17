package paintexp.tool;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import simplebuilder.SimpleToggleGroupBuilder;

enum SelectOption {
	TRANSPARENT,
	OPAQUE;

    public static List<Node> createSelectOptions(ChangeListener<? super Toggle> listener, Object option) {
		final int size = 30;
        return new SimpleToggleGroupBuilder()
            .addToggle(PaintTool.getIconByURL("opaqueSelection.png", size), SelectOption.OPAQUE)
            .addToggle(PaintTool.getIconByURL("transparentSelection.png", size), SelectOption.TRANSPARENT)
            .onChange(listener).select(option).getTogglesAs(Node.class);
    }

}