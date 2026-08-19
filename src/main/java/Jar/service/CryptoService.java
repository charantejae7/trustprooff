package Jar.service;

import org.springframework.stereotype.Service;
import java.security.*;
import java.util.Base64;

@Service
public class CryptoService {

    private final KeyPair keyPair;

    // 1. Generate the College's RSA Key Pair when the application starts
    public CryptoService() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        this.keyPair = generator.generateKeyPair();
        System.out.println("✅ TrustProof Cryptographic Keys Generated Successfully!");
    }

    // 2. Sign the Student Data using the College's Private Key
    public String signData(String studentPayload) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(keyPair.getPrivate());
        privateSignature.update(studentPayload.getBytes());
        byte[] signatureBytes = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    // 3. Export the Public Key so anyone can verify the certificate
    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
    // 4. Verify a signature to catch hackers and tampered certificates
    public boolean verifySignature(String studentPayload, String base64Signature) {
        try {
            // Re-create the math check using the College's Public Key
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(keyPair.getPublic());

            // Feed it the text the employer scanned
            publicSignature.update(studentPayload.getBytes());

            // Decode the massive signature string back into bytes
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);

            // This returns TRUE if it matches, and FALSE if even a single letter was tampered with!
            return publicSignature.verify(signatureBytes);

        } catch (Exception e) {
            return false; // If anything goes wrong, reject it as a fake!
        }
    }
}
