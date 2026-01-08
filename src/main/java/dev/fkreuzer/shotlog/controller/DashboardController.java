package dev.fkreuzer.shotlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String login() {
        return "dashboard";
    }

    @GetMapping("/test-notifications")
    public String testNotifications() {
        return "test-notifications";
    }

}
