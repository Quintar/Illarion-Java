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
package illarion.mapedit.data;

import illarion.mapedit.resource.Overlay;
import javolution.util.FastList;

import java.util.List;

/**
 * This object represents a tile with a coordinate, an id and a music id.
 *
 * @author Tim
 */
public class MapTile {
    /**
     * The tile id.
     */
    private final int id;
    /**
     * The ID of the overlay.
     */
    private final int overlayID;
    /**
     * The id of the shape
     */
    private final int shapeID;
    /**
     * The music id.
     */
    private final int musicID;
    /**
     * The items on top of this tile
     */
    private final List<MapItem> mapItems;
    /**
     * The warp point on this tile, may be {@code null}.
     */
    private MapWarpPoint mapWarpPoint;

    public MapTile(final int baseId, final int overlayID, final int shapeID, final int musicID,
                   final List<MapItem> mapItems, final MapWarpPoint mapWarpPoint) {
        id = baseId;
        this.overlayID = overlayID;
        this.shapeID = shapeID;
        this.musicID = musicID;
        this.mapWarpPoint = mapWarpPoint;
        this.mapItems = new FastList<MapItem>();
        if (mapItems != null) {
            this.mapItems.addAll(mapItems);
        }
    }

    /**
     * Creates a new tile with the coordinates, the id and the music id.
     *
     * @param id
     * @param musicID
     */
    public MapTile(final int id, final int overlayID, final int shapeID, final int musicID) {
        this(id, overlayID, shapeID, musicID, null, null);
    }

    /**
     * Creates a copy of the other tile.
     *
     * @param old
     */
    public MapTile(final MapTile old) {
        this(old.id, old.overlayID, old.shapeID, old.musicID, old.mapItems, old.mapWarpPoint);
    }

    public MapTile(final int id, final MapTile old) {
        this((Overlay.shapeID(id) == 0) ? id : Overlay.baseID(id),
                (Overlay.shapeID(id) == 0) ? 0 : Overlay.overlayID(id),
                Overlay.shapeID(id),
                old.musicID,
                old.mapItems,
                old.mapWarpPoint);
    }

    public MapTile(final int overlayID, final int shapeID, MapTile old) {
        this(old.id, overlayID, shapeID, old.musicID, old.mapItems, old.mapWarpPoint);
    }

    public MapTile(final int baseID, final int overlayID, final int shapeID, MapTile old) {
        this(baseID, overlayID, shapeID, old.musicID, old.mapItems, old.mapWarpPoint);
    }

    /**
     * Returns the tile id.
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the music id.
     *
     * @return
     */
    public int getMusicID() {
        return musicID;
    }

    /**
     * TODO: Remove that
     *
     * @return The list of items on this tile.
     */
    public List<MapItem> getMapItems() {
        return mapItems;
    }

    /**
     * @return The warp point on this tile, may be {@code null}.
     */
    public MapWarpPoint getMapWarpPoint() {
        return mapWarpPoint;
    }

    public int getOverlayID() {
        return overlayID;
    }

    public int getShapeID() {
        return shapeID;
    }

    /**
     * Sets the warp point of this tile.
     *
     * @param mapWarpPoint the new warp, may be {@code null}.
     */
    public void setMapWarpPoint(final MapWarpPoint mapWarpPoint) {
        this.mapWarpPoint = mapWarpPoint;
    }
}
