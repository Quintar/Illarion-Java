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
package illarion.easynpc.parsed.talk;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;

/**
 * This interface is the common talking condition interface used to store the consequences of a talking line.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public interface TalkConsequence {
    /**
     * The base module of all consequences.
     */
    @SuppressWarnings("nls")
    String BASE_LUA_MODULE = "npc.base.consequence.";

    /**
     * Get the LUA module needed for this consequence.
     *
     * @return the LUA module needed for this consequence
     */
    @Nullable
    String getLuaModule();

    /**
     * Write the data of this talking consequence to a easyNPC script.
     *
     * @param target the writer that takes the data
     * @throws IOException thrown in case the writing operations fail.
     */
    void writeEasyNpc(Writer target) throws IOException;

    /**
     * Write the data of this talking consequence to a LUA script.
     *
     * @param target the writer that takes the data
     * @throws IOException thrown in case the writing operations fail
     */
    void writeLua(Writer target) throws IOException;
}
