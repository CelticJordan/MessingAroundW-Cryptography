import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Utils {

	public static Map<String, String> readData2File() {
        	System.out.println("Enter data");

        	Map<String, String> data = new HashMap<String, String>();

        	Scanner in = new Scanner(System.in);
        	System.out.print("Name of message author : ");
        	String author = in.nextLine();
        	data.put("author", author);

        	System.out.print("Message (One Line) : ");
        	String message = in.nextLine();
        	data.put("message", message);

        	return data;
	}


	public static String map2json(Map<String, String> data) {
        	StringBuilder toret = new StringBuilder();
        	toret.append('{');
        	if (data != null) {
        		for (Map.Entry<String, String> entry : data.entrySet()) {
                		if (toret.length() > 1) {
                    			toret.append(',');
                		}
                		toret.append('\"');
                		toret.append(clearString(entry.getKey()));
                		toret.append('\"');
                		toret.append(':');
                		toret.append('\"');
                		toret.append(clearString(entry.getValue()));
                		toret.append('\"');
            		}
        	}
        	toret.append('}');
        	return toret.toString();
    	}

    	private static String clearString(String str) {
        	return str.replaceAll("\\{|\\}|:|,|\\\"", "");
    	}

	public static SecretKey generateSecretKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        	KeyGenerator DESGenerator = KeyGenerator.getInstance("DES", "BC");
        	DESGenerator.init(56);
        	SecretKey KS = DESGenerator.generateKey();
        	return KS;
    	}

	public static PublicKey recoverKU(String fileName) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
	        KeyFactory keyFactoryRSA = KeyFactory.getInstance("RSA", "BC");

        	File KUFile = new File(fileName);
       		int KUFileSize = (int) KUFile.length();
        	byte[] bufferPub = new byte[KUFileSize];
        	FileInputStream in = new FileInputStream(KUFile);
        	in.read(bufferPub, 0, KUFileSize);
        	in.close();

	        X509EncodedKeySpec KUSpec = new X509EncodedKeySpec(bufferPub);
        	PublicKey KU = keyFactoryRSA.generatePublic(KUSpec);

        	return KU;
    	}

    	public static PrivateKey recoverKR(String fileName) throws FileNotFoundException, IOException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        	KeyFactory keyFactoryRSA = KeyFactory.getInstance("RSA", "BC");

	        File KRFile = new File(fileName);
        	int KRFileSize = (int) KRFile.length();
	        byte[] bufferPriv = new byte[KRFileSize];
        	FileInputStream in = new FileInputStream(KRFile);
        	in.read(bufferPriv, 0, KRFileSize);
        	in.close();

        	PKCS8EncodedKeySpec KRSpec = new PKCS8EncodedKeySpec(bufferPriv);
        	PrivateKey KR = keyFactoryRSA.generatePrivate(KRSpec);

        	return KR;
    	}

	public static byte[] symmetricCipher(String json, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException, IOException {
        	Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        	cipher.init(Cipher.ENCRYPT_MODE, key);
        	byte[] cipherBuffer = cipher.doFinal(json.getBytes());

        	return cipherBuffer;
    	}

    	public static byte[] symmetricDecipher(byte[] data, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException, IOException {
        	Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        	cipher.init(Cipher.DECRYPT_MODE, key);
        	byte[] decipherBuffer = cipher.doFinal(data);

        	return decipherBuffer;
    	}

    	public static byte[] asymmetricCipherKs(SecretKey ks, PublicKey KU) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        	Cipher cipher = Cipher.getInstance("RSA", "BC");
        	cipher.init(Cipher.ENCRYPT_MODE, KU);
        	byte[] cipherBuffer;
        	cipherBuffer = cipher.doFinal(ks.getEncoded());

        	return cipherBuffer;
    	}

    	public static byte[] asymmetricDecipherKs(byte[] encryptedKs, PrivateKey KR) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        	Cipher cipher = Cipher.getInstance("RSA", "BC");
        	cipher.init(Cipher.DECRYPT_MODE, KR);
        	byte[] decipherBuffer = cipher.doFinal(encryptedKs);

        	return decipherBuffer;
    	}

    	public static SecretKey byte2KS(byte[] ksbinary) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        	SecretKeyFactory secretKeyFactoryDES = SecretKeyFactory.getInstance("DES", "BC");
        	DESKeySpec DESspec = new DESKeySpec(ksbinary);
        	SecretKey KS = secretKeyFactoryDES.generateSecret(DESspec);

        	return KS;
    	}

	public static byte[] createSignature(byte[] dataToSign, PrivateKey KR) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        	Signature signFactory = Signature.getInstance("SHA256withRSA", "BC");
        	signFactory.initSign(KR);
        	signFactory.update(dataToSign);
        	byte[] signData = signFactory.sign();

        	return signData;
	}

    	public static boolean verifySignature(byte[] data, byte[] signature, PublicKey KU) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException{
        	Signature signFactory = Signature.getInstance("SHA256withRSA", "BC");
        	signFactory.initVerify(KU);
        	signFactory.update(data);
        	boolean check = signFactory.verify(signature);

        	return check;
	}
}

