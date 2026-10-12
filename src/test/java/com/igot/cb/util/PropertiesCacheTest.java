package com.igot.cb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PropertiesCacheTest {

    @Test
    void getInstance_returnsSameSingletonInstance() {
        PropertiesCache first = PropertiesCache.getInstance();
        PropertiesCache second = PropertiesCache.getInstance();

        assertSame(first, second);
    }

    @Test
    void getProperty_unknownKey_returnsKeyItself() {
        PropertiesCache cache = PropertiesCache.getInstance();

        String result = cache.getProperty("some.unknown.property.key.for.test");

        assertEquals("some.unknown.property.key.for.test", result);
    }

    @Test
    void readProperty_unknownKey_returnsNull() {
        PropertiesCache cache = PropertiesCache.getInstance();

        String result = cache.readProperty("some.unknown.property.key.for.test");

        assertNull(result);
    }
}
