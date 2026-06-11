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

package org.wso2.carbon.event.stream.core.exception;

import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;

/**
 * Exception thrown when a single stream consumer fails to process an event.
 */
public final class ConsumerFailureException extends EventStreamException {

    private final String consumerType;
    private final String streamId;

    public ConsumerFailureException(String consumerType, String streamId, Throwable cause) {

        super(EventStreamConstants.ErrorMessage.CONSUMER_FAILURE
                .formatDescription(consumerType, streamId, cause.getMessage()), cause);
        this.consumerType = consumerType;
        this.streamId = streamId;
    }

    public String getConsumerType() {

        return consumerType;
    }

    public String getStreamId() {

        return streamId;
    }

    @Override
    public String toString() {

        return getMessage();
    }
}
