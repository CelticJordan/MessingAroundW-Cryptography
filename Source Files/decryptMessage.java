import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class decryptMessage {

	public static final void main(String[] args) throws IOException, FileNotFoundException, InvalidKeySpecException, 
		NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, 
			IllegalBlockSizeException, BadPaddingException, SignatureException {

	if (args.length == 3) {
		Security.addProvider(new BouncyCastleProvider());
		Package message = PackageDAO.readPackage(args[0]);
		Block encryptedDataBlock = new Block("Ciphered Data", message.getContentBlock("Ciphered Data"));
		Block encryptedKsBlock = new Block("Ciphered Secret Key", message.getContentBlock("Ciphered Secret Key"));
		Block dataSignatureBlock = new Block("Data Signature", message.getContentBlock("Data Signature"));

		byte[] encryptedData = encryptedDataBlock.getContent();
		byte[] encryptedKs = encryptedKsBlock.getContent();
		byte[] dataSignature = dataSignatureBlock.getContent();

		String ReceiverPrivateKeyFile = args[1];
                String SenderPublicKeyFile = args[2];
		PrivateKey ReceiverPrivateKey = Utils.recoverKR(ReceiverPrivateKeyFile);
		PublicKey SenderPublicKey = Utils.recoverKU(SenderPublicKeyFile);

		byte[] Ks = Utils.asymmetricDecipherKs(encryptedKs, ReceiverPrivateKey);
		SecretKey originalKs = Utils.byte2KS(Ks);
		byte[] data = Utils.symmetricDecipher(encryptedData, originalKs);
		boolean checkSignature = Utils.verifySignature(data, dataSignature, SenderPublicKey);

		if(checkSignature == false) {
			System.out.println("Integrity check: failed.\n");
		} else if (checkSignature == true) {
			System.out.println("Integrity check: Correct.\n");
                        String str = new String(data, StandardCharsets.UTF_8);
			try (PrintWriter out = new PrintWriter("DECRYPTED" + args[0])) {
                        out.println(str);
                    }
		}

	} else {
                System.out.println("Incorrect number of arguments, number expected: 3\n");
                System.out.println("Arguments expected: route to the file to decrypt, route to the Receiver's Private Key and route to Sender's Public Key");
        }

    }
}
