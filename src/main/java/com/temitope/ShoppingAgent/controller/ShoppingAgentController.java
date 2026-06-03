package com.temitope.ShoppingAgent.controller;

import com.temitope.ShoppingAgent.dto.ChatRequest;
import com.temitope.ShoppingAgent.dto.ChatResponse;
import com.temitope.ShoppingAgent.service.ShoppingAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping")
@RequiredArgsConstructor
public class ShoppingAgentController {

    private final ShoppingAgentService agentService;

    /**
     * Main chat endpoint. Accepts a customer message and returns an agent reply.
     *
     * POST /api/v1/shopping/chat
     * {
     *   "message": "I need a gift under ₦20,000 for a 5-year-old",
     *   "userId": "U001",
     *   "cartId": "CART-001",
     *   "sessionId": "optional-session-id"
     * }
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {

        log.info("Received chat request from userId={}", request.getUserId());
        ChatResponse response = agentService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Clear a conversation session (reset memory).
     * DELETE /api/v1/shopping/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        agentService.clearSession(sessionId);
        return ResponseEntity.ok(Map.of(
                "message", "Session cleared successfully.",
                "sessionId", sessionId
        ));
    }

    /**
     * Health check endpoint.
     * GET /api/v1/shopping/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "agent", "ShoppingAssistant",
                "model", "OPENAI"
        ));
    }
}
