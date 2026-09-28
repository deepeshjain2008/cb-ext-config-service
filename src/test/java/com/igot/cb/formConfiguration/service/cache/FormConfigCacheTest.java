package com.igot.cb.formConfiguration.service.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.formConfiguration.entity.FormConfigurationEntity;
import com.igot.cb.formConfiguration.repository.FormConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormConfigCacheTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FormConfigurationRepository repository;

    private FormConfigCache formConfigCache;

    @BeforeEach
    void setUp() {
        formConfigCache = new FormConfigCache(repository, objectMapper);
    }

    private FormConfigurationEntity entity(long id, String type, String subtype, String portal,
                                            String rootOrg, String role, Double clientVersion) {
        FormConfigurationEntity e = new FormConfigurationEntity();
        e.setId(id);
        e.setName("name-" + id);
        e.setType(type);
        e.setSubtype(subtype);
        e.setPortal(portal);
        e.setClientVersion(clientVersion);
        e.setCreatedAt("2026-01-01");
        JsonNode criteria = objectMapper.createObjectNode()
                .put("rootOrg", rootOrg)
                .put("role", role);
        e.setCriteria(rootOrg == null && role == null ? null : criteria);
        e.setData(objectMapper.createObjectNode().put("key", "value"));
        return e;
    }

    @Test
    void isLoaded_shouldBeFalseBeforeAnyReload() {
        assertFalse(formConfigCache.isLoaded());
    }

    @Test
    void get_shouldReturnNullForUnknownKey() {
        assertNull(formConfigCache.get("does.not.exist"));
    }

    @Test
    void reload_shouldPopulateSnapshotFromRepository() {
        FormConfigurationEntity e = entity(1L, "type1", "sub1", "portal1", "org1", "role1", 1.0);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(e));

        formConfigCache.reload();

        assertTrue(formConfigCache.isLoaded());
        String key = FormConfigCache.cacheKey("type1", "sub1", "portal1", "org1", "role1", 1.0);
        FormConfigCache.CachedFormConfig cached = formConfigCache.get(key);
        assertEquals("name-1", cached.result().get("name"));
        assertEquals("type1", cached.result().get("type"));
        assertEquals("2026-01-01", cached.createdAt());
        assertEquals("value", ((Map<?, ?>) cached.result().get("data")).get("key"));
    }

    @Test
    void reload_shouldSkipEntityMissingCriteria() {
        FormConfigurationEntity missingCriteria = entity(1L, "type1", "sub1", "portal1", null, null, 1.0);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(missingCriteria));

        formConfigCache.reload();

        assertTrue(formConfigCache.isLoaded());
        String key = FormConfigCache.cacheKey("type1", "sub1", "portal1", null, null, 1.0);
        assertNull(formConfigCache.get(key));
    }

    @Test
    void reload_shouldSkipEntityMissingRoleOnly() {
        FormConfigurationEntity missingRole = entity(1L, "type1", "sub1", "portal1", "org1", null, 1.0);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(missingRole));

        formConfigCache.reload();

        String key = FormConfigCache.cacheKey("type1", "sub1", "portal1", "org1", null, 1.0);
        assertNull(formConfigCache.get(key));
    }

    @Test
    void reload_shouldLetLastDuplicateKeyWin() {
        FormConfigurationEntity first = entity(1L, "type1", "sub1", "portal1", "org1", "role1", 1.0);
        FormConfigurationEntity second = entity(2L, "type1", "sub1", "portal1", "org1", "role1", 1.0);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(first, second));

        formConfigCache.reload();

        String key = FormConfigCache.cacheKey("type1", "sub1", "portal1", "org1", "role1", 1.0);
        assertEquals("name-2", formConfigCache.get(key).result().get("name"));
    }

    @Test
    void reload_shouldRetainPreviousSnapshotWhenRepositoryFails() {
        FormConfigurationEntity e = entity(1L, "type1", "sub1", "portal1", "org1", "role1", 1.0);
        when(repository.findAll(any(Sort.class))).thenReturn(List.of(e));
        formConfigCache.reload();
        String key = FormConfigCache.cacheKey("type1", "sub1", "portal1", "org1", "role1", 1.0);
        assertEquals("name-1", formConfigCache.get(key).result().get("name"));

        when(repository.findAll(any(Sort.class))).thenThrow(new RuntimeException("DB down"));
        formConfigCache.reload();

        assertEquals("name-1", formConfigCache.get(key).result().get("name"));
    }

    @Test
    void init_shouldTriggerInitialReload() {
        when(repository.findAll(any(Sort.class))).thenReturn(List.of());

        formConfigCache.init();

        assertTrue(formConfigCache.isLoaded());
    }

    @Test
    void cacheKey_shouldIncludeOnlyNonNullOptionalSegments() {
        String fullKey = FormConfigCache.cacheKey("t", "s", "p", "org", "role", 2.0);
        assertTrue(fullKey.endsWith(".t.s.p.org.role.2.0"));

        String minimalKey = FormConfigCache.cacheKey("t", "s", "p", null, null, null);
        assertTrue(minimalKey.endsWith(".t.s.p"));
        assertFalse(minimalKey.contains("org"));
    }
}
