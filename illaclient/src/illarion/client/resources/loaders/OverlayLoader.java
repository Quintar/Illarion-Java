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
package illarion.client.resources.loaders;

import illarion.client.graphics.Sprite;
import illarion.client.graphics.SpriteBuffer;
import illarion.client.resources.ResourceFactory;
import illarion.client.resources.data.OverlayTemplate;
import illarion.common.util.TableLoaderOverlay;
import illarion.common.util.TableLoaderSink;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;

/**
 * This class is used to load the overlay definitions from the resource table that was created using the
 * configuration tool. The class will create the required overlay objects and send them to the overlay factory that
 * takes care for distributing those objects.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class OverlayLoader extends AbstractResourceLoader<OverlayTemplate> implements
        TableLoaderSink<TableLoaderOverlay> {
    /**
     * The logger that is used to report error messages.
     */
    private static final Logger LOGGER = Logger.getLogger(ItemLoader.class);

    /**
     * Trigger the loading sequence for this loader.
     */
    @Override
    public ResourceFactory<OverlayTemplate> call() {
        if (!hasTargetFactory()) {
            throw new IllegalStateException("targetFactory not set yet.");
        }

        final ResourceFactory<OverlayTemplate> factory = getTargetFactory();

        factory.init();
        new TableLoaderOverlay(this);
        factory.loadingFinished();

        return factory;
    }

    public static final String OVERLAY_PATH = "data/tiles/";

    public static final int OVERLAY_VARIATIONS = 28;

    /**
     * Handle a single line of the resource table.
     */
    @Override
    public boolean processRecord(final int line, @Nonnull final TableLoaderOverlay loader) {
        final int id = loader.getTileId();
        final String name = loader.getOverlayFile();

        final Sprite overlaySprite = SpriteBuffer.getInstance().getSprite(OVERLAY_PATH, name, OVERLAY_VARIATIONS, 0,
                0, Sprite.HAlign.center, Sprite.VAlign.middle, false);
        final OverlayTemplate template = new OverlayTemplate(id, overlaySprite);

        try {
            getTargetFactory().storeResource(template);
        } catch (@Nonnull final IllegalStateException ex) {
            LOGGER.error("Failed adding overlay to internal factory. ID: " + id + " - Filename: " + name);
        }

        return true;
    }

}
