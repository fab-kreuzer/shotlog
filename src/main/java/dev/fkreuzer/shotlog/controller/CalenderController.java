package dev.fkreuzer.shotlog.controller;

import dev.fkreuzer.shotlog.domain.Session;
import dev.fkreuzer.shotlog.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CalenderController extends DefaultShotLogController {

    private final SessionService sessionService;

    public CalenderController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/calender")
    public String calender(Model model) {
        setCurrentPage(model, "calender");
        return "calender";
    }

    @GetMapping("/api/sessions")
    public ResponseEntity<List<Session>> sessions(Model model) {
        return ResponseEntity.ok(sessionService.findAllByUser(getCurrentUser()));
    }
}
