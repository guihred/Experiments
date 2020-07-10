package ml.data;

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