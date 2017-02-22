package crypt;

import org.junit.Assert;
import org.junit.Test;

public final class ShiftCipherTest {
	@Test
	public void test() {
		int k = 20;
		String s = "cryptoisfun";
		String encrypt = ShiftCipherExercise.encrypt(k, s);
		Assert.assertEquals(s, ShiftCipherExercise.decrypt(k, encrypt));
	}

}
