/*
 * This file is part of the Illarion Common Library.
 *
 * Copyright © 2013 - Illarion e.V.
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
package illarion.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to handle the encryption of the files that are stored by the client.
 * The encryption created by this class bases on a private and a public key.
 *
 * @author Nop
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class Crypto {
    /**
     * The method that is used for encrypting the files.
     */
    @SuppressWarnings("nls")
    private static final String CRYPTMODE = "DES/ECB/PKCS5Padding";

    /**
     * String for the transformation name "DES"
     */
    private static final String DES = "DES"; //$NON-NLS-1$

    /**
     * The error and debug logger of the client.
     */
    private static final Logger LOGGER = Logger.getLogger(Crypto.class.getName());

    /**
     * The filename of the private key.
     */
    private static final String PRIVATE_KEY = "private.key"; //$NON-NLS-1$

    /**
     * The filename of the public key.
     */
    private static final String PUBLIC_KEY = "public.key"; //$NON-NLS-1$

    /**
     * String for the transformation name "RSA"
     */
    private static final String RSA = "RSA"; //$NON-NLS-1$

    /**
     * The private key instance that was loaded into the this class.
     */
    @Nullable
    private PrivateKey privateKey;

    /**
     * The public key instance that was loaded into the this class.
     */
    @Nullable
    private PublicKey publicKey;

    /**
     * Decrypt a file by using the public key that was prepared with the class
     * constructor.
     *
     * @param src the crypt source file
     * @param dst the decrypted file
     * @return true if all went well, false if something went wrong
     */
    @SuppressWarnings("nls")
    public boolean decrypt(final InputStream src, @Nonnull final OutputStream dst) {
        try {
            final DataInputStream in = new DataInputStream(src);
            final int length = in.readInt();
            final byte[] wrappedKey = new byte[length];
            int readPos = 0;
            while (readPos < length) {
                readPos += in.read(wrappedKey, readPos, length - readPos);
            }

            // unwrap with RSA key
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.UNWRAP_MODE, publicKey);
            final Key key = cipher.unwrap(wrappedKey, DES, Cipher.SECRET_KEY);

            // decode data
            cipher = Cipher.getInstance(CRYPTMODE);
            cipher.init(Cipher.DECRYPT_MODE, key);

            pumpData(in, dst, cipher);
        } catch (@Nonnull final Exception e) {
            LOGGER.log(Level.SEVERE, "Decryping the resource failed.", e);
            return false;
        }
        return true;
    }

    /**
     * Decrypt a input stream using the public key used by this class.
     *
     * @param src the input stream
     * @return the input stream that supplies the descripted data
     */
    @Nonnull
    @SuppressWarnings("nls")
    public InputStream decryptedStream(final InputStream src) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } catch (@Nonnull final Exception e) {
            LOGGER.log(Level.SEVERE, "Creating Cipher failed.", e);
        }

        return new CipherInputStream(src, cipher);
    }

    /**
     * Encrypt a data stream with the the private key. This is only possible in
     * case the configuration tool constructed this class with both keys, the
     * public key and the private key.
     *
     * @param src data to encrypt
     * @param dst target stream for encryption
     * @throws GeneralSecurityException  in case something went wrong with the
     *                                   encryption
     * @throws NoSuchPaddingException    in case there is something wrong with the
     *                                   selected decryption method
     * @throws InvalidKeyException       in case the private key is invalid
     * @throws IllegalBlockSizeException in case the streams are corrupted
     * @throws IOException               in case something failed at reading or writing the
     *                                   data
     */
    @SuppressWarnings("nls")
    public void encrypt(@Nonnull final InputStream src, final OutputStream dst)
            throws GeneralSecurityException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, IOException {
        if (privateKey == null) {
            throw new IllegalStateException("No keys loaded");
        }

        // generate a random DES key
        final KeyGenerator keygen = KeyGenerator.getInstance(DES);
        final SecureRandom random = new SecureRandom();
        keygen.init(random);
        final SecretKey key = keygen.generateKey();

        // wrap with RSA public key
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.WRAP_MODE, privateKey);
        final byte[] wrappedKey = cipher.wrap(key);
        final DataOutputStream out = new DataOutputStream(dst);
        out.writeInt(wrappedKey.length);
        out.write(wrappedKey);

        // encrypt data
        cipher = Cipher.getInstance(CRYPTMODE);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        pumpData(src, out, cipher);
    }

    /**
     * Encrypt a stream using the public key used by this class.
     *
     * @param src the output stream
     * @return the output stream that takes the unencrypted data and forwards it
     *         to the output stream set with the parameter
     */
    @Nonnull
    @SuppressWarnings("nls")
    public OutputStream encryptedStream(final OutputStream src) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (@Nonnull final Exception e) {
            LOGGER.log(Level.SEVERE, "Creating Cipher failed.", e);
        }

        return new CipherOutputStream(src, cipher);
    }

    /**
     * Check if this class has a private key that can be used to encrypt data.
     *
     * @return <code>true</code> in case there is a private key loaded
     */
    public boolean hasPrivateKey() {
        return (privateKey != null);
    }

    /**
     * Check if this class has a public key that can be used to decrypt data.
     *
     * @return <code>true</code> in case there is a public key loaded
     */
    public boolean hasPublicKey() {
        return (publicKey != null);
    }

    /**
     * Load the private key from any source available.
     */
    @SuppressWarnings("nls")
    public void loadPrivateKey() {
        try {
            final File keyFile = new File(PRIVATE_KEY);
            if (keyFile.exists() && keyFile.isFile() && keyFile.canRead()
                    && loadPrivateKeyImpl(new FileInputStream(keyFile))) {
                return;
            }
        } catch (@Nonnull final FileNotFoundException e) {
            // the file was not found -> ignore
        }

        if (loadPrivateKeyImpl(Crypto.class.getClassLoader()
                .getResourceAsStream(PRIVATE_KEY))) {
            return;
        }

        try {
            final String keyValue =
                    System.getProperty("illarion.common.crypto.private");
            if ((keyValue != null)
                    && loadPrivateKeyImpl(new FileInputStream(new File(keyValue)))) {
                return;
            }
        } catch (@Nonnull final FileNotFoundException e) {
            // the file was not found -> ignore
        }


        if (loadPrivateKeyImpl(Crypto.class.getClassLoader()
                .getResourceAsStream(System.getProperty("illarion.common.crypto.private")))) {
            return;
        }
        LOGGER.log(Level.SEVERE, "Loading the private key failed.");
    }

    /**
     * Load the private key from a input stream.
     *
     * @param in the input stream the load the private key from
     */
    @SuppressWarnings("nls")
    public void loadPrivateKey(final InputStream in) {
        if (!loadPrivateKeyImpl(in)) {
            LOGGER.log(Level.SEVERE, "Loading the private key failed.");
        }
    }

    /**
     * Load the public key from any source available.
     */
    @SuppressWarnings("nls")
    public void loadPublicKey() {
        try {
            final File keyFile = new File(PUBLIC_KEY);
            if (keyFile.exists() && keyFile.isFile() && keyFile.canRead()
                    && loadPrivateKeyImpl(new FileInputStream(keyFile))) {
                return;
            }
        } catch (@Nonnull final FileNotFoundException e) {
            // the file was not found -> ignore
        }

        if (loadPublicKeyImpl(Crypto.class.getClassLoader()
                .getResourceAsStream(PUBLIC_KEY))) {
            return;
        }

        try {
            final String keyValue =
                    System.getProperty("illarion.common.crypto.public");
            if (keyValue != null) {
                if (loadPublicKeyImpl(Crypto.class.getClassLoader().getResourceAsStream(keyValue))) {
                    return;
                } else if (loadPublicKeyImpl(new FileInputStream(new File(keyValue)))) {
                    return;
                }
            }
        } catch (@Nonnull final FileNotFoundException e) {
            // the file was not found -> ignore
        }

        LOGGER.log(Level.SEVERE, "Loading the public key failed.");
    }

    /**
     * Load the public key from a input stream.
     *
     * @param in the input stream the load the public key from
     */
    @SuppressWarnings("nls")
    public void loadPublicKey(final InputStream in) {
        if (!loadPublicKeyImpl(in)) {
            LOGGER.log(Level.SEVERE, "Loading the public key failed.");
        }
    }

    /**
     * Load the key from a input stream.
     *
     * @param in the input stream the load the private key from
     * @return <code>true</code> in case the key was loaded successfully
     */
    @Nullable
    private static Key loadKeyImpl(@Nullable final InputStream in) {
        if (in != null) {
            ObjectInputStream keyIn = null;
            try {
                keyIn = new ObjectInputStream(in);
                return (Key) keyIn.readObject();
            } catch (@Nonnull final Exception e) {
                // loading the key failed
            } finally {
                try {
                    if (keyIn != null) {
                        keyIn.close();
                    }
                } catch (@Nonnull final IOException e) {
                    // loading the key failed
                }
            }
        }
        return null;
    }

    /**
     * Load the private key from a input stream.
     *
     * @param in the input stream the load the private key from
     * @return <code>true</code> in case the key was loaded successfully
     */
    private boolean loadPrivateKeyImpl(final InputStream in) {
        final Key loadKey = loadKeyImpl(in);
        if (loadKey == null) {
            return false;
        }
        try {
            privateKey = (PrivateKey) loadKey;
        } catch (@Nonnull final Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Load the public key from a input stream.
     *
     * @param in the input stream the load the public key from
     * @return <code>true</code> in case the key was loaded successfully
     */
    private boolean loadPublicKeyImpl(final InputStream in) {
        final Key loadKey = loadKeyImpl(in);
        if (loadKey == null) {
            return false;
        }
        try {
            publicKey = (PublicKey) loadKey;
        } catch (@Nonnull final Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Uses a cipher to transform the bytes in an input stream and sends the
     * transformed bytes to an output stream.
     *
     * @param in     the input stream
     * @param out    the output stream
     * @param cipher the cipher that transforms the bytes
     * @throws IOException              in case reading or writing the data failed
     * @throws GeneralSecurityException in case something is wrong with the
     *                                  cipher
     */
    private static void pumpData(@Nonnull final InputStream in, @Nonnull final OutputStream out,
                                 @Nonnull final Cipher cipher) throws IOException, GeneralSecurityException {
        final int blockSize = cipher.getBlockSize();
        final int outputSize = cipher.getOutputSize(blockSize);
        final byte[] inBytes = new byte[blockSize];
        byte[] outBytes = new byte[outputSize];

        int inLength = 0;
        boolean more = true;
        while (more) {
            inLength = in.read(inBytes);
            if (inLength == blockSize) {
                final int outLength =
                        cipher.update(inBytes, 0, blockSize, outBytes);
                out.write(outBytes, 0, outLength);
            } else {
                more = false;
            }
        }
        if (inLength > 0) {
            outBytes = cipher.doFinal(inBytes, 0, inLength);
        } else {
            outBytes = cipher.doFinal();
        }
        out.write(outBytes);
        out.flush();
    }
}
