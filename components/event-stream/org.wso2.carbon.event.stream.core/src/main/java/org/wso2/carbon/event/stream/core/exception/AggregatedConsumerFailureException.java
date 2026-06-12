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

import java.util.Collections;
import java.util.List;

/**
 * Exception aggregating failures from one or more stream consumers during event dispatch.
 */
public class AggregatedConsumerFailureException extends EventStreamException {

    private final List<ConsumerFailureException> failures;

    public AggregatedConsumerFailureException(List<ConsumerFailureException> failures) {

        super(failures.isEmpty()
                ? EventStreamConstants.ErrorMessage.EVENT_DISPATCH_NO_FAILURES.getCode()
                : EventStreamConstants.ErrorMessage.MULTIPLE_CONSUMERS_FAILED.getCode(),
                buildMessage(failures));
        this.failures = Collections.unmodifiableList(failures);
    }

    public List<ConsumerFailureException> getFailures() {

        return failures;
    }

    private static String buildMessage(List<ConsumerFailureException> failures) {

        if (failures.isEmpty()) {
            return EventStreamConstants.ErrorMessage.EVENT_DISPATCH_NO_FAILURES.getMessage();
        }
        StringBuilder failureList = new StringBuilder();
        for (int i = 0; i < failures.size(); i++) {
            if (i > 0) {
                failureList.append("; ");
            }
            failureList.append('[').append(failures.get(i)).append(']');
        }
        return EventStreamConstants.ErrorMessage.MULTIPLE_CONSUMERS_FAILED
                .formatMessage(failures.size(), failureList.toString());
    }
}
