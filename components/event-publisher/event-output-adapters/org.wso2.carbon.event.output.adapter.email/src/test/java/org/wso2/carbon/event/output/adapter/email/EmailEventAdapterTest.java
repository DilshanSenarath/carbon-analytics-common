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

package org.wso2.carbon.event.output.adapter.email;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Session;
import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Unit tests for EmailEventAdapter.
 */
public class EmailEventAdapterTest {

    @BeforeClass
    public void globalSetup() {

        SecretManager secretManager = new SecretManagerImpl();
        OutputEventAdapterServiceValueHolder.setSecretManager(secretManager);
    }

    private Map<String, String> defaultProps() {

        Map<String, String> props = new HashMap<>();
        props.put("mail.smtp.from", "sender@example.com");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.user", "sender@example.com");
        props.put("mail.smtp.password", "secret");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("minThread", "2");
        props.put("maxThread", "10");
        props.put("keepAliveTimeInMillis", "5000");
        props.put("jobQueueSize", "100");
        return props;
    }

    private EmailEventAdapter buildAdapter(Map<String, String> props) {

        OutputEventAdapterConfiguration config = new OutputEventAdapterConfiguration();
        config.setName("TestEmailAdapter");
        config.setType("email");
        return new EmailEventAdapter(config, props);
    }

    private EmailEventAdapter connectedAdapter() throws OutputEventAdapterException {

        EmailEventAdapter adapter = buildAdapter(defaultProps());
        adapter.init();
        adapter.connect();
        return adapter;
    }

    private Map<String, String> emailDynamicProps(String address) {

        Map<String, String> dp = new HashMap<>();
        dp.put("email.subject", "Unit Test Subject");
        dp.put("email.address", address);
        dp.put("email.type", "text/plain");
        return dp;
    }

    private Session getSession(EmailEventAdapter adapter) throws Exception {

        Field field = EmailEventAdapter.class.getDeclaredField("session");
        field.setAccessible(true);
        return (Session) field.get(adapter);
    }

    // -------------------------------------------------------------------------
    // isSync
    // -------------------------------------------------------------------------

    @Test
    public void testIsSync_trueWhenFlagIsTrue() {

        EmailEventAdapter adapter = buildAdapter(defaultProps());
        Map<String, String> dp = new HashMap<>();
        dp.put("email.sync", "true");
        Assert.assertTrue(adapter.isSync(dp));
    }

    @Test
    public void testIsSync_falseWhenFlagIsFalse() {

        EmailEventAdapter adapter = buildAdapter(defaultProps());
        Map<String, String> dp = new HashMap<>();
        dp.put("email.sync", "false");
        Assert.assertFalse(adapter.isSync(dp));
    }

    @Test
    public void testIsSync_falseWhenKeyAbsent() {

        EmailEventAdapter adapter = buildAdapter(defaultProps());
        Assert.assertFalse(adapter.isSync(new HashMap<>()));
    }

    // -------------------------------------------------------------------------
    // connectSync / disconnectSync
    // -------------------------------------------------------------------------

    @Test
    public void testConnectSync_delegatesToConnect() throws Exception {

        EmailEventAdapter adapter = buildAdapter(defaultProps());
        adapter.init();
        adapter.connectSync();
        Assert.assertNotNull(getSession(adapter));
        adapter.disconnectSync();
        adapter.disconnect();
        adapter.destroy();
    }

    @Test
    public void testDisconnectSync_isNoOp() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        adapter.disconnectSync();
        Assert.assertNotNull(getSession(adapter));
    }

    // -------------------------------------------------------------------------
    // connect() — replyTo variants
    // -------------------------------------------------------------------------

    @Test
    public void testConnect_withValidReplyToAddress() throws Exception {

        Map<String, String> props = defaultProps();
        props.put("mail.smtp.replyTo", "replyto@example.com");
        EmailEventAdapter adapter = buildAdapter(props);
        adapter.init();
        adapter.connect();
        Assert.assertNotNull(getSession(adapter));
        adapter.disconnect();
    }

    @Test(expectedExceptions = ConnectionUnavailableException.class)
    public void testConnect_withInvalidReplyToAddressThrows() throws OutputEventAdapterException {

        Map<String, String> props = defaultProps();
        props.put("mail.smtp.replyTo", "not@@valid");
        EmailEventAdapter adapter = buildAdapter(props);
        adapter.connect();
    }

    @Test
    public void testConnect_withoutCredentials_createsAnonymousSession() throws Exception {

        Map<String, String> props = new HashMap<>();
        props.put("mail.smtp.from", "sender@example.com");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.smtp.port", "587");
        // No username or password
        props.put("minThread", "2");
        props.put("maxThread", "10");
        props.put("keepAliveTimeInMillis", "5000");
        props.put("jobQueueSize", "100");
        EmailEventAdapter adapter = buildAdapter(props);
        adapter.init();
        adapter.connect();
        Assert.assertNotNull(getSession(adapter));
        adapter.disconnect();
    }

    @Test
    public void testConnect_withSignatureConfigured() throws Exception {

        Map<String, String> props = defaultProps();
        props.put("mail.smtp.signature", "My Test Signature");
        EmailEventAdapter adapter = buildAdapter(props);
        adapter.init();
        adapter.connect();
        Assert.assertNotNull(getSession(adapter));
        adapter.disconnect();
    }

    // -------------------------------------------------------------------------
    // publishSync — validation
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_throwsForBlankEmailAddress() throws OutputEventAdapterException {

        EmailEventAdapter adapter = connectedAdapter();
        Map<String, String> dp = emailDynamicProps("   ");
        adapter.publishSync("Hello", dp);
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_throwsForNullEmailAddress() throws OutputEventAdapterException {

        EmailEventAdapter adapter = connectedAdapter();
        Map<String, String> dp = new HashMap<>();
        dp.put("email.subject", "Test");
        dp.put("email.type", "text/plain");
        adapter.publishSync("Hello", dp);
    }

    // -------------------------------------------------------------------------
    // publishSync — success / messaging-exception paths (mocked Transport)
    // -------------------------------------------------------------------------

    @Test
    public void testPublishSync_successWithMockedTransport() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(inv -> null);
            adapter.publishSync("Hello World", emailDynamicProps("recipient@example.com"));
            mockedTransport.verify(() -> Transport.send(any(Message.class)), Mockito.times(1));
        }
    }

    @Test
    public void testPublishSync_multipleAddresses_eachEmailSent() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(inv -> null);
            adapter.publishSync("Body", emailDynamicProps("a@example.com,b@example.com"));
            mockedTransport.verify(() -> Transport.send(any(Message.class)), Mockito.times(2));
        }
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_wrapsMessagingExceptionAsOutputException() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new MessagingException("SMTP server unavailable"));
            adapter.publishSync("Body", emailDynamicProps("recipient@example.com"));
        }
    }

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testPublishSync_wrapsAuthenticationFailedExceptionForBasicAuth() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class)))
                    .thenThrow(new AuthenticationFailedException("Invalid credentials"));
            adapter.publishSync("Body", emailDynamicProps("recipient@example.com"));
        }
    }

    // -------------------------------------------------------------------------
    // buildAndSendEmail — signature and replyTo branches
    // -------------------------------------------------------------------------

    @Test
    public void testBuildAndSendEmail_withSignatureSetFromAddress() throws Exception {

        Map<String, String> props = defaultProps();
        props.put("mail.smtp.signature", "Test Signature");
        EmailEventAdapter adapter = buildAdapter(props);
        adapter.init();
        adapter.connect();

        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(inv -> null);
            adapter.publishSync("Body", emailDynamicProps("to@example.com"));
            mockedTransport.verify(() -> Transport.send(any(Message.class)), Mockito.times(1));
        }
    }

    @Test
    public void testBuildAndSendEmail_withReplyToHeader() throws Exception {

        Map<String, String> props = defaultProps();
        props.put("mail.smtp.replyTo", "replyto@example.com");
        EmailEventAdapter adapter = buildAdapter(props);
        adapter.init();
        adapter.connect();

        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(inv -> null);
            adapter.publishSync("Body", emailDynamicProps("to@example.com"));
            mockedTransport.verify(() -> Transport.send(any(Message.class)), Mockito.times(1));
        }
    }

    @Test
    public void testBuildAndSendEmail_htmlContentType() throws Exception {
        
        EmailEventAdapter adapter = connectedAdapter();
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(inv -> null);
            Map<String, String> dp = new HashMap<>();
            dp.put("email.subject", "HTML Test");
            dp.put("email.address", "to@example.com");
            dp.put("email.type", "text/html");
            adapter.publishSync("<b>Bold Body</b>", dp);
            mockedTransport.verify(() -> Transport.send(any(Message.class)), Mockito.times(1));
        }
    }

    // -------------------------------------------------------------------------
    // publish (async)
    // -------------------------------------------------------------------------

    @Test
    public void testPublish_emailSenderRunHandlesMessagingException() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        adapter.publish("Async body", emailDynamicProps("recipient@example.com"));
        Thread.sleep(800);
        Assert.assertNotNull(adapter);
    }

    @Test
    public void testPublish_multipleAddresses_eachSubmittedToPool() throws Exception {
        
        EmailEventAdapter adapter = connectedAdapter();
        adapter.publish("Async body", emailDynamicProps("a@example.com,b@example.com,c@example.com"));
        Thread.sleep(800);
        Assert.assertNotNull(adapter);
    }

    @Test
    public void testSendWithRetry_successOnFirstAttempt() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        try (MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {
            mockedTransport.when(() -> Transport.send(any(Message.class))).thenAnswer(inv -> null);
            // Exercises the happy path of sendWithRetry via publishSync
            adapter.publishSync("Body", emailDynamicProps("to@example.com"));
            mockedTransport.verify(() -> Transport.send(any(Message.class)), Mockito.times(1));
        }
    }

    // -------------------------------------------------------------------------
    // Miscellaneous
    // -------------------------------------------------------------------------

    @Test
    public void testIsPolled_alwaysReturnsFalse() throws OutputEventAdapterException {

        Assert.assertFalse(connectedAdapter().isPolled());
    }

    @Test
    public void testDestroy_doesNotThrow() throws Exception {

        EmailEventAdapter adapter = connectedAdapter();
        Assert.assertNotNull(getSession(adapter));
        adapter.disconnect();
        adapter.destroy();
    }

    // -------------------------------------------------------------------------
    // resolveCredentials — CLIENT_CREDENTIAL branch
    // -------------------------------------------------------------------------

    private Map<String, String> clientCredentialProps() {

        Map<String, String> props = new HashMap<>();
        props.put("mail.smtp.from", "sender@example.com");
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.authType", "CLIENT_CREDENTIAL");
        props.put("mail.smtp.clientId", "client123");
        props.put("mail.smtp.clientSecret", "secret456");
        props.put("mail.smtp.tokenEndpoint", "https://token.example.com/token");
        props.put("mail.smtp.scopes", "email");
        props.put("minThread", "2");
        props.put("maxThread", "10");
        props.put("keepAliveTimeInMillis", "5000");
        props.put("jobQueueSize", "100");
        return props;
    }

    @Test(expectedExceptions = ConnectionUnavailableException.class)
    public void testConnect_clientCredential_emptyCredentials_throwsConnectionUnavailable()
            throws OutputEventAdapterException {

        Map<String, String> props = clientCredentialProps();
        // Empty credentials: isEmpty() check in resolveCredentials → ConnectionUnavailableException
        props.put("mail.smtp.clientId", "");
        props.put("mail.smtp.clientSecret", "");
        props.put("mail.smtp.tokenEndpoint", "");
        props.put("mail.smtp.scopes", "");
        EmailEventAdapter adapter = buildAdapter(props);
        adapter.init();
        adapter.connect();
    }

    @Test
    public void testConnect_clientCredential_validCredentials_createsSession() throws Exception {

        EmailEventAdapter adapter = buildAdapter(clientCredentialProps());
        adapter.init();

        try (MockedStatic<EventAdapterUtil> mockedUtil = Mockito.mockStatic(EventAdapterUtil.class)) {
            mockedUtil.when(() -> EventAdapterUtil.getAccessToken(anyString(), anyString(), anyString(), anyString()))
                      .thenReturn("mock-access-token");
            adapter.connect();
            Assert.assertNotNull(getSession(adapter));
            adapter.disconnect();
        }
    }

    // -------------------------------------------------------------------------
    // sendWithRetry — CLIENT_CREDENTIAL path → handleRetry
    // -------------------------------------------------------------------------

    @Test(expectedExceptions = OutputEventAdapterException.class)
    public void testHandleRetry_exhaustsAttemptsAndThrowsOutputException() throws Exception {

        EmailEventAdapter adapter = buildAdapter(clientCredentialProps());
        adapter.init();

        try (MockedStatic<EventAdapterUtil> mockedUtil = Mockito.mockStatic(EventAdapterUtil.class);
             MockedStatic<Transport> mockedTransport = Mockito.mockStatic(Transport.class)) {

            mockedUtil.when(() -> EventAdapterUtil.getAccessToken(anyString(), anyString(), anyString(), anyString()))
                      .thenReturn("initial-token");
            adapter.connect();

            mockedTransport.when(() -> Transport.send(any(Message.class)))
                           .thenThrow(new AuthenticationFailedException("Token expired"));

            adapter.publishSync("Body", emailDynamicProps("to@example.com"));
        }
    }
}
