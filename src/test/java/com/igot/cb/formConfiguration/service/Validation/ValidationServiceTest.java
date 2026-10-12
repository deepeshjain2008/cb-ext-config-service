package com.igot.cb.formConfiguration.service.Validation;

import com.igot.cb.util.Constants;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationServiceTest {

    private final ValidationService validationService = new ValidationService();

    private Map<String, Object> wrap(Map<String, Object> requestObject) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put(Constants.Parameters.REQUEST, requestObject);
        return wrapper;
    }

    private Map<String, Object> validReadRequest() {
        Map<String, Object> req = new HashMap<>();
        req.put(Constants.TYPE, "page");
        req.put(Constants.SUBTYPE, "player");
        req.put(Constants.PORTAL, "mobile");
        req.put(Constants.CLIENT_VERSION, 1.0);
        return req;
    }

    private Map<String, Object> validRoleRootOrgCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.ROLE, "PUBLIC");
        criteria.put(Constants.ROOTORG, "org1");
        return criteria;
    }

    private Map<String, Object> validCreateRequest() {
        Map<String, Object> req = validReadRequest();
        req.put(Constants.NAME, "testName");
        req.put(Constants.CRITERIA, validRoleRootOrgCriteria());
        return req;
    }

    @Test
    void validateForm_read_allValid_returnsSuccessful() {
        String result = validationService.validateForm(wrap(validReadRequest()), Constants.Parameters.READ);
        assertEquals(Constants.SUCCESSFUL, result);
    }

    @Test
    void validateForm_read_typeMissing_returnsFieldTypeMissing() {
        Map<String, Object> req = validReadRequest();
        req.remove(Constants.TYPE);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.READ);
        assertEquals(Constants.ResponseMessages.FIELD_TYPE_MISSING, result);
    }

    @Test
    void validateForm_read_subtypeMissing_returnsFieldSubtypeMissing() {
        Map<String, Object> req = validReadRequest();
        req.remove(Constants.SUBTYPE);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.READ);
        assertEquals(Constants.ResponseMessages.FIELD_SUBTYPE_MISSING, result);
    }

    @Test
    void validateForm_read_portalMissing_returnsFieldPortalMissing() {
        Map<String, Object> req = validReadRequest();
        req.remove(Constants.PORTAL);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.READ);
        assertEquals(Constants.ResponseMessages.FIELD_PORTAL_MISSING, result);
    }

    @Test
    void validateForm_read_clientVersionMissing_returnsFieldClientVersionMissing() {
        Map<String, Object> req = validReadRequest();
        req.remove(Constants.CLIENT_VERSION);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.READ);
        assertEquals(Constants.ResponseMessages.FIELD_CLIENTVERSION_MISSING, result);
    }

    @Test
    void validateForm_read_dataPresent_returnsBadRequest() {
        Map<String, Object> req = validReadRequest();
        req.put(Constants.DATA, Map.of("field", "value"));
        String result = validationService.validateForm(wrap(req), Constants.Parameters.READ);
        assertEquals(Constants.ResponseMessages.BAD_REQUEST, result);
    }

    @Test
    void validateForm_create_allValid_returnsSuccessful() {
        String result = validationService.validateForm(wrap(validCreateRequest()), Constants.Parameters.CREATE);
        assertEquals(Constants.SUCCESSFUL, result);
    }

    @Test
    void validateForm_create_nameMissing_returnsFieldNameMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.NAME);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.CREATE);
        assertEquals(Constants.ResponseMessages.FIELD_NAME_MISSING, result);
    }

    @Test
    void validateForm_create_nameTooLong_returnsFieldNameInvalidLength() {
        Map<String, Object> req = validCreateRequest();
        req.put(Constants.NAME, "a".repeat(251));
        String result = validationService.validateForm(wrap(req), Constants.Parameters.CREATE);
        assertEquals(Constants.ResponseMessages.FIELD_NAME_INVALID_LENGTH, result);
    }

    @Test
    void validateForm_create_criteriaMissing_returnsFieldCriteriaMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.CRITERIA);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.CREATE);
        assertEquals(Constants.ResponseMessages.FIELD_CRIETERIA_MISSING, result);
    }

    @Test
    void validateForm_create_designationCriteria_rootOrgMissing_returnsFieldMinistryOrStateTypeMissing() {
        Map<String, Object> req = validCreateRequest();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.DESIGNATION, List.of("STATE_LEAD"));
        req.put(Constants.CRITERIA, criteria);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.CREATE);
        assertEquals(Constants.ResponseMessages.FIELD_MINISTRY_OR_STATE_TYPE_MISSING, result);
    }

    @Test
    void validateForm_create_designationCriteria_rootOrgPresent_returnsSuccessful() {
        Map<String, Object> req = validCreateRequest();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.DESIGNATION, List.of("STATE_LEAD"));
        criteria.put(Constants.ROOTORG, "ministry");
        req.put(Constants.CRITERIA, criteria);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.CREATE);
        assertEquals(Constants.SUCCESSFUL, result);
    }

    @Test
    void validateForm_create_roleRootOrgCriteria_roleMissing_returnsFieldRoleMissing() {
        Map<String, Object> req = validCreateRequest();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.ROOTORG, "org1");
        req.put(Constants.CRITERIA, criteria);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.CREATE);
        assertEquals(Constants.ResponseMessages.FIELD_ROLE_MISSING, result);
    }

    @Test
    void validateForm_create_roleRootOrgCriteria_rootOrgMissing_returnsFieldRootOrgMissing() {
        Map<String, Object> req = validCreateRequest();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.ROLE, "PUBLIC");
        req.put(Constants.CRITERIA, criteria);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.CREATE);
        assertEquals(Constants.ResponseMessages.FIELD_ROOTORG_MISSING, result);
    }

    @Test
    void validateForm_update_nameMissing_returnsFieldNameMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.NAME);
        String result = validationService.validateForm(wrap(req), Constants.Parameters.UPDATE);
        assertEquals(Constants.ResponseMessages.FIELD_NAME_MISSING, result);
    }

    @Test
    void validateV2CreateForm_allValid_returnsSuccessful() {
        String result = validationService.validateV2CreateForm(wrap(validCreateRequest()));
        assertEquals(Constants.SUCCESSFUL, result);
    }

    @Test
    void validateV2CreateForm_emptyRequest_returnsCheckRequestParams() {
        String result = validationService.validateV2CreateForm(new HashMap<>());
        assertEquals(Constants.CHECK_REQUEST_PARAMS, result);
    }

    @Test
    void validateV2CreateForm_missingRequestKey_returnsCheckRequestParams() {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("notRequest", Map.of("a", "b"));
        String result = validationService.validateV2CreateForm(wrapper);
        assertEquals(Constants.CHECK_REQUEST_PARAMS, result);
    }

    @Test
    void validateV2CreateForm_unexpectedKey_returnsBadRequest() {
        Map<String, Object> req = new HashMap<>();
        req.put("unexpectedField", "value");
        String result = validationService.validateV2CreateForm(wrap(req));
        assertEquals(Constants.ResponseMessages.BAD_REQUEST, result);
    }

    @Test
    void validateV2CreateForm_nameMissing_returnsFieldNameMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.NAME);
        String result = validationService.validateV2CreateForm(wrap(req));
        assertEquals(Constants.ResponseMessages.FIELD_NAME_MISSING, result);
    }

    @Test
    void validateV2CreateForm_nameTooLong_returnsFieldNameInvalidLength() {
        Map<String, Object> req = validCreateRequest();
        req.put(Constants.NAME, "a".repeat(251));
        String result = validationService.validateV2CreateForm(wrap(req));
        assertEquals(Constants.ResponseMessages.FIELD_NAME_INVALID_LENGTH, result);
    }

    @Test
    void validateV2CreateForm_typeMissing_returnsFieldTypeMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.TYPE);
        String result = validationService.validateV2CreateForm(wrap(req));
        assertEquals(Constants.ResponseMessages.FIELD_TYPE_MISSING, result);
    }

    @Test
    void validateV2CreateForm_subtypeMissing_returnsFieldSubtypeMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.SUBTYPE);
        String result = validationService.validateV2CreateForm(wrap(req));
        assertEquals(Constants.ResponseMessages.FIELD_SUBTYPE_MISSING, result);
    }

    @Test
    void validateV2CreateForm_portalMissing_returnsFieldPortalMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.PORTAL);
        String result = validationService.validateV2CreateForm(wrap(req));
        assertEquals(Constants.ResponseMessages.FIELD_PORTAL_MISSING, result);
    }

    @Test
    void validateV2CreateForm_clientVersionMissing_returnsFieldClientVersionMissing() {
        Map<String, Object> req = validCreateRequest();
        req.remove(Constants.CLIENT_VERSION);
        String result = validationService.validateV2CreateForm(wrap(req));
        assertEquals(Constants.ResponseMessages.FIELD_CLIENTVERSION_MISSING, result);
    }
}
