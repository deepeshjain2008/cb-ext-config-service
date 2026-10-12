package com.igot.cb.formConfiguration.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormConfigurationEntityTest {

    @Test
    void noArgsConstructor_settersAndGetters_roundTrip() {
        JsonNode criteria = new ObjectMapper().valueToTree(java.util.Map.of("role", "PUBLIC"));
        JsonNode data = new ObjectMapper().valueToTree(java.util.Map.of("field", "value"));

        FormConfigurationEntity entity = new FormConfigurationEntity();
        entity.setId(1L);
        entity.setName("testName");
        entity.setType("page");
        entity.setSubtype("player");
        entity.setPortal("mobile");
        entity.setCriteria(criteria);
        entity.setData(data);
        entity.setClientVersion(1.0);
        entity.setCreatedAt("2026-01-01");
        entity.setUpdatedAt("2026-01-02");
        entity.setCreatedBy("user1");
        entity.setUpdatedBy("user2");

        assertEquals(1L, entity.getId());
        assertEquals("testName", entity.getName());
        assertEquals("page", entity.getType());
        assertEquals("player", entity.getSubtype());
        assertEquals("mobile", entity.getPortal());
        assertEquals(criteria, entity.getCriteria());
        assertEquals(data, entity.getData());
        assertEquals(1.0, entity.getClientVersion());
        assertEquals("2026-01-01", entity.getCreatedAt());
        assertEquals("2026-01-02", entity.getUpdatedAt());
        assertEquals("user1", entity.getCreatedBy());
        assertEquals("user2", entity.getUpdatedBy());
    }

    @Test
    void allArgsConstructor_setsAllFields() {
        FormConfigurationEntity entity = new FormConfigurationEntity(
                2L, "name2", "type2", "subtype2", "portal2", null, null, 2.0,
                "created", "updated", "createdBy", "updatedBy");

        assertEquals(2L, entity.getId());
        assertEquals("name2", entity.getName());
        assertEquals("type2", entity.getType());
        assertEquals("subtype2", entity.getSubtype());
        assertEquals("portal2", entity.getPortal());
        assertEquals(2.0, entity.getClientVersion());
    }
}
