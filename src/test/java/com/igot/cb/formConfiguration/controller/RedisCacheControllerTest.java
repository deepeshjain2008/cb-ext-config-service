package com.igot.cb.formConfiguration.controller;

import com.igot.cb.formConfiguration.service.cache.CacheService;
import com.igot.cb.util.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisCacheControllerTest {

    @Test
    void deleteCache_returnsResponseFromService() throws Exception {
        CacheService cacheService = mock(CacheService.class);
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.OK);
        when(cacheService.deleteCache()).thenReturn(response);

        RedisCacheController controller = new RedisCacheController(cacheService);
        ResponseEntity<ApiResponse> result = controller.deleteCache();

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void deleteCache_notFound_returnsResponseFromService() throws Exception {
        CacheService cacheService = mock(CacheService.class);
        ApiResponse response = new ApiResponse();
        response.setResponseCode(HttpStatus.NOT_FOUND);
        when(cacheService.deleteCache()).thenReturn(response);

        RedisCacheController controller = new RedisCacheController(cacheService);
        ResponseEntity<ApiResponse> result = controller.deleteCache();

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
