package in.lazygod.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {
    private static String secret;

    public static void setSecret(String key) {
        secret = key;
    }

    private static Cipher cipher(int mode) throws Exception {
        SecretKeySpec spec = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(mode, spec);
        return c;
    }

    public static String encrypt(String plain) throws Exception {
        Cipher c = cipher(Cipher.ENCRYPT_MODE);
        byte[] enc = c.doFinal(plain.getBytes());
        return Base64.getEncoder().encodeToString(enc);
    }

    public static String decrypt(String encrypted) throws Exception {
        Cipher c = cipher(Cipher.DECRYPT_MODE);
        byte[] dec = c.doFinal(Base64.getDecoder().decode(encrypted));
        return new String(dec);
    }
}
