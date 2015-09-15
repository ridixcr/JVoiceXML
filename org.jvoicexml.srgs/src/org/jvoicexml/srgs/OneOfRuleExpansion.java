/*
 * JVoiceXML - A free VoiceXML implementation.
 *
 * Copyright (C) 2015 JVoiceXML group - http://jvoicexml.sourceforge.net
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

package org.jvoicexml.srgs;

import java.util.List;

import org.apache.log4j.Logger;
import org.jvoicexml.srgs.sisr.ExecutableSemanticInterpretation;
import org.jvoicexml.srgs.sisr.SemanticInterpretationBlock;

public class OneOfRuleExpansion implements RuleExpansion {
    private static final Logger LOGGER = Logger
            .getLogger(OneOfRuleExpansion.class);
    private List<RuleExpansion> subRules = new java.util.ArrayList<RuleExpansion>();
    private ExecutableSemanticInterpretation executableSI = null;
    private SemanticInterpretationBlock initialSI = null;

    /**
     * Add executable SI (tag) to item to return if matched. Note, per spec only
     * the last tag associated with the element is kept.
     * 
     * @param si
     */
    public void setExecutionSemanticInterpretation(ExecutableSemanticInterpretation si) {
        executableSI = si;
    }

    public void addSubRule(RuleExpansion rule) {
        subRules.add(rule);
    }

    public void addInitialSI(String si) {
        if (initialSI == null) {
            initialSI = new SemanticInterpretationBlock();
        }
        initialSI.append(si);
    }

    @Override
    public MatchConsumption match(List<String> tokens, int offset) {
        if (subRules.isEmpty()) { // Not allowed per DTD, but not validating
            return new MatchConsumption(executableSI);
        }

        // Return first match, or a failure
        for (RuleExpansion rule : subRules) {
            MatchConsumption individualResult = rule.match(tokens, offset);
            if (individualResult != null) {
                if (initialSI != null)
                    individualResult.addExecutableSemanticInterpretation(initialSI);

                individualResult.addExecutableSemanticInterpretation(executableSI);
                return individualResult;
            }
        }

        return null;
    }

    public void dump(String pad) {
        LOGGER.debug(pad + "one-of");

        for (RuleExpansion rule : subRules) {
            rule.dump(pad + " ");
        }
        if (executableSI != null)
            executableSI.dump(pad);
    }

}
