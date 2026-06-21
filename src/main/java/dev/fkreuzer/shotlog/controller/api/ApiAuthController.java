package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.controller.DefaultShotLogController;
import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController extends DefaultShotLogController {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiAuthController(UserAccountRepository userAccountRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo() {
        UserAccount user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401)
                    .build();
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("roles", user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        userInfo.put("authorities", user.authorityNames());

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String displayName = body.get("displayName");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Benutzername ist erforderlich"));
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Passwort muss mindestens 6 Zeichen lang sein"));
        }

        if (userAccountRepository.findByUsername(username)
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Benutzername existiert bereits"));
        }

        // Assign default USER role
        Set<Role> roles = new HashSet<>();
        roleRepository.findByName("USER")
                .ifPresent(roles::add);

        UserAccount newUser = new UserAccount(
                username,
                passwordEncoder.encode(password),
                displayName,
                roles
        );

        userAccountRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "Benutzer erfolgreich erstellt"));
    }
}
