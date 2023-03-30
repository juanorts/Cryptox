import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

public class Cryptox implements Serializable{
    private static final long serialVersionUID = 1L;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final String ASCII = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ASCIISimbols = "~`!@#$%^&*()_-+={[}]|\\\\:;\\\"'<,>.?/ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ASCIISimbolsDigits = "~`!@#$%^&*()_-+={[}]|\\\\\\\\:;\\\\\\\"'<,>.?/ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ASCIIDigits = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	private String key;
	private String salt;
    
    private SecretKey cryptoxKey;

    public Cryptox(String masterKey) {
    	SecretKeyFactory secretKeyFactory;
    	KeySpec keySpec;
    	this.key = hashKey(masterKey);
    	this.salt = hashKey(hashKey(masterKey));
    	try {
    		secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);
    		keySpec = new PBEKeySpec(key.toCharArray(), salt.getBytes(), 65536, 256);
    		cryptoxKey = secretKeyFactory.generateSecret(keySpec);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public static String hashKey(String key) {
        String hashedKey = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            hashedKey = bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashedKey;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

	public String getAES(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
    	byte[] initVector = new byte[16];
    	IvParameterSpec initVectorParameterSpec = new IvParameterSpec(initVector);
    	SecretKeySpec secretKey = new SecretKeySpec(cryptoxKey.getEncoded(), "AES");
   		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
   		cipher.init(Cipher.ENCRYPT_MODE, secretKey, initVectorParameterSpec);
   		return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
    }
    
    public String getAESDecrypt(String data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
    	byte[] initVector = new byte[16];
    	
    	IvParameterSpec initVectorParameterSpec = new IvParameterSpec(initVector);
    	SecretKeySpec secretKey = new SecretKeySpec(cryptoxKey.getEncoded(), "AES");
    	Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    	cipher.init(Cipher.DECRYPT_MODE, secretKey, initVectorParameterSpec);
    	return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
    }
    
    public byte[] getAES(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] initVector = new byte[16];
        IvParameterSpec initVectorParameterSpec = new IvParameterSpec(initVector);
        SecretKeySpec secretKey = new SecretKeySpec(cryptoxKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, initVectorParameterSpec);
        return cipher.doFinal(data);
    }

    public byte[] getAESDecrypt(byte[] data) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] initVector = new byte[16];
        IvParameterSpec initVectorParameterSpec = new IvParameterSpec(initVector);
        SecretKeySpec secretKey = new SecretKeySpec(cryptoxKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, initVectorParameterSpec);
        return cipher.doFinal(data);
    }

    
    public String generateRandomPassword(int length, boolean hasSimbols, boolean hasDigits) {
    	Random random = new Random();
    	StringBuilder sb = new StringBuilder();
    	if(!hasSimbols && !hasDigits) {
    		for(int i = 0; i < length; i++) {
    			int index = random.nextInt(ASCII.length());
    			char randomChar = ASCII.charAt(index);
    			sb.append(randomChar);
    		}
    	} else if(!hasSimbols) {
    		for(int i = 0; i < length; i++) {
    			int index = random.nextInt(ASCIIDigits.length());
    			char randomChar = ASCIIDigits.charAt(index);
    			sb.append(randomChar);
    		}
    	} else if(!hasDigits) {
    		for(int i = 0; i < length; i++) {
    			int index = random.nextInt(ASCIISimbols.length());
    			char randomChar = ASCIISimbols.charAt(index);
    			sb.append(randomChar);
    		}
    	} else {
    		for(int i = 0; i < length; i++) {
    			int index = random.nextInt(ASCIISimbolsDigits.length());
    			char randomChar = ASCIISimbolsDigits.charAt(index);
    			sb.append(randomChar);
    		}
    	}
    	
    	return sb.toString();
    }
}

