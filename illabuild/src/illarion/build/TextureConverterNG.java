/*
 * This file is part of the Illarion Build Utility.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Build Utility is free software: you can redistribute and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * The Illarion Build Utility is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with the Illarion Build Utility. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package illarion.build;

import illarion.build.imagepacker.ImagePacker;
import illarion.common.data.Book;
import illarion.common.util.Crypto;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Manifest.Attribute;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

/**
 * This converter mainly converts the PNG image files into a format optimized for OpenGL, in order to improve the speed
 * of loading the client. It also put the texture images together to image maps. And it checks the contents of the
 * archives and removes useless contents.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class TextureConverterNG
        extends Task {
    /**
     * The identifier of the texture format.
     */
    private static final String TEXTURE_FORMAT = "png";

    /**
     * This is a small helper class used to ensure that the directory and the name of the file remain separated in a
     * specific manner.
     *
     * @author Martin Karing &lt;nitram@illarion.org&gt;
     */
    public static final class FileEntry {
        /**
         * The directory.
         */
        private final File directory;

        /**
         * The name of the file
         */
        private final String fileName;

        /**
         * Create a new file entry that stores directory and filename separated.
         *
         * @param dir  the directory
         * @param file the file
         */
        public FileEntry(final File dir, final String file) {
            directory = dir;
            fileName = file;
        }

        /**
         * Get the directory of this file entry.
         *
         * @return the directory
         */
        public File getDirectory() {
            return directory;
        }

        /**
         * Get the full file construct that points to the file described by this entry.
         *
         * @return the newly created file construct
         */
        @Nonnull
        public File getFile() {
            return new File(directory, fileName);
        }

        /**
         * Get the filename of this file entry.
         *
         * @return the filename
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Strip a part of the directory from the filename.
         *
         * @param stripDir the part of the directory to strip away
         * @return the new file entry
         */
        @Nonnull
        public FileEntry stripDirectory(final String stripDir) {
            return new FileEntry(new File(directory, stripDir), fileName.replace(stripDir, "")); //$NON-NLS-1$
        }
    }

    /**
     * The file names of the book files that were found but not handled yet.
     */
    @Nonnull
    private final List<FileEntry> bookFiles;

    /**
     * Crypto instance used to crypt the table files.
     */
    @Nonnull
    private final Crypto crypto;

    /**
     * The file lists that are supposed to be converted.
     */
    @Nonnull
    private final List<FileList> filelists;

    /**
     * The filename of the file that is converted used in case the images are dumbed to the file system.
     */
    @Nullable
    private String fileName = null;

    /**
     * The file sets that are supposed to be converted.
     */
    @Nonnull
    private final List<FileSet> filesets;

    /**
     * The manifest that is put into the target file.
     */
    private Manifest man;

    /**
     * The file names of the misc files that were found but not handled yet.
     */
    @Nonnull
    private final List<FileEntry> miscFiles;

    /**
     * The file that contains the private key to use
     */
    private File privateKeyFile;

    /**
     * Print a filelist to the system.
     */
    private boolean showFileList = false;

    /**
     * The file names of the table files that were found but not handled yet.
     */
    @Nonnull
    private final List<FileEntry> tableFiles;

    /**
     * The jar source file that will be processed.
     */
    private File targetFile;

    /**
     * The file names of texture files that were found in the list and were not handled yet.
     */
    @Nonnull
    private final List<FileEntry> textureFiles;

    /**
     * The file names of texture files that shall be get included into a texture pack and rather get a texture alone.
     */
    @Nonnull
    private final List<FileEntry> textureNoPackFiles;

    /**
     * Constructor of the Texture converter. Sets up all needed variables for the proper conversion to the OpenGL
     * texture format.
     */
    public TextureConverterNG() {
        crypto = new Crypto();
        tableFiles = new ArrayList<FileEntry>();
        bookFiles = new ArrayList<FileEntry>();
        miscFiles = new ArrayList<FileEntry>();
        textureNoPackFiles = new ArrayList<FileEntry>();
        textureFiles = new ArrayList<FileEntry>();
        filesets = new ArrayList<FileSet>();
        filelists = new ArrayList<FileList>();
    }

    /**
     * Converts an integer value into a byte array.
     *
     * @param value the integer value that should be converted to a byte array
     * @return the byte array created
     */
    @Nonnull
    public static final byte[] intToByteArray(final int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    /**
     * Main method to start this converter. It expects one archive file starting with raw_ as argument.
     *
     * @param args The name of the archive file starting with raw_. A file with the replaced header rsc_ will be
     *             created
     *             that contains the new data.
     */
    @SuppressWarnings("nls")
    public static void main(@Nonnull final String[] args) {
        final TextureConverterNG converter = new TextureConverterNG();
        for (final String arg : args) {
            if (arg.contains("filelist")) {
                converter.setDumpFilelist(true);
            } else {
                converter.setTarget(new File(arg));
            }
        }

        if (!converter.hasTarget()) {
            return;
        }
        converter.convert();
    }

    /**
     * Set the manifest that is supposed to be used in the target file.
     *
     * @param manifest the manifest
     */
    public void addConfiguredManifest(final Manifest manifest) {
        man = manifest;
    }

    /**
     * Adds a list of files to process.
     *
     * @param list a list of files to process
     */
    public void addFilelist(final FileList list) {
        filelists.add(list);
    }

    /**
     * Adds a set of files to process.
     *
     * @param set a set of files to process
     */
    public void addFileset(final FileSet set) {
        filesets.add(set);
    }

    /**
     * Set the manifest that is supposed to be used in the target file.
     *
     * @param manifest the manifest
     */
    public void addManifest(final Manifest manifest) {
        man = manifest;
    }

    /**
     * Execute the task and pack the jar files that are specified.
     */
    @Override
    public void execute()
            throws BuildException {
        validate();
        convert();
    }

    /**
     * Check if the target of the file operation is set.
     *
     * @return <code>true</code> if the source file is set
     */
    public boolean hasTarget() {
        return (targetFile != null);
    }

    /**
     * Set the dump file list flag. This causes that all processed files are written to the console.
     *
     * @param value the dump file list task
     */
    public void setDumpFilelist(final boolean value) {
        showFileList = value;
    }

    /**
     * Set the manifest that is supposed to be used in the target file.
     *
     * @param manifest the manifest
     */
    public void setManifest(final Manifest manifest) {
        man = manifest;
    }

    /**
     * Set the file that contains the private key used to encrypt the data
     *
     * @param file the file with the private key
     */
    public void setPrivateKey(final File file) {
        privateKeyFile = file;
    }

    /**
     * Set the file that will be used to store all data.
     *
     * @param file target file
     */
    public void setTarget(@Nonnull final File file) {
        targetFile = file;
        fileName = file.getName().replace("raw_", "").replace("rsc_", "") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                // //$NON-NLS-4$
                .replace(".jar", "").replace(".tmp", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Read one file and put it into the file list based on its file name.
     *
     * @param directory the directory of the file
     * @param filename  the name of the file
     */
    private void analyseAndOrderFile(final File directory, @Nonnull final String filename) {
        final String cleanFileName = filename.replace('\\', '/');
        final FileEntry entry = new FileEntry(directory, cleanFileName);

        if (cleanFileName.contains("notouch_") || cleanFileName.contains("mouse_cursors")) {
            miscFiles.add(entry);
        } else if (cleanFileName.endsWith(".png")) { //$NON-NLS-1$
            if (cleanFileName.contains("nopack_")) { //$NON-NLS-1$
                textureNoPackFiles.add(entry);
            } else {
                textureFiles.add(entry);
            }
        } else if (cleanFileName.endsWith(".tbl")) { //$NON-NLS-1$
            tableFiles.add(entry);
        } else if (cleanFileName.endsWith(".book.xml")) { //$NON-NLS-1$
            bookFiles.add(entry);
        } else if (cleanFileName.startsWith("META-INF")) { //$NON-NLS-1$
            return;
        } else {
            miscFiles.add(entry);
        }
    }

    /**
     * Analyze all set files and order them into the processing lists
     *
     * @throws BuildException in case anything goes wrong
     * @throws IOException    in case dumping the file list fails
     */
    @SuppressWarnings("nls")
    private void buildFileList()
            throws BuildException, IOException {
        for (final FileSet fileset : filesets) {
            final DirectoryScanner ds = fileset.getDirectoryScanner(getProject());

            final File dir = fileset.getDir(getProject());
            for (final String file : ds.getIncludedFiles()) {
                analyseAndOrderFile(dir, file);
            }
        }

        for (final FileList filelist : filelists) {
            final File dir = filelist.getDir(getProject());
            for (final String file : filelist.getFiles(getProject())) {
                analyseAndOrderFile(dir, file);
            }
        }

        if (showFileList) {
            final File ausgabedatei = new File(fileName + ".txt");
            final FileWriter fw = new FileWriter(ausgabedatei);
            final BufferedWriter bw = new BufferedWriter(fw);
            final PrintWriter pw = new PrintWriter(bw);
            pw.println("Table Files:");
            for (final FileEntry entry : tableFiles) {
                pw.println("-- " + entry.getFileName());
            }
            pw.println();
            pw.println("Misc Files:");
            for (final FileEntry entry : miscFiles) {
                pw.println("-- " + entry.getFileName());
            }
            pw.println();
            pw.println("Texture Files:");
            for (final FileEntry entry : textureFiles) {
                pw.println("-- " + entry.getFileName());
            }
            pw.println();
            pw.println("Not packed Files:");
            for (final FileEntry entry : textureNoPackFiles) {
                pw.println("-- " + entry.getFileName());
            }
            pw.println();
            pw.println("Book Files:");
            for (final FileEntry entry : bookFiles) {
                pw.println("-- " + entry.getFileName());
            }
            pw.close();
        }
    }

    /**
     * Starting function of the converter.
     *
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    @SuppressWarnings("nls")
    private void convert() {
        JarOutputStream outJar = null;
        try {
            // build the filelists
            buildFileList();
            System.out.println("File list done");
            System.out.println("Misc: " + miscFiles.size());
            System.out.println("Table: " + tableFiles.size());
            System.out.println("Texture: " + textureFiles.size());
            System.out.println("Book: " + bookFiles.size());

            if (man == null) {
                man = new Manifest();
            }
            try {
                man.getMainSection().addAttributeAndCheck(new Attribute("Images-packed-by",
                        "Illarion TextureConverterNG 1.3"));

                man.addConfiguredAttribute(new Attribute(Manifest.ATTRIBUTE_MANIFEST_VERSION,
                        Manifest.DEFAULT_MANIFEST_VERSION));
            } catch (@Nonnull final ManifestException e) {
                // ignore
            }

            // open the output filestream
            outJar = new JarOutputStream(new FileOutputStream(targetFile));
            outJar.setLevel(3);
            outJar.setMethod(ZipEntry.DEFLATED);

            outJar.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            final PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outJar));
            man.write(pWriter);
            pWriter.flush();
            outJar.closeEntry();

            // write the table files
            writeTableFiles(outJar);
            System.out.println("tablefiles done!!!");

            // write the texture files
            writeTextureFiles(outJar);
            System.out.println("texturefiles done");

            // write the texture files that do not get packed
            writeTextureNoPackFiles(outJar);
            System.out.println("not packed texturefiles done");

            // write the misc files
            writeMiskFiles(outJar);
            System.out.println("Miscfiles done");

            writeBookFiles(outJar);
            System.out.println("Bookfiles done");
        } catch (@Nonnull final FileNotFoundException e) {
            System.out.println("ERROR: File " + targetFile.getAbsolutePath() + " was not found, stopping converter");
            return;
        } catch (@Nonnull final IOException e) {
            System.out.println("ERROR: File " + targetFile.getName() + " was not readable, stopping converter");
            return;
        } finally {
            if (outJar != null) {
                try {
                    outJar.close();
                } catch (@Nonnull final IOException e) {
                    // closing failed
                }
            }
        }
    }

    /**
     * Check if the settings of this task are good to be executed.
     *
     * @throws BuildException in case anything at the settings for this task is wrong
     */
    @SuppressWarnings("nls")
    private void validate()
            throws BuildException {
        if (targetFile == null) {
            throw new BuildException("a file is needed");
        }
    }

    /**
     * Write the book files to the new archive. This simply checks if the book files can be properly read into the
     * data structures without errors and considers them valid then. Doing so should prevent the client to run into
     * any trouble when loading the book files.
     *
     * @param outJar the target archive the compressed book files are written to
     * @throws IOException in case there is anything wrong with the input or the output file stream
     */
    @SuppressWarnings("nls")
    private void writeBookFiles(@Nonnull final JarOutputStream outJar)
            throws BuildException {
        if (bookFiles.isEmpty()) {
            return;
        }

        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        for (final FileEntry fileEntry : bookFiles) {
            FileChannel inChannel = null;
            try {
                final Document document = docBuilderFactory.newDocumentBuilder().parse(fileEntry.getFile());
                final Book book = new Book(document);
                System.out.println("Book " + fileEntry.getFileName() + " read with "
                        + Integer.toString(book.getEnglishBook().getPageCount()) + " pages");

                final JarEntry dstEntry = new JarEntry(fileEntry.getFileName());
                dstEntry.setMethod(ZipEntry.DEFLATED);

                // write data to zip
                outJar.putNextEntry(dstEntry);

                inChannel = new FileInputStream(fileEntry.getFile()).getChannel();
                final WritableByteChannel outChannel = Channels.newChannel(outJar);

                final long size = inChannel.size();
                final long maxSize = Math.min(size, 67076096);
                long position = 0;
                while (position < size) {
                    position += inChannel.transferTo(position, maxSize, outChannel);
                }
                outJar.closeEntry();
            } catch (@Nonnull final Exception e) {
                throw new BuildException(e);
            } finally {
                if ((inChannel != null) && inChannel.isOpen()) {
                    try {
                        inChannel.close();
                    } catch (@Nonnull final IOException e) {
                        // nothing to do
                    }
                }
            }
        }
        bookFiles.clear();
    }

    /**
     * Write all files stored in {@link #miscFiles} into the output file stream without changing them.
     *
     * @param outJar the output stream the new data entries are stored in.
     * @throws IOException in case there is anything wrong with the input or the output file stream
     */
    @SuppressWarnings("nls")
    private void writeMiskFiles(@Nonnull final JarOutputStream outJar)
            throws BuildException {
        if (miscFiles.isEmpty()) {
            System.gc();
            return;
        }

        for (final FileEntry fileEntry : miscFiles) {
            FileChannel inChannel = null;
            try {
                final JarEntry entry = new JarEntry(fileEntry.getFileName().replace("notouch_", ""));
                entry.setMethod(ZipEntry.DEFLATED);
                outJar.putNextEntry(new JarEntry(entry));

                inChannel = new FileInputStream(fileEntry.getFile()).getChannel();
                final WritableByteChannel outChannel = Channels.newChannel(outJar);

                final long size = inChannel.size();
                final long maxSize = Math.min(size, 67076096);
                long position = 0;
                while (position < size) {
                    position += inChannel.transferTo(position, maxSize, outChannel);
                }
                outJar.closeEntry();
            } catch (@Nonnull final IOException e) {
                throw new BuildException(e);
            } finally {
                if ((inChannel != null) && inChannel.isOpen()) {
                    try {
                        inChannel.close();
                    } catch (@Nonnull final IOException e) {
                        // nothing to do
                    }
                }
            }
        }
    }

    /**
     * Encrypt and write the table files to the new archive.
     *
     * @param outJar the target archive the encrypted table files are written to
     * @throws IOException in case there is anything wrong with the input or the output file stream
     */
    @SuppressWarnings("nls")
    private void writeTableFiles(@Nonnull final JarOutputStream outJar)
            throws BuildException {

        if (tableFiles.isEmpty()) {
            return;
        }

        if (privateKeyFile != null) {
            try {
                crypto.loadPrivateKey(new FileInputStream(privateKeyFile));
            } catch (@Nonnull final FileNotFoundException e) {
                // did not work
            }
        }
        if (!crypto.hasPrivateKey()) {
            crypto.loadPrivateKey();
        }

        for (final FileEntry fileEntry : tableFiles) {
            final File currentFile = fileEntry.getFile();
            InputStream in = null;
            try {
                in = new BufferedInputStream(new FileInputStream(currentFile));
                final ByteArrayOutputStream dst = new ByteArrayOutputStream((int) currentFile.length());

                crypto.encrypt(in, dst);
                final byte[] outBuf = dst.toByteArray();

                final JarEntry dstEntry = new JarEntry(fileEntry.getFileName().replace(".tbl", ".dat"));
                dstEntry.setMethod(ZipEntry.STORED);
                dstEntry.setSize(outBuf.length);

                // build crc
                final CRC32 crc = new CRC32();
                crc.update(outBuf);
                dstEntry.setCrc(crc.getValue());

                // write data to zip
                outJar.putNextEntry(dstEntry);
                outJar.write(outBuf);
                outJar.closeEntry();

            } catch (@Nonnull final Exception e) {
                throw new BuildException(e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (@Nonnull final IOException e) {
                        // nothing
                    }
                }
            }
        }
        tableFiles.clear();
    }

    private static int packTextures(@Nonnull final JarOutputStream outJar, final String folder, @Nonnull final ImagePacker packer,
                                    @Nullable final String filename) {
        int atlasFiles = 0;
        String usedFileName;
        while (!packer.isEverythingDone()) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.newDocument();
                Node rootNode = document.createElement("sprites");
                document.appendChild(rootNode);

                BufferedImage result = null;
                if ((result = packer.packImages(document, rootNode)) == null) {
                    break;
                }

                if (filename == null) {
                    usedFileName = folder + "atlas-" + atlasFiles;
                } else {
                    usedFileName = folder + filename;
                }

                outJar.putNextEntry(new JarEntry(usedFileName + "." + TEXTURE_FORMAT));
                ImageIO.write(result, TEXTURE_FORMAT, outJar);
                outJar.closeEntry();

                outJar.putNextEntry(new JarEntry(usedFileName + ".xml"));
                // Prepare the DOM document for writing
                Source source = new DOMSource(document);
                Result xmlResult = new StreamResult(outJar);

                // Write the DOM document to the file
                Transformer xformer = TransformerFactory.newInstance().newTransformer();
                xformer.transform(source, xmlResult);
                outJar.closeEntry();

                atlasFiles++;
            } catch (@Nonnull final IOException e) {
                e.printStackTrace();
                throw new BuildException(e);
            } catch (@Nonnull final Exception e) {
                e.printStackTrace();
                throw new BuildException(e);
            }
        }

        return atlasFiles;
    }

    /**
     * Pack the textures files together, convert them for OpenGL and store them in the the new archive file.
     *
     * @param outJar the target archive the encrypted table files are written t
     */
    @SuppressWarnings("nls")
    private void writeTextureFiles(@Nonnull final JarOutputStream outJar)
            throws BuildException {
        if (textureFiles.isEmpty()) {
            return;
        }

        final ImagePacker packer = new ImagePacker();
        final String folder = "data/" + fileName + "/";

        for (final FileEntry fileEntry : textureFiles) {
            packer.addImage(fileEntry.stripDirectory(folder));
        }
        textureFiles.clear();

        packer.printTypeCounts();

        int atlasFiles = packTextures(outJar, folder, packer, null);

        DataOutputStream stream;
        try {
            outJar.putNextEntry(new JarEntry(folder + "atlas.count"));
            stream = new DataOutputStream(outJar);
            stream.writeInt(atlasFiles);
            stream.flush();
        } catch (@Nonnull final IOException e) {
            e.printStackTrace();
            throw new BuildException(e);
        } catch (@Nonnull final Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        } finally {
            try {
                outJar.closeEntry();
                outJar.flush();
            } catch (IOException e) {
                e.printStackTrace();
                throw new BuildException(e);
            }
        }
    }

    /**
     * Convert the texture files for OpenGL and store them in the the new archive file.
     *
     * @param outJar the target archive the encrypted table files are written to
     * @throws BuildException In case anything goes wrong
     */
    @SuppressWarnings("nls")
    private void writeTextureNoPackFiles(@Nonnull final JarOutputStream outJar)
            throws BuildException {
        if (textureNoPackFiles.isEmpty()) {
            return;
        }

        final ImagePacker packer = new ImagePacker();

        final String folder = "data/" + fileName + "/";

        for (final FileEntry fileEntry : textureNoPackFiles) {
            packer.addImage(fileEntry.stripDirectory(folder));

            String filename = fileEntry.stripDirectory(folder).getFileName();
            filename = filename.replace("notouch_", "");
            filename = filename.substring(0, filename.lastIndexOf('.'));

            packTextures(outJar, folder, packer, filename);
        }
        textureNoPackFiles.clear();
    }

}
