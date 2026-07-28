package dev.fkreuzer.shotlog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    // Forward all non-API, non-static routes to the SPA entry point so that
    // client-side (history mode) routes like /settings/profile resolve on a
    // full page reload. Each segment uses [^\.]* so paths containing a dot
    // (e.g. /assets/app.js) are excluded and static resources are served
    // directly; /api/** is handled by the more specific REST controllers.
    // Spring's PathPattern parser forbids captures after **, so the nested
    // levels are enumerated explicitly (deepest client route is 2 levels).
    @GetMapping(value = {
            "/",
            "/{p1:[^\\.]*}",
            "/{p1:[^\\.]*}/{p2:[^\\.]*}",
            "/{p1:[^\\.]*}/{p2:[^\\.]*}/{p3:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
