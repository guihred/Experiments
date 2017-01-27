package others;

import javafx.scene.text.Text;

public class SimpleTextBuilder extends SimpleShapeBuilder<Text, SimpleTextBuilder> implements SimpleBuilder<Text> {

	private Text text;

	public SimpleTextBuilder() {
		super(new Text());
		text = shape;
	}

	public SimpleTextBuilder managed(boolean x) {
		text.setManaged(x);
		return this;
	}
	public SimpleTextBuilder font(javafx.scene.text.Font x) {
		text.setFont(x);
		return this;
	}

	public SimpleTextBuilder fontSmoothingType(javafx.scene.text.FontSmoothingType x) {
		text.setFontSmoothingType(x);
		return this;
	}

	public SimpleTextBuilder strikethrough(boolean x) {
		text.setStrikethrough(x);
		return this;
	}

	public SimpleTextBuilder text(java.lang.String x) {
		text.setText(x);
		return this;
	}

	public SimpleTextBuilder textAlignment(javafx.scene.text.TextAlignment x) {
		text.setTextAlignment(x);
		return this;
	}

	public SimpleTextBuilder textOrigin(javafx.geometry.VPos x) {
		text.setTextOrigin(x);

		return this;
	}

	public SimpleTextBuilder underline(boolean x) {
		text.setUnderline(x);

		return this;
	}

	public SimpleTextBuilder wrappingWidth(double x) {
		text.setWrappingWidth(x);

		return this;
	}

	public SimpleTextBuilder x(double x) {
		text.setX(x);

		return this;
	}

	public SimpleTextBuilder y(double x) {
		text.setY(x);
		return this;
	}



}