package com.temitope.ShoppingAgent.config;

import com.temitope.ShoppingAgent.tools.ShoppingTools;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.tool.Toolkit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires up the AgentScope ReActAgent with:
 *  - ShoppingTools registered as callable tools
 *  - System prompt describing the agent's role
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AgentConfig {

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.model}")
    private String modelName;

    @Value("${openai.base-url}")
    private String baseUrl;

    @Value("${agent.system-prompt}")
    private String systemPrompt;

    @Value("${agent.max-iterations:10}")
    private int maxIterations;

    private final ShoppingTools shoppingTools;

    @Bean
    public OpenAIChatModel agentScopeOpenAiModel() {
        return OpenAIChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(openAiApiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public Toolkit shoppingToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(shoppingTools);   // annotations on ShoppingTools drive registration
        return toolkit;
    }

    @Bean
    public ReActAgent shoppingAgent(OpenAIChatModel agentScopeOpenAiModel, Toolkit toolkit) {
        log.info("Initializing ShoppingAssistant agent with model={}", modelName);
        return ReActAgent.builder()
                .name("ShoppingAssistant")
                .model(agentScopeOpenAiModel)
                .toolkit(toolkit)
                .sysPrompt(systemPrompt)
                .maxIters(maxIterations)
                .build();
    }

}
