/*
 * This file is part of the Illarion easyNPC Editor.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion easyNPC Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion easyNPC Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion easyNPC Editor.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.easynpc.parser.talk.conditions;

import illarion.easynpc.Lang;
import illarion.easynpc.parsed.talk.TalkCondition;
import illarion.easynpc.parsed.talk.conditions.ConditionTrigger;
import illarion.easynpc.parser.talk.ConditionParser;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the trigger condition. Its able to parse a trigger out of the condition collection string.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class Trigger extends ConditionParser {
    /**
     * This pattern is used to find the strings in the condition and to remove them properly.
     */
    @SuppressWarnings("nls")
    private static final Pattern STRING_FIND = Pattern.compile("\\s*\"([^\"]*)\"\\s*,\\s*");

    /**
     * Extract a condition from the working string.
     */
    @Nullable
    @Override
    @SuppressWarnings("nls")
    public TalkCondition extract() {
        if (getNewLine() == null) {
            throw new IllegalStateException("Can't extract if no state set.");
        }

        final Matcher stringMatcher = STRING_FIND.matcher(getNewLine().replace("\\\"", "''"));
        if (stringMatcher.find()) {
            final String trigger = stringMatcher.group(1).replace("''", "\\\"");
            setLine(stringMatcher.replaceFirst("").replace("''", "\\\""));

            return new ConditionTrigger(trigger);
        }

        return null;
    }

    @Override
    public String getDescription() {
        return Lang.getMsg(getClass(), "Docu.description"); //$NON-NLS-1$
    }

    @Override
    public String getExample() {
        return Lang.getMsg(getClass(), "Docu.example"); //$NON-NLS-1$
    }

    @Override
    public String getSyntax() {
        return Lang.getMsg(getClass(), "Docu.syntax"); //$NON-NLS-1$
    }

    @Override
    public String getTitle() {
        return Lang.getMsg(getClass(), "Docu.title"); //$NON-NLS-1$
    }

    @Override
    public void enlistHighlightedWords(final TokenMap map) {
        // nothing
    }
}
