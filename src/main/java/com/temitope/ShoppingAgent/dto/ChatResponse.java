package com.temitope.ShoppingAgent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String reply;
    private String sessionId;
    private List<String> toolsUsed;    // which tools the agent called
    private int iterations;            // how many ReAct steps were taken
    private boolean success;
    private String error;
}