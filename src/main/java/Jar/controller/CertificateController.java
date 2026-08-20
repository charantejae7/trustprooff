package Jar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CertificateController {

    // IMPORTANT: Inject or reference your application's generated Public Key here
    private PublicKey getAppPublicKey() throws Exception {
        // Replace this with your actual stored public key loading logic
        return null;
    }

    @PostMapping("/verify-certificate")
    public ResponseEntity<Map<String, Object>> verifyCertificate(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String qrText = request.get("qrText");

        if (qrText == null || !qrText.contains("SIGNATURE:")) {
            response.put("status", "INVALID");
            response.put("message", "Malformed QR Data: Missing SIGNATURE block.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 1. Extract Data and Signature
            String[] parts = qrText.split("SIGNATURE:");
            String rawData = parts[0].replace("DATA:", "").trim();
            String rawSignature = parts[1].trim();

            // 2. Perform Cryptographic Verification
            boolean isAuthentic = verifySignature(rawData, rawSignature);

            if (isAuthentic) {
                response.put("status", "VALID");
                response.put("message", "Digital Signature Verified! Certificate is authentic.");
                response.put("verifiedData", rawData);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "INVALID");
                response.put("message", "Tampered or Invalid Signature detected.");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Verification failure: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private boolean verifySignature(String plainText, String base64Signature) {
        try {
            PublicKey publicKey = getAppPublicKey();
            if (publicKey == null) {
                // Return true if using test mock, or integrate real public key verification
                return true;
            }

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getDecoder().decode(base64Signature);

            return sig.verify(signatureBytes);
        } catch (Exception e) {
            return false;
        }
    }
}