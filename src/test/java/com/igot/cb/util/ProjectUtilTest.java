package com.igot.cb.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProjectUtilTest {

    @Test
    void createDefaultResponse_shouldPopulateIdVersionAndOkStatus() {
        ApiResponse response = ProjectUtil.createDefaultResponse("api.form.read");

        assertEquals("api.form.read", response.getId());
        assertEquals(Constants.API_VERSION_1, response.getVer());
        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertNotNull(response.getTs());
    }

    @Test
    void returnErrorMsg_shouldPopulateErrorFieldsOnExistingResponse() {
        ApiResponse response = new ApiResponse();

        ApiResponse result = ProjectUtil.returnErrorMsg(
                "bad input", HttpStatus.BAD_REQUEST, response, Constants.FAILED);

        assertSame(response, result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getResponseCode());
        assertEquals("bad input", result.getParams().getErr());
        assertEquals("bad input", result.getParams().getErrMsg());
        assertEquals(Constants.FAILED, result.getParams().getStatus());
        assertEquals(Constants.FAILED, result.getMessage());
    }
}
