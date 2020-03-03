import java.io.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import javax.crypto.*;

public class encryptMessage {

		public static final void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException, NoSuchPaddingException,
			 InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, FileNotFoundException,
				 InvalidKeySpecException, SignatureException {

		if (args.length == 3) {
			Security.addProvider(new BouncyCastleProvider());
			String ReceiverPublicKeyFile = args[1];
			String SenderPrivateKeyFile = args[2];
			Map<String, String> data = Utils.readData2File();

			String dataJson = Utils.map2json(data);
			SecretKey Ks = Utils.generateSecretKey();
			PublicKey ReceiverPublicKey = Utils.recoverKU(ReceiverPublicKeyFile);
					PrivateKey SenderPrivateKey = Utils.recoverKR(SenderPrivateKeyFile);

			byte[] encryptedData = Utils.symmetricCipher(dataJson, Ks);
			byte[] encryptedKs = Utils.asymmetricCipherKs(Ks, ReceiverPublicKey);
			byte[] dataSignature = Utils.createSignature(dataJson.getBytes(), SenderPrivateKey);

			Package message = new Package();
			message.addBlock("Ciphered Data", encryptedData);
			message.addBlock("Ciphered Secret Key", encryptedKs);
			message.addBlock("Data Signature", dataSignature);

			PackageDAO.writePackage(args[0], message);
			System.out.println("Message encrypted successfully\n");

		} else {
			System.out.println("Incorrect number of arguments, number expected: 3\n");
			System.out.println("Arguments expected: output file name, route to the Receiver's Public Key and route to Sender's Private Key\n");
		}

    }
}