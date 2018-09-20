package exercise;

import crypt.ShiftCipherExercise;
import crypt.VigenereCCipher;
import crypt.VigenereCipher;
import crypt.VigenereXORCipher;
import org.junit.Assert;
import org.junit.Test;
import utils.HasLogging;

public final class ShiftCipherTest implements HasLogging {
    @Test
    public void test() {
        int k = 20;
        String s = "cryptoisfun";
        String encrypt = ShiftCipherExercise.encrypt(k, s);
        Assert.assertEquals("Description should be equal", s, ShiftCipherExercise.decrypt(k, encrypt));
    }

    @Test
    public void vigenereTest() {
        new VigenereCCipher().inicio();
    }

    @Test
    public void vigenereCipherTest() {
        VigenereCipher vigenereCypher = new VigenereCipher();
        String k = "spy";
        String msg = "seeyouatnoon";
        String encrypt = vigenereCypher.encrypt(k, msg);
        String decrypt = vigenereCypher.decrypt(k, encrypt);
        Assert.assertEquals("Vigenere Cipher should be equal", msg, decrypt);
    }

    @Test
    public void vigenereXORTest() {
        try {
            VigenereXORCipher vigenereCypher = new VigenereXORCipher();
            String k = "spy";
            String msg = "seeyouatnoon";
            String encrypt = vigenereCypher.encrypt(k, msg);
            String decrypt = vigenereCypher.decrypt(k, encrypt);
            Assert.assertEquals("Vigenere Cipher should be equal", msg, decrypt);
        } catch (Exception e) {
            VigenereXORCipher.LOGGER.error("", e);
        }
    }


}
