package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import dev.fkreuzer.shotlog.web.ApiResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ApiSettingsController {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public ApiSettingsController(UserAccountRepository userAccountRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder,
                                 MessageSource messageSource) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    // ---- User Management ----

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        List<UserAccount> users = userAccountRepository.findAll();
        List<Map<String, Object>> result = users.stream()
                .map(this::mapUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String displayName = (String) body.get("displayName");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("user.usernameRequired")));
        }

        if (displayName == null || displayName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("user.displayNameRequired")));
        }

        if (userAccountRepository.findByUsername(username)
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("user.usernameExists")));
        }

        Set<Role> roles = resolveRoles(body.get("roleIds"));

        UserAccount newUser = new UserAccount(
                username,
                passwordEncoder.encode(password),
                displayName,
                roles
        );

        userAccountRepository.save(newUser);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("user.created", newUser.getDisplayName())));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<UserAccount> userOpt = userAccountRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(msg("user.notFound")));
        }

        UserAccount user = userOpt.get();

        if (body.containsKey("username")) {
            String username = (String) body.get("username");
            if (username == null || username.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(msg("user.usernameRequired")));
            }
            Optional<UserAccount> existingUser = userAccountRepository.findByUsername(username);
            if (existingUser.isPresent() && !existingUser.get()
                    .getId()
                    .equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(msg("user.usernameExists")));
            }
            user.setUsername(username);
        }

        if (body.containsKey("displayName")) {
            String displayName = (String) body.get("displayName");
            if (displayName == null || displayName.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(msg("user.displayNameRequired")));
            }
            user.setDisplayName(displayName);
        }

        String password = (String) body.get("password");
        if (password != null && !password.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        if (body.containsKey("roleIds")) {
            Set<Role> roles = resolveRoles(body.get("roleIds"));
            user.setRoles(roles);
        }

        userAccountRepository.save(user);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("user.updated", user.getDisplayName())));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<UserAccount> userAccounts = userAccountRepository.findById(id);
        if (userAccounts.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(msg("user.notFound")));
        }
        userAccountRepository.deleteById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("user.deleted", userAccounts.get()
                        .getUsername())));
    }

    // ---- Role Management ----

    @GetMapping("/roles")
    public ResponseEntity<List<Map<String, Object>>> getRoles() {
        List<Role> roles = roleRepository.findAll();
        List<Map<String, Object>> result = roles.stream()
                .map(role -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", role.getId());
                    map.put("name", role.getName());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (roleRepository.findByName(name)
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("role.exists")));
        }

        Role role = new Role(name);
        roleRepository.save(role);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("role.created", role.getName())));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(msg("role.notFound")));
        }

        String name = body.get("name");
        Optional<Role> existingRole = roleRepository.findByName(name);
        if (existingRole.isPresent() && !existingRole.get()
                .getId()
                .equals(id)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("role.exists")));
        }

        Role role = roleOpt.get();
        role.setName(name);
        roleRepository.save(role);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("role.updated", role.getName())));
    }

    @DeleteMapping("/roles/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        Optional<Role> roleToDelete = roleRepository.findById(id);
        if (roleToDelete.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(msg("role.notFound")));
        }

        List<UserAccount> usersWithRole = userAccountRepository.findAll()
                .stream()
                .filter(user -> user.getRoles()
                        .stream()
                        .anyMatch(role -> role.getId()
                                .equals(id)))
                .toList();

        if (!usersWithRole.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("role.deleteAssigned",
                            usersWithRole.stream()
                                    .map(UserAccount::getDisplayName)
                                    .collect(Collectors.joining(", ")))));
        }

        roleRepository.deleteById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("role.deleted", roleToDelete.get()
                        .getName())));
    }

    // ---- Helpers ----

    private Map<String, Object> mapUser(UserAccount user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("displayName", user.getDisplayName());
        map.put("roles", user.getRoles()
                .stream()
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getName());
                    return roleMap;
                })
                .collect(Collectors.toList()));
        return map;
    }

    @SuppressWarnings("unchecked")
    private Set<Role> resolveRoles(Object roleIdsObj) {
        Set<Role> roles = new HashSet<>();
        if (roleIdsObj instanceof List<?> roleIdsList) {
            for (Object idObj : roleIdsList) {
                Long roleId = Long.valueOf(idObj.toString());
                roleRepository.findById(roleId)
                        .ifPresent(roles::add);
            }
        }
        return roles;
    }
}