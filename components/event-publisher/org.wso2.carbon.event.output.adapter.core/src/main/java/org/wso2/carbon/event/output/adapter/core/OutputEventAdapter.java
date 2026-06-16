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

import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.core.internal.EventAdapterConstants;

import java.util.Map;

/**
 * This is a EventAdapter type. these interface let users to publish subscribe messages according to
 * some type. this type can either be local, jms or ws
 */
public interface OutputEventAdapter {

    /**
     * The init of the adapter, this will be called only once be for connect() and testConnect()
     * @throws OutputEventAdapterException if there are any configuration errors
     */
    void init() throws OutputEventAdapterException;

    /**
     * Used to test the connection
     * @throws TestConnectionNotSupportedException if test connection is not supported by the adapter
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    void testConnect() throws TestConnectionNotSupportedException, ConnectionUnavailableException;

    /**
     * Will be called to connect to the backend before events are published
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    void connect() throws ConnectionUnavailableException;

    /**
     * To publish the events
     * @param message event to be published, it can be Map,OMElement or String
     * @param dynamicProperties  the dynamic properties of the event
     * @throws ConnectionUnavailableException if it cannot connect to the backend
     */
    void publish(Object message, Map<String, String> dynamicProperties) throws ConnectionUnavailableException;

    /**
     * Will be called after all publishing is done, or when ConnectionUnavailableException is thrown
     */
    void disconnect();

    /**
     * Called once before the first {@link #publishSync} invocation to establish a connection for
     * the synchronous publish path. Adapters that support synchronous delivery must override this
     * method to set up a sync-specific connection (e.g. a dedicated blocking transport).
     * 
     * The default throws {@link OutputEventAdapterException} so that adapters which have not
     * implemented sync support fail explicitly rather than silently reusing the async connection.
     *
     * @throws ConnectionUnavailableException If the connection cannot be established.
     * @throws OutputEventAdapterException    If synchronous connection is not supported.
     */
    default void connectSync() throws ConnectionUnavailableException, OutputEventAdapterException {

        throw new OutputEventAdapterException(EventAdapterConstants.ErrorMessage.SYNC_CONNECT_UNSUPPORTED.getCode(),
                EventAdapterConstants.ErrorMessage.SYNC_CONNECT_UNSUPPORTED.getMessage());
    }

    /**
     * Called when a sync-path connection failure occurs, or during adapter teardown if the sync
     * path was ever connected. Adapters that override {@link #connectSync()} must override this
     * method to release the resources it opened.
     *
     * The default throws {@link OutputEventAdapterException} to catch adapters that implement
     * {@link #connectSync()} but forget to implement the corresponding teardown.
     *
     * @throws OutputEventAdapterException If synchronous disconnection is not supported.
     */
    default void disconnectSync() throws OutputEventAdapterException {
        
        throw new OutputEventAdapterException(EventAdapterConstants.ErrorMessage.SYNC_DISCONNECT_UNSUPPORTED.getCode(),
                EventAdapterConstants.ErrorMessage.SYNC_DISCONNECT_UNSUPPORTED.getMessage());
    }

    /**
     * Will be called at the end to clean all the resources consumed
     */
    void destroy();

    /**
     * Whether events get accumulated at the adopter and clients connect to it to collect events
     * @return is polled
     */
    boolean isPolled();

    /**
     * Publishes the event synchronously, blocking until delivery succeeds or fails.
     * Adapters that support synchronous delivery should override this method.
     *
     * @param message           Event to be published.
     * @param dynamicProperties The dynamic properties of the event.
     * @throws OutputEventAdapterException If this adapter does not support synchronous publishing.
     */
    default void publishSync(Object message, Map<String, String> dynamicProperties)
            throws OutputEventAdapterException {

        throw new OutputEventAdapterException(EventAdapterConstants.ErrorMessage.SYNC_PUBLISH_UNSUPPORTED.getCode(),
                EventAdapterConstants.ErrorMessage.SYNC_PUBLISH_UNSUPPORTED.getMessage());
    }

    /**
     * Indicates whether the adapter should deliver the next message synchronously, based on the
     * caller-supplied dynamic properties.
     *
     * @param dynamicProperties Per-message dynamic properties supplied by the caller.
     * @return {@code true} if the message should be delivered via {@link #publishSync}.
     */
    default boolean isSync(Map<String, String> dynamicProperties) {

        return false;
    }

}
