package com.temitope.ShoppingAgent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    @NotBlank(message = "Message cannot be blank")
    private String message;

    private String userId = "GUEST";
    private String cartId;              // optional — for discount flow
    private String sessionId;           // optional — for multi-turn context
}
