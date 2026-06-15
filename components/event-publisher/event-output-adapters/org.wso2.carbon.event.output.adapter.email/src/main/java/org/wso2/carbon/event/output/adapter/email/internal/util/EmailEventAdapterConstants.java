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

package org.wso2.carbon.event.output.adapter.email.internal.util;

public class EmailEventAdapterConstants {

    private EmailEventAdapterConstants() {
    }

    public static final String EMAIL_PROVIDER = "EMAIL_PROVIDER";
    public static final String ADAPTER_TYPE_EMAIL = "email";
    public static final String ADAPTER_MESSAGE_EMAIL_ADDRESS = "email.address";
    public static final String ADAPTER_MESSAGE_EMAIL_ADDRESS_HINT = "emailAddress.hint";
    public static final String ADAPTER_MESSAGE_EMAIL_SUBJECT = "email.subject";
    public static final String ADAPTER_MESSAGE_EMAIL_SYNC = "email.sync";
    public static final String ADAPTER_MESSAGE_EMAIL_SYNC_HINT = "emailSync.hint";
    public static final String APAPTER_MESSAGE_EMAIL_TYPE = "email.type";
    public static final String ADAPTER_MESSAGE_EMAIL_TYPE_HINT = "emailType.hint";
    public static final String ADAPTER_EMAIL_SMTP_PORT = "mail.smtp.port";
    public static final String ADAPTER_EMAIL_SMTP_USER  = "mail.smtp.user";
    public static final String ADAPTER_EMAIL_SMTP_PASSWORD  = "mail.smtp.password";
    public static final String ADAPTER_EMAIL_SMTP_FROM  = "mail.smtp.from";
    public static final String ADAPTER_EMAIL_SMTP_HOST  = "mail.smtp.host";
    public static final String ADAPTER_EMAIL_SMTP_AUTH  = "mail.smtp.auth";
    public static final String ADAPTER_EMAIL_SMTP_STARTTLS_ENABLE  = "mail.smtp.starttls.enable";
    public static final String ADAPTER_EMAIL_AUTH_TYPE = "mail.smtp.authType";
    public static final String ADAPTER_EMAIL_CLIENT_ID = "mail.smtp.clientId";
    public static final String ADAPTER_EMAIL_CLIENT_SECRET = "mail.smtp.clientSecret";
    public static final String ADAPTER_EMAIL_TOKEN_ENDPOINT = "mail.smtp.tokenEndpoint";
    public static final String ADAPTER_EMAIL_SCOPES = "mail.smtp.scopes";
    public static final String ADAPTER_EMAIL_SMTP_AUTH_MECHANISMS = "mail.smtp.auth.mechanisms";
    public static final String ADAPTER_EMAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";
    public static final String MIN_THREAD_NAME = "minThread";
    public static final String MAX_THREAD_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String EMAIL_SEPARATOR = ",";
    public static final int MIN_THREAD = 8;
    public static final int MAX_THREAD = 100;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLS = 20000;
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final String SMTP_PROTOCOL = "smtp";
    public static final int MAX_RETRY_ATTEMPTS = 2;
    public static final String XOAUTH2 = "XOAUTH2";

    /**
     * Authentication types.
     */
    public static final String CLIENT_CREDENTIAL = "CLIENT_CREDENTIAL";
    public static final String BASIC = "BASIC";

    /**
     * SMTP property constants.
     */
    public static final String MAIL_SMTP_HOST = "mail.smtp.host";
    public static final String MAIL_SMTP_PORT = "mail.smtp.port";

    /**
     * Credential Properties.
     */
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "userName";
    public static final String PASSWORD = "password";

    /**
     * Email Adapter MIME types.
     */
    public static final String MAIL_TEXT_PLAIN = "text/plain";
    public static final String MAIL_TEXT_HTML = "text/html";

    /**
     * SMTP optional property constants.
     */
    public static final String MAIL_SMTP_REPLY_TO = "mail.smtp.replyTo";
    public static final String MAIL_SMTP_SIGNATURE = "mail.smtp.signature";

    /**
     * Error messages for the email adapter sync publish path.
     */
    public enum ErrorMessage {

        SYNC_EMAIL_SEND_FAILED(
                "EMAIL-OA-65000",
                "Failed to send email to '%s' due to an SMTP/messaging error."),

        SYNC_EMAIL_MISSING_ADDRESS(
                "EMAIL-OA-65001",
                "Cannot send email synchronously: the email address dynamic property is not set."),

        SYNC_EMAIL_ENCODING_FAILED(
                "EMAIL-OA-65002",
                "Failed to build email to '%s': unsupported character encoding in address or sender signature."),

        SYNC_EMAIL_AUTH_FAILED(
                "EMAIL-OA-65003",
                "Failed to send email to '%s': SMTP authentication failed."),

        SYNC_EMAIL_SEND_REJECTED(
                "EMAIL-OA-65004",
                "Failed to send email to '%s': message rejected by the server (check recipient address validity).");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        public String formatMessage(Object... args) {

            return String.format(message, args);
        }

        @Override
        public String toString() {
            
            return code + " | " + message;
        }
    }
}
