package cn.wemarket.wxfront.common.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 配置文件中出现密码参数时，应为密文存储，故应提供解密方法
 * */
public class RSAUtils {
    private static final String KEY_ALGORITHM = "RSA";
    private static final String CHARSETS_UTF8 = "UTF-8";
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static final int MAX_DECRYPT_BLOCK = 128;

    public static String encryptWithPublicKey(String plainText, PublicKey publicKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] data = plainText.getBytes(CHARSETS_UTF8);
        int inputLen = data.length;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int offset = 0;
            byte[] cache = null;
            int i = 0;

            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_ENCRYPT_BLOCK;
            }

            return Base64.encodeBase64String(out.toByteArray());
        }
    }

    public static String decryptWithPrivateKey(String encryptData, PrivateKey privateKey)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] data = Base64.decodeBase64(encryptData);
        int inputLen = data.length;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int offset = 0;
            byte[] cache = null;
            int i = 0;

            while (inputLen - offset > 0) {
                if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                    cache = cipher.doFinal(data, offset, MAX_DECRYPT_BLOCK);
                } else {
                    cache = cipher.doFinal(data, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i++;
                offset = i * MAX_DECRYPT_BLOCK;
            }

            return new String(out.toByteArray(), CHARSETS_UTF8);
        }
    }
}
