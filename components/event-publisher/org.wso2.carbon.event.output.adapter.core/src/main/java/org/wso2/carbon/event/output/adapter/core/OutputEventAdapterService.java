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


import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.List;
import java.util.Map;

/**
 * OSGI interface for the EventAdapter Service
 */

public interface OutputEventAdapterService {


    /**
     * this method returns all the available event adapter types. UI use this details to
     * show the types and the properties to be set to the user when creating the
     * event adapter objects.
     *
     * @return list of available types
     */
    List<String> getOutputEventAdapterTypes();

    /**
     * This method returns the event adapter dto for a specific event adapter type
     *
     * @param eventAdapterType
     * @return
     */
    OutputEventAdapterSchema getOutputEventAdapterSchema(String eventAdapterType);


    void create(OutputEventAdapterConfiguration outputEventAdapterConfiguration) throws OutputEventAdapterException;

    /**
     * publishes the message using the given event adapter to the given topic.
     *  @param name              - name of the event adapter
     * @param dynamicProperties
     */
    void publish(String name, Map<String, String> dynamicProperties, Object message);

    /**
     * Publishes the message synchronously and propagates any failure to the caller.
     * Use this instead of {@link #publish} when the caller needs to know whether delivery succeeded.
     * Intended for adapters that override {@code publishSync()} (e.g. email, http).
     *
     * @param name              Name of the event adapter.
     * @param dynamicProperties Per-message dynamic properties.
     * @param message           Event payload.
     * @throws OutputEventAdapterException If the adapter cannot connect or fails to deliver the message.
     */
    void publishSync(String name, Map<String, String> dynamicProperties, Object message)
            throws OutputEventAdapterException;

    /**
     * Returns whether the named adapter should deliver the next message synchronously given the
     * supplied dynamic properties. Callers (typically event consumers) use this to choose between
     * {@link #publish} and {@link #publishSync} on a per-message basis.
     *
     * @param name              Name of the event adapter.
     * @param dynamicProperties Per-message dynamic properties.
     * @return {@code true} if the adapter requests synchronous delivery.
     */
    boolean isSync(String name, Map<String, String> dynamicProperties);

    /**
     * publish testConnect message using the given event adapter.
     *
     * @param outputEventAdapterConfiguration - Configuration Details of the event adapter
     */
    void testConnection(OutputEventAdapterConfiguration outputEventAdapterConfiguration) throws OutputEventAdapterException, TestConnectionNotSupportedException;

    void destroy(String name);

    boolean isPolled(String outputAdapterName) throws OutputEventAdapterException;
}
