package dev.fkreuzer.shotlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {"/", "/login", "/dashboard", "/overview", "/calender", "/settings"})
    public String forward() {
        return "forward:/index.html";
    }
}
