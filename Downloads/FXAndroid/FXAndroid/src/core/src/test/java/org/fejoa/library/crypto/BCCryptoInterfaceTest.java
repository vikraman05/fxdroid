package org.fejoa.library.crypto;

import junit.framework.TestCase;
import org.fejoa.library.crypto.BCCryptoInterface;
import org.fejoa.library.crypto.CryptoException;
import org.fejoa.library.crypto.CryptoHelper;
import org.fejoa.library.crypto.CryptoSettings;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;


public class BCCryptoInterfaceTest extends TestCase {

    public void testCrypto() throws Exception {
        CryptoSettings settings = CryptoSettings.getDefault();
        CryptoSettings.setDefaultEC(settings);
        doTest(settings);

        CryptoSettings.setDefaultRSA(settings);
        doTest(settings);
    }

    private void doTest(CryptoSettings settings) throws CryptoException, IOException {
        BCCryptoInterface cryptoInterface = new BCCryptoInterface();
        KeyPair keyPair = cryptoInterface.generateKeyPair(settings.publicKey);

        // encrypt asymmetric + signature
        String clearTextAsym = "hello crypto asymmetric";
        byte encryptedAsymmetric[] = cryptoInterface.encryptAsymmetric(clearTextAsym.getBytes(), keyPair.getPublic(),
                settings.publicKey);
        byte signature[] = cryptoInterface.sign(clearTextAsym.getBytes(), keyPair.getPrivate(), settings.signature);

        // encrypt symmetric
        String clearTextSym = "hello crypto symmetric";
        byte iv[] = cryptoInterface.generateInitializationVector(settings.symmetric.ivSize);
        SecretKey secretKey = cryptoInterface.generateSymmetricKey(settings.symmetric);
        byte encryptedSymmetric[] = cryptoInterface.encryptSymmetric(clearTextSym.getBytes(), secretKey, iv,
                settings.symmetric);

        // store keys to pem and restore
        String privateKeyString = CryptoHelper.convertToPEM(keyPair.getPrivate());
        String publicKeyString = CryptoHelper.convertToPEM(keyPair.getPublic());
        byte secretKeyBytes[] = secretKey.getEncoded();
        PrivateKey privateKey = CryptoHelper.privateKeyFromPem(privateKeyString);
        PublicKey publicKey = CryptoHelper.publicKeyFromPem(publicKeyString);
        secretKey = CryptoHelper.symmetricKeyFromRaw(secretKeyBytes, settings.symmetric);

        // test if we can decrypt / verify the signature
        byte decryptedAsymmetric[] = cryptoInterface.decryptAsymmetric(encryptedAsymmetric, privateKey,
                settings.publicKey);
        assertTrue(Arrays.equals(clearTextAsym.getBytes(), decryptedAsymmetric));
        assertTrue(cryptoInterface.verifySignature(clearTextAsym.getBytes(), signature, publicKey, settings.signature));
        byte decryptedSymmetric[] = cryptoInterface.decryptSymmetric(encryptedSymmetric, secretKey, iv,
                settings.symmetric);
        assertTrue(Arrays.equals(clearTextSym.getBytes(), decryptedSymmetric));

        // check if encryption still works with the public key that we converted to pem and back
        byte encryptedAsymmetricAfterPem[] = cryptoInterface.encryptAsymmetric(clearTextAsym.getBytes(), publicKey,
                settings.publicKey);
        byte decryptedAsymmetricAfterPem[] = cryptoInterface.decryptAsymmetric(encryptedAsymmetric, privateKey,
                settings.publicKey);
        assertTrue(Arrays.equals(clearTextAsym.getBytes(), decryptedAsymmetricAfterPem));

        // symmetric stream test
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStream outputStream = cryptoInterface.encryptSymmetric(byteArrayOutputStream, secretKey, iv,
                settings.symmetric);
        outputStream.write(clearTextSym.getBytes());
        outputStream.flush();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        InputStream inputStream = cryptoInterface.decryptSymmetric(byteArrayInputStream, secretKey, iv,
                settings.symmetric);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        assertEquals(clearTextSym, reader.readLine());

        // test if kdf gives the same value twice
        String password = "testPassword348#";
        byte salt[] = cryptoInterface.generateSalt();
        SecretKey kdfKey1 = cryptoInterface.deriveKey(password, salt, settings.masterPassword.kdfAlgorithm, 20000, 256);
        assertEquals(32, kdfKey1.getEncoded().length);
        SecretKey kdfKey2 = cryptoInterface.deriveKey(password, salt, settings.masterPassword.kdfAlgorithm, 20000, 256);
        assertTrue(Arrays.equals(kdfKey1.getEncoded(), kdfKey2.getEncoded()));
    }

    public void testConvergentEncryption() throws Exception {
        CryptoSettings settings = CryptoSettings.getDefault();

        BCCryptoInterface cryptoInterface = new BCCryptoInterface();

        // encrypt symmetric
        String clearTextSym = "hello crypto symmetric";
        byte iv[] = cryptoInterface.generateInitializationVector(settings.symmetric.ivSize);
        SecretKey secretKey = cryptoInterface.generateSymmetricKey(settings.symmetric);
        byte encryptedSymmetric[] = cryptoInterface.encryptSymmetric(clearTextSym.getBytes(), secretKey, iv,
                settings.symmetric);
        byte encryptedSymmetric2[] = cryptoInterface.encryptSymmetric(clearTextSym.getBytes(), secretKey, iv,
                settings.symmetric);

        assertTrue(Arrays.equals(encryptedSymmetric, encryptedSymmetric2));
    }
}
