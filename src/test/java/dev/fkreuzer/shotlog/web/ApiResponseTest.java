package dev.fkreuzer.shotlog.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void newResponse_shouldStartWithEmptyLists() {
        // Act
        ApiResponse response = new ApiResponse();

        // Assert
        assertTrue(response.getErrors().isEmpty());
        assertTrue(response.getWarnings().isEmpty());
        assertTrue(response.getSuccesses().isEmpty());
        assertTrue(response.getInfos().isEmpty());
    }

    @Test
    void addMethods_shouldAppendToMatchingListAndReturnSameInstance() {
        // Arrange
        ApiResponse response = new ApiResponse();

        // Act
        ApiResponse returned = response
                .addError("e1")
                .addWarning("w1")
                .addSuccess("s1")
                .addInfo("i1");

        // Assert
        assertSame(response, returned);
        assertEquals("e1", response.getErrors().get(0));
        assertEquals("w1", response.getWarnings().get(0));
        assertEquals("s1", response.getSuccesses().get(0));
        assertEquals("i1", response.getInfos().get(0));
    }

    @Test
    void errorFactory_shouldPopulateOnlyErrors() {
        // Act
        ApiResponse response = ApiResponse.error("a", "b");

        // Assert
        assertEquals(2, response.getErrors().size());
        assertEquals("a", response.getErrors().get(0));
        assertEquals("b", response.getErrors().get(1));
        assertTrue(response.getWarnings().isEmpty());
        assertTrue(response.getSuccesses().isEmpty());
        assertTrue(response.getInfos().isEmpty());
    }

    @Test
    void warningFactory_shouldPopulateOnlyWarnings() {
        // Act
        ApiResponse response = ApiResponse.warning("w");

        // Assert
        assertEquals(1, response.getWarnings().size());
        assertEquals("w", response.getWarnings().get(0));
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void successFactory_shouldPopulateOnlySuccesses() {
        // Act
        ApiResponse response = ApiResponse.success("ok");

        // Assert
        assertEquals(1, response.getSuccesses().size());
        assertEquals("ok", response.getSuccesses().get(0));
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void infoFactory_shouldPopulateOnlyInfos() {
        // Act
        ApiResponse response = ApiResponse.info("note");

        // Assert
        assertEquals(1, response.getInfos().size());
        assertEquals("note", response.getInfos().get(0));
        assertTrue(response.getSuccesses().isEmpty());
    }
}
