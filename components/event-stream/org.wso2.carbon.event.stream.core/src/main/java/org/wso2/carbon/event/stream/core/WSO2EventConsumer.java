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

package org.wso2.carbon.event.stream.core;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.exception.EventStreamException;

public interface WSO2EventConsumer {

    public String getStreamId();

    /**
     * This method will be triggered for all listeners whenever an event is received
     *
     * @param event the event object which will be an instance of {@link org.wso2.carbon.databridge.commons.Event}
     */
    public void onEvent(Event event);

    /**
     * Propagates errors to the caller if the underlying consumer throws supported exceptions.
     * Defaults to {@link #onEvent(Event)} for consumers that do not need error propagation.
     *
     * @param event The event object which will be an instance of {@link Event}.
     * @throws EventStreamException If the consumer signals a failure.
     */
    default void onEventWithErrorPropagation(Event event) throws EventStreamException {

        onEvent(event);
    }

    /**
     * This method will be triggered when a new stream definition is added.
     * This method will be triggered only when the input events are of type WSO2Event
     *
     * @param definition the stream definition as an object.
     */
    public void onAddDefinition(StreamDefinition definition);

    /**
     * This method will be triggered when a new stream definition is removed.
     * This method will be triggered only when the input events are of type WSO2Event
     *
     * @param definition the stream definition as an object.
     */
    public void onRemoveDefinition(StreamDefinition definition);

}
