package Jar.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_certificates")
public class StudentCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String course;
    private String cgpa;

    @Column(name = "qr_data", columnDefinition = "TEXT")
    private String qrData;

    // --- GETTERS AND SETTERS (These fix lines 77 and 80!) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getCgpa() { return cgpa; }
    public void setCgpa(String cgpa) { this.cgpa = cgpa; }
    public String getQrData() { return qrData; }
    public void setQrData(String qrData) { this.qrData = qrData; }
}