/*
 * File:    $HeadURL$
 * Version: $LastChangedRevision$
 * Date:    $Date$
 * Author:  $LastChangedBy$
 *
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2012 JVoiceXML group - http://jvoicexml.sourceforge.net
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

package org.jvoicexml.implementation.kinect;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.jvoicexml.event.plain.implementation.SpokenInputEvent;
import org.jvoicexml.event.plain.jvxml.InputEvent;
import org.jvoicexml.mock.implementation.MockSpokenInputListener;

/**
 * Test cases for {@link KinectRecognizer}.
 * <p>
 * Make sure that a Kinect is attached to your computer when running these
 * tests.
 * </p>
 * 
 * @author Dirk Schnelle-Walka
 * @version $Revision$
 * @since 0.7.6
 */
public final class TestKinectRecognizer {
    /** Logger for this class. */
    private static final Logger LOGGER = Logger
            .getLogger(TestKinectRecognizer.class);

    /**
     * Test method for {@link KinectRecognizer#allocate()}.
     * 
     * @throws Exception
     *             test failed
     */
    @Test
    public void testAllocate() throws Exception {
        final KinectRecognizer recognizer = new KinectRecognizer(null);
        recognizer.allocate();
        Assert.assertTrue(recognizer.isAllocated());
    }

    /**
     * Test method for {@link KinectRecognizer#deallocate()}.
     * 
     * @throws Exception
     *             test failed
     */
    @Test
    public void testDeallocate() throws Exception {
        final KinectRecognizer recognizer = new KinectRecognizer(null);
        recognizer.allocate();
        Assert.assertTrue(recognizer.isAllocated());
        recognizer.deallocate();
        Assert.assertFalse(recognizer.isAllocated());
    }

    /**
     * Test method for {@link KinectRecognizer#allocate()}.
     * 
     * @throws Exception
     *             test failed
     */
    @Test
    public void testMultipleAllocates() throws Exception {
        final KinectRecognizer recognizer = new KinectRecognizer(null);
        for (int i = 0; i < 10; i++) {
            recognizer.allocate();
            Assert.assertTrue(recognizer.isAllocated());
            recognizer.deallocate();
            Assert.assertFalse(recognizer.isAllocated());
        }
    }

    /**
     * Test method fpr {@link KinectRecognizer#startRecognition()}
     * 
     * @throws Exception
     *             test failed
     */
    @Test
    public void testStartRecognition() throws Exception {
        final KinectSpokenInput input = new KinectSpokenInput();
        final MockSpokenInputListener listener = new MockSpokenInputListener();
        input.addListener(listener);
        final KinectRecognizer recognizer = new KinectRecognizer(input);
        recognizer.allocate();
        recognizer.startRecognition();
        LOGGER.info("Say 'FORWARD'!");
        listener.waitSize(1, 10000);
        final SpokenInputEvent event = listener.get(0);
        final InputEvent inputEvent = (InputEvent) event;
        final KinectRecognitionResult result = (KinectRecognitionResult) inputEvent
                .getInputResult();
        Assert.assertEquals("FORWARD", result.getUtterance());
    }

    /**
     * Test method for {@link KinectRecognizer#startRecognition()}
     * 
     * @throws Exception
     *             test failed
     */
    @Test
    public void testStartRecognitionMultiple() throws Exception {
        final KinectSpokenInput input = new KinectSpokenInput();
        final MockSpokenInputListener listener = new MockSpokenInputListener();
        input.addListener(listener);
        final KinectRecognizer recognizer = new KinectRecognizer(input);
        recognizer.allocate();
        for (int i = 0; i < 3; i++) {
            listener.clear();
            recognizer.startRecognition();
            LOGGER.info("Say 'one' " + i);
            listener.waitSize(1, 10000);
            final SpokenInputEvent event = listener.get(0);
            final InputEvent inputEvent = (InputEvent) event;
            final KinectRecognitionResult result = (KinectRecognitionResult) inputEvent
                    .getInputResult();
            Assert.assertEquals("one", result.getUtterance());
            recognizer.stopRecognition();
        }
        recognizer.deallocate();
    }

    @Test
    public void testStopRecognition() {
        Assert.fail("Not yet implemented");
    }

}
