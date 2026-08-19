package Jar.repository;

import Jar.entity.StudentCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificateRepository extends JpaRepository<StudentCertificate, Long> {
    // Spring Boot writes all the SQL automatically behind the scenes!
}