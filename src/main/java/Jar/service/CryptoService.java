package Jar.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.security.*;
import java.util.Base64;

@Service
public class CryptoService {

    private final KeyPair keyPair;
    // This will create a hidden file in your project folder to store the keys forever
    private static final String KEY_FILE = "college_keys.dat";

    public CryptoService() throws Exception {
        File file = new File(KEY_FILE);

        if (file.exists()) {
            // 1. The file exists! Load the permanent keys so old QR codes still work
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                this.keyPair = (KeyPair) ois.readObject();
                System.out.println("✅ TrustProof Keys successfully loaded from file!");
            }
        } else {
            // 2. First time running? Generate real keys and save them to the file forever
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, new SecureRandom());
            this.keyPair = generator.generateKeyPair();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(this.keyPair);
                System.out.println("✅ New TrustProof Keys generated and securely saved to college_keys.dat!");
            }
        }
    }

    public String signData(String studentPayload) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(keyPair.getPrivate());
        privateSignature.update(studentPayload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(privateSignature.sign());
    }

    // The Controller will call this to get the real, perfectly formatted Public Key
    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}