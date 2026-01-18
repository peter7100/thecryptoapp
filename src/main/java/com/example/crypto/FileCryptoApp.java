package com.example.crypto;

import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.util.Properties;

/*@ nullable_by_default @*/
public class FileCryptoApp {

    private static final String TRANSFORMATION = "AES/CTR/NoPadding";
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) throws Exception {
        byte[] key = new byte[16];
        byte[] iv = new byte[16];

        SecureRandom random = new SecureRandom();
        random.nextBytes(key);
        random.nextBytes(iv);

        encrypt("input.txt", "encrypted.dat", key, iv);
        decrypt("encrypted.dat", "decrypted.txt", key, iv);

        System.out.println("\n ===============OUTPUT====================.");
        System.out.println("\nEncryption and decryption completed successfully.");
    }

    /*@ 
      @ public normal_behavior
      @ requires input != null && input.length() > 0;
      @ requires output != null && output.length() > 0;
      @ requires key != null && key.length == 16;
      @ requires iv != null && iv.length == 16;
      @ ensures true;
      @*/
    public static void encrypt(/*@ non_null @*/ String input, /*@ non_null @*/ String output, 
                              byte[] key, byte[] iv) throws Exception {
        process(input, output, key, iv, Cipher.ENCRYPT_MODE);
    }

    /*@ 
      @ public normal_behavior
      @ requires input != null && input.length() > 0;
      @ requires output != null && output.length() > 0;
      @ requires key != null && key.length == 16;
      @ requires iv != null && iv.length == 16;
      @ ensures true;
      @*/
    public static void decrypt(/*@ non_null @*/ String input, /*@ non_null @*/ String output, 
                              byte[] key, byte[] iv) throws Exception {
        process(input, output, key, iv, Cipher.DECRYPT_MODE);
    }

    /*@ 
      @ private normal_behavior
      @ requires inputFile != null && inputFile.length() > 0;
      @ requires outputFile != null && outputFile.length() > 0;
      @ requires key != null && key.length == 16;
      @ requires iv != null && iv.length == 16;
      @ requires mode == Cipher.ENCRYPT_MODE || mode == Cipher.DECRYPT_MODE;
      @ ensures true;
      @*/
    private static void process(/*@ non_null @*/ String inputFile, /*@ non_null @*/ String outputFile,
                               byte[] key, byte[] iv, int mode) throws Exception {

        Properties properties = new Properties();
        CryptoCipher cipher = Utils.getCipherInstance(TRANSFORMATION, properties);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(mode, keySpec, ivSpec);

        try (FileChannel in = new FileInputStream(inputFile).getChannel();
             FileChannel out = new FileOutputStream(outputFile).getChannel()) {

            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            ByteBuffer outputBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            while (true) {
                inputBuffer.clear();
                int bytesRead = in.read(inputBuffer);
                if (bytesRead == -1) break;

                inputBuffer.flip();
                outputBuffer.clear();
                cipher.update(inputBuffer, outputBuffer);

                outputBuffer.flip();
                while (outputBuffer.hasRemaining()) {
                    out.write(outputBuffer);
                }
            }

            ByteBuffer empty = ByteBuffer.allocateDirect(0);
            outputBuffer.clear();
            cipher.doFinal(empty, outputBuffer);

            outputBuffer.flip();
            while (outputBuffer.hasRemaining()) {
                out.write(outputBuffer);
            }

        } finally {
            cipher.close();
        }
    }
}
