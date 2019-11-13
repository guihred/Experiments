package others;

import java.util.function.IntBinaryOperator;

public final class EanFactorReducer implements IntBinaryOperator {
	private int factor = 1;

	@Override
	public int applyAsInt(int sum, int i) {
		factor = factor == 1 ? 3 : 1;
		return sum + i * factor;
	}

    public static boolean validate(final String eanCode) {
    
    	int checksum = eanCode.chars().limit(eanCode.length() - 1L).map(i -> Character.getNumericValue((char) i))
    			.reduce(0, new EanFactorReducer());
    	checksum = (10 - checksum % 10) % 10;
    	return checksum == Character.getNumericValue(eanCode.charAt(eanCode.length() - 1));
    }
}