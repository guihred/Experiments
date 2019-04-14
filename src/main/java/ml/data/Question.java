package ml.data;

import java.util.Objects;

class Question {
	private final String colName;
	private final Object ob;
	private QuestionType type = QuestionType.EQ;
	private double infoGain;

	public Question(String colName, Object ob) {
		this.colName = colName;
		this.ob = ob;
	}

	public Question(String colName, Object ob, QuestionType type) {
		this.colName = colName;
		this.ob = ob;
		this.type = type;
	}

	public boolean answer(Object test) {
		if (type == QuestionType.EQ) {
			return Objects.equals(test, ob) || Objects.equals(test, Objects.toString(ob));
		}
		if (type == QuestionType.GE) {
			return test instanceof Number && ((Number) test).doubleValue() >= ((Number) ob).doubleValue();
		}
		if (type == QuestionType.GT) {
			return test instanceof Number && ((Number) test).doubleValue() > ((Number) ob).doubleValue();
		}
		if (type == QuestionType.LE) {
			return test instanceof Number && ((Number) test).doubleValue() <= ((Number) ob).doubleValue();
		}
		if (type == QuestionType.LT) {
			return test instanceof Number && ((Number) test).doubleValue() < ((Number) ob).doubleValue();
		}
		return false;
	}

	public String getColName() {
		return colName;
	}

	public double getInfoGain() {
		return infoGain;
	}

	public void setInfoGain(double infoGain) {
		this.infoGain = infoGain;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", getColName(), type.getSign(), ob);
	}

	public enum QuestionType {
		EQ("=="),
		GE(">="),
		LE("<="),
		LT("<"),
		GT(">");

		private final String sign;

		private QuestionType(String sign) {
			this.sign = sign;
		}

		public String getSign() {
			return sign;
		}

	}

}