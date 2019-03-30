package ml.data;

import java.util.Objects;

class Question {
	private final Object ob;
	private QuestionType type = QuestionType.EQ;

	public Question(Object ob) {
		this.ob = ob;
	}

	public Question(Object ob, QuestionType type) {
		this.ob = ob;
		this.type = type;
	}

	public boolean answer(Object test) {
		return type == QuestionType.EQ && Objects.equals(test, ob)
				|| type == QuestionType.GE && test instanceof Number
						&& ((Number) test).doubleValue() >= ((Number) ob).doubleValue()
				|| type == QuestionType.GT && test instanceof Number
						&& ((Number) test).doubleValue() > ((Number) ob).doubleValue();
	}

	public enum QuestionType {
		EQ,
		GT,
		GE,
	}

}