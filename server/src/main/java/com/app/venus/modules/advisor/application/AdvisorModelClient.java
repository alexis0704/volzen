package com.app.venus.modules.advisor.application;

public interface AdvisorModelClient {
    String completeJson(String prompt, String systemPrompt);

    String providerName();
}
