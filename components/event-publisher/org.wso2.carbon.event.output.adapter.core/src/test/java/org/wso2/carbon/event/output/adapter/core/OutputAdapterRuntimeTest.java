/*
 * Copyright (c) 2015-2026, WSO2 LLC. (http://www.wso2.com).
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.internal.OutputAdapterRuntime;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for OutputAdapterRuntime.
 */
public class OutputAdapterRuntimeTest {

    private static final String ADAPTER_NAME = "TestAdapter";
    private static final Map<String, String> DYNAMIC_PROPS = Collections.singletonMap("key", "value");

    private OutputEventAdapter mockAdapter;
    private OutputAdapterRuntime runtime;

    @BeforeMethod
    public void setUp() throws OutputEventAdapterException {

        mockAdapter = mock(OutputEventAdapter.class);
        runtime = new OutputAdapterRuntime(mockAdapter, ADAPTER_NAME);
    }

    // --- publishSync ---

    @Test
    public void testPublishSyncConnectsOnFirstCall() throws Exception {

        runtime.publishSync("msg", DYNAMIC_PROPS);

        verify(mockAdapter, times(1)).connectSync();
        verify(mockAdapter, times(1)).publishSync("msg", DYNAMIC_PROPS);
    }

    @Test
    public void testPublishSyncReusesConnectionOnSubsequentCalls() throws Exception {

        runtime.publishSync("msg1", DYNAMIC_PROPS);
        runtime.publishSync("msg2", DYNAMIC_PROPS);

        verify(mockAdapter, times(1)).connectSync();
        verify(mockAdapter, times(2)).publishSync(any(), any());
    }

    @Test
    public void testPublishSyncWrapsConnectionUnavailableExceptionFromConnectSync() throws Exception {

        doThrow(new ConnectionUnavailableException("connect failed"))
                .when(mockAdapter).connectSync();

        try {
            runtime.publishSync("msg", DYNAMIC_PROPS);
            fail("Expected OutputEventAdapterException");
        } catch (OutputEventAdapterException e) {
            assertTrue(e.getCause() instanceof ConnectionUnavailableException);
        }
        verify(mockAdapter, never()).publishSync(any(), any());
    }

    @Test
    public void testPublishSyncDoesNotCallDisconnectSyncWhenConnectSyncFails() throws Exception {

        doThrow(new ConnectionUnavailableException("connect failed"))
                .when(mockAdapter).connectSync();

        try {
            runtime.publishSync("msg", DYNAMIC_PROPS);
        } catch (OutputEventAdapterException ignored) {
        }

        verify(mockAdapter, never()).disconnectSync();
    }

    @Test
    public void testPublishSyncCallsDisconnectSyncWhenPublishFails() throws Exception {

        doThrow(new ConnectionUnavailableException("publish failed"))
                .when(mockAdapter).publishSync(any(), any());

        try {
            runtime.publishSync("msg", DYNAMIC_PROPS);
            fail("Expected OutputEventAdapterException");
        } catch (OutputEventAdapterException e) {
            assertTrue(e.getCause() instanceof ConnectionUnavailableException);
        }
        verify(mockAdapter, times(1)).disconnectSync();
    }

    @Test
    public void testPublishSyncToleratesDisconnectSyncFailureOnPublishError() throws Exception {

        doThrow(new ConnectionUnavailableException("publish failed"))
                .when(mockAdapter).publishSync(any(), any());
        doThrow(new OutputEventAdapterException("disconnect failed"))
                .when(mockAdapter).disconnectSync();

        try {
            runtime.publishSync("msg", DYNAMIC_PROPS);
            fail("Expected OutputEventAdapterException");
        } catch (OutputEventAdapterException e) {
            // The publish failure should be the one rethrown, not the disconnect failure.
            assertTrue(e.getCause() instanceof ConnectionUnavailableException);
        }
    }

    // --- isSync ---

    @Test
    public void testIsSyncReturnsTrueWhenAdapterReturnsTrue() {

        when(mockAdapter.isSync(DYNAMIC_PROPS)).thenReturn(true);

        assertTrue(runtime.isSync(DYNAMIC_PROPS));
    }

    @Test
    public void testIsSyncReturnsFalseWhenAdapterReturnsFalse() {

        when(mockAdapter.isSync(DYNAMIC_PROPS)).thenReturn(false);

        assertFalse(runtime.isSync(DYNAMIC_PROPS));
    }

    @Test
    public void testIsSyncDelegatesToUnderlyingAdapter() {

        runtime.isSync(DYNAMIC_PROPS);

        verify(mockAdapter, times(1)).isSync(DYNAMIC_PROPS);
    }

    // --- destroy ---

    @Test
    public void testDestroySkipsDisconnectSyncWhenNotSyncConnected() throws Exception {

        runtime.destroy();

        verify(mockAdapter, times(1)).disconnect();
        verify(mockAdapter, times(1)).destroy();
        verify(mockAdapter, never()).disconnectSync();
    }

    @Test
    public void testDestroyCallsDisconnectSyncWhenSyncConnected() throws Exception {

        runtime.publishSync("msg", DYNAMIC_PROPS);
        runtime.destroy();

        verify(mockAdapter, times(1)).disconnectSync();
        verify(mockAdapter, times(1)).destroy();
    }

    @Test
    public void testDestroyCallsAdapterDestroyEvenWhenDisconnectSyncThrows() throws Exception {

        runtime.publishSync("msg", DYNAMIC_PROPS);
        doThrow(new OutputEventAdapterException("disconnect sync failed"))
                .when(mockAdapter).disconnectSync();

        runtime.destroy();

        verify(mockAdapter, times(1)).destroy();
    }
}
