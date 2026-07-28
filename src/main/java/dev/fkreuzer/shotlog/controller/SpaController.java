package dev.fkreuzer.shotlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    // Forward all non-API, non-static routes to the SPA entry point so that
    // client-side (history mode) routes like /settings/profile resolve on a
    // full page reload. Paths containing a dot (e.g. /assets/app.js) are
    // excluded so static resources are still served directly, and /api/**
    // is handled by the more specific REST controllers.
    @GetMapping(value = {"/", "/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
