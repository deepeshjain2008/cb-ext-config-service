package com.igot.cb.formConfiguration.controller;

import com.igot.cb.formConfiguration.service.cache.CacheService;
import com.igot.cb.util.ApiResponse;
import com.igot.cb.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisCacheControllerTest {

    @InjectMocks
    private RedisCacheController redisCacheController;

    @Mock
    private CacheService redisCacheService;

    @Test
    void deleteCache_shouldReturnServiceResponseWithItsOwnStatusCode() throws Exception {
        ApiResponse serviceResponse = new ApiResponse();
        serviceResponse.setResponseCode(HttpStatus.OK);
        serviceResponse.getParams().setStatus(Constants.SUCCESSFUL);
        when(redisCacheService.deleteCache()).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse> result = redisCacheController.deleteCache();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(serviceResponse, result.getBody());
    }

    @Test
    void deleteCache_shouldUseNonOkStatusFromServiceResponse() throws Exception {
        ApiResponse serviceResponse = new ApiResponse();
        serviceResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        when(redisCacheService.deleteCache()).thenReturn(serviceResponse);

        ResponseEntity<ApiResponse> result = redisCacheController.deleteCache();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    void deleteCache_shouldPropagateExceptionFromService() {
        when(redisCacheService.deleteCache()).thenThrow(new RuntimeException("Redis unreachable"));

        assertThrows(RuntimeException.class, () -> redisCacheController.deleteCache());
    }
}
