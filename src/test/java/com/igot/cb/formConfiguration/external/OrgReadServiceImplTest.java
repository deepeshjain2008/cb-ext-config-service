package com.igot.cb.formConfiguration.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.formConfiguration.service.cache.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgReadServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CacheService cacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrgReadServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrgReadServiceImpl(restTemplate, cacheService, objectMapper);
    }

    @Test
    void getMinistryOrStateType_blankRootOrgId_returnsNull() {
        assertNull(service.getMinistryOrStateType("", "token"));
        assertNull(service.getMinistryOrStateType(null, "token"));
    }

    @Test
    void getMinistryOrStateType_wildcardRootOrgId_returnsNull() {
        assertNull(service.getMinistryOrStateType("*", "token"));
    }

    @Test
    void getMinistryOrStateType_cacheHit_returnsCachedValue() {
        when(cacheService.getCache(anyString())).thenReturn("\"ministry\"");

        String result = service.getMinistryOrStateType("org1", "token");

        assertEquals("ministry", result);
        verify(restTemplate, org.mockito.Mockito.never()).postForObject(any(), any(), any());
    }

    @Test
    void getMinistryOrStateType_cacheDeserializeFails_fallsBackToRestCall() throws Exception {
        when(cacheService.getCache(anyString())).thenReturn("not-valid-json");
        JsonNode response = objectMapper.valueToTree(
                Map.of("result", Map.of("response", Map.of("ministryOrStateType", "state"))));
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), org.mockito.ArgumentMatchers.eq(JsonNode.class)))
                .thenReturn(response);

        String result = service.getMinistryOrStateType("org1", "token");

        assertEquals("state", result);
    }

    @Test
    void getMinistryOrStateType_restCall_ministryType_returnsMinistryAndCaches() {
        when(cacheService.getCache(anyString())).thenReturn(null);
        JsonNode response = objectMapper.valueToTree(
                Map.of("result", Map.of("response", Map.of("ministryOrStateType", "MINISTRY"))));
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), org.mockito.ArgumentMatchers.eq(JsonNode.class)))
                .thenReturn(response);

        String result = service.getMinistryOrStateType("org1", "token");

        assertEquals("ministry", result);
        verify(cacheService).putCache(anyString(), org.mockito.ArgumentMatchers.eq("ministry"));
    }

    @Test
    void getMinistryOrStateType_restCall_spvFallsBackToSbOrgType() {
        when(cacheService.getCache(anyString())).thenReturn(null);
        JsonNode response = objectMapper.valueToTree(
                Map.of("result", Map.of("response", Map.of(
                        "ministryOrStateType", "SPV",
                        "sbOrgType", "state"))));
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), org.mockito.ArgumentMatchers.eq(JsonNode.class)))
                .thenReturn(response);

        String result = service.getMinistryOrStateType("org1", "token");

        assertEquals("state", result);
    }

    @Test
    void getMinistryOrStateType_restCall_noToken_noUsableFields_returnsNull() {
        when(cacheService.getCache(anyString())).thenReturn(null);
        JsonNode response = objectMapper.valueToTree(Map.of("result", Map.of("response", Map.of())));
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), org.mockito.ArgumentMatchers.eq(JsonNode.class)))
                .thenReturn(response);

        String result = service.getMinistryOrStateType("org1", null);

        assertNull(result);
        verify(cacheService, org.mockito.Mockito.never()).putCache(anyString(), any());
    }

    @Test
    void getMinistryOrStateType_restCall_nullResponse_returnsNull() {
        when(cacheService.getCache(anyString())).thenReturn(null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), org.mockito.ArgumentMatchers.eq(JsonNode.class)))
                .thenReturn(null);

        assertNull(service.getMinistryOrStateType("org1", "token"));
    }

    @Test
    void getMinistryOrStateType_restCallThrows_returnsNull() {
        when(cacheService.getCache(anyString())).thenReturn(null);
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), org.mockito.ArgumentMatchers.eq(JsonNode.class)))
                .thenThrow(new RuntimeException("downstream error"));

        assertNull(service.getMinistryOrStateType("org1", "token"));
    }
}
