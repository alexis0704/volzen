package com.app.venus.modules.advisor.application;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.app.venus.modules.advisor.interfaces.dto.request.AdvisorLocationContextRequest;

@Component
public class AdvisorKnowledgeBase {
    private static final Pattern FIELD = Pattern.compile("^\\s*(\\w+):\\s*(.+)\\s*$");
    private static final Pattern JSON_OBJECT = Pattern.compile("\\{([^{}]+)}");
    private final List<AdvisorKnowledgeSnippet> snippets;
    private final Map<String, AdvisorKnowledgeSource> sources;

    @Autowired
    public AdvisorKnowledgeBase(ResourcePatternResolver resolver) {
        try {
            Resource[] resources = resolver.getResources("classpath:/advisor/knowledge/*");
            List<NamedContent> files = new ArrayList<>();
            for (Resource resource : resources) {
                files.add(new NamedContent(resource.getFilename(), resource.getContentAsString(StandardCharsets.UTF_8)));
            }
            LoadedKnowledge loaded = load(files);
            this.snippets = loaded.snippets();
            this.sources = loaded.sources();
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to load advisor knowledge files.", ex);
        }
    }

    AdvisorKnowledgeBase(List<NamedContent> files) {
        LoadedKnowledge loaded = load(files);
        this.snippets = loaded.snippets();
        this.sources = loaded.sources();
    }

    public AdvisorRetrievalResult retrieve(String message, AdvisorLocationContextRequest locationContext, int limit) {
        String query = normalize(message + " " + locationWords(locationContext));
        boolean hasLocationIntent = hasAny(query, "district", "location", "hcmc", "ho chi minh", "demand", "parking")
                || locationContext != null;

        List<AdvisorKnowledgeSnippet> scored = snippets.stream()
                .filter(snippet -> hasLocationIntent || !snippet.id().startsWith("location-"))
                .map(snippet -> snippet.withScore(score(query, snippet)))
                .filter(snippet -> snippet.score() > 0)
                .sorted(Comparator.comparingInt(AdvisorKnowledgeSnippet::score).reversed())
                .limit(limit)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        includeProductBoundaries(scored);
        if (scored.stream().noneMatch(snippet -> snippet.score() > 1)) {
            return new AdvisorRetrievalResult(scored, false, AdvisorContract.FALLBACK_ANSWER);
        }
        return new AdvisorRetrievalResult(scored, true, null);
    }

    public List<AdvisorKnowledgeSnippet> snippets() {
        return snippets;
    }

    public Map<String, AdvisorKnowledgeSource> sources() {
        return sources;
    }

    static LoadedKnowledge load(List<NamedContent> files) {
        AdvisorKnowledgeValidator validator = new AdvisorKnowledgeValidator();
        Map<String, AdvisorKnowledgeSource> sources = new LinkedHashMap<>();
        List<PartialSnippet> partials = new ArrayList<>();

        for (NamedContent file : files) {
            List<String> validationErrors = validator.validate(file.name(), file.content());
            if (!validationErrors.isEmpty()) {
                throw new IllegalStateException(String.join("; ", validationErrors));
            }
            if (file.name().endsWith(".md")) {
                parseMarkdown(file, partials, sources);
            } else if (file.name().endsWith(".json")) {
                parseLocationJson(file, partials, sources);
            }
        }

        Set<String> snippetIds = new HashSet<>();
        List<AdvisorKnowledgeSnippet> snippets = partials.stream()
                .map(partial -> {
                    if (!snippetIds.add(partial.id())) {
                        throw new IllegalStateException("Duplicate advisor knowledge item id: " + partial.id());
                    }
                    AdvisorKnowledgeSource source = sources.get(partial.sourceId());
                    if (source == null) {
                        throw new IllegalStateException("Missing source metadata for sourceId: " + partial.sourceId());
                    }
                    return new AdvisorKnowledgeSnippet(
                            partial.id(),
                            partial.sourceId(),
                            partial.claimType(),
                            partial.claim(),
                            source,
                            partial.dataAsOf() == null ? source.lastReviewed() : partial.dataAsOf(),
                            0);
                })
                .toList();
        return new LoadedKnowledge(snippets, Map.copyOf(sources));
    }

    private static void parseMarkdown(NamedContent file, List<PartialSnippet> partials, Map<String, AdvisorKnowledgeSource> sources) {
        boolean inSources = false;
        Map<String, String> current = null;
        for (String line : file.content().split("\\R")) {
            if (line.startsWith("## Sources")) {
                addMarkdownItem(current, inSources, partials, sources);
                current = null;
                inSources = true;
                continue;
            }
            if (line.startsWith("- id:") || line.startsWith("- sourceId:")) {
                addMarkdownItem(current, inSources, partials, sources);
                current = new HashMap<>();
                String[] parts = line.substring(2).split(":", 2);
                current.put(parts[0].trim(), parts[1].trim());
                continue;
            }
            Matcher matcher = FIELD.matcher(line);
            if (current != null && matcher.matches()) {
                current.put(matcher.group(1), matcher.group(2));
            }
        }
        addMarkdownItem(current, inSources, partials, sources);
    }

    private static void addMarkdownItem(Map<String, String> item, boolean source, List<PartialSnippet> partials,
            Map<String, AdvisorKnowledgeSource> sources) {
        if (item == null || item.isEmpty()) {
            return;
        }
        if (source) {
            addSource(sources, new AdvisorKnowledgeSource(
                    item.get("sourceId"),
                    item.get("title"),
                    item.get("url"),
                    LocalDate.parse(item.get("lastReviewed")),
                    item.get("sourceType")));
            return;
        }
        partials.add(new PartialSnippet(
                item.get("id"),
                item.get("sourceId"),
                item.get("claimType"),
                item.getOrDefault("note", ""),
                item.containsKey("date") ? LocalDate.parse(item.get("date")) : null));
    }

    private static void parseLocationJson(NamedContent file, List<PartialSnippet> partials, Map<String, AdvisorKnowledgeSource> sources) {
        String rootSourceId = extract(file.content(), "\"sourceId\"\\s*:\\s*\"([^\"]+)\"");
        String dataAsOf = extract(file.content(), "\"dataAsOf\"\\s*:\\s*\"([^\"]+)\"");
        String sourceType = extract(file.content(), "\"sourceType\"\\s*:\\s*\"([^\"]+)\"");
        addSource(sources, new AdvisorKnowledgeSource(
                rootSourceId,
                "Volzen MVP demo location signals",
                "internal",
                LocalDate.parse(dataAsOf),
                sourceType));

        Matcher matcher = JSON_OBJECT.matcher(file.content());
        while (matcher.find()) {
            String object = matcher.group(1);
            String id = extract(object, "\"id\"\\s*:\\s*\"([^\"]+)\"");
            if (id == null) {
                continue;
            }
            String district = extract(object, "\"district\"\\s*:\\s*\"([^\"]+)\"");
            String city = extract(object, "\"city\"\\s*:\\s*\"([^\"]+)\"");
            String demand = extract(object, "\"demandPotentialLabel\"\\s*:\\s*\"([^\"]+)\"");
            String stay = extract(object, "\"longStayParkingPotential\"\\s*:\\s*\"([^\"]+)\"");
            partials.add(new PartialSnippet(
                    "location-" + id,
                    rootSourceId,
                    "internal/pilot",
                    city + " " + district + " has " + demand + " based on proxy signals; long-stay parking potential is " + stay + ".",
                    LocalDate.parse(dataAsOf)));
        }
    }

    private static void addSource(Map<String, AdvisorKnowledgeSource> sources, AdvisorKnowledgeSource source) {
        if (source.sourceId() == null || source.title() == null || source.url() == null
                || source.lastReviewed() == null || source.sourceType() == null) {
            throw new IllegalStateException("Advisor source metadata is incomplete.");
        }
        AdvisorKnowledgeSource existing = sources.putIfAbsent(source.sourceId(), source);
        if (existing != null && !existing.equals(source)) {
            throw new IllegalStateException("Duplicate advisor sourceId with conflicting metadata: " + source.sourceId());
        }
    }

    private void includeProductBoundaries(List<AdvisorKnowledgeSnippet> scored) {
        snippets.stream()
                .filter(snippet -> snippet.id().startsWith("policy-"))
                .limit(2)
                .filter(snippet -> scored.stream().noneMatch(existing -> existing.id().equals(snippet.id())))
                .forEach(snippet -> scored.add(snippet.withScore(1)));
    }

    private int score(String query, AdvisorKnowledgeSnippet snippet) {
        String haystack = normalize(snippet.id() + " " + snippet.claim() + " " + snippet.source().title());
        int score = 0;
        for (String token : query.split("\\s+")) {
            if (token.length() >= 3 && haystack.contains(token)) {
                score++;
            }
        }
        if (hasAny(query, "sell", "electricity") && snippet.claim().contains("does not sell electricity")) {
            score += 8;
        }
        if (hasAny(query, "hotel", "onboarding", "partner") && snippet.id().contains("hotel")) {
            score += 8;
        }
        if (hasAny(query, "host", "hosting", "sign up", "signup", "become", "onboarding")
                && snippet.id().contains("host-onboarding")) {
            score += 12;
        }
        if (hasAny(query, "payment", "pay", "card", "wallet", "momo", "method")
                && snippet.id().contains("payment-methods")) {
            score += 12;
        }
        if (hasAny(query, "connector", "ccs", "type") && snippet.id().contains("connector")) {
            score += 8;
        }
        if (hasAny(query, "district 7", "d7") && snippet.id().contains("d7")) {
            score += 10;
        }
        if (hasAny(query, "meter", "safety", "responsibility") && snippet.id().startsWith("policy-operator")) {
            score += 8;
        }
        return score;
    }

    private String locationWords(AdvisorLocationContextRequest locationContext) {
        if (locationContext == null) {
            return "";
        }
        return String.join(" ",
                value(locationContext.district()),
                value(locationContext.city()),
                value(locationContext.address()),
                value(locationContext.curatedLocationId()));
    }

    private static boolean hasAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static String extract(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }

    public record NamedContent(String name, String content) {
    }

    record PartialSnippet(String id, String sourceId, String claimType, String claim, LocalDate dataAsOf) {
    }

    record LoadedKnowledge(List<AdvisorKnowledgeSnippet> snippets, Map<String, AdvisorKnowledgeSource> sources) {
    }
}
