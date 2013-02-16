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
package illarion.easynpc.parser;

import illarion.easynpc.EasyNpcScript;
import illarion.easynpc.Lang;
import illarion.easynpc.ParsedNpc;
import illarion.easynpc.docu.DocuEntry;
import illarion.easynpc.parsed.ParsedComment;
import illarion.easynpc.writer.EasyNpcWriter;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles all comment blocks. It extracts the pure text and stores
 * it so it can be properly insert into the main NPC script.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class NpcComment implements NpcType {

    /**
     * The pattern to find out of this line is a comment block
     */
    @SuppressWarnings("nls")
    private static final Pattern COMMENT_BLOCK = Pattern.compile("^\\s*--\\s*(.*)$", Pattern.MULTILINE);

    /**
     * Check if the line is a comment block
     *
     * @param line the line that is supposed to be parsed.
     * @return <code>true</code> in case the line can be parsed by this class
     */
    @Override
    public boolean canParseLine(@Nonnull final EasyNpcScript.Line line) {
        return COMMENT_BLOCK.matcher(line.getLine()).find();
    }

    @Nonnull
    @SuppressWarnings("nls")
    @Override
    public DocuEntry getChild(final int index) {
        throw new IllegalArgumentException(
                "There are no childen in this group.");
    }

    @Override
    public int getChildCount() {
        return 0;
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

    /**
     * Add the comment block to the parsed NPC.
     */
    @SuppressWarnings("nls")
    @Override
    public void parseLine(@Nonnull final EasyNpcScript.Line line, @Nonnull final ParsedNpc npc) {
        if (line.getLine().contains(EasyNpcWriter.AC_HEADER)) {
            return;
        }
        final Matcher matcher = COMMENT_BLOCK.matcher(line.getLine());
        final String commentBlock = matcher.replaceAll("$1");
        npc.addNpcData(new ParsedComment(commentBlock));
    }

    @Override
    public void enlistHighlightedWords(final TokenMap map) {
        // nothing
    }
}
