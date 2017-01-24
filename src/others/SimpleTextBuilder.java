package others;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SimpleTextBuilder {
	private Text text = new Text();

	public Text build() {

		return text;
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

	public SimpleTextBuilder fill(Color fill) {
		text.setFill(fill);
		return this;
	}

	public SimpleTextBuilder styleClass(String string) {
		text.setStyle(string);
		return this;
	}

	public SimpleTextBuilder id(String id) {
		text.setId(id);
		return this;
	}
}