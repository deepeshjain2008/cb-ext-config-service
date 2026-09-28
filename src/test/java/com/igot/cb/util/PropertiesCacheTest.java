package com.igot.cb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PropertiesCacheTest {

    @Test
    void getInstance_shouldReturnSameSingletonInstanceOnRepeatedCalls() {
        PropertiesCache first = PropertiesCache.getInstance();
        PropertiesCache second = PropertiesCache.getInstance();

        assertSame(first, second);
    }

    @Test
    void getProperty_shouldReturnValueLoadedFromApplicationProperties() {
        String value = PropertiesCache.getInstance().getProperty("sso.realm");

        assertEquals("sunbird", value);
    }

    @Test
    void getProperty_shouldFallBackToKeyItselfWhenNotConfiguredAnywhere() {
        String key = "totally.unknown.property.not.configured";

        String value = PropertiesCache.getInstance().getProperty(key);

        assertEquals(key, value);
    }

    @Test
    void readProperty_shouldReturnValueLoadedFromApplicationProperties() {
        String value = PropertiesCache.getInstance().readProperty("redis.timeout");

        assertEquals("2000", value);
    }

    @Test
    void readProperty_shouldReturnNullWhenNotConfiguredAnywhere() {
        String value = PropertiesCache.getInstance().readProperty("totally.unknown.property.not.configured");

        assertNull(value);
    }
}
