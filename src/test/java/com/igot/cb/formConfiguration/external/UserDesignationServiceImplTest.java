package com.igot.cb.formConfiguration.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.authentication.model.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDesignationServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserDesignationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserDesignationServiceImpl(restTemplate);
    }

    @Test
    void resolveUserProfile_blankUserId_setsEmptyDesignationsAndSkipsCall() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("");

        service.resolveUserProfile(userDetails, "token");

        assertEquals(List.of(), userDetails.getDesignations());
    }

    @Test
    void resolveUserProfile_success_setsDesignations() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("user1");

        JsonNode response = objectMapper.valueToTree(Map.of(
                "result", Map.of("response", Map.of(
                        "profileDetails", Map.of("professionalDetails", List.of(
                                Map.of("designation", "MDO_ADMIN"),
                                Map.of("designation", "")
                        ))
                ))
        ));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(response));

        service.resolveUserProfile(userDetails, "token");

        assertEquals(List.of("MDO_ADMIN"), userDetails.getDesignations());
    }

    @Test
    void resolveUserProfile_noProfessionalDetails_setsEmptyDesignations() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("user1");

        JsonNode response = objectMapper.valueToTree(Map.of("result", Map.of("response", Map.of())));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class)))
                .thenReturn(ResponseEntity.ok(response));

        service.resolveUserProfile(userDetails, "token");

        assertTrue(userDetails.getDesignations().isEmpty());
    }

    @Test
    void resolveUserProfile_restCallThrows_setsEmptyDesignations() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId("user1");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(JsonNode.class)))
                .thenThrow(new RuntimeException("downstream error"));

        service.resolveUserProfile(userDetails, "token");

        assertTrue(userDetails.getDesignations().isEmpty());
    }
}
