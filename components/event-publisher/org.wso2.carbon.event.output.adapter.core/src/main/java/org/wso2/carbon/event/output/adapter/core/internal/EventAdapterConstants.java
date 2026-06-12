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


public final class EventAdapterConstants {

    private EventAdapterConstants() {
    }

    public static final String GLOBAL_CONFIG_FILE_NAME = "output-event-adapters.xml";

    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

    public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";

    public static final String ADAPTER_EMAIL_AUTH_TYPE = "mail.smtp.authType";
    public static final String ADAPTER_EMAIL_SMTP_USER = "mail.smtp.user";

    public static final String BASIC = "BASIC";

    /**
     * Error messages for the output-adapter synchronous publish path.
     */
    public enum ErrorMessage {

        SYNC_CONNECT_UNSUPPORTED(
                "OA-65000",
                "Synchronous connection is not supported by this adapter type."),

        SYNC_DISCONNECT_UNSUPPORTED(
                "OA-65001",
                "Synchronous disconnection is not supported by this adapter type."),

        SYNC_PUBLISH_UNSUPPORTED(
                "OA-65002",
                "Synchronous publishing is not supported by this adapter type."),

        SYNC_PATH_DISCONNECT_FAILED(
                "OA-65003",
                "Error disconnecting sync path for Output Adapter '%s'."),

        SYNC_PUBLISH_FAILED(
                "OA-65004",
                "Output Adapter '%s' failed to publish synchronously.");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }

        public String getMessage() { return message; }

        public String formatMessage(Object... args) { return String.format(message, args); }

        @Override
        public String toString() { return code + " | " + message; }
    }
}
