package com.igot.cb.formConfiguration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.authentication.model.UserDetails;
import com.igot.cb.authentication.util.AccessTokenValidator;
import com.igot.cb.formConfiguration.entity.FormConfigurationEntity;
import com.igot.cb.formConfiguration.external.OrgReadService;
import com.igot.cb.formConfiguration.external.UserDesignationService;
import com.igot.cb.formConfiguration.repository.FormConfigurationRepository;
import com.igot.cb.formConfiguration.rule.FormConfigResolutionContext;
import com.igot.cb.formConfiguration.rule.FormConfigRuleEngine;
import com.igot.cb.formConfiguration.service.Validation.ValidationService;
import com.igot.cb.formConfiguration.service.cache.CacheService;
import com.igot.cb.util.ApiResponse;
import com.igot.cb.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormsConfigurationServiceImplTest {

    @InjectMocks
    private FormsConfigurationServiceImpl service;

    @Mock
    private AccessTokenValidator accessTokenValidator;

    @Mock
    private FormConfigurationRepository repository;

    @Mock
    private ValidationService validationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FormConfigRuleEngine formConfigRuleEngine;

    @Mock
    private OrgReadService orgReadService;

    @Mock
    private UserDesignationService userDesignationService;

    @Mock
    private CacheService cacheService;

    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        userDetails = new UserDetails();
        userDetails.setUserId("user1");
        userDetails.setOrg("org1");
        userDetails.setUserRoles(List.of("PUBLIC"));
    }

    private Map<String, Object> getRequest() {
        Map<String, Object> data = new HashMap<>();
        data.put("field", "value");

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("rootOrg", "org1");
        criteria.put("role", "PUBLIC");

        Map<String, Object> req = new HashMap<>();
        req.put(Constants.NAME, "testName");
        req.put(Constants.TYPE, "page");
        req.put(Constants.SUBTYPE, "player test");
        req.put(Constants.PORTAL, "mobile");
        req.put(Constants.CLIENT_VERSION, 1.0);
        req.put(Constants.DATA, data);
        req.put(Constants.CRITERIA, criteria);

        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put(Constants.Parameters.REQUEST, req);
        return wrapper;
    }

    private FormConfigurationEntity entity() {
        FormConfigurationEntity entity = new FormConfigurationEntity();
        entity.setName("testName");
        entity.setType("page");
        entity.setSubtype("player test");
        entity.setPortal("mobile");
        entity.setData(mock(com.fasterxml.jackson.databind.JsonNode.class));
        entity.setCreatedAt("now");
        return entity;
    }

    @Test
    void readFormConfig_admin_found() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType(eq("ignored-org"), isNull())).thenReturn(null);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.of(entity()));
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(new HashMap<>());

        ApiResponse response = service.readFormConfig(getRequest(), "admin1", "ignored-org", List.of("IGNORED"), true);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        ArgumentCaptor<FormConfigResolutionContext> ctxCaptor = ArgumentCaptor.forClass(FormConfigResolutionContext.class);
        verify(formConfigRuleEngine).resolve(ctxCaptor.capture());
        FormConfigResolutionContext ctx = ctxCaptor.getValue();
        assertEquals("page", ctx.getType());
        assertEquals("player test", ctx.getSubtype());
        assertEquals("mobile", ctx.getPortal());
        assertEquals(1.0, ctx.getClientVersion());
        assertEquals("ignored-org", ctx.getRootOrg());
        assertEquals(List.of("IGNORED"), ctx.getRoles());
    }

    @Test
    void readFormConfig_admin_notFound() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType(any(), any())).thenReturn(null);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.empty());

        ApiResponse response = service.readFormConfig(getRequest(), "admin1", "ignored-org", List.of("IGNORED"), true);

        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
    }

    @Test
    void readFormConfig_volunteer_found() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType(eq("org1"), eq("token"))).thenReturn(null);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.of(entity()));
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(new HashMap<>());

        ApiResponse response = service.readFormConfig(getRequest(), "token", null, null, false);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        ArgumentCaptor<FormConfigResolutionContext> ctxCaptor = ArgumentCaptor.forClass(FormConfigResolutionContext.class);
        verify(formConfigRuleEngine).resolve(ctxCaptor.capture());
        assertEquals("org1", ctxCaptor.getValue().getRootOrg());
        assertEquals(List.of("PUBLIC"), ctxCaptor.getValue().getRoles());
    }

    @Test
    void readFormConfig_volunteer_notFound() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType(any(), any())).thenReturn(null);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.empty());

        ApiResponse response = service.readFormConfig(getRequest(), "token", null, null, false);

        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
    }

    @Test
    void createFormConfigV2_success() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateV2CreateForm(anyMap())).thenReturn(Constants.SUCCESSFUL);

        ApiResponse response = service.createFormConfigV2(getRequest(), "token");

        assertEquals(HttpStatus.OK, response.getResponseCode());
        verify(repository).save(any(FormConfigurationEntity.class));
    }

    @Test
    void createFormConfigV2_validationFailure() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateV2CreateForm(anyMap())).thenReturn("Validation error");

        ApiResponse response = service.createFormConfigV2(getRequest(), "token");

        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
    }

    @Test
    void readFormConfigById_success() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(repository.findById(1L)).thenReturn(Optional.of(entity()));

        ApiResponse response = service.readFormConfigById(1L, "token", false);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertFalse(((Map<String, Object>) response.getResult()).containsKey(Constants.CRITERIA));
    }

    @Test
    void readFormConfigById_admin_includesCriteria() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(repository.findById(1L)).thenReturn(Optional.of(entity()));
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(new HashMap<>());

        ApiResponse response = service.readFormConfigById(1L, "token", true);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        assertTrue(((Map<String, Object>) response.getResult()).containsKey(Constants.CRITERIA));
    }

    @Test
    void readFormConfigById_notFound() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ApiResponse response = service.readFormConfigById(1L, "token", false);

        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
    }

    @Test
    void updateFormConfigV2_success() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenReturn(Constants.SUCCESSFUL);

        FormConfigurationEntity original = entity();
        original.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(original));

        Map<String, Object> req = getRequest();
        ((Map<String, Object>) req.get(Constants.Parameters.REQUEST)).put("id", 1L);

        ApiResponse response = service.updateFormConfigV2(req, "token");

        assertEquals(HttpStatus.OK, response.getResponseCode());
        verify(repository).save(any(FormConfigurationEntity.class));
    }

    @Test
    void updateFormConfigV2_duplicateCriteria_conflict() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenReturn(Constants.SUCCESSFUL);

        FormConfigurationEntity original = entity();
        original.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(original));

        // Real JsonNode (not a Mockito mock) so isDuplicateCriteria's .get()/.isNull()/.isArray() calls behave correctly.
        com.fasterxml.jackson.databind.JsonNode sameCriteria =
                new ObjectMapper().valueToTree(Map.of("role", "PUBLIC", "rootOrg", "org1"));
        when(objectMapper.valueToTree(any())).thenReturn(sameCriteria);

        // Another row already occupying that exact type/subtype/portal/clientVersion + role/rootOrg combo.
        FormConfigurationEntity otherRow = entity();
        otherRow.setId(2L);
        otherRow.setCriteria(sameCriteria);
        when(repository.findByTypeAndSubtypeAndPortalAndClientVersion("page", "player test", "mobile", 1.0))
                .thenReturn(List.of(otherRow));

        Map<String, Object> req = getRequest();
        ((Map<String, Object>) req.get(Constants.Parameters.REQUEST)).put("id", 1L);

        ApiResponse response = service.updateFormConfigV2(req, "token");

        assertEquals(HttpStatus.CONFLICT, response.getResponseCode());
        assertEquals(Constants.ResponseMessages.FIELD_CRITERIA_ALREADY_EXISTS, response.getParams().getErrMsg());
        verify(repository, never()).save(any());
    }

    @Test
    void listFormConfigs_success() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(repository.findAll()).thenReturn(List.of(entity()));

        ApiResponse response = service.listFormConfigs("token");

        assertEquals(HttpStatus.OK, response.getResponseCode());
    }

    @Test
    void readFormConfig_volunteer_invalidToken_returnsUnauthorized() {
        when(accessTokenValidator.fetchUserDetailsFromToken("bad-token")).thenReturn(null);

        ApiResponse response = service.readFormConfig(getRequest(), "bad-token", null, null, false);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getResponseCode());
        assertEquals(Constants.INVALID_AUTH_TOKEN, response.getParams().getErrMsg());
    }

    @Test
    void readFormConfig_validationFailure_returnsBadRequest() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn("Validation error");

        ApiResponse response = service.readFormConfig(getRequest(), "admin1", "org1", List.of("ADMIN"), true);

        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
    }

    @Test
    void readFormConfig_exception_returnsInternalServerError() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType(any(), any())).thenThrow(new RuntimeException("downstream error"));

        ApiResponse response = service.readFormConfig(getRequest(), "admin1", "org1", List.of("ADMIN"), true);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
        assertEquals(Constants.FAILED, response.getParams().getStatus());
    }

    @Test
    void readFormConfig_tokenOrgMinistry_doesNotCallOrgReadTwice() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType("org1", "token")).thenReturn("ministry");
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.of(entity()));
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(new HashMap<>());

        ApiResponse response = service.readFormConfig(getRequest(), "token", null, null, false);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        ArgumentCaptor<FormConfigResolutionContext> ctxCaptor = ArgumentCaptor.forClass(FormConfigResolutionContext.class);
        verify(formConfigRuleEngine).resolve(ctxCaptor.capture());
        assertEquals("ministry", ctxCaptor.getValue().getDesignationMinistryOrStateType());
        assertEquals("org1", ctxCaptor.getValue().getDesignationRootOrg());
        verify(orgReadService, times(1)).getMinistryOrStateType(any(), any());
    }

    @Test
    void readFormConfig_tokenOrgState_resolvesStateDesignation() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType("org1", "token")).thenReturn("state");
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.of(entity()));
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(new HashMap<>());

        ApiResponse response = service.readFormConfig(getRequest(), "token", null, null, false);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        ArgumentCaptor<FormConfigResolutionContext> ctxCaptor = ArgumentCaptor.forClass(FormConfigResolutionContext.class);
        verify(formConfigRuleEngine).resolve(ctxCaptor.capture());
        assertEquals("state", ctxCaptor.getValue().getDesignationMinistryOrStateType());
    }

    @Test
    void readFormConfig_inputRootOrgResolvesMinistry() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType("org1", "token")).thenReturn(null);
        when(orgReadService.getMinistryOrStateType("inputOrg", "token")).thenReturn("ministry");
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.of(entity()));
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(new HashMap<>());

        Map<String, Object> req = getRequest();
        ((Map<String, Object>) req.get(Constants.Parameters.REQUEST)).put(Constants.ROOTORG, "inputOrg");

        ApiResponse response = service.readFormConfig(req, "token", null, null, false);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        ArgumentCaptor<FormConfigResolutionContext> ctxCaptor = ArgumentCaptor.forClass(FormConfigResolutionContext.class);
        verify(formConfigRuleEngine).resolve(ctxCaptor.capture());
        assertEquals("ministry", ctxCaptor.getValue().getDesignationMinistryOrStateType());
        assertEquals("inputOrg", ctxCaptor.getValue().getDesignationRootOrg());
    }

    @Test
    void readFormConfig_wildcardInputRootOrg_isIgnored() {
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.READ))).thenReturn(Constants.SUCCESSFUL);
        when(orgReadService.getMinistryOrStateType(eq("org1"), any())).thenReturn(null);
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(formConfigRuleEngine.resolve(any(FormConfigResolutionContext.class))).thenReturn(Optional.of(entity()));
        when(objectMapper.convertValue(any(), eq(Map.class))).thenReturn(new HashMap<>());

        Map<String, Object> req = getRequest();
        ((Map<String, Object>) req.get(Constants.Parameters.REQUEST)).put(Constants.ROOTORG, "*");

        ApiResponse response = service.readFormConfig(req, "token", null, null, false);

        assertEquals(HttpStatus.OK, response.getResponseCode());
        ArgumentCaptor<FormConfigResolutionContext> ctxCaptor = ArgumentCaptor.forClass(FormConfigResolutionContext.class);
        verify(formConfigRuleEngine).resolve(ctxCaptor.capture());
        assertNull(ctxCaptor.getValue().getDesignationMinistryOrStateType());
        verify(orgReadService, times(1)).getMinistryOrStateType(any(), any());
    }

    @Test
    void createFormConfigV2_invalidToken_returnsUnauthorized() {
        when(accessTokenValidator.fetchUserDetailsFromToken("bad-token")).thenReturn(null);

        ApiResponse response = service.createFormConfigV2(getRequest(), "bad-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getResponseCode());
    }

    @Test
    void createFormConfigV2_nameAlreadyExists_returnsConflict() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateV2CreateForm(anyMap())).thenReturn(Constants.SUCCESSFUL);
        when(repository.existsByName("testName")).thenReturn(true);

        ApiResponse response = service.createFormConfigV2(getRequest(), "token");

        assertEquals(HttpStatus.CONFLICT, response.getResponseCode());
        assertEquals(Constants.ResponseMessages.FIELD_NAME_ALREADY_EXISTS, response.getParams().getErrMsg());
    }

    @Test
    void createFormConfigV2_duplicateCriteria_returnsConflict() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateV2CreateForm(anyMap())).thenReturn(Constants.SUCCESSFUL);
        when(repository.existsByName("testName")).thenReturn(false);
        when(repository.findByTypeAndSubtypeAndPortalAndClientVersion("page", "player test", "mobile", 1.0))
                .thenReturn(List.of(entity()));

        Map<String, Object> req = getRequest();
        ((Map<String, Object>) req.get(Constants.Parameters.REQUEST)).remove(Constants.CRITERIA);

        ApiResponse response = service.createFormConfigV2(req, "token");

        assertEquals(HttpStatus.CONFLICT, response.getResponseCode());
        verify(repository, never()).save(any());
    }

    @Test
    void createFormConfigV2_exception_returnsInternalServerError() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateV2CreateForm(anyMap())).thenThrow(new RuntimeException("boom"));

        ApiResponse response = service.createFormConfigV2(getRequest(), "token");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
    }

    @Test
    void readFormConfigById_invalidToken_returnsUnauthorized() {
        when(accessTokenValidator.fetchUserDetailsFromToken("bad-token")).thenReturn(null);

        ApiResponse response = service.readFormConfigById(1L, "bad-token", false);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getResponseCode());
    }

    @Test
    void updateFormConfigV2_invalidToken_returnsUnauthorized() {
        when(accessTokenValidator.fetchUserDetailsFromToken("bad-token")).thenReturn(null);

        ApiResponse response = service.updateFormConfigV2(getRequest(), "bad-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getResponseCode());
    }

    @Test
    void updateFormConfigV2_validationFailure_returnsBadRequest() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenReturn("Validation error");

        ApiResponse response = service.updateFormConfigV2(getRequest(), "token");

        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
    }

    @Test
    void updateFormConfigV2_formIdMissing_returnsBadRequest() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenReturn(Constants.SUCCESSFUL);

        ApiResponse response = service.updateFormConfigV2(getRequest(), "token");

        assertEquals(HttpStatus.BAD_REQUEST, response.getResponseCode());
        assertEquals("Field formId/id is missing", response.getParams().getErrMsg());
    }

    @Test
    void updateFormConfigV2_formNotFound_returnsNotFound() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenReturn(Constants.SUCCESSFUL);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Map<String, Object> req = getRequest();
        ((Map<String, Object>) req.get(Constants.Parameters.REQUEST)).put("id", 99L);

        ApiResponse response = service.updateFormConfigV2(req, "token");

        assertEquals(HttpStatus.NOT_FOUND, response.getResponseCode());
    }

    @Test
    void updateFormConfigV2_nameAlreadyExists_returnsConflict() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenReturn(Constants.SUCCESSFUL);

        FormConfigurationEntity original = entity();
        original.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.existsByNameAndIdNot("testName", 1L)).thenReturn(true);

        Map<String, Object> req = getRequest();
        ((Map<String, Object>) req.get(Constants.Parameters.REQUEST)).put("id", 1L);

        ApiResponse response = service.updateFormConfigV2(req, "token");

        assertEquals(HttpStatus.CONFLICT, response.getResponseCode());
        assertEquals(Constants.ResponseMessages.FIELD_NAME_ALREADY_EXISTS, response.getParams().getErrMsg());
    }

    @Test
    void updateFormConfigV2_noScopeOrCriteriaChange_skipsDuplicateCheck() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenReturn(Constants.SUCCESSFUL);

        FormConfigurationEntity original = entity();
        original.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(original));

        Map<String, Object> req = new HashMap<>();
        Map<String, Object> inner = new HashMap<>();
        inner.put("id", 1L);
        req.put(Constants.Parameters.REQUEST, inner);

        ApiResponse response = service.updateFormConfigV2(req, "token");

        assertEquals(HttpStatus.OK, response.getResponseCode());
        verify(repository, never()).findByTypeAndSubtypeAndPortalAndClientVersion(any(), any(), any(), any());
        verify(repository).save(original);
    }

    @Test
    void updateFormConfigV2_exception_returnsInternalServerError() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(validationService.validateForm(anyMap(), eq(Constants.Parameters.UPDATE))).thenThrow(new RuntimeException("boom"));

        ApiResponse response = service.updateFormConfigV2(getRequest(), "token");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
    }

    @Test
    void listFormConfigs_invalidToken_returnsUnauthorized() {
        when(accessTokenValidator.fetchUserDetailsFromToken("bad-token")).thenReturn(null);

        ApiResponse response = service.listFormConfigs("bad-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getResponseCode());
    }

    @Test
    void listFormConfigs_exception_returnsInternalServerError() {
        when(accessTokenValidator.fetchUserDetailsFromToken("token")).thenReturn(userDetails);
        when(repository.findAll()).thenThrow(new RuntimeException("boom"));

        ApiResponse response = service.listFormConfigs("token");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseCode());
    }
}
