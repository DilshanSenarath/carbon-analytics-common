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

import org.wso2.carbon.event.stream.core.exception.EventStreamException;
import org.wso2.siddhi.core.event.Event;

/**
 * Represents event sinks that fetch events from the junction.
 */
public interface SiddhiEventConsumer {

    public String getStreamId();

    public void consumeEvents(Event[] events);

    public void consumeEvent(Event event);

    /**
     * Propagates errors to the caller if the underlying consumer throws supported exceptions.
     * Defaults to {@link #consumeEvent(Event)} for consumers that do not need error propagation.
     *
     * @param event The event object which will be an instance of {@link Event}.
     * @throws EventStreamException If the consumer signals a delivery failure.
     */
    default void consumeEventWithErrorPropagation(Event event) throws EventStreamException {

        consumeEvent(event);
    }

    public void shutdown();
}
