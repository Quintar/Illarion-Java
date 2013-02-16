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
package illarion.mapedit.graphics;

import illarion.common.graphics.TextureAtlas;
import javolution.util.FastComparator;
import javolution.util.FastMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * This is the texture atlas implementation that uses AWT images.
 *
 * @author Martin Karing &gt;nitram@illarion.org&lt;
 */
public final class TextureAtlasAwt implements TextureAtlas<BufferedImage> {
    /**
     * The map to assign the texture names with the images of this atlas.
     */
    @Nonnull
    private final Map<String, BufferedImage> textures;

    /**
     * Create a new instance of the texture atlas.
     *
     * @param image      the buffered image that contains the entire texture
     * @param textureDef the XML document that contains the locations of the sub-images on the large image
     */
    public TextureAtlasAwt(@Nonnull final BufferedImage image, @Nonnull final Document textureDef) {
        final FastMap<String, BufferedImage> textureMap = new FastMap<String, BufferedImage>();
        textureMap.setKeyComparator(FastComparator.STRING);
        textureMap.setValueComparator(FastComparator.DEFAULT);
        textures = textureMap;

        final NodeList list = textureDef.getElementsByTagName("sprite");
        for (int i = 0; i < list.getLength(); i++) {
            final Element element = (Element) list.item(i);

            final String name = element.getAttribute("name");
            final int x = Integer.parseInt(element.getAttribute("x"));
            final int y = Integer.parseInt(element.getAttribute("y"));
            final int width = Integer.parseInt(element.getAttribute("width"));
            final int height = Integer.parseInt(element.getAttribute("height"));

            textures.put(name, image.getSubimage(x, y, width, height));
        }
    }

    @Override
    public boolean containsTexture(final String texture) {
        return textures.containsKey(texture);
    }

    @Override
    public BufferedImage getTexture(final String texture) {
        return textures.get(texture);
    }
}
