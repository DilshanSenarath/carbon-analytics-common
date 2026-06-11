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

import org.apache.http.entity.StringEntity;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.identity.external.api.client.api.service.AbstractAPIClientManager;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP client manager for synchronous event publishing, backed by a dedicated connection pool.
 */
class SyncHttpClientManager extends AbstractAPIClientManager {

    private final int retryCount;

    SyncHttpClientManager(APIClientConfig config, int retryCount) {

        super(config);
        this.retryCount = retryCount;
    }

    /**
     * Sends an HTTP request synchronously and returns the response.
     *
     * @param url        Target endpoint URL.
     * @param httpMethod HTTP method to use (GET, POST, or PUT).
     * @param headers    Request headers, including any auth header.
     * @param payload    Request body; ignored for GET requests.
     * @return The API response containing status code and body.
     * @throws APIClientException If the request cannot be executed.
     */
    APIResponse send(String url, APIRequestContext.HttpMethod httpMethod,
                     Map<String, String> headers, String payload) throws APIClientException {

        // The headers parameter already contains the resolved authentication headers. 
        // Therefore, the built-in authentication builder is skipped.
        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        APIRequestContext.Builder contextBuilder = new APIRequestContext.Builder()
                .httpMethod(httpMethod)
                .apiAuthentication(authentication)
                .endpointUrl(url)
                .headers(headers);

        if (httpMethod == APIRequestContext.HttpMethod.POST || httpMethod == APIRequestContext.HttpMethod.PUT) {
            contextBuilder.payload(new StringEntity(payload != null ? payload : "", StandardCharsets.UTF_8));
        }

        APIInvocationConfig invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(retryCount);

        return callAPI(contextBuilder.build(), invocationConfig);
    }
}
