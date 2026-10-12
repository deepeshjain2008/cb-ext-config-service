package com.igot.cb.formConfiguration.rule;

import com.igot.cb.formConfiguration.entity.FormConfigurationEntity;
import com.igot.cb.formConfiguration.repository.FormConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoCriteriaConfigurationRuleTest {

    @Mock
    private FormConfigurationRepository repository;

    private NoCriteriaConfigurationRule rule;

    @BeforeEach
    void setUp() {
        rule = new NoCriteriaConfigurationRule(repository);
    }

    private FormConfigResolutionContext ctx() {
        return FormConfigResolutionContext.builder()
                .type("page").subtype("player").portal("mobile").clientVersion(1.0).build();
    }

    @Test
    void ruleName_returnsNoCriteria() {
        assertEquals("noCriteria", rule.ruleName());
    }

    @Test
    void supports_alwaysReturnsTrue() {
        assertTrue(rule.supports(ctx()));
    }

    @Test
    void find_delegatesToRepository() {
        FormConfigurationEntity entity = new FormConfigurationEntity();
        when(repository.findByTypeAndSubtypeAndPortalAndClientVersionAndCriteriaIsNull("page", "player", "mobile", 1.0))
                .thenReturn(Optional.of(entity));

        Optional<FormConfigurationEntity> result = rule.find(ctx());

        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
    }

    @Test
    void buildCacheKey_returnsNonNullKey() {
        String key = rule.buildCacheKey(ctx());
        assertNotNull(key);
        assertTrue(key.contains("noCriteria"));
    }
}
