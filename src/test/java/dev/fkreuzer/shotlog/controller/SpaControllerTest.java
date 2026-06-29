package dev.fkreuzer.shotlog.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpaControllerTest {

    private final SpaController controller = new SpaController();

    @Test
    void forward_shouldForwardToIndexHtml() {
        // Act
        String view = controller.forward();

        // Assert
        assertEquals("forward:/index.html", view);
    }
}
