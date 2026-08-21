package Jar.controller;

import Jar.entity.StudentCertificate;
import Jar.entity.User;
import Jar.repository.CertificateRepository;
import Jar.repository.UserRepository;
import Jar.service.CryptoService;
import Jar.service.PdfService;
import Jar.service.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class CertificateController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private PdfService pdfService; // <-- ADDED PDF SERVICE

    @Autowired
    private QrCodeService qrCodeService; // <-- ADDED QR SERVICE

    // ==========================================
    // ENDPOINT 1: REAL SUPABASE LOGIN
    // ==========================================
    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        Map<String, String> response = new HashMap<>();
        try {
            String inputUser = credentials.get("username").trim();
            String inputPass = credentials.get("password").trim();

            User dbUser = userRepository.findByUsername(inputUser);

            if (dbUser != null && dbUser.getPassword().equals(inputPass)) {
                response.put("status", "success");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Invalid username or password.");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("message", "Database error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // ENDPOINT 2: GENERATE & SAVE STUDENT
    // ==========================================
    @PostMapping("/students/add")
    public ResponseEntity<Map<String, String>> addStudent(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        try {
            String name = request.get("name");
            String course = request.get("course");
            String cgpa = request.get("cgpa");

            String payload = "Name: " + name + ", Course: " + course + ", CGPA: " + cgpa;
            String signature = cryptoService.signData(payload);
            String fullQrData = "DATA:" + payload + "SIGNATURE:" + signature;

            StudentCertificate certificate = new StudentCertificate();
            certificate.setName(name);
            certificate.setCourse(course);
            certificate.setCgpa(cgpa);
            certificate.setQrData(fullQrData);
            certificateRepository.save(certificate);

            response.put("status", "success");
            response.put("qrData", fullQrData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to generate: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // ENDPOINT 3: VERIFY QR CODE
    // ==========================================
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCertificate(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String qrContent = request.get("qrContent");
            if (qrContent == null || !qrContent.contains("SIGNATURE:")) {
                response.put("status", "INVALID");
                response.put("message", "Invalid format: SIGNATURE block is missing.");
                return ResponseEntity.badRequest().body(response);
            }

            String[] parts = qrContent.split("SIGNATURE:");
            String data = parts[0].replace("DATA:", "").trim();
            String signatureBase64 = parts[1].replaceAll("\\s+", "");

            String publicKeyString = cryptoService.getPublicKey();
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyString);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            if (sig.verify(Base64.getDecoder().decode(signatureBase64))) {
                response.put("status", "VALID");
                response.put("verifiedData", data);
            } else {
                response.put("status", "INVALID");
                response.put("message", "Digital signature does not match.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Verification failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ==========================================
    // ENDPOINT 4: GET ALL STUDENTS FOR DASHBOARD
    // ==========================================
    @GetMapping("/students")
    public ResponseEntity<List<StudentCertificate>> getAllStudents() {
        try {
            return ResponseEntity.ok(certificateRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==========================================
    // ENDPOINT 5: DOWNLOAD SECURE PDF CERTIFICATE
    // ==========================================
    @GetMapping("/students/{id}/pdf")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        try {
            // 1. Find the student in the database
            Optional<StudentCertificate> studentOpt = certificateRepository.findById(id);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            StudentCertificate student = studentOpt.get();

            // 2. Generate the QR Code image
            byte[] qrImage = qrCodeService.generateQrCodeImage(student.getQrData());

            // 3. Generate the actual PDF document
            byte[] pdfBytes = pdfService.generateCertificate(
                    student.getName(),
                    student.getCourse(),
                    student.getCgpa(),
                    qrImage
            );

            // 4. Send the PDF to the user's browser for download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // This forces the browser to download the file with the student's name
            headers.setContentDispositionFormData("attachment", student.getName().replace(" ", "_") + "_Certificate.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}