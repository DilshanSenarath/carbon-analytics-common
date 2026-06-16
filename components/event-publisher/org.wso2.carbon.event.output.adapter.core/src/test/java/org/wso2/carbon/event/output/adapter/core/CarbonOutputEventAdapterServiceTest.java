/*
 * Copyright (c) 2005-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.event.output.adapter.core;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.internal.CarbonOutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfig;
import org.wso2.carbon.event.output.adapter.core.internal.config.AdapterConfigs;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for CarbonOutputEventAdapterService.
 */
public class CarbonOutputEventAdapterServiceTest {

    private static final int TENANT_ID = -9001;
    private static final String ADAPTER_TYPE = "mock";
    private static final String ADAPTER_NAME = "MockAdapter";

    private CarbonOutputEventAdapterService adapterService;
    private OutputEventAdapter mockAdapter;

    @BeforeMethod
    public void setUp() throws OutputEventAdapterException {

        System.setProperty("carbon.home",
                Paths.get("src", "test", "resources", "carbon-context").toString());

        mockAdapter = mock(OutputEventAdapter.class);

        OutputEventAdapterServiceValueHolder.setGlobalAdapterConfigs(buildGlobalAdapterConfigs());

        adapterService = new CarbonOutputEventAdapterService();
        adapterService.registerEventAdapterFactory(new SyncCapableMockFactory(mockAdapter));

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(TENANT_ID);

        OutputEventAdapterConfiguration config = new OutputEventAdapterConfiguration();
        config.setName(ADAPTER_NAME);
        config.setType(ADAPTER_TYPE);
        adapterService.create(config);
    }

    @AfterMethod
    public void tearDown() {

        adapterService.destroy(ADAPTER_NAME);
        PrivilegedCarbonContext.unloadTenant(TENANT_ID);
    }

    @Test
    public void testPublishSyncConnectsOnFirstCall() throws Exception {

        adapterService.publishSync(ADAPTER_NAME, dynamicProps(), "message");

        verify(mockAdapter, times(1)).connectSync();
        verify(mockAdapter, times(1)).publishSync("message", dynamicProps());
    }

    @Test
    public void testPublishSyncReusesConnectionOnSubsequentCalls() throws Exception {

        adapterService.publishSync(ADAPTER_NAME, dynamicProps(), "msg1");
        adapterService.publishSync(ADAPTER_NAME, dynamicProps(), "msg2");

        verify(mockAdapter, times(1)).connectSync();
        verify(mockAdapter, times(2)).publishSync(any(), any());
    }

    @Test
    public void testPublishSyncThrowsWhenConnectSyncFails() throws Exception {

        doThrow(new ConnectionUnavailableException("connect unavailable"))
                .when(mockAdapter).connectSync();

        try {
            adapterService.publishSync(ADAPTER_NAME, dynamicProps(), "message");
            fail("Expected OutputEventAdapterException");
        } catch (OutputEventAdapterException e) {
            // expected — connection failure is wrapped.
        }
        verify(mockAdapter, never()).publishSync(any(), any());
    }

    @Test
    public void testPublishSyncDisconnectsSyncWhenPublishFails() throws Exception {

        doThrow(new ConnectionUnavailableException("publish unavailable"))
                .when(mockAdapter).publishSync(any(), any());

        try {
            adapterService.publishSync(ADAPTER_NAME, dynamicProps(), "message");
            fail("Expected OutputEventAdapterException");
        } catch (OutputEventAdapterException e) {
            // expected
        }
        verify(mockAdapter, times(1)).disconnectSync();
    }

    @Test(expectedExceptions = OutputEventAdapterRuntimeException.class)
    public void testPublishSyncThrowsRuntimeExceptionWhenAdapterNotFound()
            throws OutputEventAdapterException {

        adapterService.publishSync("nonexistent", dynamicProps(), "message");
    }

    @Test
    public void testIsSyncReturnsTrueWhenAdapterIndicatesSync() {

        when(mockAdapter.isSync(any())).thenReturn(true);

        assertTrue(adapterService.isSync(ADAPTER_NAME, dynamicProps()));
    }

    @Test(expectedExceptions = OutputEventAdapterRuntimeException.class)
    public void testIsSyncThrowsRuntimeExceptionWhenAdapterNotFound() {

        adapterService.isSync("nonexistent", dynamicProps());
    }

    private static Map<String, String> dynamicProps() {

        Map<String, String> props = new HashMap<>();
        props.put("key", "value");
        return props;
    }

    private static AdapterConfigs buildGlobalAdapterConfigs() {

        AdapterConfig config = new AdapterConfig();
        config.setType(ADAPTER_TYPE);
        config.setGlobalProperties(new ArrayList<>());

        List<AdapterConfig> configs = new ArrayList<>();
        configs.add(config);

        AdapterConfigs adapterConfigs = new AdapterConfigs();
        adapterConfigs.setAdapterConfigs(configs);
        return adapterConfigs;
    }

    private static class SyncCapableMockFactory extends OutputEventAdapterFactory {

        private final OutputEventAdapter adapter;

        SyncCapableMockFactory(OutputEventAdapter adapter) {

            this.adapter = adapter;
        }

        @Override
        public String getType() {

            return ADAPTER_TYPE;
        }

        @Override
        public List<String> getSupportedMessageFormats() {

            return Collections.emptyList();
        }

        @Override
        public List<Property> getStaticPropertyList() {

            return null;
        }

        @Override
        public List<Property> getDynamicPropertyList() {

            return null;
        }

        @Override
        public String getUsageTips() {

            return null;
        }

        @Override
        public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration config,
            Map<String, String> globalProperties) {

            return adapter;
        }
    }
}
