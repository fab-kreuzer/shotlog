package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.repository.RoleRepository;
import dev.fkreuzer.shotlog.repository.UserAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SettingsController {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsController(UserAccountRepository userAccountRepository, 
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/settings")
    public String settings(Model model, Authentication authentication) {
        // Get all users for admin panel
        List<UserAccount> users = userAccountRepository.findAll();
        model.addAttribute("users", users);

        // Get all roles for admin panel
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);

        // Add current user to model
        String username = authentication.getName();
        UserAccount currentUser = userAccountRepository.findByUsername(username).orElseThrow();
        model.addAttribute("currentUser", currentUser);

        return "settings";
    }

    // User Management

    @PostMapping("/settings/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String createUser(@RequestParam String username, 
                            @RequestParam String password,
                            @RequestParam(required = false) List<Long> roleIds,
                            RedirectAttributes redirectAttributes) {

        if (userAccountRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Benutzername existiert bereits");
            return "redirect:/settings";
        }

        Set<Role> roles = new HashSet<>();
        if (roleIds != null) {
            roles = roleIds.stream()
                    .map(id -> roleRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }

        UserAccount newUser = new UserAccount(
                username,
                passwordEncoder.encode(password),
                roles
        );

        userAccountRepository.save(newUser);
        redirectAttributes.addFlashAttribute("success", "Benutzer erfolgreich erstellt");

        return "redirect:/settings";
    }

    @PutMapping("/settings/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                      @RequestParam(required = false) String username,
                                      @RequestParam(required = false) String password,
                                      @RequestParam(required = false) List<Long> roleIds) {

        Optional<UserAccount> userOpt = userAccountRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserAccount user = userOpt.get();

        // Update username if provided
        if (username != null && !username.isBlank()) {
            // Check if username is already taken by another user
            Optional<UserAccount> existingUser = userAccountRepository.findByUsername(username);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                return ResponseEntity.badRequest().body("Benutzername existiert bereits");
            }
            user.setUsername(username);
        }

        // Update password if provided
        if (password != null && !password.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        // Update roles if provided
        if (roleIds != null) {
            Set<Role> roles = roleIds.stream()
                    .map(roleId -> roleRepository.findById(roleId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        userAccountRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/settings/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userAccountRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userAccountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Role Management

    @PostMapping("/settings/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String createRole(@RequestParam String name,
                           RedirectAttributes redirectAttributes) {

        if (roleRepository.findByName(name).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Rolle existiert bereits");
            return "redirect:/settings";
        }

        Role role = new Role();
        role.setName(name);
        roleRepository.save(role);

        redirectAttributes.addFlashAttribute("success", "Rolle erfolgreich erstellt");
        return "redirect:/settings";
    }

    @PutMapping("/settings/roles/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable Long id,
                                      @RequestParam String name) {

        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Check if name is already taken by another role
        Optional<Role> existingRole = roleRepository.findByName(name);
        if (existingRole.isPresent() && !existingRole.get().getId().equals(id)) {
            return ResponseEntity.badRequest().body("Rolle existiert bereits");
        }

        Role role = roleOpt.get();
        role.setName(name);
        roleRepository.save(role);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/settings/roles/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Check if role is assigned to any users
        List<UserAccount> usersWithRole = userAccountRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getId().equals(id)))
                .toList();

        if (!usersWithRole.isEmpty()) {
            return ResponseEntity.badRequest().body("Rolle kann nicht gelöscht werden, da sie Benutzern zugewiesen ist");
        }

        roleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
