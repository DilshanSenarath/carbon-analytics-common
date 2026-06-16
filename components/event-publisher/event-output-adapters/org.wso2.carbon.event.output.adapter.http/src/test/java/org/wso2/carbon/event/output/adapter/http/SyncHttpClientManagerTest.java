/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.output.adapter.http;

import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SyncHttpClientManager}.
 */
public class SyncHttpClientManagerTest {

    private static final String TEST_URL = "http://localhost:8080/api/test";
    private static final int DEFAULT_RETRY_COUNT = 3;

    private SyncHttpClientManager spyManager;
    private APIResponse successResponse;

    @BeforeClass
    public void globalSetup() {

        System.setProperty("carbon.home",
                Paths.get("src", "test", "resources", "carbon-context").toString());
    }

    @BeforeMethod
    public void setUp() throws Exception {

        spyManager = spy(new SyncHttpClientManager(mockApiClientConfig(), DEFAULT_RETRY_COUNT));
        successResponse = new APIResponse(200, "OK");
        doReturn(successResponse).when(spyManager).callAPI(any(), any());
    }

    private static APIClientConfig mockApiClientConfig() {

        APIClientConfig config = mock(APIClientConfig.class);
        when(config.getHttpReadTimeoutInMillis()).thenReturn(30000);
        when(config.getHttpConnectionRequestTimeoutInMillis()).thenReturn(10000);
        when(config.getHttpConnectionTimeoutInMillis()).thenReturn(10000);
        when(config.getPoolSizeToBeSet()).thenReturn(100);
        when(config.getMaxPerRoute()).thenReturn(50);
        when(config.getResponseLimitInBytes()).thenReturn(1048576L);
        when(config.getProxyHost()).thenReturn(null);
        return config;
    }

    // -----------------------------------------------------------------------
    // send() — HTTP method selection
    // -----------------------------------------------------------------------

    @Test
    public void testSend_postMethod_callsApiWithPostMethod() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.POST, new HashMap<>(), "payload");

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertEquals(APIRequestContext.HttpMethod.POST, contextCaptor.getValue().getHttpMethod());
    }

    @Test
    public void testSend_putMethod_callsApiWithPutMethod() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.PUT, new HashMap<>(), "payload");

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertEquals(APIRequestContext.HttpMethod.PUT, contextCaptor.getValue().getHttpMethod());
    }

    @Test
    public void testSend_getMethod_callsApiWithGetMethod() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertEquals(APIRequestContext.HttpMethod.GET, contextCaptor.getValue().getHttpMethod());
    }

    // -----------------------------------------------------------------------
    // send() — payload handling
    // -----------------------------------------------------------------------

    @Test
    public void testSend_postMethod_setsPayloadOnContext() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.POST, new HashMap<>(), "test-payload");

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertNotNull(contextCaptor.getValue().getPayload(),
                "POST requests must set a payload on the context");
    }

    @Test
    public void testSend_putMethod_setsPayloadOnContext() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.PUT, new HashMap<>(), "test-payload");

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertNotNull(contextCaptor.getValue().getPayload(),
                "PUT requests must set a payload on the context");
    }

    @Test
    public void testSend_getMethod_doesNotSetPayload() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), "ignored");

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertNull(contextCaptor.getValue().getPayload(),
                "GET requests must not set a payload on the context");
    }

    @Test
    public void testSend_nullPayloadForPost_setsEmptyStringEntity() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.POST, new HashMap<>(), null);

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertNotNull(contextCaptor.getValue().getPayload(),
                "POST with null payload must still set a non-null entity (empty string fallback)");
    }

    // -----------------------------------------------------------------------
    // send() — URL forwarding
    // -----------------------------------------------------------------------

    @Test
    public void testSend_urlForwardedToContext() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertEquals(TEST_URL, contextCaptor.getValue().getEndpointUrl());
    }

    // -----------------------------------------------------------------------
    // send() — headers forwarding
    // -----------------------------------------------------------------------

    @Test
    public void testSend_headersForwardedToContext() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "custom-value");
        headers.put("Authorization", "Bearer some-token");
        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.POST, headers, "payload");

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Map<String, String> capturedHeaders = contextCaptor.getValue().getHeaders();
        Assert.assertEquals("custom-value", capturedHeaders.get("X-Custom-Header"));
        Assert.assertEquals("Bearer some-token", capturedHeaders.get("Authorization"));
    }

    @Test
    public void testSend_emptyHeaders_noHeadersOnContext() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertTrue(contextCaptor.getValue().getHeaders().isEmpty());
    }

    // -----------------------------------------------------------------------
    // send() — authentication is always NONE
    // -----------------------------------------------------------------------

    @Test
    public void testSend_authTypeIsNone_forPostMethod() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.POST, new HashMap<>(), "payload");

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertEquals(APIAuthentication.AuthType.NONE,
                contextCaptor.getValue().getApiAuthentication().getType(),
                "Auth type must always be NONE — auth headers are pre-resolved in the headers map");
    }

    @Test
    public void testSend_authTypeIsNone_forGetMethod() throws Exception {

        ArgumentCaptor<APIRequestContext> contextCaptor = ArgumentCaptor.forClass(APIRequestContext.class);

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);

        verify(spyManager).callAPI(contextCaptor.capture(), any());
        Assert.assertEquals(APIAuthentication.AuthType.NONE,
                contextCaptor.getValue().getApiAuthentication().getType());
    }

    // -----------------------------------------------------------------------
    // send() — retry count is propagated to invocation config
    // -----------------------------------------------------------------------

    @Test
    public void testSend_retryCountSetOnInvocationConfig() throws Exception {

        int expectedRetryCount = 5;
        SyncHttpClientManager manager = spy(new SyncHttpClientManager(mockApiClientConfig(), expectedRetryCount));
        doReturn(successResponse).when(manager).callAPI(any(), any());
        ArgumentCaptor<APIInvocationConfig> invocationConfigCaptor =
                ArgumentCaptor.forClass(APIInvocationConfig.class);

        manager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);

        verify(manager).callAPI(any(), invocationConfigCaptor.capture());
        Assert.assertEquals(expectedRetryCount, invocationConfigCaptor.getValue().getAllowedRetryCount());
    }

    @Test
    public void testSend_zeroRetryCount_setOnInvocationConfig() throws Exception {

        SyncHttpClientManager manager = spy(new SyncHttpClientManager(mockApiClientConfig(), 0));
        doReturn(successResponse).when(manager).callAPI(any(), any());
        ArgumentCaptor<APIInvocationConfig> invocationConfigCaptor =
                ArgumentCaptor.forClass(APIInvocationConfig.class);

        manager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);

        verify(manager).callAPI(any(), invocationConfigCaptor.capture());
        Assert.assertEquals(0, invocationConfigCaptor.getValue().getAllowedRetryCount());
    }

    // -----------------------------------------------------------------------
    // send() — response passthrough
    // -----------------------------------------------------------------------

    @Test
    public void testSend_returnsResponseFromCallApi() throws Exception {

        APIResponse expectedResponse = new APIResponse(201, "Created");
        doReturn(expectedResponse).when(spyManager).callAPI(any(), any());

        APIResponse result = spyManager.send(
                TEST_URL, APIRequestContext.HttpMethod.POST, new HashMap<>(), "payload");

        Assert.assertSame(expectedResponse, result);
    }

    @Test
    public void testSend_200Response_isReturnedUnmodified() throws Exception {

        APIResponse response = new APIResponse(200, "success body");
        doReturn(response).when(spyManager).callAPI(any(), any());

        APIResponse result = spyManager.send(
                TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);

        Assert.assertEquals(200, result.getStatusCode());
        Assert.assertEquals("success body", result.getResponseBody());
    }

    // -----------------------------------------------------------------------
    // send() — exception propagation
    // -----------------------------------------------------------------------

    @Test(expectedExceptions = APIClientException.class)
    public void testSend_apiClientException_propagates() throws Exception {

        doThrow(new APIClientException(
                ErrorMessageConstant.ErrorMessage.ERROR_CODE_WHILE_INVOKING_API, "connection refused"))
                .when(spyManager).callAPI(any(), any());

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.POST, new HashMap<>(), "payload");
    }

    @Test(expectedExceptions = APIClientException.class)
    public void testSend_apiClientException_propagates_forGetMethod() throws Exception {

        doThrow(new APIClientException(
                ErrorMessageConstant.ErrorMessage.ERROR_CODE_WHILE_INVOKING_API, "timeout"))
                .when(spyManager).callAPI(any(), any());

        spyManager.send(TEST_URL, APIRequestContext.HttpMethod.GET, new HashMap<>(), null);
    }
}
