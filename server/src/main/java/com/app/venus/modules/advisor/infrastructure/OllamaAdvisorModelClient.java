package com.app.venus.modules.advisor.infrastructure;

import org.springframework.stereotype.Component;

import com.app.venus.modules.advisor.application.AdvisorModelClient;
import com.app.venus.modules.ai.application.AiOperation;
import com.app.venus.modules.ai.application.AiRequest;
import com.app.venus.modules.ai.infrastructure.OllamaClient;

@Component
public class OllamaAdvisorModelClient implements AdvisorModelClient {
    private final OllamaClient ollamaClient;

    public OllamaAdvisorModelClient(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    @Override
    public String completeJson(String prompt, String systemPrompt) {
        return ollamaClient.complete(new AiRequest(AiOperation.EXTRACT_STRUCTURED, prompt, systemPrompt,
                "Return only the Volzen Advisor JSON response contract.")).text();
    }

    @Override
    public String providerName() {
        return "ollama";
    }
}
