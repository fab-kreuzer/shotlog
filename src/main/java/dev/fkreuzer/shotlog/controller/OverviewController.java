package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.domain.ShootingPlace;
import dev.fkreuzer.shotlog.domain.datatypes.SessionType;
import dev.fkreuzer.shotlog.service.SessionService;
import java.util.ArrayList;
import java.util.List;

import dev.fkreuzer.shotlog.service.ShootingPlaceService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OverviewController extends DefaultShotLogController {

    private final SessionService sessionService;
    private final ShootingPlaceService shootingPlaceService;

    public OverviewController(SessionService sessionService, ShootingPlaceService shootingPlaceService) {
        this.sessionService = sessionService;
        this.shootingPlaceService = shootingPlaceService;
    }

    @GetMapping("/overview")
    public String overview(@RequestParam(name = "type") String type, Model model) {
        SessionType sessionType = SessionType.valueOf(type.toUpperCase());
        model.addAttribute("type", sessionType);
        model.addAttribute("sessions", sessionService.findAllByUserAndType(getCurrentUser(), sessionType));
        setCurrentPage(model, "overview", "type", type);
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
        return "redirect:/overview?type=" + session.getSessionType().toUrlFormat();
    }

    @DeleteMapping("/sessions/delete/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteByIdAndUser(id, getCurrentUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/get/{id}")
    @ResponseBody
    public ResponseEntity<Session> getSessionById(@PathVariable Long id) {
        return sessionService.findByIdAndUser(id, getCurrentUser())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/update/{id}")
    public String updateSession(@PathVariable Long id, @ModelAttribute Session updatedSession, HttpServletRequest request) {
        // Update the existing session with the new data
        updatedSession.setId(id);
        updatedSession.setUser(getCurrentUser());

        // Ensure series list is initialized
        if (updatedSession.getSeries() == null) {
            updatedSession.setSeries(new ArrayList<>());
        }

        sessionService.save(updatedSession);

        return "redirect:" + request.getHeader("Referer");
    }
}
