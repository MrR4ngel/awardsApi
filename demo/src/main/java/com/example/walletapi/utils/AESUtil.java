package com.example.walletapi.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class AESUtil {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    // Chave de 16 bytes para AES-128 (mesma chave usada no Python)
    private static final String SECRET_KEY = "7235347196759404";

    public static String encrypt(String data) throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[16];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

        byte[] encryptedDataWithIV = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, encryptedDataWithIV, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedDataWithIV, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedDataWithIV);
    }

    public static String decrypt(String encryptedData) throws Exception {
        // Imprime o valor completo recuperado para depuração
        System.out.println("Encrypted data from DB: " + encryptedData);

        // Decodifica a string Base64 para bytes
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        System.out.println("Decoded total length: " + decodedData.length + " bytes");

        // Extrai o IV: os 16 primeiros bytes
        byte[] iv = Arrays.copyOfRange(decodedData, 0, 16);
        System.out.println("IV (Base64): " + Base64.getEncoder().encodeToString(iv));

        // O restante são os bytes do ciphertext
        byte[] ciphertext = Arrays.copyOfRange(decodedData, 16, decodedData.length);
        System.out.println("Ciphertext (Base64): " + Base64.getEncoder().encodeToString(ciphertext));

        // Inicializa o Cipher para decriptação
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // Descriptografa
        byte[] decrypted = cipher.doFinal(ciphertext);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
