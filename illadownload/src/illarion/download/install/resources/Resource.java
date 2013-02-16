/*
 * This file is part of the Illarion Download Utility.
 *
 * Copyright © 2012 - Illarion e.V.
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
package illarion.download.install.resources;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.util.Collection;

/**
 * This class in general contains informations what resources need to be
 * downloaded and how the applications are launched.
 *
 * @author Martin Karing
 * @version 1.00
 * @since 1.00
 */
public interface Resource {
    /**
     * The hostname of the webserver of Illarion.
     */
    @SuppressWarnings("nls")
    String ILLARION_HOST = "http://illarion.org";

    /**
     * The file extension of the resource files that need to be downloaded.
     */
    @SuppressWarnings("nls")
    String RESSOURCE_FILE_EXT = ".illares";

    /**
     * Get the class path entries that are needed to use this resource. This is
     * a collection of names of jar files that need to be included to the class
     * path.
     *
     * @return the list of needed jar files
     */
    @Nullable
    Collection<File> getClassPath();

    /**
     * Get the dependencies of this resource. All dependencies need to be
     * downloaded to get this resource working.
     *
     * @return the collection of required dependencies or <code>null</code> in
     *         case there are no dependencies
     */
    @Nullable
    Collection<Resource> getDependencies();

    /**
     * Get the full path to the class that is called in order to launch this
     * application. This function must not be called in case the resource is not
     * start-able.
     *
     * @return the path to the class that needs to be started
     * @throws IllegalStateException in case {@link #isStartable()} is set to
     *                               <code>false<code> for this resource
     */
    @Nonnull
    String getLaunchClass();

    /**
     * Get the human readable identifier of this resource.
     *
     * @return the name of this resource
     */
    String getName();

    /**
     * Get the program arguments this resource applies to the java call.
     *
     * @return a collection with one argument in each entry, or
     *         <code>null</code> in case no arguments are needed
     */
    @Nullable
    Collection<String> getProgramArgument();

    /**
     * Get the URLs that are needed to get downloaded in order to get this
     * resource running.
     *
     * @return the collection of URLs needed for this resource
     */
    @Nullable
    Collection<URL> getRequiredResources();

    /**
     * This function has to return the sub directory the resource is supposed to
     * be located in. When unpacking the resource, the resulting files will be
     * placed in this directory.
     *
     * @return the directory to locate the resources in
     */
    String getSubDirectory();

    /**
     * Get the VM arguments this resource applies to the java call.
     *
     * @return a collection with one VM argument in each entry, or
     *         <code>null</code> in case no arguments are needed
     */
    @Nullable
    Collection<String> getVMArguments();

    /**
     * Check if this class can be started.
     *
     * @return <code>true</code> in case it is possible to start that
     *         application
     */
    boolean isStartable();
}
