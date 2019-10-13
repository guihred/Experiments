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
        int size = 30;
        List<Node> togglesAs = new SimpleToggleGroupBuilder()
            .addToggle(PaintToolHelper.getIconByURL("opaqueSelection.png", size), SelectOption.OPAQUE)
            .addToggle(PaintToolHelper.getIconByURL("transparentSelection.png", size), SelectOption.TRANSPARENT)
            .onChange(listener).select(option).getTogglesAs(Node.class);
        return togglesAs;
    }

}