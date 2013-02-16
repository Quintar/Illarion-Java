/*
 * This file is part of the Illarion Mapeditor.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Mapeditor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Mapeditor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Mapeditor.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.mapedit.events.map;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;

/**
 * @author Tim
 */
public class RepaintRequestEvent {
    private final Rectangle dirty;

    public RepaintRequestEvent() {
        this(null);
    }

    public RepaintRequestEvent(final Rectangle dirty) {
        this.dirty = dirty;
    }

    public void doRepaint(@Nonnull final JPanel c) {
        if (dirty != null) {
            c.repaint(dirty);
        } else {
            c.repaint();
        }

    }
}
