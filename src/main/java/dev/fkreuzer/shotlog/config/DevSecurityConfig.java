package dev.fkreuzer.shotlog.config;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevSecurityConfig {

    private final UserAccountRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepo;

    @PostConstruct
    void seedAdmin() {
        if (userRepo.findByUsername("admin").isPresent()) {
            return;
        }

        Role adminRole = new Role("ADMIN");
        roleRepo.save(adminRole);
        UserAccount admin = new UserAccount("admin", passwordEncoder.encode("admin"), Set.of(adminRole));
        userRepo.save(admin);
        userRepo.save(admin);
    }
}
