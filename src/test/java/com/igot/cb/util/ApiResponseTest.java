package com.igot.cb.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    void defaultConstructor_setsVersionTimestampAndParams() {
        ApiResponse response = new ApiResponse();

        assertEquals("v1", response.getVer());
        assertNotNull(response.getTs());
        assertNotNull(response.getParams());
        assertNotNull(response.getParams().getResMsgId());
    }

    @Test
    void idConstructor_setsId() {
        ApiResponse response = new ApiResponse("resp-1");

        assertEquals("resp-1", response.getId());
    }

    @Test
    void settersAndGetters_roundTrip() {
        ApiResponse response = new ApiResponse();
        response.setId("id1");
        response.setVer("v2");
        response.setTs("ts1");
        response.setMessage("msg");
        response.setResponseCode(HttpStatus.OK);
        ApiRespParam params = new ApiRespParam("msgId");
        response.setParams(params);

        assertEquals("id1", response.getId());
        assertEquals("v2", response.getVer());
        assertEquals("ts1", response.getTs());
        assertEquals("msg", response.getMessage());
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertEquals(params, response.getParams());
    }

    @Test
    void putGetContainsKey_workOnResultMap() {
        ApiResponse response = new ApiResponse();

        response.put("key1", "value1");

        assertEquals("value1", response.get("key1"));
        assertTrue(response.containsKey("key1"));
        assertFalse(response.containsKey("missing"));
    }

    @Test
    void putAll_mergesMapIntoResult() {
        ApiResponse response = new ApiResponse();

        response.putAll(java.util.Map.of("a", 1, "b", 2));

        assertEquals(1, response.get("a"));
        assertEquals(2, response.get("b"));
    }

    @Test
    void setResult_replacesResultMap() {
        ApiResponse response = new ApiResponse();
        java.util.Map<String, Object> newResult = new java.util.HashMap<>();
        newResult.put("x", "y");

        response.setResult(newResult);

        assertEquals(newResult, response.getResult());
    }
}
