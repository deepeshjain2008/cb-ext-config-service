package com.igot.cb.formConfiguration.rule;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormConfigCacheKeysTest {

    @Test
    void build_withNullRolesAndNoDesignation_usesUnderscoreSegments() {
        String key = FormConfigCacheKeys.build("noCriteria", "page", "player", "mobile", 1.0, null, null, null);

        assertEquals("form.config.result.noCriteria.page.player.mobile.1.0._._", key);
    }

    @Test
    void build_withEmptyRoles_usesUnderscoreSegment() {
        String key = FormConfigCacheKeys.build("roleRootOrg", "page", "player", "mobile", 1.0, "org1", List.of(), null);

        assertTrue(key.endsWith("org1._"));
    }

    @Test
    void build_sortsAndFiltersBlankRoles() {
        String key = FormConfigCacheKeys.build("roleRootOrg", "page", "player", "mobile", 1.0, "org1",
                List.of("ZEBRA", "", "ALPHA"), null);

        assertTrue(key.endsWith("org1.ALPHA|ZEBRA"));
    }

    @Test
    void build_withDesignationSegment_appendsHash() {
        String key = FormConfigCacheKeys.build("designationMinistry", "page", "player", "mobile", 1.0,
                "ministry", null, "MDO_ADMIN");

        assertNotNull(key);
        String hashSegment = key.substring(key.length() - 16);
        assertTrue(hashSegment.matches("[0-9a-f]{16}"));
        assertEquals("form.config.result.designationMinistry.page.player.mobile.1.0.ministry._." + hashSegment, key);
    }

    @Test
    void build_sameDesignationSegment_producesSameHash() {
        String key1 = FormConfigCacheKeys.build("designationMinistry", "page", "player", "mobile", 1.0,
                "ministry", null, "MDO_ADMIN|STATE_LEAD");
        String key2 = FormConfigCacheKeys.build("designationMinistry", "page", "player", "mobile", 1.0,
                "ministry", null, "MDO_ADMIN|STATE_LEAD");

        assertEquals(key1, key2);
    }

    @Test
    void build_blankDesignationSegment_omitsHashSegment() {
        String key = FormConfigCacheKeys.build("roleRootOrg", "page", "player", "mobile", 1.0, "org1", null, "  ");

        assertEquals("form.config.result.roleRootOrg.page.player.mobile.1.0.org1._", key);
    }
}
