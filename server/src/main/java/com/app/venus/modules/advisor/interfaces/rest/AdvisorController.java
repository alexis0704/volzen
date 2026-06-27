package com.app.venus.modules.advisor.interfaces.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.venus.modules.advisor.application.AdvisorContract;
import com.app.venus.modules.advisor.application.VolzenAdvisorService;
import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorChatRequest;
import com.app.venus.modules.advisor.interfaces.dto.response.AdvisorChatResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping(AdvisorContract.CHAT_ENDPOINT)
public class AdvisorController {
    private final VolzenAdvisorService advisorService;

    public AdvisorController(VolzenAdvisorService advisorService) {
        this.advisorService = advisorService;
    }

    @PostMapping
    public AdvisorChatResponse chat(@Valid @RequestBody AdvisorChatRequest request) {
        return advisorService.chat(request);
    }
}
