package com.app.venus.modules.advisor.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.advisor")
public class AppAdvisorProperties {
    private String provider = "openai";
    private int timeoutSeconds = 60;
    private boolean requestProviderOverrideEnabled = false;
    private OpenAi openai = new OpenAi();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isRequestProviderOverrideEnabled() {
        return requestProviderOverrideEnabled;
    }

    public void setRequestProviderOverrideEnabled(boolean requestProviderOverrideEnabled) {
        this.requestProviderOverrideEnabled = requestProviderOverrideEnabled;
    }

    public OpenAi getOpenai() {
        return openai;
    }

    public void setOpenai(OpenAi openai) {
        this.openai = openai;
    }

    public boolean hasOpenAiVectorStore() {
        return openai.vectorStoreId != null && !openai.vectorStoreId.isBlank();
    }

    public static class OpenAi {
        private String model;
        private String vectorStoreId;

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getVectorStoreId() {
            return vectorStoreId;
        }

        public void setVectorStoreId(String vectorStoreId) {
            this.vectorStoreId = vectorStoreId;
        }
    }
}
