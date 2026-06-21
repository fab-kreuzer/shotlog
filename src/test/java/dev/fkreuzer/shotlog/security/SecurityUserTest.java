package dev.fkreuzer.shotlog.security;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUserTest {

    @Test
    void domain_shouldReturnUnderlyingUserAccount() {
        UserAccount account = new UserAccount("user1", "hash", Set.of());
        SecurityUser securityUser = new SecurityUser(account);

        assertSame(account, securityUser.domain());
    }

    @Test
    void getUsername_shouldReturnUsernameFromUserAccount() {
        UserAccount account = new UserAccount("testUser", "hash", Set.of());
        SecurityUser securityUser = new SecurityUser(account);

        assertEquals("testUser", securityUser.getUsername());
    }

    @Test
    void getPassword_shouldReturnPasswordHashFromUserAccount() {
        UserAccount account = new UserAccount("user", "myPasswordHash", Set.of());
        SecurityUser securityUser = new SecurityUser(account);

        assertEquals("myPasswordHash", securityUser.getPassword());
    }

    @Test
    void getAuthorities_shouldMapRolesToGrantedAuthorities() {
        Role roleUser = new Role("USER");
        Role roleAdmin = new Role("ADMIN");
        UserAccount account = new UserAccount("user", "hash", Set.of(roleUser, roleAdmin));
        SecurityUser securityUser = new SecurityUser(account);

        Collection<? extends GrantedAuthority> authorities = securityUser.getAuthorities();

        assertEquals(2, authorities.size());
        Set<String> authorityStrings = Set.of(
                authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new)
        );
        assertTrue(authorityStrings.contains("ROLE_USER"));
        assertTrue(authorityStrings.contains("ROLE_ADMIN"));
    }

    @Test
    void getAuthorities_shouldReturnEmptyList_whenNoRoles() {
        UserAccount account = new UserAccount("user", "hash", Set.of());
        SecurityUser securityUser = new SecurityUser(account);

        assertTrue(securityUser.getAuthorities()
                .isEmpty());
    }

    @Test
    void isAccountNonExpired_shouldReturnTrue() {
        UserAccount account = new UserAccount("user", "hash", Set.of());
        SecurityUser securityUser = new SecurityUser(account);

        assertTrue(securityUser.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked_shouldReturnTrue() {
        UserAccount account = new UserAccount("user", "hash", Set.of());
        SecurityUser securityUser = new SecurityUser(account);

        assertTrue(securityUser.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired_shouldReturnTrue() {
        UserAccount account = new UserAccount("user", "hash", Set.of());
        SecurityUser securityUser = new SecurityUser(account);

        assertTrue(securityUser.isCredentialsNonExpired());
    }

    @Test
    void isEnabled_shouldReturnTrue_whenUserAccountIsEnabled() {
        UserAccount account = new UserAccount("user", "hash", Set.of());
        account.setEnabled(true);
        SecurityUser securityUser = new SecurityUser(account);

        assertTrue(securityUser.isEnabled());
    }

    @Test
    void isEnabled_shouldReturnFalse_whenUserAccountIsDisabled() {
        UserAccount account = new UserAccount("user", "hash", Set.of());
        account.setEnabled(false);
        SecurityUser securityUser = new SecurityUser(account);

        assertFalse(securityUser.isEnabled());
    }
}
