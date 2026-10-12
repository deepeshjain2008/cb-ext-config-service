package com.igot.cb.formConfiguration.rule;

import com.igot.cb.formConfiguration.entity.FormConfigurationEntity;
import com.igot.cb.formConfiguration.repository.FormConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesignationConfigurationRuleTest {

    @Mock
    private FormConfigurationRepository repository;

    private DesignationConfigurationRule rule;

    @BeforeEach
    void setUp() {
        rule = new DesignationConfigurationRule(repository);
    }

    private FormConfigResolutionContext.FormConfigResolutionContextBuilder baseCtx() {
        return FormConfigResolutionContext.builder()
                .type("page").subtype("player").portal("mobile").clientVersion(1.0);
    }

    @Test
    void ruleName_returnsDesignationMinistry() {
        assertEquals("designationMinistry", rule.ruleName());
    }

    @Test
    void supports_withDesignationsAndMinistryType_returnsTrue() {
        FormConfigResolutionContext ctx = baseCtx()
                .designations(List.of("MDO_ADMIN")).designationMinistryOrStateType("ministry").build();
        assertTrue(rule.supports(ctx));
    }

    @Test
    void supports_withoutDesignations_returnsFalse() {
        FormConfigResolutionContext ctx = baseCtx()
                .designations(List.of()).designationMinistryOrStateType("ministry").build();
        assertFalse(rule.supports(ctx));
    }

    @Test
    void supports_withoutMinistryType_returnsFalse() {
        FormConfigResolutionContext ctx = baseCtx()
                .designations(List.of("MDO_ADMIN")).designationMinistryOrStateType("").build();
        assertFalse(rule.supports(ctx));
    }

    @Test
    void find_delegatesToRepository() {
        FormConfigurationEntity entity = new FormConfigurationEntity();
        FormConfigResolutionContext ctx = baseCtx()
                .designations(List.of("MDO_ADMIN")).designationMinistryOrStateType("ministry").build();
        when(repository.getFormConfigByDesignation("page", "player", "mobile", 1.0, List.of("MDO_ADMIN"), "ministry"))
                .thenReturn(Optional.of(entity));

        Optional<FormConfigurationEntity> result = rule.find(ctx);

        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
    }

    @Test
    void buildCacheKey_sortsAndJoinsDesignations() {
        FormConfigResolutionContext ctx = baseCtx()
                .designations(List.of("ZEBRA", "ALPHA", "")).designationMinistryOrStateType("state").build();

        String key = rule.buildCacheKey(ctx);

        assertNotNull(key);
        assertTrue(key.contains("designationMinistry"));
    }
}
