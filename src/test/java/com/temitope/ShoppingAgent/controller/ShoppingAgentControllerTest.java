package com.temitope.ShoppingAgent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.temitope.ShoppingAgent.dto.ChatRequest;
import com.temitope.ShoppingAgent.dto.ChatResponse;
import com.temitope.ShoppingAgent.service.ShoppingAgentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingAgentController.class)
class ShoppingAgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private ShoppingAgentService agentService;

    @Test
    @DisplayName("GET /health returns UP status")
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/shopping/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.agent").value("ShoppingAssistant"));
    }

    @Test
    @DisplayName("POST /chat with valid request returns agent reply")
    void chat_success() throws Exception {
        ChatResponse mockResponse = ChatResponse.builder()
                .reply("I found 3 great toys under ₦20,000 for a 5-year-old!")
                .sessionId("sess-001")
                .success(true)
                .build();

        Mockito.when(agentService.chat(any())).thenReturn(mockResponse);

        ChatRequest request = new ChatRequest(
                "I need a gift under ₦20,000 for a 5-year-old", "U001", "CART-001", null);

        mockMvc.perform(post("/api/v1/shopping/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.reply").exists())
                .andExpect(jsonPath("$.sessionId").value("sess-001"));
    }

    @Test
    @DisplayName("POST /chat with blank message returns 400")
    void chat_blankMessage_returns400() throws Exception {
        ChatRequest request = new ChatRequest("", "U001", null, null);

        mockMvc.perform(post("/api/v1/shopping/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"));
    }

    @Test
    @DisplayName("DELETE /session/{id} clears session and returns confirmation")
    void clearSession_success() throws Exception {
        mockMvc.perform(delete("/api/v1/shopping/session/sess-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Session cleared successfully."))
                .andExpect(jsonPath("$.sessionId").value("sess-001"));
    }
}
