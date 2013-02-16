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

import illarion.client.resources.data.OverlayTemplate;

import javax.annotation.Nonnull;

/**
 * The overlay factory loads and stores all graphical representations of the
 * overlays that create the map of Illarion.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 * @author Nop
 */
public final class OverlayFactory extends AbstractTemplateFactory<OverlayTemplate> {
    /**
     * The singleton instance of this class.
     */
    private static final OverlayFactory INSTANCE = new OverlayFactory();

    /**
     * Get the singleton instance of this factory.
     *
     * @return the singleton instance of this factory.
     */
    @Nonnull
    public static OverlayFactory getInstance() {
        return INSTANCE;
    }

    /**
     * The private constructor to ensure that no instance but the singleton
     * instance is created.
     */
    private OverlayFactory() {
    }

    /**
     * The initialization function to prepare the class for loading the
     * resources.
     */
    @Override
    public void init() {
    }
}
