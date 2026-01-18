package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

public class DefaultShotLogController {

    UserAccount getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.domain();
        }
        return null;
    }

    protected void setCurrentPage(Model model, String page) {
        model.addAttribute("currentPage", page);
    }

    protected void setCurrentPage(Model model, String page, String param, String value) {
        model.addAttribute("currentPage", page);
        model.addAttribute("currentPageParam", param);
        model.addAttribute("currentPageValue", value);
    }

}
