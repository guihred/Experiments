package ml.data;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class Question {
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
        if (type == QuestionType.NE) {
            return !Objects.equals(test, ob) && !Objects.equals(test, Objects.toString(ob));
        }
        if (type == QuestionType.CONTAINS) {
            return StringUtils.containsIgnoreCase(Objects.toString(test), Objects.toString(ob));
        }
		if (!(test instanceof Number) || !(ob instanceof Number)) {
			return false;
		}
		double number0 = ((Number) test).doubleValue();
		double number1 = ((Number) ob).doubleValue();
		switch (type) {
			case GE:
				return number0 >= number1;
			case GT:
				return number0 > number1;
			case LE:
				return number0 <= number1;
			case LT:
				return number0 < number1;
			default:
				return false;
		}
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
        NE("!="),
        CONTAINS("contains"),
		GE(">="),
		LE("<="),
		LT("<"),
		GT(">");

		private final String sign;

        QuestionType(String sign) {
			this.sign = sign;
		}

		public String getSign() {
			return sign;
		}

	}

}