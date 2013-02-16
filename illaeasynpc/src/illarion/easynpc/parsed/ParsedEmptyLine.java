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
package illarion.easynpc.parsed;

import illarion.easynpc.writer.EasyNpcWriter;
import illarion.easynpc.writer.LuaWriter;
import illarion.easynpc.writer.SQLBuilder;
import javolution.lang.Immutable;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;

/**
 * This class is used to store a empty line in the parsed NPC. This is needed to ensure to keep the formatting of the
 * NPC script.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class ParsedEmptyLine implements ParsedData, Immutable {
    /**
     * Empty lines do not effect the query.
     */
    @Override
    public void buildSQL(final SQLBuilder builder) {
        // nothing to add to the query.
    }

    /**
     * Check the stages effected by this empty lines.
     */
    @Override
    public boolean effectsEasyNpcStage(final EasyNpcWriter.WritingStage stage) {
        return false;
    }

    /**
     * Get the stages effected by the free lines.
     *
     * @return <code>false</code> always because no stages are effected
     */
    @Override
    public boolean effectsLuaWritingStage(final LuaWriter.WritingStage stage) {
        return false;
    }

    /**
     * Get the modules needed by the empty lines.
     *
     * @return <code>null</code> always
     */
    @Nullable
    @Override
    public String[] getRequiredModules() {
        return null;
    }

    /**
     * Empty lines follow a strict predefined pattern. They are removed entirely
     * from the easyNPC script and so never written.
     */
    @Override
    public void writeEasyNpc(final Writer target,
                             final EasyNpcWriter.WritingStage stage) throws IOException {
        // empty lines are never written to the easyNPC script
    }

    /**
     * Write the LUA code for those empty lines. Empty lines are discarded by
     * default.
     */
    @Override
    public void writeLua(final Writer target,
                         final LuaWriter.WritingStage stage) throws IOException {
        // nothing to do
    }

}
