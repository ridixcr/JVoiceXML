/*
 * File:    $HeadURL$
 * Version: $LastChangedRevision$
 * Date:    $Date$
 * Author:  $LastChangedBy$
 *
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2015 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.jvoicexml.implementation.text;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvoicexml.ConnectionInformation;
import org.jvoicexml.SpeakableSsmlText;
import org.jvoicexml.client.text.TextListener;
import org.jvoicexml.client.text.TextServer;
import org.jvoicexml.event.ErrorEvent;
import org.jvoicexml.event.JVoiceXMLEvent;
import org.jvoicexml.event.plain.implementation.RecognitionEvent;
import org.jvoicexml.event.plain.implementation.SpokenInputEvent;
import org.jvoicexml.implementation.GrammarImplementation;
import org.jvoicexml.implementation.SpokenInputListener;
import org.jvoicexml.implementation.SrgsXmlGrammarImplementation;
import org.jvoicexml.xml.srgs.SrgsXmlDocument;
import org.jvoicexml.xml.ssml.SsmlDocument;

/**
 * Test cases for {@link TextTelephony}.
 * @author Dirk Schnelle-Walka
 * @version $Revision$
 * @since 0.6
 */
public final class TestTextTelephony
    implements TextListener, SpokenInputListener {
    /** Maximal number of milliseconds to wait for a receipt. */
    private static final int MAX_WAIT = 1000;

    /** Port number to use. */
    private static final int PORT = 4244;

    /** Text server to receive the data. */
    private TextServer server;

    /** The telephony object to test. */
    private TextTelephony telephony;

    /** Object lock to wait for a result. */
    private final Object lock;

    /** The session id. */
    private String sessionId;

    /** Last received object. */
    private SsmlDocument receivedDocument;

    /** The last received event. */
    private TextRecognitionResult receivedResult;

    /**
     * Creates a new object.
     */
    public TestTextTelephony() {
        lock = new Object();
    }

    /**
     * {@inheritDoc}
     */
    @Before
    public void setUp() throws Exception {
        server = new TextServer(PORT);
        server.addTextListener(this);
        server.start();
        server.waitStarted();
        final ConnectionInformation client = server.getConnectionInformation();
        telephony = new TextTelephony();
        telephony.connect(client);
        server.waitConnected();
        receivedDocument = null;
        sessionId = UUID.randomUUID().toString();
    }

    /**
     * {@inheritDoc}
     */
    @After
    public void tearDown() throws Exception {
        final ConnectionInformation client = server.getConnectionInformation();
        telephony.disconnect(client);

        server.stopServer();
    }

    /**
     * Test method for {@link org.jvoicexml.implementation.text.TextTelephony#play(org.jvoicexml.SystemOutput, java.util.Map)}.
     * @exception Exception
     *            test failed.
     * @exception JVoiceXMLEvent
     *            test failed.
     */
    @Test(timeout=5000)
    public void testPlay() throws Exception, JVoiceXMLEvent {
        final TextSynthesizedOutput textOutput = new TextSynthesizedOutput();
        final String prompt = "testPlay";
        final SpeakableSsmlText speakable = new SpeakableSsmlText(prompt);
        textOutput.queueSpeakable(speakable, sessionId, null);
        telephony.play(textOutput, null);
        synchronized (lock) {
            lock.wait(MAX_WAIT);
        }
        assertEquals(speakable.getDocument(), receivedDocument);
    }

    /**
     * Test method for {@link org.jvoicexml.implementation.text.TextTelephony#record(org.jvoicexml.UserInput, java.util.Map)}.
     * @exception Exception
     *            test failed.
     * @exception JVoiceXMLEvent
     *            test failed.
     */
    @Test(timeout=5000)
    public void testRecord() throws Exception, JVoiceXMLEvent {
        final TextSpokenInput textInput = new TextSpokenInput();
        textInput.startRecognition(null, null);
        textInput.addListener(this);
        final String utterance = "testRecord";    
        mockGrammarChecker(textInput, utterance);
        telephony.record(textInput, null);
        Assert.assertTrue(telephony.isBusy());
        server.sendInput(utterance);
        synchronized (lock) {
            lock.wait(MAX_WAIT);
        }
        Assert.assertNotNull(receivedResult);
        // equals should be already done in mocked grammar
        //Assert.assertEquals(utterance, receivedResult.getUtterance()); 
    }

    /**
     * Mocks a grammar checker to accept the utterance 
     * and pass it to the textInput.
     * @param textInput the input to use the mocked grammar
     * @param utterance the valid text to be allowed in grammar
     * @since 0.7.6
     */
    private void mockGrammarChecker(final TextSpokenInput textInput, 
            final String utterance)
            throws JVoiceXMLEvent, ParserConfigurationException {
            SrgsXmlDocument doc = new SrgsXmlDocument();
            doc.setGrammarSimple("mock", utterance);
            final SrgsXmlGrammarImplementation impl = 
                    new SrgsXmlGrammarImplementation(doc, null);
            final Collection<GrammarImplementation<?>> grammars;
            grammars = new java.util.ArrayList<>();
            grammars.add(impl);
            textInput.activateGrammars(grammars);
    }

    /**
     * Checks if the given SSML documents are equal based on their content.
     * @param doc1 the expected document
     * @param doc2 the document to check
     * @since 0.7.6
     */
    private void assertEquals(final SsmlDocument doc1,
            final SsmlDocument doc2) {
        Assert.assertNotNull(doc1);
        Assert.assertNotNull(doc2);
        Assert.assertEquals(doc1.getTextContent(), doc2.getTextContent());
    }

    /**
     * {@inheritDoc}
     */
    public void outputSsml(final SsmlDocument document) {
        receivedDocument = document;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void inputStatusChanged(final SpokenInputEvent event) {
        final String type = event.getEventType();
        if (type.equals(RecognitionEvent.EVENT_TYPE)) {
            final RecognitionEvent recEvent = (RecognitionEvent) event;
            receivedResult = (TextRecognitionResult) recEvent.getRecognitionResult();
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void started() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void connected(final InetSocketAddress remote) {
    }

    /**
     * {@inheritDoc}
     */
    public void disconnected() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inputError(final ErrorEvent error) {
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void expectingInput() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inputClosed() {
    }
}
