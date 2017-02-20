package simplebuilder;

import javafx.scene.text.Text;

public class SimpleTextBuilder extends SimpleShapeBuilder<Text, SimpleTextBuilder> {

	public SimpleTextBuilder() {
		super(new Text());
	}

	public SimpleTextBuilder managed(boolean x) {
		shape.setManaged(x);
		return this;
	}
	public SimpleTextBuilder font(javafx.scene.text.Font x) {
		shape.setFont(x);
		return this;
	}

	public SimpleTextBuilder fontSmoothingType(javafx.scene.text.FontSmoothingType x) {
		shape.setFontSmoothingType(x);
		return this;
	}

	public SimpleTextBuilder strikethrough(boolean x) {
		shape.setStrikethrough(x);
		return this;
	}

	public SimpleTextBuilder text(java.lang.String x) {
		shape.setText(x);
		return this;
	}

	public SimpleTextBuilder textAlignment(javafx.scene.text.TextAlignment x) {
		shape.setTextAlignment(x);
		return this;
	}

	public SimpleTextBuilder textOrigin(javafx.geometry.VPos x) {
		shape.setTextOrigin(x);

		return this;
	}

	public SimpleTextBuilder underline(boolean x) {
		shape.setUnderline(x);

		return this;
	}

	public SimpleTextBuilder wrappingWidth(double x) {
		shape.setWrappingWidth(x);

		return this;
	}

	public SimpleTextBuilder x(double x) {
		shape.setX(x);

		return this;
	}

	public SimpleTextBuilder y(double x) {
		shape.setY(x);
		return this;
	}



}