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

import illarion.client.IllaClient;
import illarion.client.input.CurrentMouseLocationEvent;
import illarion.client.world.GameMap;
import illarion.client.world.MapDimensions;
import illarion.client.world.World;
import illarion.common.graphics.Layers;
import illarion.common.graphics.MapConstants;
import illarion.common.types.Location;
import illarion.common.types.Rectangle;
import javolution.util.FastComparator;
import javolution.util.FastTable;
import org.apache.log4j.Logger;
import org.newdawn.slick.*;
import org.newdawn.slick.opengl.renderer.SGL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The map display manager stores and manages all objects displayed on the map. It takes care for rendering the objects
 * in the proper order, for animations of the entire map and it manages the current location of the avatar.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 * @author Nop
 */
public final class MapDisplayManager
        implements AnimatedMove {
    /**
     * This comparator is used to order the display list in case it is needed.
     *
     * @author Martin Karing &lt;nitram@illarion.org&gt;
     */
    private static final class DisplayListComparator
            extends FastComparator<DisplayItem> {
        /**
         * The serialization UID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Public constructor to allow the parent class creating instances.
         */
        DisplayListComparator() {
            // nothing to do
        }

        /**
         * Test of two objects are equal. This returns <code>true</code> also in case both objects are
         * <code>null</code>.
         */
        @Override
        public boolean areEqual(final DisplayItem o1, final DisplayItem o2) {
            return o1 == o2;
        }

        /**
         * Compare two instances of the object and there relative offset that is needed for ordering this objects.
         */
        @Override
        public int compare(@Nonnull final DisplayItem o1, @Nonnull final DisplayItem o2) {
            return o2.getZOrder() - o1.getZOrder();
        }

        /**
         * Get the hash code of a object.
         */
        @Override
        public int hashCodeOf(@Nullable final DisplayItem obj) {
            if (obj == null) {
                return 0;
            }
            return obj.hashCode();
        }
    }

    /**
     * Offset of the tiles due the perspective of the map view.
     */
    public static final int TILE_PERSPECTIVE_OFFSET = 3;

    private boolean active;

    // scrolling offset
    @Nonnull
    private final MoveAnimation ani;

    @Nonnull
    private final FadingCorridor corridor;
    @Nonnull
    private final FastTable<DisplayItem> display;
    @Nonnull
    private final DisplayListComparator displayListComperator;
    @Nonnull
    private final WeatherRenderer weatherRenderer;

    private int dL;
    private int dX;
    private int dY;

    private int elevation;

    private final Color fadeOutColor = new Color(0);

    @Nonnull
    private final MoveAnimation levelAni;

    @Nonnull
    private final Location origin;

    private static final Logger LOGGER = Logger.getLogger(MapDisplayManager.class);

    public MapDisplayManager() {
        active = false;
        ani = new MoveAnimation(this);

        display = new FastTable<DisplayItem>();
        displayListComperator = new DisplayListComparator();
        display.setValueComparator(displayListComperator);
        corridor = FadingCorridor.getInstance();
        origin = new Location();

        weatherRenderer = new WeatherRenderer();

        dX = 0;
        dY = 0;

        levelAni = new MoveAnimation(new AnimatedMove() {
            @Override
            public void animationFinished(final boolean ok) {
                // nothing to do here
            }

            @Override
            public void setPosition(final int x, final int y, final int z) {
                dL = y;
            }
        });
        elevation = 0;
        dL = 0;
    }

    /**
     * Make item visible on screen
     *
     * @param item the item that needs to be added to the screen
     */
    @SuppressWarnings("nls")
    public void add(@Nonnull final DisplayItem item) {
        synchronized (display) {
            insertSorted(item);
        }
    }

    /**
     * Animate the movement of the game map
     *
     * @param dir
     * @param speed
     * @param run
     */
    public void animate(final int dir, final int speed, final boolean run) {
        // remember move dir and
        int mod = 1;
        if (run) {
            mod = 2;
        }
        switch (dir) {
            case Location.DIR_NORTH:
                // animate map
                ani.start(0, 0, MapConstants.STEP_X * mod, -MapConstants.STEP_Y * mod, speed);
                break;
            case Location.DIR_NORTHEAST:
                // animate map
                ani.start(0, 0, 0, -MapConstants.TILE_H * mod, speed);
                break;
            case Location.DIR_EAST:
                // animate map
                ani.start(0, 0, -MapConstants.STEP_X * mod, -MapConstants.STEP_Y * mod, speed);
                break;
            case Location.DIR_SOUTHEAST:
                // animate map
                ani.start(0, 0, -MapConstants.TILE_W * mod, 0, speed);
                break;
            case Location.DIR_SOUTH:
                // animate map
                ani.start(0, 0, -MapConstants.STEP_X * mod, MapConstants.STEP_Y * mod, speed);
                break;
            case Location.DIR_SOUTHWEST:
                // animate map
                ani.start(0, 0, 0, MapConstants.TILE_H * mod, speed);
                break;
            case Location.DIR_WEST:
                // animate map
                ani.start(0, 0, MapConstants.STEP_X * mod, MapConstants.STEP_Y * mod, speed);
                break;
            case Location.DIR_NORTHWEST:
                // animate map
                ani.start(0, 0, MapConstants.TILE_W, 0, speed);
                break;
            default:
                animationFinished(false);

        }
        // start separate Elevation animation
        final int fromElevation = elevation;
        elevation = World.getMap().getElevationAt(World.getPlayer().getLocation());
        if (elevation != fromElevation) {
            levelAni.start(0, -fromElevation, 0, -elevation, speed);
        }

        // adjust Z-order after update
        final Avatar playerAvatar = World.getPlayer().getCharacter().getAvatar();
        if (playerAvatar != null) {
            readd(playerAvatar);
        }
    }

    /**
     * Map movement is complete
     *
     * @param ok
     */
    @Override
    public void animationFinished(final boolean ok) {
        // move graphical player position to new location
        setLocation(World.getPlayer().getLocation());

        // remove surplus tiles from the map
        // Game.getMap().clipMap();
    }

    public int getElevation() {
        return elevation;
    }

    public int getWorldX(final int x) {
        return ((x - getMapCenterX()) + origin.getDcX()) - dX;
    }

    public int getWorldY(final int y) {
        return ((y - getMapCenterY()) + origin.getDcY()) - dY;
    }

    private static int getMapCenterX() {
        final GameContainer window = IllaClient.getInstance().getContainer();
        return window.getWidth() >> 1;
    }

    private static int getMapCenterY() {
        final GameContainer window = IllaClient.getInstance().getContainer();
        return window.getHeight() >> 1;
    }

    /**
     * Fix avatar's position in the middle of the screen and Z-Order
     *
     * @param av
     */
    public void glueAvatarToOrigin(@Nonnull final Avatar av) {
        av.setScreenPos(origin.getDcX() - dX, (origin.getDcY() - dY) + dL, origin.getDcZ(), Layers.CHARS);
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Display lookat text
     *
     * @param x    screen coordinates
     * @param y
     * @param text
     */
    public void lookAt(int x, int y, final String text) {
        x += (getMapCenterX() - origin.getDcX()) + dX;
        y += (getMapCenterY() - origin.getDcY()) + dY;
        // y += MAP_CENTER_Y + dY;

        // Tooltip tip = Tooltip.create();
        // tip.initText(text);
        // tip.show(x, y + MapConstants.TILE_H);
        // tip.setColor(Colors.white);
        // Gui.getInstance().addToolTip(tip);

    }

    /**
     * Remove and add a item again. This has to be done in case the value of a item changed.
     *
     * @param item the display item to remove and add again
     */
    public void readd(@Nullable final DisplayItem item) {
        if (item == null) {
            assert false : "Trying to add NULL displayItem";
            return;
        }

        synchronized (display) {
            if (display.remove(item)) {
                insertSorted(item);
            } else {
                LOGGER.warn("Adding the item " + item + " to the display list again failed.");
            }
        }
    }

    /**
     * The list of areas that became dirty because the display item in this area got removed.
     */
    @Nonnull
    private final List<Rectangle> removedAreaList = new ArrayList<Rectangle>();

    /**
     * Remove item from screen
     *
     * @param item remove one display item from the list of items
     */
    public void remove(@Nonnull final DisplayItem item) {
        synchronized (display) {
            if (display.remove(item)) {
                final Rectangle displayRect = item.getLastDisplayRect();
                if (!displayRect.isEmpty()) {
                    removedAreaList.add(new Rectangle(displayRect));
                }
            } else {
                LOGGER.error("Removing display item " + item + " failed.");
            }
        }
    }

    /**
     * The queue of interactive events that need to be send to the display components at the next update.
     */
    @Nonnull
    private Queue<MapInteractionEvent> eventQueue = new ConcurrentLinkedQueue<MapInteractionEvent>();

    /**
     * Publish a event to the event queue that is send to all entries on the map display.
     *
     * @param event the event to publish
     */
    public void publishInteractionEvent(final MapInteractionEvent event) {
        eventQueue.offer(event);
    }

    /**
     * Update the display entries.
     *
     * @param container the container that holds the game
     * @param delta     the time in milliseconds since the last update
     */
    public void update(@Nonnull final GameContainer container, final int delta) {
        if (!active) {
            return;
        }

        final int centerX = container.getWidth() >> 1;
        final int centerY = container.getHeight() >> 1;

        final int offX = (centerX - origin.getDcX()) + dX;
        final int offY = (centerY - origin.getDcY()) + dY - dL;

        final Avatar av = World.getPlayer().getCharacter().getAvatar();
        if (av != null) {
            glueAvatarToOrigin(av);
            corridor.setCorridor(av);
        }

        Camera.getInstance().setViewport(-offX, -offY, container.getWidth(), container.getHeight());
        Camera.getInstance().clearDirtyAreas();

        synchronized (display) {
            for (int i1 = 0; i1 < removedAreaList.size(); i1++) {
                final Rectangle rect = removedAreaList.get(i1);
                Camera.getInstance().markAreaDirty(rect);
            }
            removedAreaList.clear();
            synchronized (GameMap.LIGHT_LOCK) {
                while (true) {
                    final MapInteractionEvent event = eventQueue.poll();
                    if (event == null) {
                        break;
                    }

                    publishEvent(container, delta, event);
                }
                publishEvent(container, delta, new CurrentMouseLocationEvent(container.getInput()));

                // update the items, backwards
                for (int i = display.size() - 1; i >= 0; i--) {
                    display.get(i).update(container, delta);
                }
            }
        }

        weatherRenderer.update(container, delta);

        if (fadeOutColor.getAlpha() > 0) {
            fadeOutColor.a = AnimationUtility.approach(fadeOutColor.getAlpha(), 0, 0, 255, delta) / 255.f;
        }
    }

    /**
     * Send a event to all displayed items.
     *
     * @param container the container that displays the game
     * @param delta     the time since the last update
     * @param event     the event that needs to be processed
     */
    private void publishEvent(final GameContainer container, final int delta,
                              @Nonnull final MapInteractionEvent event) {
        for (int i = display.size() - 1; i >= 0; i--) {
            if (display.get(i).processEvent(container, delta, event)) {
                return;
            }
        }
        event.eventNotHandled();
    }

    /**
     * This image is used to render the game screen on. It holds the texture that holds the current graphics of the
     * game. This will contain {@code null} in case render-to-texture is not supported by the host system.
     */
    private Image gameScreenImage;

    /**
     * This value is set true in case the display size is changed.
     */
    private boolean changedDisplaySize;

    /**
     * This reports to the GUI that the display size got changed.
     */
    public void reportChangedDisplaySize() {
        changedDisplaySize = true;
    }

    /**
     * Render all visible map items
     *
     * @param g the graphics component that is used to render the screen
     * @param c the game container the map is rendered in
     */
    public void render(@Nonnull final Graphics g, @Nonnull final GameContainer c) {
        if (!active) {
            return;
        }

        if ((gameScreenImage == null) || changedDisplaySize) {
            changedDisplaySize = false;
            final int width = MapDimensions.getInstance().getOnScreenWidth();
            final int height = MapDimensions.getInstance().getOnScreenHeight();
            if (gameScreenImage != null) {
                if ((gameScreenImage.getTextureHeight() >= height) && (gameScreenImage.getTextureWidth() >= width)) {
                    gameScreenImage = gameScreenImage.getSubImage(0, 0, width, height);
                } else {
                    try {
                        gameScreenImage.destroy();
                        gameScreenImage = null;
                    } catch (SlickException e) {
                        LOGGER.warn("Destruction of old backing image failed.");
                    }
                }
            }
            if (gameScreenImage == null) {
                try {
                    gameScreenImage = Image.createOffscreenImage(MapDimensions.getInstance().getOnScreenWidth(),
                            MapDimensions.getInstance().getOnScreenHeight(), SGL.GL_LINEAR);
                } catch (SlickException e) {
                    LOGGER.error("Rendering to texture fails.", e);
                    return;
                }
            }
        }

        final Graphics usedGraphics;
        try {
            usedGraphics = gameScreenImage.getGraphics();
        } catch (SlickException e) {
            LOGGER.warn("Fetching render to texture context failed.", e);
            return;
        }

        renderImpl(usedGraphics);

        Image resultImage;
        try {
            resultImage = weatherRenderer.postProcess(gameScreenImage);
        } catch (SlickException e) {
            LOGGER.error("Postprocessing the output image failed!", e);
            resultImage = gameScreenImage;
        }
        g.drawImage(resultImage, 0, 0);

        Camera.getInstance().renderDebug(g);

        if (fadeOutColor.getAlpha() > 0) {
            g.setColor(fadeOutColor);
            g.setLineWidth(3.f);
            g.fillRect(0, 0, c.getWidth(), c.getHeight());
        }
    }

    public int getHeightOffset() {
        if (gameScreenImage == null) {
            return 0;
        }
        return gameScreenImage.getTexture().getTextureHeight() - gameScreenImage.getHeight();
    }

    /**
     * Implementation of the core rendering function that just renders the map to the assigned graphic context.
     *
     * @param g the graphics context used for the render operation
     */
    private void renderImpl(@Nonnull final Graphics g) {
        final Camera camera = Camera.getInstance();

        Graphics.setCurrent(g);
        g.pushTransform();

        g.translate(-camera.getViewportOffsetX(), -camera.getViewportOffsetY());
        Camera.getInstance().clearDirtyAreas(g);

        synchronized (display) {
            synchronized (GameMap.LIGHT_LOCK) {
                // draw all items
                for (int i = 0, displaySize = display.size(); i < displaySize; i++) {
                    display.get(i).draw(g);
                }
            }
        }

        g.popTransform();
    }

    public void setActive(final boolean active) {
        if (!active) {
            fadeOutColor.a = 1.f;
        }
        this.active = active;
    }

    /**
     * Move the map origin to a new location
     *
     * @param location
     */
    public void setLocation(@Nonnull final Location location) {
        // origin.setSC(location.scX, location.scY, 0);
        origin.set(location);
        ani.stop();
        levelAni.stop();
        elevation = World.getMap().getElevationAt(origin);
        dX = 0;
        dY = 0;
        dL = -elevation;
        Camera.getInstance().markEverythingDirty();
    }

    /**
     * Scroll map
     *
     * @param x
     * @param y
     */
    @Override
    public void setPosition(final int x, final int y, final int z) {
        if ((dX == x) && (dY == y) && (dL == z)) {
            return;
        }

        dX = x;
        dY = y;
        dL = z;

        Camera.getInstance().markEverythingDirty();

        // Gui.getInstance().getManager().notifyMovement();
    }

    private void insertSorted(@Nonnull final DisplayItem item) {
        int currentStart = 0;
        int currentEnd = display.size() - 1;

        while (currentStart <= currentEnd) {
            final int middle = currentStart + ((currentEnd - currentStart) >> 1);
            final DisplayItem foundItem = display.get(middle);
            final int compareResult = displayListComperator.compare(foundItem, item);

            if (compareResult < 0) {
                currentStart = middle + 1;
            } else if (compareResult > 0) {
                currentEnd = middle - 1;
            } else {
                display.add(middle, item);
                return;
            }
        }

        display.add(currentStart, item);
    }
}
