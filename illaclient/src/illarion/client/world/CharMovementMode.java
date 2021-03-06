/*
 * This file is part of the Illarion Client.
 *
 * Copyright © 2013 - Illarion e.V.
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
package illarion.client.world;

/**
 * This Enumerator contains the possible value for the movement methods of a character.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public enum CharMovementMode {
    /**
     * This constant means that no movement is done. The character is only turning around or warping.
     */
    None,

    /**
     * This movement mode means that the character is walking.
     */
    Walk,

    /**
     * This constant means that the character is running.
     */
    Run,

    /**
     * This constant means that the character is being pushed.
     */
    Push;
}
