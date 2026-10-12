package com.igot.cb.formConfiguration.rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.formConfiguration.entity.FormConfigurationEntity;
import com.igot.cb.formConfiguration.service.cache.CacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormConfigRuleEngineTest {

    @Mock
    private CacheService cacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private FormConfigRuleProperties propertiesWith(Map<String, Integer> scores) {
        FormConfigRuleProperties props = new FormConfigRuleProperties();
        props.setScores(new LinkedHashMap<>(scores));
        return props;
    }

    private FormConfigLookupRule mockRule(String name, boolean supports) {
        FormConfigLookupRule rule = mock(FormConfigLookupRule.class);
        when(rule.ruleName()).thenReturn(name);
        when(rule.supports(any())).thenReturn(supports);
        return rule;
    }

    private FormConfigResolutionContext ctx() {
        return FormConfigResolutionContext.builder().type("page").subtype("player").portal("mobile").clientVersion(1.0).build();
    }

    @Test
    void resolve_noRuleSupports_returnsEmpty() {
        FormConfigLookupRule rule = mockRule("noCriteria", false);
        FormConfigRuleEngine engine = new FormConfigRuleEngine(List.of(rule), propertiesWith(Map.of("noCriteria", 10)),
                cacheService, objectMapper);

        assertTrue(engine.resolve(ctx()).isEmpty());
        verify(cacheService, never()).getCache(anyString());
    }

    @Test
    void resolve_cacheHit_returnsDeserializedEntity() {
        FormConfigLookupRule rule = mockRule("noCriteria", true);
        when(rule.buildCacheKey(any())).thenReturn("cache-key");
        FormConfigurationEntity entity = new FormConfigurationEntity();
        entity.setName("cachedEntity");
        String cachedJson = toJson(entity);
        when(cacheService.getCache("cache-key")).thenReturn(cachedJson);

        FormConfigRuleEngine engine = new FormConfigRuleEngine(List.of(rule), propertiesWith(Map.of("noCriteria", 10)),
                cacheService, objectMapper);

        Optional<FormConfigurationEntity> result = engine.resolve(ctx());

        assertTrue(result.isPresent());
        assertEquals("cachedEntity", result.get().getName());
        verify(rule, never()).find(any());
    }

    @Test
    void resolve_cacheReturnsEmptyMarker_skipsToNextRule() {
        FormConfigLookupRule firstRule = mockRule("designationMinistry", true);
        when(firstRule.buildCacheKey(any())).thenReturn("key1");
        when(cacheService.getCache("key1")).thenReturn("EMPTY");

        FormConfigLookupRule secondRule = mockRule("noCriteria", true);
        when(secondRule.buildCacheKey(any())).thenReturn("key2");
        FormConfigurationEntity entity = new FormConfigurationEntity();
        when(cacheService.getCache("key2")).thenReturn(null);
        when(secondRule.find(any())).thenReturn(Optional.of(entity));

        FormConfigRuleEngine engine = new FormConfigRuleEngine(List.of(firstRule, secondRule),
                propertiesWith(Map.of("designationMinistry", 200, "noCriteria", 10)), cacheService, objectMapper);

        Optional<FormConfigurationEntity> result = engine.resolve(ctx());

        assertTrue(result.isPresent());
        verify(firstRule, never()).find(any());
    }

    @Test
    void resolve_cacheDeserializeFails_fallsBackToFind() {
        FormConfigLookupRule rule = mockRule("noCriteria", true);
        when(rule.buildCacheKey(any())).thenReturn("key1");
        when(cacheService.getCache("key1")).thenReturn("not-valid-json");
        FormConfigurationEntity entity = new FormConfigurationEntity();
        when(rule.find(any())).thenReturn(Optional.of(entity));

        FormConfigRuleEngine engine = new FormConfigRuleEngine(List.of(rule), propertiesWith(Map.of("noCriteria", 10)),
                cacheService, objectMapper);

        Optional<FormConfigurationEntity> result = engine.resolve(ctx());

        assertTrue(result.isPresent());
        verify(cacheService).putCache("key1", entity);
    }

    @Test
    void resolve_ruleFindEmpty_cachesEmptyMarkerAndReturnsEmpty() {
        FormConfigLookupRule rule = mockRule("noCriteria", true);
        when(rule.buildCacheKey(any())).thenReturn("key1");
        when(cacheService.getCache("key1")).thenReturn(null);
        when(rule.find(any())).thenReturn(Optional.empty());

        FormConfigRuleEngine engine = new FormConfigRuleEngine(List.of(rule), propertiesWith(Map.of("noCriteria", 10)),
                cacheService, objectMapper);

        Optional<FormConfigurationEntity> result = engine.resolve(ctx());

        assertFalse(result.isPresent());
        verify(cacheService).putCache("key1", "EMPTY");
    }

    @Test
    void resolve_higherScoreRuleTriedFirst() {
        FormConfigLookupRule lowPriority = mockRule("noCriteria", true);
        when(lowPriority.buildCacheKey(any())).thenReturn("low-key");

        FormConfigLookupRule highPriority = mockRule("designationMinistry", true);
        when(highPriority.buildCacheKey(any())).thenReturn("high-key");
        FormConfigurationEntity entity = new FormConfigurationEntity();
        when(cacheService.getCache("high-key")).thenReturn(null);
        when(highPriority.find(any())).thenReturn(Optional.of(entity));

        FormConfigRuleEngine engine = new FormConfigRuleEngine(List.of(lowPriority, highPriority),
                propertiesWith(Map.of("noCriteria", 10, "designationMinistry", 200)), cacheService, objectMapper);

        engine.resolve(ctx());

        verify(lowPriority, never()).find(any());
    }

    @Test
    void resolve_ruleWithoutConfiguredScore_defaultsToZeroAndIsTriedLast() {
        FormConfigLookupRule unscored = mockRule("unscoredRule", true);
        when(unscored.buildCacheKey(any())).thenReturn("unscored-key");

        FormConfigLookupRule scored = mockRule("designationMinistry", true);
        when(scored.buildCacheKey(any())).thenReturn("scored-key");
        FormConfigurationEntity entity = new FormConfigurationEntity();
        when(cacheService.getCache("scored-key")).thenReturn(null);
        when(scored.find(any())).thenReturn(Optional.of(entity));

        FormConfigRuleEngine engine = new FormConfigRuleEngine(List.of(unscored, scored),
                propertiesWith(Map.of("designationMinistry", 200)), cacheService, objectMapper);

        engine.resolve(ctx());

        verify(unscored, never()).find(any());
    }

    private String toJson(FormConfigurationEntity entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
