package Jar.controller;

import Jar.entity.StudentCertificate;
import Jar.entity.User;
import Jar.repository.CertificateRepository;
import Jar.repository.UserRepository;
import Jar.service.CryptoService;
import Jar.service.PdfService;
import Jar.service.QrCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/certificate")
public class CertificateController {

    private final UserRepository userRepository;
    private final CryptoService cryptoService;
    private final QrCodeService qrCodeService;
    private final PdfService pdfService;
    private final CertificateRepository certificateRepository;

    public CertificateController(UserRepository userRepository,
                                 CryptoService cryptoService,
                                 QrCodeService qrCodeService,
                                 PdfService pdfService,
                                 CertificateRepository certificateRepository) {
        this.userRepository = userRepository;
        this.cryptoService = cryptoService;
        this.qrCodeService = qrCodeService;
        this.pdfService = pdfService;
        this.certificateRepository = certificateRepository;
    }

    // Direct Login - No 2FA
    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return "redirect:/admin-dashboard.html";
        }
        return "redirect:/login.html?error=true";
    }

    // ... (keep the rest of your methods: issue, verify-scan, all, test)

    @PostMapping("/issue")
    public ResponseEntity<byte[]> issueCertificate(@RequestParam String studentName, @RequestParam String course, @RequestParam String cgpa) throws Exception {
        String studentData = "Name: " + studentName + ", Course: " + course + ", CGPA: " + cgpa;
        String signature = cryptoService.signData(studentData);
        StudentCertificate record = new StudentCertificate();
        record.setStudentName(studentName);
        record.setCourse(course);
        record.setCgpa(cgpa);
        record.setDigitalSignature(signature);
        record.setIssueDate(LocalDateTime.now());
        certificateRepository.save(record);
        byte[] qrImageBytes = qrCodeService.generateQrCodeImage("DATA:\n" + studentData + "\n\nSIGNATURE:\n" + signature);
        byte[] pdfBytes = pdfService.generateCertificate(studentName, course, cgpa, qrImageBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", studentName.replaceAll("\\s+", "_") + "_Certificate.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/all")
    @ResponseBody
    public List<StudentCertificate> getAllCertificates() {
        return certificateRepository.findAll();
    }
}