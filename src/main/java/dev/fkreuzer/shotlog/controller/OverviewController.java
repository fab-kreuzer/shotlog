package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.service.SessionService;
import java.util.ArrayList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OverviewController extends DefaultShotLogController {

    private final SessionService sessionService;

    public OverviewController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/overview")
    public String overview(@RequestParam(name = "type") String type, Model model) {
        SessionType sessionType = SessionType.valueOf(type.toUpperCase());
        model.addAttribute("type", sessionType);
        model.addAttribute("sessions", sessionService.findAllByUserAndType(getCurrentUser(), sessionType));
        return "overview";
    }

    @PostMapping("/sessions/create")
    public String createSession(@ModelAttribute Session session) {
        // Ensure series list is initialized
        if (session.getSeries() == null) {
            session.setSeries(new ArrayList<>());
        }

        session.setUser(getCurrentUser());

        sessionService.save(session);
        return "redirect:/overview?type=training";
    }
}
