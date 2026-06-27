package com.app.venus.modules.advisor.application;

import java.util.List;

import com.app.venus.shared.web.ApiPaths;

public final class AdvisorContract {
    public static final String CHAT_ENDPOINT = ApiPaths.API_V1 + "/advisor/chat";
    public static final String FALLBACK_ANSWER = "I don't have a verified answer for that yet.";
    public static final int MIN_ANSWER_SENTENCES = 2;
    public static final int MAX_ANSWER_SENTENCES = 4;
    public static final int TARGET_MAX_WORDS = 75;

    public static final List<String> REQUEST_FIELDS = List.of(
            "message",
            "conversationId",
            "locationContext",
            "preferredProvider");

    public static final List<String> LOCATION_CONTEXT_FIELDS = List.of(
            "district",
            "city",
            "address",
            "lat",
            "lng",
            "nearbyChargerCount",
            "siteTypeSignals",
            "longStayParkingPotential",
            "demandPotentialLabel",
            "curatedLocationId");

    public static final List<String> RESPONSE_FIELDS = List.of(
            "answer",
            "sourceIds",
            "grounded",
            "needsProfessionalReview",
            "dataAsOf",
            "provider",
            "unsupportedReason");

    public static final List<String> ANSWER_RULES = List.of(
            "Grounded answers must include sourceIds.",
            "Unsupported answers must return grounded=false.",
            "Legal answers are informational only and must not make legal conclusions.",
            "Market answers must include dates when source material contains dates.",
            "Location answers must use charging-demand potential or proxy estimate based on location signals.",
            "Exact EV-driver counts must not be claimed unless a verified source exists in the knowledge base.",
            "Incomplete site-readiness answers must say operator or professional review is needed.");

    public static final List<String> UNSUPPORTED_TOPICS = List.of(
            "general chat",
            "legal conclusions",
            "electrical certifications",
            "exact profitability forecasts",
            "real-time charger availability",
            "unverified district EV-user counts");

    private AdvisorContract() {
    }
}
