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
package illarion.download.install.resources.db;

import illarion.common.util.DirectoryManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

/**
 * This class represents a single file that was extracted from the downloaded resource files. It contains the
 * information and the logic to perform a secure check if the file is valid.
 *
 * @author Martin Karing
 * @version 1.01
 * @since 1.01
 */
public final class DBFile implements Externalizable {
    /**
     * The current version for the serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The checksum data of the file. That is only used to perform a detailed check on the content of the file.
     */
    private long checksum;

    /**
     * The actual file in the file system that is stored using this database file.
     */
    private File file;

    /**
     * The date when this file was last changed.
     */
    private long lastChangeDate;

    /**
     * Constructor for deserialization.
     */
    public DBFile() {
        // nothing
    }

    /**
     * Create a database entry for a new file.
     *
     * @param fileEntry the file this db entry represents
     */
    @SuppressWarnings("nls")
    public DBFile(@Nonnull final File fileEntry) {
        final String localFileName = fileEntry.getAbsolutePath();
        final String dataDirectory = DirectoryManager.getInstance().getDataDirectory().getAbsolutePath();
        if (!localFileName.startsWith(dataDirectory)) {
            throw new IllegalArgumentException("File is not inside the data directory.");
        }
        file = fileEntry;
        checksum = generateChecksum(fileEntry);
        lastChangeDate = file.lastModified();
    }

    /**
     * Generate the checksum from a file.
     *
     * @param file the file to generate the checksum from
     * @return the generated checksum
     */
    @SuppressWarnings("nls")
    private static long generateChecksum(@Nullable final File file) {
        if (file == null) {
            throw new IllegalStateException("Can't generate a checksum without a file");
        }

        CheckedInputStream cis = null;
        long checksum = 0L;
        try {
            cis = new CheckedInputStream(new FileInputStream(file), new Adler32());
            final byte[] tempBuf = new byte[128];
            while (cis.read(tempBuf) >= 0) {
                // nothing to do, just read the stream and generate the
                // checksum.
            }
        } catch (@Nonnull final IOException e) {
            // failed to read the stream.
        } finally {
            if (cis != null) {
                checksum = cis.getChecksum().getValue();
                try {
                    cis.close();
                } catch (@Nonnull final IOException e) {
                    // failed to close the stream, does not matter.
                }
            }
        }

        return checksum;
    }

    /**
     * Check if this database file is equal to another.
     *
     * @return <code>true</code> if, and only if the parameter is a object of the type DBFile and both DBFile entries
     *         point to the same file in the file system.
     */
    @Override
    public boolean equals(@Nullable final Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof DBFile)) {
            return false;
        }

        return ((DBFile) o).file.equals(file);
    }

    /**
     * Check if the file exists in the data directory.
     *
     * @return {@code true} if the file exists
     */
    public boolean exists() {
        return file.exists() && file.isFile();
    }

    /**
     * Generate the hash code of the object.
     */
    @Override
    public int hashCode() {
        return file.hashCode();
    }

    /**
     * Check if the file in the file system is up to date related to the file stored in this database file.
     *
     * @return {@code true} if the file is up to date
     */
    public boolean isUpToDate() {
        return lastChangeDate == file.lastModified();
    }

    /**
     * Check if the checksum of the file in the file system equals the checksum stored in this file entry.
     *
     * @return {@code true} if the checksum fits
     */
    public boolean hasValidChecksum() {
        return checksum == generateChecksum(file);
    }

    @Override
    public void writeExternal(@Nonnull final ObjectOutput out) throws IOException {
        out.writeLong(serialVersionUID);
        out.writeLong(lastChangeDate);
        out.writeLong(checksum);

        final String localFileName = file.getAbsolutePath();
        final String dataDirectory = DirectoryManager.getInstance().getDataDirectory().getAbsolutePath();

        out.writeObject(localFileName.replace(dataDirectory, ""));
    }

    @Override
    public void readExternal(@Nonnull final ObjectInput in) throws IOException, ClassNotFoundException {
        final long version = in.readLong();
        if (version == 1L) {
            lastChangeDate = in.readLong();
            checksum = in.readLong();

            final String path = (String) in.readObject();
            final String dataDirectory = DirectoryManager.getInstance().getDataDirectory().getAbsolutePath();
            file = new File(dataDirectory + path);
        } else {
            throw new ClassNotFoundException("Invalid file version.");
        }
    }
}
