package com.example.sunatoriVeteran.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class FileEncryptionUtil {

    private static final String ALGORITHM = "AES";
    // 16-byte key for AES-128 (Для продакшену ключ має зберігатися у змінних оточення!)
    private static final byte[] KEY = "SecretKey1234567".getBytes();

    public static byte[] encrypt(byte[] data) throws Exception {
        Key key = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData) throws Exception {
        Key key = new SecretKeySpec(KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }
}
