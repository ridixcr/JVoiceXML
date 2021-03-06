/*
 * File:    $HeadURL$
 * Version: $LastChangedRevision$
 * Date:    $Date$
 * Author:  $LastChangedBy$
 *
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2008 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.jvoicexml.interpreter.dialog;

import org.jvoicexml.xml.VoiceXmlNode;

/**
 * Name factory to find a id for a {@link org.jvoicexml.interpreter.Dialog}.
 *
 * @author Dirk Schnelle-Walka
 * @version $Revision$
 * @since 0.7
 */
final class DialogIdFactory {
    /** The name of the id attribute. */
    private static final String ATTRIBUTE_ID = "id";

    /**
     * A sequence number to distinguish multiple form items that are created
     * within a single millisecond.
     */
    private static long sequence = 0;

    /**
     * Do not create from outside.
     */
    private DialogIdFactory() {
    }

    /**
     * Get a name for the given node using the <code>ATTRIBUTE_NAME</code>
     * attribute. If the node is nameless an internal name is generated
     * like this:
     *
     * <p>
     * <code>
     * D&lt;Long.toHexString(System.currentTimeMillis())
     * &gt;S&lt;6-digit sequence number&gt;
     * </code>
     * </p>
     *
     *
     * @param node
     *        VoiceXmlNode
     * @return id for the node.
     * @see #ATTRIBUTE_ID
     */
    public static synchronized String getId(final VoiceXmlNode node) {
        // Check if the node's id attribute is set.
        final String id = node.getAttribute(ATTRIBUTE_ID);
        if (id != null) {
            return id;
        }

        // Simple algorithm to get an internal name.
        ++sequence;

        final String leadingZeros = "000000";

        String sequenceString = leadingZeros + Long.toHexString(sequence);
        sequenceString = sequenceString.substring(sequenceString.length()
                - leadingZeros.length());

        return "D" + Long.toHexString(System.currentTimeMillis()) + "S"
                + sequenceString;
    }
}
