/*
 * Copyright  2003-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package wssec;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.ws.security.saml.SAMLIssuerFactory;
import org.apache.ws.security.saml.SAMLIssuer;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.utils.XMLUtils;
import org.apache.axis.configuration.NullProvider;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.saml.WSSecSignatureSAML;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.handler.WSHandler;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.w3c.dom.Document;

import org.opensaml.SAMLAssertion;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * WS-Security Test Case
 * <p/>
 * 
 * @author Davanum Srinivas (dims@yahoo.com)
 */
public class TestWSSecurityNewST2 extends TestCase implements CallbackHandler {
    private static Log log = LogFactory.getLog(TestWSSecurityNewST2.class);
    static final String soapMsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "   <soapenv:Body>" +
            "      <ns1:testMethod xmlns:ns1=\"uri:LogTestService2\"></ns1:testMethod>" +
            "   </soapenv:Body>" +
            "</soapenv:Envelope>";

    static final WSSecurityEngine secEngine = new WSSecurityEngine();

    static final Crypto crypto = CryptoFactory.getInstance("crypto.properties");
    MessageContext msgContext;
    Message message;

    /**
     * TestWSSecurity constructor
     * <p/>
     * 
     * @param name name of the test
     */
    public TestWSSecurityNewST2(String name) {
        super(name);
    }

    /**
     * JUnit suite
     * <p/>
     * 
     * @return a junit test suite
     */
    public static Test suite() {
        return new TestSuite(TestWSSecurityNewST2.class);
    }

    /**
     * Main method
     * <p/>
     * 
     * @param args command line args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Setup method
     * <p/>
     * 
     * @throws Exception Thrown when there is a problem in setup
     */
    protected void setUp() throws Exception {
        AxisClient tmpEngine = new AxisClient(new NullProvider());
        msgContext = new MessageContext(tmpEngine);
        message = getSOAPMessage();
    }

    /**
     * Constructs a soap envelope
     * <p/>
     * 
     * @return soap envelope
     * @throws Exception if there is any problem constructing the soap envelope
     */
    protected Message getSOAPMessage() throws Exception {
        InputStream in = new ByteArrayInputStream(soapMsg.getBytes());
        Message msg = new Message(in);
        msg.setMessageContext(msgContext);
        return msg;
    }

    /**
     * Test that encrypt and decrypt a WS-Security envelope.
     * This test uses the RSA_15 alogrithm to transport (wrap) the symmetric
     * key.
     * <p/>
     * 
     * @throws Exception Thrown when there is any problem in signing or verification
     */
    public void testSAMLSignedSenderVouches() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml.properties");

        SAMLAssertion assertion = saml.newAssertion();

        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        
        log.info("Before SAMLSignedSenderVouches....");
        
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document signedDoc = wsSign.build(doc, null, assertion, issuerCrypto, issuerKeyName, issuerKeyPW, secHeader);
        log.info("After SAMLSignedSenderVouches....");

        /*
         * convert the resulting document into a message first. The toAxisMessage()
         * method performs the necessary c14n call to properly set up the signed
         * document and convert it into a SOAP message. Check that the contents can't
         * be read (checking if we can find a specific substring). After that we extract it
         * as a document again for further processing.
         */

        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        if (log.isDebugEnabled()) {
            log.debug("Signed SAML message (sender vouches):");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        // String encryptedString = signedMsg.getSOAPPartAsString();
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        verify(signedDoc);

    }
    
    
    /**
     * Test the default issuer class as specified in SAMLIssuerFactory. The configuration
     * file "saml3.properties" has no "org.apache.ws.security.saml.issuerClass" property,
     * and so the default value is used (A bad value was previously used for the default
     * value).
     */
    public void testDefaultIssuerClass() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml3.properties");

        SAMLAssertion assertion = saml.newAssertion();

        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        
        log.info("Before SAMLSignedSenderVouches....");
        
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document signedDoc = wsSign.build(doc, null, assertion, issuerCrypto, issuerKeyName, issuerKeyPW, secHeader);
        log.info("After SAMLSignedSenderVouches....");

        /*
         * convert the resulting document into a message first. The toAxisMessage()
         * method performs the necessary c14n call to properly set up the signed
         * document and convert it into a SOAP message. Check that the contents can't
         * be read (checking if we can find a specific substring). After that we extract it
         * as a document again for further processing.
         */

        Message signedMsg = SOAPUtil.toAxisMessage(signedDoc);
        if (log.isDebugEnabled()) {
            log.debug("Signed SAML message (sender vouches):");
            XMLUtils.PrettyElementToWriter(signedMsg.getSOAPEnvelope().getAsDOM(), new PrintWriter(System.out));
        }
        // String encryptedString = signedMsg.getSOAPPartAsString();
        signedDoc = signedMsg.getSOAPEnvelope().getAsDocument();
        verify(signedDoc);
    }
    
    
    /**
     * A test for WSS-62: "the crypto file not being retrieved in the doReceiverAction
     * method for the Saml Signed Token"
     * 
     * https://issues.apache.org/jira/browse/WSS-62
     */
    public void testWSS62() throws Exception {
        SOAPEnvelope unsignedEnvelope = message.getSOAPEnvelope();
        SAMLIssuer saml = SAMLIssuerFactory.getInstance("saml.properties");

        SAMLAssertion assertion = saml.newAssertion();

        String issuerKeyName = saml.getIssuerKeyName();
        String issuerKeyPW = saml.getIssuerKeyPassword();
        Crypto issuerCrypto = saml.getIssuerCrypto();
        WSSecSignatureSAML wsSign = new WSSecSignatureSAML();
        wsSign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        
        Document doc = unsignedEnvelope.getAsDocument();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        
        Document signedDoc = 
            wsSign.build(doc, null, assertion, issuerCrypto, issuerKeyName, issuerKeyPW, secHeader);
        
        //
        // Now verify it but first call Handler#doReceiverAction
        //
        final WSSConfig cfg = WSSConfig.getNewInstance();
        final RequestData reqData = new RequestData();
        reqData.setWssConfig(cfg);
        reqData.setMsgContext(new java.util.TreeMap());
        java.util.Map msgContext = new java.util.HashMap();
        msgContext.put(WSHandlerConstants.SIG_PROP_FILE, "crypto.properties");
        reqData.setMsgContext(msgContext);
        
        MyHandler handler = new MyHandler();
        handler.doit(WSConstants.ST_SIGNED, reqData);
        
        secEngine.processSecurityHeader(
            signedDoc, null, this, reqData.getSigCrypto(), reqData.getDecCrypto()
        );
        
        //
        // Negative test
        //
        msgContext.put(WSHandlerConstants.SIG_PROP_FILE, "crypto.properties.na");
        reqData.setMsgContext(msgContext);
        
        handler = new MyHandler();
        try {
            handler.doit(WSConstants.ST_SIGNED, reqData);
            fail("Failure expected on a bad crypto properties file");
        } catch (RuntimeException ex) {
            // expected
        }
    }

    
    /**
     * Verifies the soap envelope
     * <p/>
     * 
     * @param doc
     * @throws Exception Thrown when there is a problem in verification
     */
    private void verify(Document doc) throws Exception {
        secEngine.processSecurityHeader(doc, null, this, crypto);
        SOAPUtil.updateSOAPMessage(doc, message);
        String decryptedString = message.getSOAPPartAsString();
        assertTrue(decryptedString.indexOf("LogTestService2") > 0 ? true : false);
    }

    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                /*
                 * here call a function/method to lookup the password for
                 * the given identifier (e.g. a user name or keystore alias)
                 * e.g.: pc.setPassword(passStore.getPassword(pc.getIdentfifier))
                 * for Testing we supply a fixed name here.
                 */
                pc.setPassword("security");
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
    
    /**
     * a trivial extension of the WSHandler type
     */
    public static class MyHandler extends WSHandler {
        
        public Object 
        getOption(String key) {
            return null;
        }
        
        public void 
        setProperty(
            Object msgContext, 
            String key, 
            Object value
        ) {
        }

        public Object 
        getProperty(Object ctx, String key) {
            java.util.Map ctxMap = (java.util.Map)ctx;
            return ctxMap.get(key);
        }
    
        public void 
        setPassword(Object msgContext, String password) {
        }
        
        public String 
        getPassword(Object msgContext) {
            return null;
        }

        void doit(
            int action, 
            RequestData reqData
        ) throws org.apache.ws.security.WSSecurityException {
            doReceiverAction(action, reqData);
        }
    }
}
