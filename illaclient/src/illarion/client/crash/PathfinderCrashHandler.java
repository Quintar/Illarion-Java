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
package illarion.client.crash;

import illarion.client.util.Pathfinder;

import javax.annotation.Nonnull;

/**
 * The crash handler that takes care for crashes of the path finder. It will try
 * to restart the processor in case it crashed.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class PathfinderCrashHandler extends AbstractCrashHandler {
    /**
     * The singleton instance of this crash handler to avoid to many instances
     * of this one.
     */
    private static final PathfinderCrashHandler INSTANCE =
            new PathfinderCrashHandler();

    /**
     * The private constructor that is used to avoid the creation of any other
     * instances but the singleton instance.
     */
    private PathfinderCrashHandler() {
        super();
    }

    /**
     * Get the singleton instance of this class.
     *
     * @return the singleton instance of this class
     */
    @Nonnull
    public static PathfinderCrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Get the message that describes the problem human readable.
     *
     * @return the error message
     */
    @Nonnull
    @SuppressWarnings("nls")
    @Override
    protected String getCrashMessage() {
        return "crash.pathfinder";
    }

    /**
     * Prepare everything for a proper restart of the pathfinder.
     *
     * @return <code>true</code> in case a restart of the connection is needed
     */
    @Override
    protected boolean restart() {
        Pathfinder.restartPathfinder();

        return false;
    }
}
