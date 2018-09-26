package fxpro.ch07;

import javafx.util.StringConverter;

class SimpleStringConverter extends StringConverter<Number> {
	@Override
	public Number fromString(String s) {
		return Integer.valueOf(s) * 10;
	}

	@Override
	public String toString(Number n) {
		return String.valueOf(n.intValue() / 10);
	}
}