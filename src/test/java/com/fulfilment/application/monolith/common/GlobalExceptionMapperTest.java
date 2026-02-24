package com.fulfilment.application.monolith.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class GlobalExceptionMapperTest {

    private GlobalExceptionMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mapper = new GlobalExceptionMapper(objectMapper);
    }

    @Test
    void testToResponseWithWebApplicationException() {
        WebApplicationException ex = new WebApplicationException("Custom Error", 400);
        ObjectNode node = new ObjectMapper().createObjectNode();
        when(objectMapper.createObjectNode()).thenReturn(node);

        Response response = mapper.toResponse(ex);

        assertEquals(400, response.getStatus());
        assertEquals("Custom Error", ((ObjectNode) response.getEntity()).get("error").asText());
    }

    @Test
    void testToResponseWithGenericException() {
        RuntimeException ex = new RuntimeException("Generic Error");
        ObjectNode node = new ObjectMapper().createObjectNode();
        when(objectMapper.createObjectNode()).thenReturn(node);

        Response response = mapper.toResponse(ex);

        assertEquals(500, response.getStatus());
        assertEquals("Generic Error", ((ObjectNode) response.getEntity()).get("error").asText());
    }
}
