package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.controller.DefaultShotLogController;
import dev.fkreuzer.shotlog.domain.*;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController extends DefaultShotLogController {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShootingPlaceService shootingPlaceService;
    private final MessageSource messageSource;

    public ApiAuthController(UserAccountRepository userAccountRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder,
                             ShootingPlaceService shootingPlaceService,
                             MessageSource messageSource) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.shootingPlaceService = shootingPlaceService;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUserInfo() {
        UserAccount current = getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(401)
                    .build();
        }

        // The principal is a detached snapshot captured at login, so its teams
        // collection is stale. Re-load from the repository to reflect changes
        // made after login (e.g. being added to a team).
        UserAccount user = userAccountRepository.findById(current.getId())
                .orElse(current);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("roles", user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        userInfo.put("authorities", user.authorityNames());
        userInfo.put("permissions", user.getRoles()
                .stream()
                .flatMap(role -> role.getPermissions()
                        .stream())
                .map(Permission::getPermissionName)
                .distinct()
                .collect(Collectors.toList()));
        ShootingPlace homeClub = user.getHomeClub();
        userInfo.put("homeClubId", homeClub != null ? homeClub.getId() : null);
        userInfo.put("homeClubName", homeClub != null ? homeClub.getClub() : null);

        List<UserTeam> teams = user.getTeams();
        userInfo.put("teams", teams == null ? List.of() : teams.stream()
                .map(userTeam -> {
                    Map<String, Object> teamInfo = new HashMap<>();
                    teamInfo.put("id", userTeam.getTeam().getId());
                    teamInfo.put("name", userTeam.getTeam().getName());
                    teamInfo.put("role", userTeam.getRole());
                    var season = userTeam.getTeam().getSeason();
                    if (season != null) {
                        teamInfo.put("season", Map.of(
                                "id", season.getId(),
                                "description", season.getDescription()));
                    }
                    return teamInfo;
                })
                .collect(Collectors.toList()));

        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@RequestBody Map<String, Object> body) {
        ApiResponse response = new ApiResponse();
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
                            .body(ApiResponse.error(msg("profile.clubNotFound")));
                }
                user.setHomeClub(homeClub);
                response.addSuccess(msg("profile.homeClubSet", homeClub.getClub()));
            }
        }

        userAccountRepository.save(user);
        response.addSuccess(msg("profile.updateSuccess"));
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String displayName = body.get("displayName");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("auth.usernameRequired")));
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("auth.passwordTooShort")));
        }

        if (displayName == null || displayName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("auth.displayNameRequired")));
        }

        if (userAccountRepository.findByUsername(username)
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("auth.usernameExists")));
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
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("auth.registrationSuccess")));
    }
}