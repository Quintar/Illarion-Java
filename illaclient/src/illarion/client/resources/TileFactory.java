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
package illarion.client.resources;

import illarion.client.resources.data.TileTemplate;

import javax.annotation.Nonnull;

/**
 * The tile factory loads and stores all graphical representations of the tiles
 * that create the map of Illarion.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 * @author Nop
 */
public final class TileFactory extends AbstractTemplateFactory<TileTemplate> {
    /**
     * The singleton instance of this class.
     */
    private static final TileFactory INSTANCE = new TileFactory();

    /**
     * The ID of the tile that is supposed to be displayed by default.
     */
    public static final int DEFAULT_TILE_ID = 0;

    /**
     * Get the singleton instance of this class.
     *
     * @return the singleton instance
     */
    @Nonnull
    public static TileFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Private constructor of the tile factory that prepares this factory for
     * operation.
     */
    private TileFactory() {
        super(DEFAULT_TILE_ID);
    }
}
