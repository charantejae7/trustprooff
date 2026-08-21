package Jar.controller;

import Jar.entity.User;
import Jar.repository.UserRepository;
import Jar.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // 1. Injects your permanent cryptography keys
    @Autowired
    private CryptoService cryptoService;

    // 2. Injects your real Supabase Database connection
    @Autowired
    private UserRepository userRepository;

    // ==========================================
    // ENDPOINT 1: REAL SUPABASE LOGIN
    // ==========================================
    @PostMapping("/certificate/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        Map<String, String> response = new HashMap<>();
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            // Fetches the real user from your PostgreSQL database
            User user = userRepository.findByUsername(username);

            // Validates the credentials against the database record
            if (user != null && user.getPassword().equals(password)) {
                response.put("status", "success");
                response.put("message", "Login successful");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Invalid username or password");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Database connection error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // ENDPOINT 2: CRYPTOGRAPHIC VERIFICATION
    // ==========================================
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
            String[] parts = qrContent.split("SIGNATURE:");
            String extractedData = parts[0].replace("DATA:", "").trim();
            String extractedSignature = parts[1].replaceAll("\\s+", "");

            boolean isAuthentic = verifyDigitalSignature(extractedData, extractedSignature);

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

    // ==========================================
    // HELPER: SIGNATURE MATH
    // ==========================================
    private boolean verifyDigitalSignature(String data, String signatureBase64) {
        try {
            String publicKeyString = cryptoService.getPublicKey();
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            return sig.verify(signatureBytes);

        } catch (Exception e) {
            System.out.println("🚨 CRASH: Signature validation failed: " + e.getMessage());
            return false;
        }
    }
}