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
package illarion.client.resources;

import illarion.client.resources.data.MiscImageTemplate;

import javax.annotation.Nonnull;

/**
 * This class is used to load and store the graphics that are needed for displaying the GUI of the game.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class MiscImageFactory extends AbstractTemplateFactory<MiscImageTemplate> {
    /**
     * The ID of the attack marker image.
     */
    public static final int ATTACK_MARKER = 0;

    /**
     * The singleton instance.
     */
    private static final MiscImageFactory INSTANCE = new MiscImageFactory();

    /**
     * Get the singleton instance of this class.
     *
     * @return the singleton instance
     */
    @Nonnull
    public static MiscImageFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private MiscImageFactory() {
    }
}
