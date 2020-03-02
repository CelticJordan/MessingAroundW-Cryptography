import java.io.*;
import java.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class GenerateKeys {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			helpMessage();
			System.exit(1);
		}

		Security.addProvider(new BouncyCastleProvider()); 

		KeyPairGenerator RSAGenerator = KeyPairGenerator.getInstance("RSA", "BC"); 
		RSAGenerator.initialize(512);
		KeyPair RSAKeys = RSAGenerator.generateKeyPair();
		PrivateKey KR = RSAKeys.getPrivate();
		PublicKey KU = RSAKeys.getPublic();

		
		byte[] encodedPKCS8 = KR.getEncoded();
		FileOutputStream out = new FileOutputStream(args[0] + ".private");
		out.write(encodedPKCS8);
		out.close();

		byte[] encodedX509 = KU.getEncoded();
		out = new FileOutputStream(args[0] + ".public");
		out.write(encodedX509);
		out.close();
		
		System.out.println("512 bits RSA Keys generated on files "+args[0] + ".public"+ " & "+args[0] + ".private");

	}

	public static void helpMessage() {
		System.out.println("512 bits RSA KeyPair Generator");
		System.out.println("\tSyntax:   java GenerateKeys prefix");
		System.out.println();
	}
}

