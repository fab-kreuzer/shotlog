package dev.fkreuzer.shotlog.controller.api;

import dev.fkreuzer.shotlog.domain.Permission;
import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.PermissionRepository;
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
public class ApiSettingsController {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MessageSource messageSource;

    public ApiSettingsController(UserAccountRepository userAccountRepository,
                                 RoleRepository roleRepository,
                                 PermissionRepository permissionRepository,
                                 MessageSource messageSource) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    // ---- Permission Management ----

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('view_role_tab')")
    public ResponseEntity<List<Map<String, Object>>> getPermissions() {
        List<Map<String, Object>> result = permissionRepository.findAll()
                .stream()
                .map(this::mapPermission)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ---- Role Management ----

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('view_role_tab') or hasAuthority('view_user_tab')")
    public ResponseEntity<List<Map<String, Object>>> getRoles() {
        List<Role> roles = roleRepository.findAll();
        List<Map<String, Object>> result = roles.stream()
                .map(role -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", role.getId());
                    map.put("name", role.getName());
                    map.put("permissions", role.getPermissions()
                            .stream()
                            .map(this::mapPermission)
                            .collect(Collectors.toList()));
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('view_role_tab')")
    public ResponseEntity<?> createRole(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        if (roleRepository.findByName(name)
                .isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(msg("role.exists")));
        }

        Role role = new Role(name);
        role.setPermissions(resolvePermissions(body.get("permissionIds")));
        roleRepository.save(role);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("role.created", role.getName())));
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('view_role_tab')")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(msg("role.notFound")));
        }

        Role role = roleOpt.get();

        if (body.containsKey("name")) {
            String name = (String) body.get("name");
            Optional<Role> existingRole = roleRepository.findByName(name);
            if (existingRole.isPresent() && !existingRole.get()
                    .getId()
                    .equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(msg("role.exists")));
            }
            role.setName(name);
        }

        if (body.containsKey("permissionIds")) {
            role.setPermissions(resolvePermissions(body.get("permissionIds")));
        }

        roleRepository.save(role);
        return ResponseEntity.ok()
                .body(ApiResponse.success(msg("role.updated", role.getName())));
    }

    @DeleteMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('view_role_tab')")
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

    private Map<String, Object> mapPermission(Permission permission) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", permission.getId());
        map.put("permissionName", permission.getPermissionName());
        map.put("description", permission.getDescription());
        map.put("resource", permission.getResource());
        map.put("action", permission.getAction());
        return map;
    }

    @SuppressWarnings("unchecked")
    private Set<Permission> resolvePermissions(Object permissionIdsObj) {
        Set<Permission> permissions = new HashSet<>();
        if (permissionIdsObj instanceof List<?> permissionIdsList) {
            for (Object idObj : permissionIdsList) {
                Long permissionId = Long.valueOf(idObj.toString());
                permissionRepository.findById(permissionId)
                        .ifPresent(permissions::add);
            }
        }
        return permissions;
    }

}