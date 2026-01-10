package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.UserAccount;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.security.SecurityUser;
import dev.fkreuzer.shotlog.service.SessionService;
import java.util.ArrayList;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OverviewController {

    private final SessionService sessionService;

    public OverviewController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/overview")
    public String overview(@RequestParam(name = "type") String type, Model model) {
        SessionType sessionType = SessionType.valueOf(type.toUpperCase());
        model.addAttribute("type", sessionType);
        return "overview";
    }

    @PostMapping("/sessions/create")
    public String createSession(@ModelAttribute Session session) {
        // Ensure series list is initialized
        if (session.getSeries() == null) {
            session.setSeries(new ArrayList<>());
        }

        // Get the current user and set it for the session
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            UserAccount userAccount = securityUser.domain();
            session.setUser(userAccount);
        }

        sessionService.save(session);
        return "redirect:/overview?type=training";
    }


}
