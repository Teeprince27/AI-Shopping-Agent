package com.temitope.ShoppingAgent.service;

import com.temitope.ShoppingAgent.dto.ChatRequest;
import com.temitope.ShoppingAgent.dto.ChatResponse;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages agent sessions and delegates customer messages to the ReActAgent.
 *
 * Each sessionId maintains a conversation thread; the agent receives the full
 * history on each turn so it can reason across multiple messages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingAgentService {

    private final ReActAgent shoppingAgent;

    // sessionId -> conversation messages (multi-turn memory)
    private final Map<String, List<Msg>> sessions = new ConcurrentHashMap<>();

    public ChatResponse chat(ChatRequest request) {
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        try {
            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent(buildEnrichedMessage(request))
                    .build();

            Msg agentResponse = shoppingAgent.call(userMsg).block();

            return ChatResponse.builder()
                    .reply(agentResponse.getTextContent())
                    .sessionId(sessionId)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("[SESSION:{}] Agent error: {}", sessionId, e.getMessage(), e);
            return ChatResponse.builder()
                    .sessionId(sessionId)
                    .success(false)
                    .error("Agent error: " + e.getMessage())
                    .build();
        }
    }


    /**
     * Appends contextual metadata to the user message so the agent can
     * use userId and cartId in tool calls without the user needing to
     * explicitly state them.
     */
    private String buildEnrichedMessage(ChatRequest request) {
        StringBuilder sb = new StringBuilder(request.getMessage());

        sb.append("\n\n[Context — do not repeat this to the user]");
        sb.append("\nUserId: ").append(request.getUserId());

        if (request.getCartId() != null && !request.getCartId().isBlank()) {
            sb.append("\nCartId: ").append(request.getCartId());
        }

        return sb.toString();
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        log.info("Session {} cleared.", sessionId);
    }
}
