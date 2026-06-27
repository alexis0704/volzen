package com.app.venus.modules.advisor.infrastructure;

import org.springframework.stereotype.Component;

import com.app.venus.modules.advisor.application.AdvisorModelClient;
import com.app.venus.modules.ai.application.AiOperation;
import com.app.venus.modules.ai.application.AiRequest;
import com.app.venus.modules.ai.infrastructure.OpenAiCompatibleClient;

@Component
public class OpenAiAdvisorModelClient implements AdvisorModelClient {
    private final OpenAiCompatibleClient openAiClient;

    public OpenAiAdvisorModelClient(OpenAiCompatibleClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    @Override
    public String completeJson(String prompt, String systemPrompt) {
        return openAiClient.complete(new AiRequest(AiOperation.EXTRACT_STRUCTURED, prompt, systemPrompt,
                "Return only the Volzen Advisor JSON response contract.")).text();
    }

    @Override
    public String providerName() {
        return "openai";
    }
}
