package dev.fkreuzer.shotlog.config;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultUserInitializer implements CommandLineRunner {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdminUser();
    }

    private void createDefaultAdminUser() {
        String adminUsername = "admin";
        
        // Check if admin user already exists
        if (userAccountRepository.findByUsername(adminUsername).isPresent()) {
            log.info("Default admin user already exists, skipping creation");
            return;
        }

        // Find the ADMIN role
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("ADMIN role not found in database"));

        // Create the admin user
        String encodedPassword = passwordEncoder.encode("admin");
        UserAccount adminUser = new UserAccount(adminUsername, encodedPassword, Set.of(adminRole));
        
        userAccountRepository.save(adminUser);
        log.info("Default admin user created successfully with username: {}", adminUsername);
    }
}