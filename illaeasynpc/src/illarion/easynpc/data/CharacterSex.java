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
package illarion.easynpc.data;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.annotation.Nonnull;

/**
 * This enumerator contains the possible values for the sex of a NPC along with all information required to handle
 * this values correctly.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public enum CharacterSex {
    /**
     * Constant for the female gender of the NPC.
     */
    female(1),

    /**
     * Constant for the male gender of the NPC.
     */
    male(0);

    /**
     * The ID of this sex value used to identify it in the lua script.
     */
    private final int sexId;

    /**
     * The constructor for the NPC constant that stores the string
     * representation of the constants along with.
     *
     * @param id the ID representation of this constant.
     */
    private CharacterSex(final int id) {
        sexId = id;
    }

    /**
     * Get the ID of this sex representation.
     *
     * @return the ID of this sex representation
     */
    public int getId() {
        return sexId;
    }

    /**
     * Add this values to the highlighted tokens.
     *
     * @param map the map that stores the tokens
     */
    public static void enlistHighlightedWords(@Nonnull final TokenMap map) {
        for (CharacterSex sex : CharacterSex.values()) {
            map.put(sex.name(), Token.VARIABLE);
        }
    }
}
