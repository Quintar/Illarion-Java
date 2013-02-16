/*
 * This file is part of the Illarion Client.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Client.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.common.data;

import javax.annotation.Nonnull;

/**
 * This enumerator contains the attributes of a character the client is able to monitor.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public enum CharacterAttribute {
    HitPoints("hitpoints"), ManaPoints("mana"), FoodPoints("foodlevel"), Strength("strength"), Agility("agility"),
    Dexterity("dexterity"), Constitution("constitution"), Intelligence("intelligence"), Essence("essence"),
    Willpower("willpower"), Perception("perception");

    /**
     * The name for the attribute that is used by the server.
     */
    @Nonnull
    private final String serverName;

    /**
     * Default constructor.
     *
     * @param serverName the name used by the server to refer to this attribute
     */
    CharacterAttribute(@Nonnull final String serverName) {
        this.serverName = serverName;
    }

    /**
     * Get the server name that is used to refer to this attribute.
     *
     * @return the name used by the server to refer to this attribute
     */
    @Nonnull
    public String getServerName() {
        return serverName;
    }
}
