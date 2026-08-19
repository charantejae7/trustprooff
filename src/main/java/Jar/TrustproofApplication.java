package Jar;

import Jar.entity.User;
import Jar.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TrustproofApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrustproofApplication.class, args);
	}

	// Automatically writes the admin user into the database permanently on first boot
	@Bean
	CommandLineRunner initDatabase(UserRepository userRepository) {
		return args -> {
			if (userRepository.findByUsername("ssmrv_admin") == null) {
				User admin = new User();
				admin.setUsername("ssmrv_admin");
				admin.setPassword("cyber2026"); // Stored permanently in database storage
				userRepository.save(admin);
				System.out.println(">>> Permanent Admin User Created in Database Successfully! <<<");
			}
		};
	}
}