package com.igot.cb.formConfiguration.service.Validation;

import com.igot.cb.formConfiguration.entity.FormConfigurationEntity;
import com.igot.cb.formConfiguration.repository.FormConfigurationRepository;
import com.igot.cb.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ValidationServiceTest {

    @InjectMocks
    private ValidationService validationService;

    @Mock
    private FormConfigurationRepository formConfigurationRepository;

    private Map<String, Object> requestObjectFor(String type, String subType, String portal,
                                                   String name, Map<String, Object> criteria,
                                                   Double clientVersion) {
        Map<String, Object> requestObject = new HashMap<>();
        if (type != null) requestObject.put(Constants.TYPE, type);
        if (subType != null) requestObject.put(Constants.SUBTYPE, subType);
        if (portal != null) requestObject.put(Constants.PORTAL, portal);
        if (name != null) requestObject.put(Constants.NAME, name);
        if (criteria != null) requestObject.put(Constants.CRITERIA, criteria);
        if (clientVersion != null) requestObject.put(Constants.CLIENT_VERSION, clientVersion);
        return requestObject;
    }

    private Map<String, Object> wrap(Map<String, Object> requestObject) {
        Map<String, Object> formRequest = new HashMap<>();
        formRequest.put(Constants.Parameters.REQUEST, requestObject);
        return formRequest;
    }

    private Map<String, Object> validCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.ROLE, "role1");
        criteria.put(Constants.ROOTORG, "org1");
        return criteria;
    }

    // ---- validateForm: READ operation ----

    @Test
    void validateForm_read_shouldReturnSuccessfulWhenAllFieldsValid() {
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", null, null, 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.READ);

        assertEquals(Constants.SUCCESSFUL, result);
    }

    @Test
    void validateForm_read_shouldReturnTypeMissingWhenTypeAbsent() {
        Map<String, Object> requestObject = requestObjectFor(null, "s", "p", null, null, 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.READ);

        assertEquals(Constants.ResponseMessages.FIELD_TYPE_MISSING, result);
    }

    @Test
    void validateForm_read_shouldReturnSubtypeMissingWhenSubtypeBlank() {
        Map<String, Object> requestObject = requestObjectFor("t", "  ", "p", null, null, 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.READ);

        assertEquals(Constants.ResponseMessages.FIELD_SUBTYPE_MISSING, result);
    }

    @Test
    void validateForm_read_shouldReturnPortalMissingWhenPortalAbsent() {
        Map<String, Object> requestObject = requestObjectFor("t", "s", null, null, null, 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.READ);

        assertEquals(Constants.ResponseMessages.FIELD_PORTAL_MISSING, result);
    }

    @Test
    void validateForm_read_shouldReturnClientVersionMissingWhenAbsent() {
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", null, null, null);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.READ);

        assertEquals(Constants.ResponseMessages.FIELD_CLIENTVERSION_MISSING, result);
    }

    @Test
    void validateForm_read_shouldReturnBadRequestWhenCriteriaSuppliedOnRead() {
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", null, validCriteria(), 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.READ);

        assertEquals(Constants.ResponseMessages.BAD_REQUEST, result);
    }

    // ---- validateForm: CREATE operation ----

    @Test
    void validateForm_create_shouldReturnSuccessfulWhenAllFieldsValid() {
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", "n", validCriteria(), 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.CREATE);

        assertEquals(Constants.SUCCESSFUL, result);
    }

    @Test
    void validateForm_create_shouldReturnNameMissingWhenNameAbsent() {
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", null, validCriteria(), 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.CREATE);

        assertEquals(Constants.ResponseMessages.FIELD_NAME_MISSING, result);
    }

    @Test
    void validateForm_create_shouldReturnNameInvalidLengthWhenTooLong() {
        String longName = "n".repeat(251);
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", longName, validCriteria(), 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.CREATE);

        assertEquals(Constants.ResponseMessages.FIELD_NAME_INVALID_LENGTH, result);
    }

    @Test
    void validateForm_create_shouldReturnCriteriaMissingWhenCriteriaAbsent() {
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", "n", null, 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.CREATE);

        assertEquals(Constants.ResponseMessages.FIELD_CRIETERIA_MISSING, result);
    }

    @Test
    void validateForm_create_shouldReturnRootOrgMissingWhenRootOrgAbsentFromCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.ROLE, "role1");
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", "n", criteria, 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.CREATE);

        assertEquals(Constants.ResponseMessages.FIELD_ROOTORG_MISSING, result);
    }

    @Test
    void validateForm_update_shouldReturnRoleMissingWhenRoleAbsentFromCriteria() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.ROOTORG, "org1");
        Map<String, Object> requestObject = requestObjectFor("t", "s", "p", "n", criteria, 1.0);

        String result = validationService.validateForm(wrap(requestObject), Constants.Parameters.UPDATE);

        assertEquals(Constants.ResponseMessages.FIELD_ROLE_MISSING, result);
    }

    @Test
    void validateForm_shouldThrowNpeWhenRequestKeyMissingEntirely() {
        Map<String, Object> formRequest = new HashMap<>();
        formRequest.put("someOtherKey", "value");

        assertThrows(NullPointerException.class,
                () -> validationService.validateForm(formRequest, Constants.Parameters.READ));
    }

    // ---- validateFormData ----

    @Test
    void validateFormData_shouldReturnEntityWhenRepositoryFindsMatch() {
        Map<String, Object> criteria = validCriteria();
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.TYPE, "t");
        request.put(Constants.SUBTYPE, "s");
        request.put(Constants.PORTAL, "p");
        request.put(Constants.CRITERIA, criteria);
        request.put(Constants.CLIENT_VERSION, "1.0");
        FormConfigurationEntity entity = new FormConfigurationEntity();
        entity.setName("existing");
        when(formConfigurationRepository.getFormConfigDataByCriteria(
                anyString(), anyString(), anyString(), anyString(), anyList(), anyDouble()))
                .thenReturn(Optional.of(entity));

        FormConfigurationEntity result = validationService.validateFormData(request);

        assertEquals("existing", result.getName());
        verify(formConfigurationRepository).getFormConfigDataByCriteria(
                "t", "s", "p", "org1", Collections.singletonList("role1"), 1.0);
    }

    @Test
    void validateFormData_shouldReturnNullWhenRepositoryFindsNoMatch() {
        Map<String, Object> criteria = validCriteria();
        Map<String, Object> request = new HashMap<>();
        request.put(Constants.TYPE, "t");
        request.put(Constants.SUBTYPE, "s");
        request.put(Constants.PORTAL, "p");
        request.put(Constants.CRITERIA, criteria);
        request.put(Constants.CLIENT_VERSION, "1.0");
        when(formConfigurationRepository.getFormConfigDataByCriteria(
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        FormConfigurationEntity result = validationService.validateFormData(request);

        assertNull(result);
    }

    // ---- validateV2CreateForm ----

    private Map<String, Object> v2ValidRequestObject() {
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put(Constants.NAME, "n");
        requestObject.put(Constants.TYPE, "t");
        requestObject.put(Constants.SUBTYPE, "s");
        requestObject.put(Constants.PORTAL, "p");
        requestObject.put(Constants.CLIENT_VERSION, 1.0);
        return requestObject;
    }

    @Test
    void validateV2CreateForm_shouldReturnSuccessfulWhenAllFieldsValid() {
        String result = validationService.validateV2CreateForm(wrap(v2ValidRequestObject()));

        assertEquals(Constants.SUCCESSFUL, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnCheckRequestParamsWhenFormRequestEmpty() {
        String result = validationService.validateV2CreateForm(new HashMap<>());

        assertEquals(Constants.CHECK_REQUEST_PARAMS, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnCheckRequestParamsWhenRequestKeyMissing() {
        Map<String, Object> formRequest = new HashMap<>();
        formRequest.put("someOtherKey", "value");

        String result = validationService.validateV2CreateForm(formRequest);

        assertEquals(Constants.CHECK_REQUEST_PARAMS, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnBadRequestWhenUnknownFieldPresent() {
        Map<String, Object> requestObject = v2ValidRequestObject();
        requestObject.put("unexpectedField", "x");

        String result = validationService.validateV2CreateForm(wrap(requestObject));

        assertEquals(Constants.ResponseMessages.BAD_REQUEST, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnNameMissingWhenNameAbsent() {
        Map<String, Object> requestObject = v2ValidRequestObject();
        requestObject.remove(Constants.NAME);

        String result = validationService.validateV2CreateForm(wrap(requestObject));

        assertEquals(Constants.ResponseMessages.FIELD_NAME_MISSING, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnNameInvalidLengthWhenTooLong() {
        Map<String, Object> requestObject = v2ValidRequestObject();
        requestObject.put(Constants.NAME, "n".repeat(251));

        String result = validationService.validateV2CreateForm(wrap(requestObject));

        assertEquals(Constants.ResponseMessages.FIELD_NAME_INVALID_LENGTH, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnTypeMissingWhenTypeAbsent() {
        Map<String, Object> requestObject = v2ValidRequestObject();
        requestObject.remove(Constants.TYPE);

        String result = validationService.validateV2CreateForm(wrap(requestObject));

        assertEquals(Constants.ResponseMessages.FIELD_TYPE_MISSING, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnSubtypeMissingWhenSubtypeAbsent() {
        Map<String, Object> requestObject = v2ValidRequestObject();
        requestObject.remove(Constants.SUBTYPE);

        String result = validationService.validateV2CreateForm(wrap(requestObject));

        assertEquals(Constants.ResponseMessages.FIELD_SUBTYPE_MISSING, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnPortalMissingWhenPortalAbsent() {
        Map<String, Object> requestObject = v2ValidRequestObject();
        requestObject.remove(Constants.PORTAL);

        String result = validationService.validateV2CreateForm(wrap(requestObject));

        assertEquals(Constants.ResponseMessages.FIELD_PORTAL_MISSING, result);
    }

    @Test
    void validateV2CreateForm_shouldReturnClientVersionMissingWhenAbsent() {
        Map<String, Object> requestObject = v2ValidRequestObject();
        requestObject.remove(Constants.CLIENT_VERSION);

        String result = validationService.validateV2CreateForm(wrap(requestObject));

        assertEquals(Constants.ResponseMessages.FIELD_CLIENTVERSION_MISSING, result);
    }
}
