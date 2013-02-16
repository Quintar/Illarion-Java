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
package illarion.client.graphics;

import illarion.common.types.Rectangle;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import javax.annotation.Nonnull;

/**
 * This interface defines a simple object that can be drawn to the screen.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public interface Drawable {
    /**
     * Get the area covered by this item the last time is was rendered.
     *
     * @return the last area that was covered by this item
     */
    @Nonnull
    Rectangle getLastDisplayRect();

    /**
     * Draw the object on the screen.
     *
     * @return true in case the render operation was performed correctly
     */
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean draw(@Nonnull Graphics g);

    /**
     * Update the alpha value of this component. This is done by considering the size and the location of the
     * component and regarding the alpha target.
     *
     * @param container the container that stores the graphic
     * @param delta     the time in milliseconds since the last update
     */
    void update(@Nonnull GameContainer container, int delta);
}
