package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.controller.DefaultShotLogController;
import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
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
    private final ShootingPlaceService shootingPlaceService;

    public ApiAuthController(UserAccountRepository userAccountRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder,
                             ShootingPlaceService shootingPlaceService) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.shootingPlaceService = shootingPlaceService;
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
        ShootingPlace homeClub = user.getHomeClub();
        userInfo.put("homeClubId", homeClub != null ? homeClub.getId() : null);
        userInfo.put("homeClubName", homeClub != null ? homeClub.getClub() : null);

        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@RequestBody Map<String, Object> body) {
        UserAccount user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401)
                    .build();
        }

        if (body.containsKey("homeClubId")) {
            Object homeClubId = body.get("homeClubId");
            if (homeClubId == null) {
                user.setHomeClub(null);
            } else {
                Long id = Long.valueOf(homeClubId.toString());
                ShootingPlace homeClub = shootingPlaceService.findById(id);
                if (homeClub == null || homeClub.getId() == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Verein nicht gefunden"));
                }
                user.setHomeClub(homeClub);
            }
        }

        userAccountRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profil aktualisiert"));
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

        if (displayName == null || displayName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Anzeigename ist erforderlich"));
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
        return ResponseEntity.ok().body(Map.of("success", "Konto erfolgreich erstellt! Sie können sich jetzt anmelden."));
    }
}