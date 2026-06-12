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

package org.wso2.carbon.event.output.adapter.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.Map;

public class OutputAdapterRuntime {
    private static final Log log = LogFactory.getLog(OutputAdapterRuntime.class);
    private final OutputEventAdapter outputEventAdapter;
    private final String name;
    private volatile boolean connected = false;
    private volatile boolean syncConnected = false;
    private final DecayTimer timer = new DecayTimer();
    private volatile long nextConnectionTime;


    public OutputAdapterRuntime(OutputEventAdapter outputEventAdapter, String name) throws OutputEventAdapterException {
        this.outputEventAdapter = outputEventAdapter;
        this.name = name;
        synchronized (this) {
            outputEventAdapter.init();
//            try {
//                outputEventAdapter.connect();
//                connected = true;
//            } catch (ConnectionUnavailableException e) {
//                connected = false;
//                outputEventAdapter.disconnect();
//                log.error("Error initializing connecting on " + this.name + ", reconnection will be tried on next event arrival.", e);
//            } catch (OutputEventAdapterRuntimeException e) {
//                connected = false;
//                outputEventAdapter.disconnect();
//                log.error("Error initializing connecting on " + this.name + ", reconnection will be tried on next event arrival.", e);
//            }
        }
    }

    public void publish(Object message, Map<String, String> dynamicProperties) {
        try {
            try {
                if (connected) {
                    outputEventAdapter.publish(message, dynamicProperties);
                } else {
                    if (nextConnectionTime <= System.currentTimeMillis()) {
                        synchronized (this) {
                            if (!connected) {
                                if (nextConnectionTime <= System.currentTimeMillis()) {
                                    outputEventAdapter.connect();
                                    outputEventAdapter.publish(message, dynamicProperties);
                                    connected = true;
                                    timer.reset();
                                } else {
                                    logAndDrop(message);
                                }
                            } else {
                                outputEventAdapter.publish(message, dynamicProperties);
                            }
                        }
                    } else {
                        logAndDrop(message);
                    }
                }
            } catch (ConnectionUnavailableException e) {
                connected = false;
                if (nextConnectionTime <= System.currentTimeMillis()) {
                    synchronized (this) {
                        if (nextConnectionTime <= System.currentTimeMillis()) {
                            outputEventAdapter.disconnect();
                            timer.incrementPosition();
                            nextConnectionTime = System.currentTimeMillis() + timer.returnTimeToWait();
                            if (timer.returnTimeToWait() == 0) {
                                log.error("Connection unavailable for Output Adopter '" + name + "' reconnecting.", e);
                                publish(message, dynamicProperties);
                            } else {
                                log.error("Connection unavailable for Output Adopter '" + name + "' reconnection will be retried in " + (timer.returnTimeToWait()) + " milliseconds.", e);
                            }
                        } else {
                            logAndDrop(message);
                        }
                    }
                } else {
                    logAndDrop(message);
                }
            }
        } catch (Throwable e) {
            EventAdapterUtil.logAndDrop(name, message, null, e, log, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        }
    }

    /**
     * Publishes a message synchronously, establishing a sync-path connection first if not already
     * connected. Unlike the async publish path, this blocks until the adapter confirms delivery
     * and throws an exception on failure rather than dropping the event silently.
     *
     * @param message           The event payload to publish.
     * @param dynamicProperties Runtime properties passed to the underlying adapter.
     * @throws OutputEventAdapterException If the adapter cannot reach the destination.
     */
    public void publishSync(Object message, Map<String, String> dynamicProperties)
            throws OutputEventAdapterException {

        try {
            if (!syncConnected) {
                synchronized (this) {
                    if (!syncConnected) {
                        outputEventAdapter.connectSync();
                        syncConnected = true;
                    }
                }
            }
            outputEventAdapter.publishSync(message, dynamicProperties);
        } catch (ConnectionUnavailableException e) {
            synchronized (this) {
                if (syncConnected) {
                    syncConnected = false;
                    try {
                        outputEventAdapter.disconnectSync();
                    } catch (OutputEventAdapterException disconnectEx) {
                        log.error(EventAdapterConstants.ErrorMessage.SYNC_PATH_DISCONNECT_FAILED
                                .formatMessage(name), disconnectEx);
                    }
                }
            }
            throw new OutputEventAdapterException(EventAdapterConstants.ErrorMessage.SYNC_PUBLISH_FAILED.getCode(),
                    EventAdapterConstants.ErrorMessage.SYNC_PUBLISH_FAILED.formatMessage(name), e);
        }
    }

    /**
     * Delegates to the underlying adapter to decide whether the next message should be delivered
     * synchronously. Callers use this to route between {@link #publish} and {@link #publishSync}.
     *
     * @param dynamicProperties Per-message dynamic properties.
     * @return {@code true} if the underlying adapter requests synchronous delivery.
     */
    public boolean isSync(Map<String, String> dynamicProperties) {

        return outputEventAdapter.isSync(dynamicProperties);
    }

    private void logAndDrop(Object message) {
        log.error("Event dropped, Output Adapter '" + name + "' suspended, Adapter will be active after " + (nextConnectionTime - System.currentTimeMillis()) + " milliseconds.");
        if (log.isDebugEnabled()) {
            log.debug("Output Adapter '" + name + "' suspended, dropping event: /n" + message + "/n");
        }
    }

    public void destroy() {
        try {
            outputEventAdapter.disconnect();
            synchronized (this) {
                if (syncConnected) {
                    syncConnected = false;
                    try {
                        outputEventAdapter.disconnectSync();
                    } catch (OutputEventAdapterException e) {
                        log.error(EventAdapterConstants.ErrorMessage.SYNC_PATH_DISCONNECT_FAILED
                                .formatMessage(name), e);
                    }
                }
            }
        } finally {
            outputEventAdapter.destroy();
        }
    }

    public boolean isPolled() {
        return outputEventAdapter.isPolled();
    }
}
