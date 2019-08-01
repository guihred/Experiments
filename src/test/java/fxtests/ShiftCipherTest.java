package fxtests;

import crypt.ShiftCipherExercise;
import crypt.VigenereCCipher;
import crypt.VigenereCipher;
import crypt.VigenereXORCipher;
import org.junit.Assert;
import org.junit.Test;
import utils.HasLogging;
import utils.RunnableEx;

public final class ShiftCipherTest implements HasLogging {
    @Test
    public void testShiftCipher() {
        int k = 20;
        String s = "cryptoisfun";
        String encrypt = ShiftCipherExercise.encrypt(k, s);
        Assert.assertEquals("Description should be equal", s, ShiftCipherExercise.decrypt(k, encrypt));
    }

    @Test
    public void testVigenere() {
        new VigenereCCipher().inicio();
    }

    @Test
    public void testVigenereCipher() {
        VigenereCipher vigenereCypher = new VigenereCipher();
        String k = "spy";
        String msg = "seeyouatnoon";
        String encrypt = vigenereCypher.encrypt(k, msg);
        String decrypt = vigenereCypher.decrypt(k, encrypt);
        Assert.assertEquals("Vigenere Cipher should be equal", msg, decrypt);
    }
    @Test
    public void testVigenereXOR() {
        try {
            VigenereXORCipher vigenereCypher = new VigenereXORCipher();
            String k = "spy";
            String msg = "seeyouatnoon";
            String encrypt = vigenereCypher.encrypt(k, msg);
            String decrypt = vigenereCypher.decrypt(k, encrypt);
            Assert.assertEquals("Vigenere Cipher should be equal", msg, decrypt);
        } catch (Exception e) {
            getLogger().error("", e);
        }
    }

    @Test
    public void testVigenereXORFindKey() {
        try {
            Thread thread = new Thread(RunnableEx.make(() -> {
                long decrypt = new VigenereXORCipher().findKeySize();
                getLogger().error(" key size found {}", decrypt);
            }));
            thread.start();
            long start = System.currentTimeMillis();
            final long maxTime = 30_000;
            while (System.currentTimeMillis() - start < maxTime) {
				// DOES NOTHING
            }
            thread.interrupt();
        } catch (Exception e) {
            getLogger().error("", e);
        }
    }


}
