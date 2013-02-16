/*
 * This file is part of the Illarion Download Utility.
 *
 * Copyright © 2013 - Illarion e.V.
 *
 * The Illarion Download Utility is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Download Utility is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Download Utility.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.download.tasks.clean;

import illarion.download.tasks.unpack.Unpack;
import illarion.download.tasks.unpack.UnpackCallback;
import illarion.download.tasks.unpack.UnpackResult;

import javax.annotation.Nonnull;

/**
 * This cleaner is a small helper tasks that removes all files that were created
 * during the installation and are not needed any longer.
 *
 * @author Martin Karing
 * @version 1.00
 * @since 1.00
 */
public final class Cleaner implements UnpackCallback {
    /**
     * This is the notification that causes the cleaner to kick in. It will
     * schedule the files that were created and are not used any longer for
     * removal.
     *
     * @param unpack the unpack task
     * @param result the result of the unpack operation and the source of the
     *               data that is needed for this task
     */
    @Override
    public void reportUnpackFinished(final Unpack unpack,
                                     @Nonnull final UnpackResult result) {
        if (!result.getTarget().delete()) {
            result.getTarget().deleteOnExit();
        }
    }

    /**
     * Nothing is done here. {@inheritDoc}
     */
    @Override
    public void reportUnpackProgress(final Unpack unpack,
                                     final long bytesDone, final long bytesTotal) {
        // nothing
    }

}
