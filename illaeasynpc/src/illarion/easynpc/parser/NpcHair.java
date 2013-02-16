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

import illarion.easynpc.EasyNpcScript.Line;
import illarion.easynpc.Lang;
import illarion.easynpc.ParsedNpc;
import illarion.easynpc.docu.DocuEntry;
import illarion.easynpc.parsed.ParsedHair;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This parser is able to read the definitions for hair and beard of the NPC from the script.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class NpcHair implements NpcType {
    /**
     * The pattern to fetch the beard id.
     */
    @SuppressWarnings("nls")
    private static final Pattern BEARD_ID = Pattern.compile(
            "^\\s*(beardID)\\s*=\\s*([0-9]{1,3})[\\s;]*", Pattern.CASE_INSENSITIVE
            | Pattern.MULTILINE);

    /**
     * The pattern to fetch the hair id.
     */
    @SuppressWarnings("nls")
    private static final Pattern HAIR_ID = Pattern.compile(
            "^\\s*(hairID)\\s*=\\s*([0-9]{1,3})[\\s;]*", Pattern.CASE_INSENSITIVE
            | Pattern.MULTILINE);

    /**
     * The documentation entry for the beard ID.
     */
    private final DocuEntry beardEntry = new DocuEntry() {
        @Nonnull
        @Override
        @SuppressWarnings("nls")
        public DocuEntry getChild(final int index) {
            throw new IndexOutOfBoundsException("No children here!");
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        @SuppressWarnings("nls")
        public String getDescription() {
            return Lang.getMsg(NpcHair.class, "Docu.Beard.description");
        }

        @Override
        @SuppressWarnings("nls")
        public String getExample() {
            return Lang.getMsg(NpcHair.class, "Docu.Beard.example");
        }

        @Override
        @SuppressWarnings("nls")
        public String getSyntax() {
            return Lang.getMsg(NpcHair.class, "Docu.Beard.syntax");
        }

        @Override
        @SuppressWarnings("nls")
        public String getTitle() {
            return Lang.getMsg(NpcHair.class, "Docu.Beard.title");
        }
    };

    /**
     * The documentation entry for the hair ID.
     */
    private final DocuEntry hairEntry = new DocuEntry() {
        @Nonnull
        @Override
        @SuppressWarnings("nls")
        public DocuEntry getChild(final int index) {
            throw new IndexOutOfBoundsException("No children here!");
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        @SuppressWarnings("nls")
        public String getDescription() {
            return Lang.getMsg(NpcHair.class, "Docu.Hair.description");
        }

        @Override
        @SuppressWarnings("nls")
        public String getExample() {
            return Lang.getMsg(NpcHair.class, "Docu.Hair.example");
        }

        @Override
        @SuppressWarnings("nls")
        public String getSyntax() {
            return Lang.getMsg(NpcHair.class, "Docu.Hair.syntax");
        }

        @Override
        @SuppressWarnings("nls")
        public String getTitle() {
            return Lang.getMsg(NpcHair.class, "Docu.Hair.title");
        }
    };

    /**
     * Check if the line contains the definition of a hair ID or a beard ID.
     */
    @Override
    public boolean canParseLine(@Nonnull final Line lineStruct) {
        final String line = lineStruct.getLine();

        if (HAIR_ID.matcher(line).find()) {
            return true;
        }

        if (BEARD_ID.matcher(line).find()) {
            return true;
        }
        return false;
    }

    /**
     * Get the documentation child.
     */
    @Nonnull
    @Override
    @SuppressWarnings("nls")
    public DocuEntry getChild(final int index) {
        if (index == 0) {
            return hairEntry;
        } else if (index == 1) {
            return beardEntry;
        }

        throw new IndexOutOfBoundsException(
                "The index is too small or too large");
    }

    /**
     * This parser contains 2 children. One documentation children for the hair,
     * one for the beard.
     */
    @Override
    public int getChildCount() {
        return 2;
    }

    /**
     * Get the description for the documentation of this parser.
     */
    @Override
    @SuppressWarnings("nls")
    public String getDescription() {
        return Lang.getMsg(getClass(), "Docu.description");
    }

    /**
     * This parser contains no example. The examples are stored in the children.
     */
    @Nullable
    @Override
    public String getExample() {
        return null;
    }

    /**
     * This parser contains no syntax. The syntax is stored in the documentation
     * children of this parser.
     */
    @Nullable
    @Override
    public String getSyntax() {
        return null;
    }

    /**
     * Get the title for the documentation of this parser.
     */
    @Override
    @SuppressWarnings("nls")
    public String getTitle() {
        return Lang.getMsg(getClass(), "Docu.title");
    }

    /**
     * Parse a line of the script and filter the required data out.
     */
    @Override
    public void parseLine(@Nonnull final Line line, @Nonnull final ParsedNpc npc) {
        Matcher matcher;

        matcher = HAIR_ID.matcher(line.getLine());
        if (matcher.find()) {
            final int id = Integer.parseInt(matcher.group(2));

            npc.addNpcData(new ParsedHair(ParsedHair.HairType.Hair, id));
            return;
        }

        matcher = BEARD_ID.matcher(line.getLine());
        if (matcher.find()) {
            final int id = Integer.parseInt(matcher.group(2));

            npc.addNpcData(new ParsedHair(ParsedHair.HairType.Beard, id));
            ;
        }
    }

    @Override
    public void enlistHighlightedWords(@Nonnull final TokenMap map) {
        map.put("beardID", Token.RESERVED_WORD);
        map.put("hairID", Token.RESERVED_WORD);
    }
}
