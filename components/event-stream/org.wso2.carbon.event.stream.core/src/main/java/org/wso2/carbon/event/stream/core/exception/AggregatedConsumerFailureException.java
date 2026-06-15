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
 * Exception aggregating failures from multiple stream consumers during event dispatch.
 */
public class AggregatedConsumerFailureException extends EventStreamException {

    private final List<ConsumerFailureException> failures;

    public AggregatedConsumerFailureException(List<ConsumerFailureException> failures) {

        super(EventStreamConstants.ErrorMessage.MULTIPLE_CONSUMERS_FAILED.getCode(),
                EventStreamConstants.ErrorMessage.MULTIPLE_CONSUMERS_FAILED.formatMessage(failures.size()));
        this.failures = Collections.unmodifiableList(failures);
    }

    public List<ConsumerFailureException> getFailures() {

        return failures;
    }
}
