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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ApiSettingsController {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final MessageSource messageSource;

    public ApiSettingsController(UserAccountRepository userAccountRepository,
                                 RoleRepository roleRepository,
                                 MessageSource messageSource) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
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

}