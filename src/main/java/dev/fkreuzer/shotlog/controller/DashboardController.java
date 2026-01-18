package dev.fkreuzer.shotlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController extends DefaultShotLogController {

    @GetMapping("/dashboard")
    public String login(Model model) {
        setCurrentPage(model, "/dashboard");
        return "dashboard";
    }

}
