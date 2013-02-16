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
package illarion.download.tasks.download;

import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.Callable;

/**
 * This class describes a single download and is used to download a file and
 * monitor the download.
 *
 * @author Martin Karing
 * @version 1.01
 * @since 1.00
 */
public final class Download implements Callable<DownloadResult> {
    /**
     * This constant stores of the download is supposed to work properly or of
     * all download are just supposed to be ignored.
     */
    @SuppressWarnings("nls")
    private static final boolean DO_NOT_DOWNLOAD = Boolean.toString(true)
            .equalsIgnoreCase(
                    System.getProperty("illarion.download.nodownload",
                            Boolean.FALSE.toString()));

    /**
     * This flag is set true in case the download is canceled.
     */
    private boolean canceled;

    /**
     * The directory the downloaded files need to be extracted to.
     */
    private final String directory;

    /**
     * This variables stores the timestamp of the time the local file was last
     * modified. In case the file only was not modified since this date, the
     * file transfer will not happen.
     */
    private final long lastModified;

    /**
     * The manager that handles this download.
     */
    private final DownloadManager manager;

    /**
     * The title of this download.
     */
    private final String name;

    /**
     * The URL that was downloaded.
     */
    private final URL source;

    /**
     * The target file that is supposed to store the data that was downloaded.
     */
    private final File target;

    /**
     * This instance of the logger takes care for the logging output of this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Download.class);

    /**
     * Create a new instance of a download. This object will download one file.
     *
     * @param title             the name of this download
     * @param dir               the directory the files need to be extraced to
     * @param sourceURL         the URL to download
     * @param targetFile        the target file where the downloaded data is to store
     *                          in
     * @param localLastModified the last modified value, only download the file
     *                          in case it was modified later
     * @param callbackManager   the manager that handles this download
     */
    Download(final String title, final String dir, final URL sourceURL,
             final File targetFile, final long localLastModified,
             final DownloadManager callbackManager) {
        name = title;
        source = sourceURL;
        target = targetFile;
        manager = callbackManager;
        lastModified = localLastModified;
        directory = dir;
    }

    @Nullable
    @Override
    public DownloadResult call() throws IOException {
        DownloadResult retVal = null;

        while (retVal == null) {
            retVal = callImpl();
        }

        manager.reportDownloadFinished(this, retVal);
        return retVal;
    }

    /**
     * Cancel the download the next time its possible.
     */
    public void cancelDownload() {
        canceled = true;
    }

    /**
     * Get the directory the files need to be extraced to.
     *
     * @return the directory the file needs to be extracted to
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Get the title of this download.
     *
     * @return the title of this download
     */
    public String getName() {
        return name;
    }

    /**
     * Get the source URL of this download.
     *
     * @return the source URL of this download
     */
    public URL getSource() {
        return source;
    }

    /**
     * Get the target file of this download.
     *
     * @return the target file of this download
     */
    public File getTarget() {
        return target;
    }

    /**
     * Prepare this download and fetch the first needed informations about it.
     *
     * @return true in case the download needs to be executed in detail
     */
    @SuppressWarnings("nls")
    public boolean prepare() {
        if (DO_NOT_DOWNLOAD || !manager.isConnected()) {
            if (lastModified == 0L) {
                manager.reportDownloadFinished(this,
                        new DownloadResult(DownloadResult.Results.downloadFailed, "download.no_connection",
                                source, target, 0L, "No connection"));
            } else {
                manager.reportDownloadFinished(this,
                        new DownloadResult(DownloadResult.Results.notModified, "download.not_modified",
                                source, target, lastModified));
            }
            return false;
        }
        try {
            final URLConnection connection = source.openConnection();
            connection.setIfModifiedSince(lastModified);
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.setDoInput(true);

            connection.connect();

            if (connection instanceof HttpURLConnection) {
                final HttpURLConnection httpConn =
                        (HttpURLConnection) connection;
                switch (httpConn.getResponseCode()) {
                    case HttpURLConnection.HTTP_NOT_MODIFIED:
                        try {
                            httpConn.getInputStream().close();
                        } catch (@Nonnull final IOException ex) {
                            // nothing to do
                        }
                        if (target.exists()) {
                            manager.reportDownloadFinished(this,
                                    new DownloadResult(DownloadResult.Results.downloaded, "download.not_modified",
                                            source, target, lastModified));
                        } else {
                            manager.reportDownloadFinished(this,
                                    new DownloadResult(DownloadResult.Results.notModified, "download.not_modified",
                                            source, target, lastModified));
                        }
                        return false;
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_PARTIAL:
                        break;
                    default:
                        try {
                            httpConn.getInputStream().close();
                        } catch (@Nonnull final IOException ex) {
                            // nothing to do
                        }
                        manager.reportDownloadFinished(this,
                                new DownloadResult(DownloadResult.Results.downloadFailed, "download.not_found",
                                        source, target, 0L, "Download Error"));
                        return false;
                }
            }

            final long length = Math.max(0, connection.getContentLength());
            manager.reportProgress(this, 0L, length);

            connection.getInputStream().close();
        } catch (@Nonnull final IOException ex) {
            manager.reportDownloadFinished(this, new DownloadResult(
                    DownloadResult.Results.downloadFailed, "download.not_found",
                    source, target, 0L, ex.toString()));
            return false;
        }
        return true;
    }

    /**
     * This is the private function that is called as the call function that
     * takes care for downloading the file.
     *
     * @return the result of the download
     * @throws IOException in case the download or storing the download data fails
     */
    @Nullable
    @SuppressWarnings("nls")
    private DownloadResult callImpl() throws IOException {
        long transferred = 0L;
        if (target.exists()) {
            if (!target.canWrite() || !target.isFile()) {
                if (!target.delete()) {
                    return new DownloadResult(DownloadResult.Results.downloadFailed, "download.invalid_target", source,
                            target, 0L, "Target file is locked.");
                }
            } else {
                transferred = target.length();
            }
        }

        URLConnection connection = null;
        ReadableByteChannel inChannel = null;
        FileChannel fileChannel = null;
        long onlineFileLastMod = 0L;
        try {
            connection = source.openConnection();
            connection.setIfModifiedSince(lastModified);
            connection.setUseCaches(true);
            connection.setDoOutput(false);
            connection.setDoInput(true);

            if (transferred > 0L) {
                connection.setRequestProperty("Range", "bytes=" + transferred + '-');
                LOGGER.info("Continue download: " + source.toString());
            }

            connection.connect();

            final long length = Math.max(0, connection.getContentLength()) + transferred;

            if (length == 0) {
                connection.getInputStream().close();
                connection.getOutputStream().close();
                return null;
            }

            manager.reportProgress(this, 0L, length);

            onlineFileLastMod = connection.getLastModified();
            if ((transferred == length) && target.exists() && (target.lastModified() >= onlineFileLastMod)) {
                return new DownloadResult(DownloadResult.Results.downloaded, "download.done", source, target,
                        onlineFileLastMod);
            }

            if (connection instanceof HttpURLConnection) {
                final HttpURLConnection httpConn = (HttpURLConnection) connection;
                switch (httpConn.getResponseCode()) {
                    case HttpURLConnection.HTTP_NOT_MODIFIED:
                        try {
                            httpConn.getInputStream().close();
                        } catch (@Nonnull final IOException ex) {
                            // nothing to do
                        }
                        return new DownloadResult(DownloadResult.Results.notModified, "download.not_modified",
                                source, target, lastModified);
                    case HttpURLConnection.HTTP_OK:
                    case HttpURLConnection.HTTP_PARTIAL:
                        break;
                    default:
                        try {
                            httpConn.getInputStream().close();
                        } catch (@Nonnull final IOException ex) {
                            // nothing to do
                        }
                        return new DownloadResult(DownloadResult.Results.downloadFailed, "download.not_found",
                                source, target, 0L, "Download failed");
                }
            }

            inChannel = Channels.newChannel(connection.getInputStream());
            fileChannel = new FileOutputStream(target, true).getChannel();

            long blockLength = length / 100;
            if (blockLength < 1024) {
                blockLength = 1024;
            }

            int noTransferCounter = 0;
            long oldTransferred;
            while (transferred < length) {
                oldTransferred = transferred;
                transferred += fileChannel.transferFrom(inChannel, transferred, Math.min(length - transferred,
                        blockLength));

                if (oldTransferred == transferred) {
                    noTransferCounter++;
                } else {
                    manager.reportProgress(this, transferred, length);
                }

                if (noTransferCounter == 50) {
                    LOGGER.warn("Restarting stalled download: " + name);
                    // transfer seems stalled -> restarting
                    return null;
                }

                if (canceled) {
                    break;
                }
            }
        } finally {
            if ((inChannel != null) && inChannel.isOpen()) {
                inChannel.close();
            }
            if ((fileChannel != null) && fileChannel.isOpen()) {
                fileChannel.close();
            }
            if ((connection != null) && (connection.getInputStream() != null)) {
                connection.getInputStream().close();
            }
        }

        if (!target.setLastModified(onlineFileLastMod + 1000)) {
            return new DownloadResult(DownloadResult.Results.downloadFailed,
                    "download.file_failed", source, target, 0L, "Setting last modified failed.");
        }

        if (canceled) {
            return new DownloadResult(DownloadResult.Results.canceled,
                    "download.canceled", source, target, 0L);
        }
        return new DownloadResult(DownloadResult.Results.downloaded,
                "download.done", source, target, onlineFileLastMod);
    }
}
