package com.app.venus.modules.advisor.application;

import com.app.venus.modules.advisor.domain.AdvisorProvider;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;

public interface AdvisorChatProvider {
    AdvisorProvider provider();

    AdvisorChatResponse chat(AdvisorChatRequest request);
}
