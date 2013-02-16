/*
 * This file is part of the Illarion Common Library.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Common Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Common Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Common Library.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.common.graphics;

import illarion.common.types.Location;
import illarion.common.util.Stoppable;
import illarion.common.util.StoppableStorage;
import javolution.util.FastList;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * Manager class that handles the light. It stores the pre-calculated light rays
 * as well as the light sources that are currently in use. Also it creates and
 * removes the light sources on request.
 * <p>
 * The whole calculations are threaded, so the light map that is the target of
 * all calculation results needs to be thread save.
 * </p>
 *
 * @author Nop
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class LightTracer extends Thread implements Stoppable {
    /**
     * The current base level. Means the Z coordinate of the level the player is
     * currently on.
     */
    private static int baseLevel;

    /**
     * The logger instance that takes care for the logging output of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(LightTracer.class);

    /**
     * The maximal radius of the light. So length of the light rays is between 1
     * and the value of this constant.
     */
    public static final int MAX_RADIUS = 6;

    /**
     * The storage of the pre-calculated rays.
     */
    @Nonnull
    private final static LightRays[] RAYS;

    static {
        RAYS = new LightRays[MAX_RADIUS];

        for (int i = 0; i < MAX_RADIUS; ++i) {
            RAYS[i] = new LightRays(i + 1);
        }
    }

    /**
     * Get the current base level and so the current z level of the player
     * location.
     *
     * @return the z level of the player location
     */
    public static int getBaseLevel() {
        return baseLevel;
    }

    /**
     * Get the pre-calculated light rays for a given size of the light.
     *
     * @param size the length of the ray that is needed
     * @return the pre-calculated rays.
     */
    @SuppressWarnings("nls")
    public static LightRays getRays(final int size) {
        if ((size > 0) && (size <= MAX_RADIUS)) {
            return RAYS[size - 1];
        }

        throw new IllegalArgumentException("invalid shadow ray size " + size);
    }

    /**
     * Set the current base level. The base level always has to be the level the
     * player character is on.
     *
     * @param lvl the current z level of the player character location
     */
    public static void setBaseLevel(final int lvl) {
        baseLevel = lvl;
    }

    /**
     * Dirty flag. If this is true there are still calculations to do. If its
     * false the Light tracer has nothing left to calculate.
     */
    private volatile boolean dirty;

    /**
     * The list of light sources that are handled by this light tracer. This
     * list contains all lights that still require calculations.
     */
    @Nonnull
    private final FastList<LightSource> dirtyLights;

    /**
     * If this variable is set to <code>true</code> the light calculations are
     * restarted at the next loop of the light tracer thread cycle.
     */
    private volatile boolean doRestart = false;

    /**
     * This variable stores the last index of a tiny light that was handled.
     */
    private int lastTinyIndex = -1;

    /**
     * Object for the synchronized access on the light lists.
     */
    private final Object lightsListsLock = new Object();

    /**
     * The lighting map that is the data source and the target for the light
     * calculating results for all light sources handled by this light tracer.
     */
    private final LightingMap mapSource;

    /**
     * Pause flag that is used to interrupt the light tracer for a short time.
     */
    private boolean pause;

    /**
     * The running flag that needs to be <code>true</code> as long as the light
     * tracer is supposed to calculate the lights.
     */
    private volatile boolean running;

    /**
     * The list of light sources handled by this light tracer. This list
     * contains all lights that currently do not require any calculations.
     */
    @Nonnull
    private final FastList<LightSource> tidyLights;

    /**
     * Default constructor of the light tracer. This tracer handles all light
     * sources that are on the map source that is set with the parameter.
     *
     * @param tracerMapSource the map the lights this tracer handles are on
     */
    @SuppressWarnings("nls")
    public LightTracer(final LightingMap tracerMapSource) {
        super("LightTracer Thread");

        mapSource = tracerMapSource;
        dirtyLights = new FastList<LightSource>();
        tidyLights = new FastList<LightSource>();
        pause = false;
        running = false;
    }

    /**
     * Add a light source to the list of light sources of this tracer. This
     * causes that this light source is taken into account and is rendered by
     * this light tracer if requested.
     *
     * @param light the light that shall be added to the light tracer and so to
     *              the game screen
     */
    public void add(@Nonnull final LightSource light) {
        light.setMapSource(mapSource);
        synchronized (lightsListsLock) {
            if (!dirtyLights.contains(light)) {
                dirtyLights.add(light);
            }
            setDirty(true);
            lightsListsLock.notify();
        }
    }

    /**
     * Calculate all lights. This method is <b>not</b> multi-threaded. It just
     * triggers all calculations right away in the current thread.
     */
    public void calculate() {
        if (!dirty) {
            return;
        }

        synchronized (lightsListsLock) {
            final Iterator<LightSource> dirtyItr = dirtyLights.iterator();
            final Iterator<LightSource> tinyItr = tidyLights.iterator();

            mapSource.resetLights();

            while (tinyItr.hasNext()) {
                final LightSource light = tinyItr.next();
                light.apply();
            }

            while (dirtyItr.hasNext()) {
                final LightSource light = dirtyItr.next();
                dirtyItr.remove();
                light.calculateShadows();
                light.apply();
                tidyLights.add(light);
            }
        }
    }

    /**
     * Check if the light tracer is dirty and is not done yet calculating all
     * the lights on the map.
     *
     * @return true if there are still calculations left to do
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Check if there are no lights set.
     *
     * @return true in case this tracer does not handle any lights currently
     */
    public boolean isEmpty() {
        return tidyLights.isEmpty() && dirtyLights.isEmpty();
    }

    /**
     * Notify light system about relevant change.
     *
     * @param x the x coordinate of the location the change occurred at
     * @param y the y coordinate of the location the change occurred at
     * @param z the z coordinate of the location the change occurred at
     * @deprecated better use {@link #notifyChange(Location)}
     */
    @Deprecated
    public void notifyChange(final int x, final int y, final int z) {
        final Location loc = Location.getInstance();
        loc.setSC(x, y, z);
        notifyChange(loc);
        loc.recycle();
    }

    /**
     * Notify the light system about a change on the map. This notify is
     * forwarded to all light sources and those only take the notify into
     * account in case its within the range of their rays. So every change on
     * the map should be reported to the tracer no matter if a light is around
     * this location or not.
     *
     * @param loc the location the change occurred at
     */
    public void notifyChange(final Location loc) {
        boolean changedSomething = false;

        synchronized (lightsListsLock) {
            final Iterator<LightSource> itr = tidyLights.iterator();
            LightSource current;
            while (itr.hasNext()) {
                current = itr.next();
                current.notifyChange(loc);
                if (current.isDirty()) {
                    itr.remove();
                    dirtyLights.add(current);
                    changedSomething = true;
                }
            }
        }

        if (changedSomething) {
            restart();
        }
    }

    /**
     * Interrupt the calculations of the light tracer until {@link #start()} is
     * called next time.
     */
    public void pause() {
        pause = true;
    }

    /**
     * Refresh the light tracer and force all lights to recalculate the values.
     */
    public void refresh() {
        synchronized (lightsListsLock) {
            while (!tidyLights.isEmpty()) {
                dirtyLights.add(tidyLights.removeLast());
            }
        }
        restart();
    }

    /**
     * Move a light to the dirty lights list to have it updated at the next run.
     *
     * @param light the light that shall be updated.
     */
    public void refreshLight(@Nonnull final LightSource light) {
        synchronized (lightsListsLock) {
            if (!tidyLights.contains(light)) {
                return;
            }
            tidyLights.remove(light);

            dirtyLights.add(light);
        }
        light.refresh();
        restart();
    }

    /**
     * Remove a light source from this tracer. This causes that the light is not
     * any longer calculated and rendered.
     *
     * @param light the light source that shall be removed
     * @return true in case the light got removed, false if this operation
     *         failed
     */
    public boolean remove(final LightSource light) {
        synchronized (lightsListsLock) {
            if (dirtyLights.contains(light)) {
                dirtyLights.remove(light);
                restart();
                return true;
            }

            if (tidyLights.contains(light)) {
                tidyLights.remove(light);
                restart();
                return true;
            }
        }

        return false;
    }

    /**
     * This function renders all light effects on the map. It also causes that
     * the tracer checks if all calculations are done and all lights are applied
     * to the map. In case something of this is not done, its done within this
     * function call.
     */
    public void renderLights() {
        refresh();
        calculate();
    }

    /**
     * This method causes the light tracer to cancel all current calculations
     * and restart them.
     */
    private void restart() {
        doRestart = true;
        synchronized (lightsListsLock) {
            lightsListsLock.notify();
        }
    }

    /**
     * This method runs until the running flag is turned to false and constantly
     * calculates the lights in case its needed.
     */
    @Override
    @SuppressWarnings("nls")
    public void run() {
        while (running) {
            if (doRestart) {
                mapSource.resetLights();
                lastTinyIndex = -1;
                dirty = true;
                doRestart = false;
            }
            LightSource light = null;
            boolean dirtyLight = false;
            synchronized (lightsListsLock) {
                if (dirty && !pause) {
                    if (!tidyLights.isEmpty()
                            && ((tidyLights.size() - 1) > lastTinyIndex)) {
                        lastTinyIndex++;
                        light = tidyLights.get(lastTinyIndex);
                    } else if (!dirtyLights.isEmpty()) {
                        light = dirtyLights.removeLast();

                        if (light != null) {
                            tidyLights.add(light);
                            lastTinyIndex++;
                        }
                        dirtyLight = true;
                    } else {
                        lastTinyIndex = -1;
                    }
                } else {
                    try {
                        lightsListsLock.wait();
                    } catch (@Nonnull final InterruptedException e) {
                        LOGGER.debug("Light tracer woken up for unknown reasons", e);
                    }

                }
            }

            if (light != null) {
                if (dirtyLight) {
                    light.calculateShadows();
                }
                light.apply();
            } else {
                setDirty(false);
            }
        }
    }

    /**
     * Stop the thread as soon as possible.
     */
    @Override
    public void saveShutdown() {
        running = false;
        synchronized (lightsListsLock) {
            lightsListsLock.notify();
        }
    }

    /**
     * Update the dirty flag of the light tracer. This also triggers a
     * recalculation of the entire map in case the flag becomes dirty.
     *
     * @param newDirty the new value of the dirty flag
     */
    private void setDirty(final boolean newDirty) {
        if (!dirty && newDirty) {
            mapSource.resetLights();
            dirty = true;
        } else if (dirty && !newDirty) {
            mapSource.renderLights();
            dirty = false;
        } else {
            dirty = newDirty;
        }
    }

    /**
     * Set the running value to a new state. This is the only way to stop the
     * light tracer thread from calculating stuff.
     *
     * @param newRunning <code>false</code> to stop the light tracer thread
     */
    public void setRunning(final boolean newRunning) {
        running = newRunning;
    }

    /**
     * Overwritten start method to add the instance of the light tracer to the
     * Stoppable Storage so it shuts down at the end of the application
     * correctly.
     */
    @Override
    public synchronized void start() {
        if (pause) {
            pause = false;
            synchronized (lightsListsLock) {
                lightsListsLock.notify();
            }
            return;
        }

        if (running) {
            return;
        }
        running = true;
        super.start();
        StoppableStorage.getInstance().add(this);
    }

    /**
     * Remove all lights.
     */
    public void clear() {
        synchronized (lightsListsLock) {
            tidyLights.clear();
            dirtyLights.clear();
            restart();
        }
    }
}
