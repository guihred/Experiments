package fxtests;

import static fxtests.FXTesting.measureTime;

import crypt.ShiftCipherExercise;
import crypt.VigenereCCipher;
import crypt.VigenereCipher;
import crypt.VigenereXORCipher;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import utils.ex.HasLogging;

@SuppressWarnings("static-method")
public final class ShiftCipherTest {
    private static final Logger LOG = HasLogging.log();

    @Test
    public void testShiftCipher() {
        int k = 20;
        String s = "cryptoisfun";
        String encrypt = measureTime("ShiftCipherExercise.encrypt", () -> ShiftCipherExercise.encrypt(k, s));
        String decrypt = measureTime("ShiftCipherExercise.decrypt", () -> ShiftCipherExercise.decrypt(k, encrypt));
        Assert.assertEquals("Description should be equal", s, decrypt);

    }

    @Test
    public void testVigenere() {
        FXTesting.measureTime("VigenereCCipher.inicio", () -> VigenereCCipher.inicio());

    }

    @Test
    public void testVigenereCipher() {
        VigenereCipher vigenereCypher = new VigenereCipher();
        String k = "spy";
        String msg = "seeyouatnoon";
        String encrypt = measureTime("VigenereCipher.encrypt", () -> vigenereCypher.encrypt(k, msg));
        String decrypt = measureTime("VigenereCipher.decrypt", () -> vigenereCypher.decrypt(k, encrypt));
        Assert.assertEquals("Vigenere Cipher should be equal", msg, decrypt);
    }

    @Test
    public void testVigenereXOR() {
        try {
            VigenereXORCipher vigenereCypher = new VigenereXORCipher();
            String k = "spy";
            String msg = "seeyouatnoon";
            String encrypt = measureTime("VigenereXORCipher.encrypt", () -> vigenereCypher.encrypt(k, msg));
            String decrypt = measureTime("VigenereXORCipher.decrypt", () -> vigenereCypher.decrypt(k, encrypt));
            Assert.assertEquals("Vigenere Cipher should be equal", msg, decrypt);
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    @Test
    public void testVigenereXORFindKey() {
        FXTesting.runInTime("VigenereXORCipher.findKey(7)", () -> new VigenereXORCipher().findKey(7), 5_000);
        FXTesting.runInTime("VigenereXORCipher.encrypt", () -> new VigenereXORCipher().encrypt("plainText", "cipher"),
                5_000);
        FXTesting.runInTime("VigenereXORCipher.findKeySize",
                () -> LOG.error(" key size found {}", new VigenereXORCipher().findKeySize()), 5_000);
    }

}
