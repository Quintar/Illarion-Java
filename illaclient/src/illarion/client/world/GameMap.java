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
package illarion.client.world;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TObjectProcedure;
import illarion.client.crash.MapProcessorCrashHandler;
import illarion.client.net.server.TileUpdate;
import illarion.client.world.interactive.InteractiveMap;
import illarion.common.graphics.ColorHelper;
import illarion.common.graphics.ItemInfo;
import illarion.common.graphics.LightingMap;
import illarion.common.types.Location;
import illarion.common.util.Stoppable;
import illarion.common.util.StoppableStorage;
import org.newdawn.slick.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This handler stores all map data and ensures the updates of the map. This
 * class is fully thread save for all actions. Clipping, hiding effects and map
 * optimization is done by the GameMapProcessor.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 * @author Andreas Grob &lt;vilarion@illarion.org&gt;
 */
@ThreadSafe
public final class GameMap implements LightingMap, Stoppable {
    /**
     * This class is used to mark map tiles for removal. Only after all tiles are marked for removed the tiles
     * container can be emptied.
     *
     * @author Andreas Grob &lt;vilarion@illarion.org&gt;
     */
    private static final class RemoveHelper implements TObjectProcedure<MapTile> {
        /**
         * Executed for the given tile on the map. It will trigger the markAsRemoved method for that tile.
         *
         * @param tile the tile to remove
         * @return {@code true} always
         */
        @Override
        public boolean execute(final MapTile tile) {
            tile.markAsRemoved();
            return true;
        }
    }

    /**
     * This is a supporter class for the {@link GameMap#renderLights()} function and it triggers the renderLight
     * function on each tile its called on.
     *
     * @author Martin Karing &lt;nitram@illarion.org&gt;
     */
    private static final class RenderLightsHelper implements TObjectProcedure<MapTile> {
        /**
         * The factor of the influence of the ambient light on this tile.
         */
        private float factor;

        /**
         * The ambient light that is used to render the real tile light.
         */
        @Nullable
        private Color light;

        /**
         * Trigger the renderLight function on one tile with the parameters that were setup.
         *
         * @param tile the tile that gets its lights rendered now
         * @return {@code true} in case the tiles are getting processed, {@code false} of the helper was not properly
         *         setup
         */
        @Override
        public boolean execute(@Nullable final MapTile tile) {
            if (light == null) {
                return false;
            }
            if (tile != null) {
                tile.renderLight(factor, light);
            }
            return true;
        }

        /**
         * Setup the helper class by setting the factor of influence of the ambient light and the ambient light color
         * itself.
         *
         * @param newAmbientFactor the factor of influence of the ambient light color
         * @param ambientLight     the ambient light color itself
         */
        void setup(final float newAmbientFactor, @Nonnull final Color ambientLight) {
            factor = newAmbientFactor;
            light = ambientLight;
        }
    }

    /**
     * This class is a helper class to reset the lights. Each tile this class is executed receives a reset trigger for
     * the light value of the tile. This should be done before new light values are rendered on the tile.
     *
     * @author Martin Karing &lt;nitram@illarion.org&gt;
     */
    private static final class ResetLightsHelper implements TObjectProcedure<MapTile> {
        /**
         * This method causes the tile its called for to reset the light.
         *
         * @param tile the tile to reset
         * @return {@code true} in all cases
         */
        @Override
        public boolean execute(@Nullable final MapTile tile) {
            if (tile != null) {
                tile.resetLight();
            }
            return true;
        }
    }

    /**
     * The lock that is hold in case the light is currently rendered. If that is done right now the map must not be
     * rendered.
     */
    @Nonnull
    public static final Object LIGHT_LOCK = new Object();

    /**
     * The determines after how many remove operations the lists clean up on their own.
     */
    private static final float MAP_COMPACTION_FACTOR = 0.01f;

    /**
     * This is a helper object that triggers markAsRemoved for all tiles it is called for.
     */
    @Nonnull
    private final TObjectProcedure<MapTile> removeHelper = new RemoveHelper();

    /**
     * The interactive map that is used to handle the player interaction with the map.
     */
    @Nonnull
    private final InteractiveMap interactive;

    /**
     * The lock that secures the map tiles.
     */
    @Nonnull
    private final ReadWriteLock mapLock;

    /**
     * The handler for the overview map.
     */
    @Nonnull
    private final GameMiniMap minimap;

    /**
     * The map processor of this game map instance. This one handles the clipping and the render optimization of the
     * map.
     */
    @Nullable
    private GameMapProcessor processor;

    /**
     * A helper class for rendering the light values on all map tiles.
     */
    @Nonnull
    private final RenderLightsHelper renderLightsHelper = new RenderLightsHelper();

    /**
     * This is a helper procedure that will trigger a reset on all tiles its called upon.
     */
    @Nonnull
    private final TObjectProcedure<MapTile> resetLightsHelper = new ResetLightsHelper();

    /**
     * The tiles of the map. The key of the hash map is the location key of the tiles location.
     */
    @Nonnull
    @GuardedBy("mapLock")
    private final TLongObjectHashMap<MapTile> tiles;

    /**
     * Default constructor of the map handler.
     */
    public GameMap() {
        tiles = new TLongObjectHashMap<MapTile>(1000);
        tiles.setAutoCompactionFactor(MAP_COMPACTION_FACTOR);
        interactive = new InteractiveMap(this);

        mapLock = new ReentrantReadWriteLock();

        minimap = new GameMiniMap();
        restartMapProcessor();

        StoppableStorage.getInstance().add(this);
    }

    /**
     * Determines whether a map location accepts the light from a specific direction.
     *
     * @param loc    the location of the tile
     * @param deltaX the X-Delta of the light ray direction
     * @param deltaY the Y-Delta of the light ray direction
     * @return {@code true} if the position accepts the light, false if not
     */
    @Override
    public boolean acceptsLight(@Nonnull final Location loc, final int deltaX, final int deltaY) {
        final MapTile tile = getMapAt(loc);
        if (tile != null) {
            switch (tile.getFace()) {
                case ItemInfo.FACE_ALL:
                    return true;

                case ItemInfo.FACE_W:
                    return deltaX >= 0;

                case ItemInfo.FACE_SW:
                    return (deltaY - deltaX) < 0;

                case ItemInfo.FACE_S:
                    return deltaY <= 0;

                default:
                    return true;
            }
        }

        return false;
    }

    /**
     * Determines how much the tile blocks the view.
     *
     * @param loc the location of the tile
     * @return obscurity of the tile, 0 for clear view {@link LightingMap#BLOCKED_VIEW} for fully blocked
     */
    @Override
    public int blocksView(@Nonnull final Location loc) {
        final MapTile tile = getMapAt(loc);
        if (tile == null) {
            return 0;
        }
        return tile.getCoverage();
    }

    /**
     * Make the map processor checking if the player is inside a building or a
     * cave or something else and start fading out that tiles.
     */
    public void checkInside() {
        GameMapProcessor2.checkInside();
    }

    /**
     * Clear the entire map. That will cause that all tiles are recycled and
     * send back into the recycle factory.
     */
    public void clear() {
        mapLock.writeLock().lock();
        try {
            tiles.forEachValue(removeHelper);
            tiles.clear();
        } finally {
            mapLock.writeLock().unlock();
        }
        World.getLights().clear();
    }

    /**
     * Finish a tile update of the game map.
     */
    public void finishTileUpdate() {
        if (processor != null) {
            processor.start();
        }
    }

    /**
     * Get item elevation on a special position.
     *
     * @param loc the location that shall be checked
     * @return the elevation value
     */
    public int getElevationAt(@Nonnull final Location loc) {
        final MapTile ground = getMapAt(loc);
        if (ground != null) {
            return ground.getElevation();
        }
        return 0;
    }

    /**
     * Get the interactive map that is used to interact with this map.
     *
     * @return the map used to interact with this map
     */
    @Nonnull
    public InteractiveMap getInteractive() {
        return interactive;
    }

    /**
     * Get a map tile at a specified location.
     *
     * @param posX the x coordinate of the location of the searched tile
     * @param posY the y coordinate of the location of the searched tile
     * @param posZ the z coordinate of the location of the searched tile
     * @return the map tile at the location or {@code null}
     */
    @Nullable
    public MapTile getMapAt(final int posX, final int posY, final int posZ) {
        return getMapAt(Location.getKey(posX, posY, posZ));
    }

    /**
     * Get a map tile at a specified location.
     *
     * @param loc the location on the map
     * @return the map tile at the location or {@code null}
     */
    @Nullable
    public MapTile getMapAt(@Nonnull final Location loc) {
        return getMapAt(loc.getKey());
    }

    /**
     * Get a map tile at a specified location.
     *
     * @param key the key of the location
     * @return the map tile at the location or {@code null}
     */
    @Nullable
    public MapTile getMapAt(final long key) {
        mapLock.readLock().lock();
        try {
            return tiles.get(key);
        } finally {
            mapLock.readLock().unlock();
        }
    }

    /**
     * Get the overview map handler that is used currently.
     *
     * @return the object that handles the overview map
     */
    @Nonnull
    public GameMiniMap getMinimap() {
        return minimap;
    }

    /**
     * Check if there is a tile at one location on the map.
     *
     * @param posX the x coordinate of the location of the searched tile
     * @param posY the y coordinate of the location of the searched tile
     * @param posZ the z coordinate of the location of the searched tile
     * @return {@code true} in case there is a tile at this position
     */
    public boolean isMapAt(final int posX, final int posY, final int posZ) {
        return isMapAt(Location.getKey(posX, posY, posZ));
    }

    /**
     * Check if there is a tile at one location on the map.
     *
     * @param loc the location on the map
     * @return <code>true</code> in case there is a tile at this position
     */
    public boolean isMapAt(@Nonnull final Location loc) {
        return isMapAt(loc.getKey());
    }

    /**
     * Check if there is a tile at one location on the map.
     *
     * @param key the key to the location on the map
     * @return <code>true</code> in case there is a tile at this position
     */
    public boolean isMapAt(final long key) {
        mapLock.readLock().lock();
        try {
            return tiles.containsKey(key);
        } finally {
            mapLock.readLock().unlock();
        }
    }

    /**
     * Fetch all tiles on specified levels of the map.
     *
     * @param storage      the list that receives the references to the tiles on the specified levels
     * @param lowestLevel  the lowest level of tiles to add to the storage
     * @param highestLevel the highest level of tiles to add to the storage
     */
    public void getTiles(@Nonnull final Collection<MapTile> storage, final int lowestLevel, final int highestLevel) {
        mapLock.readLock().lock();
        try {
            tiles.forEachEntry(new TLongObjectProcedure<MapTile>() {
                @Override
                public boolean execute(final long l, @Nonnull final MapTile mapTile) {
                    final int tileLevel = mapTile.getLocation().getScZ();
                    if ((tileLevel >= lowestLevel) && (tileLevel <= highestLevel)) {
                        storage.add(mapTile);
                    }
                    return true;
                }
            });
        } finally {
            mapLock.readLock().unlock();
        }
    }

    /**
     * Remove a tile by its key from the map.
     *
     * @param key the key of the tile that is to be removed
     */
    public void removeTile(final long key) {
        mapLock.writeLock().lock();
        @Nullable MapTile removedTile = null;
        try {
            removedTile = tiles.remove(key);
        } finally {
            mapLock.writeLock().unlock();
        }

        if (removedTile != null) {
            removedTile.markAsRemoved();
        }
    }

    /**
     * Remove a tile from the map.
     *
     * @param tile the tile that is to be removed
     */
    public void removeTile(@Nonnull final MapTile tile) {
        removeTile(tile.getLocation().getKey());
    }

    /**
     * Render lights based on the tile light and the ambient light generated by the current IG time and the weather.
     */
    @Override
    public void renderLights() {
        final float factor = 1.f - ColorHelper.getLuminationf(World.getWeather().getAmbientLight());

        renderLightsHelper.setup(factor, World.getWeather().getAmbientLight());

        synchronized (LIGHT_LOCK) {
            mapLock.readLock().lock();
            try {
                tiles.forEachValue(renderLightsHelper);
            } finally {
                mapLock.readLock().unlock();
            }
        }

        World.getPeople().updateLight();
    }

    /**
     * Reset all tiles on the map back to black color.
     */
    @Override
    public void resetLights() {
        mapLock.readLock().lock();
        try {
            tiles.forEachValue(resetLightsHelper);
        } finally {
            mapLock.readLock().unlock();
        }
    }

    /**
     * Start or restart the map processor.
     */
    public void restartMapProcessor() {
        if (processor != null) {
            processor.clear();
            processor.saveShutdown();
            processor = null;
        }
        processor = new GameMapProcessor(this);
        processor.setUncaughtExceptionHandler(MapProcessorCrashHandler.getInstance());
        processor.start();
    }

    @Override
    public void saveShutdown() {
        if (processor != null) {
            processor.clear();
            processor.saveShutdown();
            processor = null;
        }
        clear();
    }

    /**
     * Set a light color on a tile.
     *
     * @param loc   the location of the map tile on the server map
     * @param color the color that shall be set for this tile
     * @see LightingMap#setLight(Location, Color)
     */
    @Override
    public void setLight(@Nonnull final Location loc, @Nonnull final Color color) {
        final MapTile tile = getMapAt(loc);
        if (tile != null) {
            tile.addLight(color);
        }
    }

    /**
     * Prepare the game map for a tile update.
     */
    public void startTileUpdate() {
        if (processor != null) {
            processor.pause();
        }
    }

    /**
     * Update the data of a tile. This does nothing but forwarding the location of the tile to the map processor so
     * it checks the tile again.
     *
     * @param tile the tile to check again
     */
    public void updateTile(@Nonnull final MapTile tile) {
        if (processor != null) {
            processor.reportUnchecked(tile.getLocation().getKey());
        }
    }

    /**
     * Perform a update of a single map tile regarding the update information. This can add a new tile,
     * update a old one or delete one tile.
     *
     * @param updateData the data of the update
     */
    @SuppressWarnings("nls")
    public void updateTile(@Nonnull final TileUpdate updateData) {
        final long locKey = updateData.getLocation().getKey();
        MapTile tile = getMapAt(locKey);

        if (updateData.getTileId() == MapTile.ID_NONE) {
            if (tile != null) {
                mapLock.writeLock().lock();
                try {
                    tiles.remove(locKey);
                } finally {
                    mapLock.writeLock().unlock();
                }
                tile.markAsRemoved();
            }
        } else {
            final boolean newTile = tile == null;

            // create a tile for this location if none was found
            if (newTile) {
                //noinspection ReuseOfLocalVariable
                tile = new MapTile(updateData.getLocation());
            }

            // update tile from update info
            tile.update(updateData);

            if (newTile) {
                mapLock.writeLock().lock();
                try {
                    try {
                        tiles.put(locKey, tile);
                    } finally {
                        mapLock.readLock().lock();
                        mapLock.writeLock().unlock();
                    }
                    GameMapProcessor2.processTile(tile);
                } finally {
                    mapLock.readLock().unlock();
                }
            }
            World.getLights().notifyChange(updateData.getLocation());

            if (processor != null) {
                processor.reportUnchecked(locKey);
            }

            // remember real map tile for use with overview map
            updateData.setMapTile(tile);

            if (World.getPlayer().getLocation().equals(tile.getLocation())) {
                World.getMusicBox().updatePlayerLocation();
            }
        }
    }
}
