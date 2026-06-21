package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.Role;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.security.SecurityUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultShotLogControllerTest {

    private final DefaultShotLogController controller = new DefaultShotLogController();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnUserAccount_whenSecurityUserIsAuthenticated() {
        // Arrange
        UserAccount account = new UserAccount("testUser", "hash", Set.of(new Role("USER")));
        SecurityUser securityUser = new SecurityUser(account);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        // Act
        UserAccount result = controller.getCurrentUser();

        // Assert
        assertSame(account, result);
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenNoAuthentication() {
        // Act
        UserAccount result = controller.getCurrentUser();

        // Assert
        assertNull(result);
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenPrincipalIsNotSecurityUser() {
        // Arrange
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("plainString", null);
        SecurityContextHolder.getContext()
                .setAuthentication(auth);

        // Act
        UserAccount result = controller.getCurrentUser();

        // Assert
        assertNull(result);
    }

    @Test
    void setCurrentPage_shouldAddCurrentPageAttribute() {
        // Arrange
        Model model = new ExtendedModelMap();

        // Act
        controller.setCurrentPage(model, "dashboard");

        // Assert
        assertEquals("dashboard", model.getAttribute("currentPage"));
    }

    @Test
    void setCurrentPage_withParamAndValue_shouldAddAllAttributes() {
        // Arrange
        Model model = new ExtendedModelMap();

        // Act
        controller.setCurrentPage(model, "overview", "type", "training");

        // Assert
        assertEquals("overview", model.getAttribute("currentPage"));
        assertEquals("type", model.getAttribute("currentPageParam"));
        assertEquals("training", model.getAttribute("currentPageValue"));
    }
}
