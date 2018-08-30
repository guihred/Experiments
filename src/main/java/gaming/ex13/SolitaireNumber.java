package gaming.ex13;

public enum SolitaireNumber {
	ACE(1,"A"),
	NUMBER_2(2,"2"),
	NUMBER_3(3,"3"),
	NUMBER_4(4,"4"),
	NUMBER_5(5,"5"),
	NUMBER_6(6,"6"),
	NUMBER_7(7,"7"),
	NUMBER_8(8,"8"),
	NUMBER_9(9,"9"),
	NUMBER_10(10,"10"),
	JACK(11,"J"),
	QUEEN(12,"Q"),
	KING(13,"K");

	private final int number;
	private final String representation;

    SolitaireNumber(int number, String representation) {
		this.number = number;
		this.representation = representation;
		
	}

	public int getNumber() {
		return number;
	}

	public String getRepresentation() {
		return representation;
	}


	
}
