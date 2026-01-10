package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class DefaultShotLogController {

    UserAccount getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.domain();
        }
        return null;
    }

}
