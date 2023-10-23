package io.sim.messages;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

// https://www.devmedia.com.br/utilizando-criptografia-simetrica-em-java/31170
// Rijndael

public class Cryptography
{
    private static final String IV = "AAAAAAAAAAAAAAAA";
    private static final String ENCRIPTION_KEY = "0123456789abcdef";

    public static byte[] encrypt(String _textopuro) throws Exception
    {
        Cipher encripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(ENCRIPTION_KEY.getBytes("UTF-8"), "AES");
        encripta.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return encripta.doFinal(_textopuro.getBytes("UTF-8"));
    }
    
    public static String decrypt(byte[] textoencriptado) throws Exception
    {
        Cipher decripta = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        SecretKeySpec key = new SecretKeySpec(ENCRIPTION_KEY.getBytes("UTF-8"), "AES");
        decripta.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        return new String(decripta.doFinal(textoencriptado),"UTF-8");
    }
}
