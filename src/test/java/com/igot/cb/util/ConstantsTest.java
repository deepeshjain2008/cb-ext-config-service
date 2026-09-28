package com.igot.cb.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantsTest {

    @Test
    void publicKeyRegexConstants_shouldMatchValuesPreviouslyInlinedInKeyManager() {
        assertEquals("(-+BEGIN PUBLIC KEY-+)", Constants.PUBLIC_KEY_HEADER);
        assertEquals("(-+END PUBLIC KEY-+)", Constants.PUBLIC_KEY_FOOTER);
        assertEquals("[\\r\\n]+", Constants.NEW_LINE_REGEX);
    }
}
