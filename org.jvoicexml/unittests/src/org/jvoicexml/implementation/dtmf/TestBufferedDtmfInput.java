/*
 * File:    $HeadURL$
 * Version: $LastChangedRevision$
 * Date:    $Date$
 * Author:  $LastChangedBy$
 *
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008-2014 JVoiceXML group - http://jvoicexml.sourceforge.net
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

package org.jvoicexml.implementation.dtmf;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvoicexml.DtmfRecognizerProperties;
import org.jvoicexml.RecognitionResult;
import org.jvoicexml.event.ErrorEvent;
import org.jvoicexml.event.JVoiceXMLEvent;
import org.jvoicexml.event.plain.implementation.NomatchEvent;
import org.jvoicexml.event.plain.implementation.RecognitionEvent;
import org.jvoicexml.event.plain.implementation.SpokenInputEvent;
import org.jvoicexml.implementation.GrammarImplementation;
import org.jvoicexml.implementation.SpokenInputListener;
import org.jvoicexml.implementation.SrgsXmlGrammarImplementation;
import org.jvoicexml.xml.srgs.Grammar;
import org.jvoicexml.xml.srgs.Item;
import org.jvoicexml.xml.srgs.ModeType;
import org.jvoicexml.xml.srgs.OneOf;
import org.jvoicexml.xml.srgs.Rule;
import org.jvoicexml.xml.srgs.Ruleref;
import org.jvoicexml.xml.srgs.SrgsXmlDocument;

/**
 * Test case for the {@link BufferedDtmfInput}.
 * 
 * @author Dirk Schnelle-Walka
 * @version $Revision$
 * @since 0.7
 */
public final class TestBufferedDtmfInput implements SpokenInputListener {
    /** The test object. */
    private BufferedDtmfInput input;

    /** Synchronisation. */
    private Object lock;

    /** The last received event. */
    private transient RecognitionResult result;

    /** DTMF recognizer properties. */
    private DtmfRecognizerProperties dtmfProps;

    /**
     * Set up the test environment.
     * 
     * @throws java.lang.Exception
     *             Test failed.
     */
    @Before
    public void setUp() throws Exception {
        lock = new Object();
        input = new BufferedDtmfInput();
        input.addListener(this);
        dtmfProps = new DtmfRecognizerProperties();
        dtmfProps.setInterdigittimeout("1s");
    }

    /**
     * Test method for
     * {@link org.jvoicexml.implementation.dtmf.BufferedDtmfInput#addDtmf(char)}
     * .
     * 
     * @exception JVoiceXMLEvent
     *                Test failed.
     * @exception Exception
     *                Test failed.
     */
    @Test(timeout = 5000)
    public void testAddCharacter() throws JVoiceXMLEvent, Exception {
        final SrgsXmlDocument document = new SrgsXmlDocument();
        final Grammar grammar = document.getGrammar();
        grammar.setMode(ModeType.DTMF);
        grammar.setRoot("test");
        final Rule rule = grammar.appendChild(Rule.class);
        rule.setId("test");
        final OneOf oneOf = rule.appendChild(OneOf.class);
        final Item item1 = oneOf.appendChild(Item.class);
        item1.addText("1");
        final Item item2 = oneOf.appendChild(Item.class);
        item2.addText("2");
        final Item item3 = oneOf.appendChild(Item.class);
        item3.addText("3");
        final SrgsXmlGrammarImplementation impl = new SrgsXmlGrammarImplementation(
                document, null);
        final Collection<GrammarImplementation<?>> grammars = new java.util.ArrayList<GrammarImplementation<?>>();
        grammars.add(impl);
        input.activateGrammars(grammars);

        input.startRecognition(null, dtmfProps);
        final char dtmf = '2';
        input.addDtmf(dtmf);
        while (result == null) {
            synchronized (lock) {
                lock.wait();
            }
        }
        Assert.assertNotNull("no result obtained", result);
        Assert.assertTrue("result should be accepted", result.isAccepted());
        Assert.assertEquals(Character.toString(dtmf), result.getUtterance());
        input.stopRecognition();

        input.startRecognition(null, dtmfProps);
        final char invalidDtmf = '4';
        input.addDtmf(invalidDtmf);

        while (result == null) {
            synchronized (lock) {
                lock.wait();
            }
        }
        Assert.assertNotNull("no result obtained", result);
        Assert.assertFalse("result should be rejected", result.isAccepted());
        Assert.assertEquals(Character.toString(invalidDtmf),
                result.getUtterance());
        input.stopRecognition();
    }

    /**
     * Test method for
     * {@link org.jvoicexml.implementation.dtmf.BufferedDtmfInput#addDtmf(char)}
     * .
     * 
     * @exception JVoiceXMLEvent
     *                Test failed.
     * @exception Exception
     *                Test failed.
     */
    @Test(timeout = 5000)
    public void testAddCharacterNoGrammar() throws JVoiceXMLEvent, Exception {
        input.startRecognition(null, dtmfProps);
        final char dtmf = '2';
        input.addDtmf(dtmf);
        synchronized (lock) {
            lock.wait();
        }
        input.stopRecognition();
        Assert.assertNotNull("no result obtained", result);
        Assert.assertFalse("result should be rejected", result.isAccepted());
        Assert.assertEquals(Character.toString(dtmf), result.getUtterance());
    }

    /**
     * Test case for {@link BufferedDtmfInput#addDtmf(char)} with a
     * 4-digit PIN.
     * 
     * @exception JVoiceXMLEvent
     *                Test failed.
     * @exception Exception
     *                Test failed.
     * @since 0.7.4
     */
    @Test(timeout = 5000)
    public void testAddCharacterPinGrammar() throws JVoiceXMLEvent, Exception {
        final SrgsXmlDocument document = new SrgsXmlDocument();
        final Grammar grammar = document.getGrammar();
        grammar.setMode(ModeType.DTMF);
        grammar.setRoot("pin");
        final Rule digit = grammar.appendChild(Rule.class);
        digit.setId("digit");
        final OneOf oneOf = digit.appendChild(OneOf.class);
        final Item item1 = oneOf.appendChild(Item.class);
        item1.addText("1");
        final Item item2 = oneOf.appendChild(Item.class);
        item2.addText("2");
        final Item item3 = oneOf.appendChild(Item.class);
        item3.addText("3");
        final Item item4 = oneOf.appendChild(Item.class);
        item4.addText("4");
        final Rule pin = grammar.appendChild(Rule.class);
        pin.setId("pin");
        pin.makePublic();
        final Item item = pin.appendChild(Item.class);
        item.setRepeat(4);
        final Ruleref ref = item.appendChild(Ruleref.class);
        ref.setUri(digit);
        final SrgsXmlGrammarImplementation impl = new SrgsXmlGrammarImplementation(
                document, null);
        final Collection<GrammarImplementation<?>> grammars = new java.util.ArrayList<GrammarImplementation<?>>();
        grammars.add(impl);
        input.activateGrammars(grammars);

        input.startRecognition(null, dtmfProps);
        final char dtmf1 = '1';
        input.addDtmf(dtmf1);
        final char dtmf2 = '2';
        input.addDtmf(dtmf2);
        final char dtmf3 = '3';
        input.addDtmf(dtmf3);
        final char dtmf4 = '4';
        input.addDtmf(dtmf4);
        synchronized (lock) {
            lock.wait();
        }
        input.stopRecognition();
        Assert.assertNotNull("no result obtained", result);
        Assert.assertTrue("result should be accepted", result.isAccepted());
        Assert.assertEquals("1234", result.getUtterance());
    }

    /**
     * {@inheritDoc}
     */
    public void inputStatusChanged(final SpokenInputEvent event) {
        if (event.getEventType().equals(RecognitionEvent.EVENT_TYPE)) {
            final RecognitionEvent recEvent = (RecognitionEvent) event;
            result = recEvent.getRecognitionResult();
        } else if (event.getEventType().equals(NomatchEvent.EVENT_TYPE)) {
            final NomatchEvent nomatch = (NomatchEvent) event;
            result = nomatch.getRecognitionResult();
        } else {
            result = null;
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void inputError(final ErrorEvent error) {
    }
}
