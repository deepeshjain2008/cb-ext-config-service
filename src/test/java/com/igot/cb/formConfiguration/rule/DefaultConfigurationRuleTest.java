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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultConfigurationRuleTest {

    @Mock
    private FormConfigurationRepository repository;

    private DefaultConfigurationRule rule;

    @BeforeEach
    void setUp() {
        rule = new DefaultConfigurationRule(repository);
    }

    private FormConfigResolutionContext ctx(String rootOrg) {
        return FormConfigResolutionContext.builder()
                .type("page").subtype("player").portal("mobile").clientVersion(1.0)
                .roles(List.of("PUBLIC")).rootOrg(rootOrg)
                .build();
    }

    @Test
    void ruleName_returnsRoleRootOrg() {
        assertEquals("roleRootOrg", rule.ruleName());
    }

    @Test
    void supports_withRolesAndRootOrg_returnsTrue() {
        assertTrue(rule.supports(ctx("org1")));
    }

    @Test
    void supports_withoutRoles_returnsFalse() {
        FormConfigResolutionContext ctx = FormConfigResolutionContext.builder()
                .roles(List.of()).rootOrg("org1").build();
        assertFalse(rule.supports(ctx));
    }

    @Test
    void supports_withoutRootOrg_returnsFalse() {
        FormConfigResolutionContext ctx = FormConfigResolutionContext.builder()
                .roles(List.of("PUBLIC")).rootOrg("").build();
        assertFalse(rule.supports(ctx));
    }

    @Test
    void find_foundForOwnRootOrg_doesNotFallBackToWildcard() {
        FormConfigurationEntity entity = new FormConfigurationEntity();
        when(repository.getDefaultFormConfigDataByCriteria("page", "player", "mobile", "org1", List.of("PUBLIC"), 1.0))
                .thenReturn(Optional.of(entity));

        Optional<FormConfigurationEntity> result = rule.find(ctx("org1"));

        assertTrue(result.isPresent());
        verify(repository, never()).getDefaultFormConfigDataByCriteria(any(), any(), any(), eq("*"), any(), any());
    }

    @Test
    void find_notFoundForOwnRootOrg_fallsBackToWildcard() {
        FormConfigurationEntity wildcardEntity = new FormConfigurationEntity();
        when(repository.getDefaultFormConfigDataByCriteria("page", "player", "mobile", "org1", List.of("PUBLIC"), 1.0))
                .thenReturn(Optional.empty());
        when(repository.getDefaultFormConfigDataByCriteria("page", "player", "mobile", "*", List.of("PUBLIC"), 1.0))
                .thenReturn(Optional.of(wildcardEntity));

        Optional<FormConfigurationEntity> result = rule.find(ctx("org1"));

        assertTrue(result.isPresent());
        assertEquals(wildcardEntity, result.get());
    }

    @Test
    void find_alreadyWildcardRootOrg_noFallbackCall() {
        when(repository.getDefaultFormConfigDataByCriteria("page", "player", "mobile", "*", List.of("PUBLIC"), 1.0))
                .thenReturn(Optional.empty());

        Optional<FormConfigurationEntity> result = rule.find(ctx("*"));

        assertFalse(result.isPresent());
        verify(repository, org.mockito.Mockito.times(1))
                .getDefaultFormConfigDataByCriteria(any(), any(), any(), any(), any(), any());
    }

    @Test
    void buildCacheKey_returnsNonNullKey() {
        String key = rule.buildCacheKey(ctx("org1"));
        assertNotNull(key);
        assertTrue(key.contains("roleRootOrg"));
    }
}
