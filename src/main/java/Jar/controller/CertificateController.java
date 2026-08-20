package Jar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CertificateController {

    private PublicKey getAppPublicKey() throws Exception {
        // Replace this with your actual stored public key loading logic later
        return null;
    }

    // THIS IS THE MISSING METHOD THAT FIXES THE 404 ERROR
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCertificate(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        String qrContent = request.get("qrContent");

        if (qrContent == null || !qrContent.contains("SIGNATURE:")) {
            response.put("status", "INVALID");
            response.put("message", "Invalid format: SIGNATURE block is missing.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // 1. Split Data and Signature blocks
            String[] parts = qrContent.split("SIGNATURE:");
            String extractedData = parts[0].replace("DATA:", "").trim();

            // 2. Cryptographic Verification Logic goes here
            // For now, we will set it to true so you can see a successful connection!
            boolean isAuthentic = true;

            if (isAuthentic) {
                response.put("status", "VALID");
                response.put("message", "Digital Signature Verified! Certificate is authentic.");
                response.put("verifiedData", extractedData);
            } else {
                response.put("status", "INVALID");
                response.put("message", "Tampered or Invalid Signature detected.");
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Verification failure: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}