package com.meridiantrust.sentinel.config;

import com.meridiantrust.sentinel.domain.AppUser;
import com.meridiantrust.sentinel.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures the seeded users have valid BCrypt password hashes at boot. The V2
 * migration inserts placeholder hashes; this sets the real ones so login works
 * consistently across environments (dev credentials only — never for prod).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        setPassword("analyst", "analyst123", "ANALYST", "Compliance Analyst");
        setPassword("admin", "admin123", "ADMIN", "Compliance Admin");
    }

    private void setPassword(String username, String rawPassword, String role, String displayName) {
        AppUser user = userRepository.findByUsername(username).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(username);
            u.setRole(role);
            u.setDisplayName(displayName);
            u.setEnabled(true);
            return u;
        });
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
        log.info("Seeded user '{}' ({}) ready", username, role);
    }
}
