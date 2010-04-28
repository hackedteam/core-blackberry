/* *************************************************
 * Copyright (c) 2010 - 2010
 * HT srl,   All rights reserved.
 * Project      : RCS, RCSBlackBerry_lib
 * File         : AutoFlashFile.java
 * Created      : 26-mar-2010
 * *************************************************/
package blackberry.fs;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
import blackberry.utils.Check;
import blackberry.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class AutoFlashFile.
 */
public final class AutoFlashFile {
    String filename;
    boolean hidden;
    boolean autoclose;

    private FileConnection fconn;
    private DataInputStream is;
    private OutputStream os;

    /**
     * Instantiates a new auto flash file.
     * 
     * @param filename_
     *            the filename_
     * @param hidden_
     *            the hidden_
     */
    public AutoFlashFile(final String filename_, final boolean hidden_) {
        filename = filename_;
        hidden = hidden_;
    }

    /**
     * Append.
     * 
     * @param message
     *            the message
     * @return true, if successful
     */
    public synchronized boolean append(final byte[] message) {
        try {
            fconn = (FileConnection) Connector.open(filename,
                    Connector.READ_WRITE);
            // #ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            // #endif

            final long size = fconn.fileSize();
            os = fconn.openOutputStream(size);
            // #ifdef DBC
            Check.asserts(os != null, "os null");
            // #endif

            os.write(message);

        } catch (final IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {

            close();
        }

        return true;
    }

    /**
     * Append.
     * 
     * @param value
     *            the value
     * @return true, if successful
     */
    public synchronized boolean append(final int value) {
        byte[] repr;
        repr = Utils.intToByteArray(value);
        return append(repr);
    }

    /**
     * Append.
     * 
     * @param message
     *            the message
     * @return true, if successful
     */
    public synchronized boolean append(final String message) {
        return append(message.getBytes());
    }

    private synchronized void close() {
        try {
            if (null != is) {
                is.close();
            }

            if (null != os) {
                os.close();
            }

            if (null != fconn) {
                fconn.close();
            }

        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Creates the.
     * 
     * @return true, if successful
     */
    public synchronized boolean create() {
        try {

            fconn = (FileConnection) Connector.open(filename,
                    Connector.READ_WRITE);
            // #ifdef DBC
            Check.asserts(fconn != null, "fconn null");
            // #endif

            if (fconn.exists()) {
                fconn.truncate(0);
            } else {
                fconn.create();
                os = fconn.openDataOutputStream();

            }

            fconn.setHidden(hidden);
            // #ifdef DBC
            Check.asserts(fconn.isHidden() == hidden, "Not Hidden as expected");
            // #endif
        } catch (final IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            close();
        }

        // #ifdef DBC
        Check.ensures(exists(), "not created");
        // #endif
        return true;
    }

    /**
     * Delete.
     */
    public synchronized void delete() {
        try {
            fconn = (FileConnection) Connector.open(filename,
                    Connector.READ_WRITE);
            // #ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            // #endif

            if (fconn.exists()) {
                fconn.delete();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     * Exists.
     * 
     * @return true, if successful
     */
    public synchronized boolean exists() {
        try {
            fconn = (FileConnection) Connector.open(filename, Connector.READ);
            // #ifdef DBC
            Check.asserts(fconn != null, "fconn null");
            // #endif

            return fconn.exists();

        } catch (final IOException e) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            close();
        }
    }

    /**
     * Gets the input stream.
     * 
     * @return the input stream
     */
    public synchronized InputStream getInputStream() {
        try {
            fconn = (FileConnection) Connector.open(filename, Connector.READ);
            // #ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            // #endif

            is = fconn.openDataInputStream();
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }

        return is;
    }

    /**
     * Read.
     * 
     * @return the byte[]
     */
    public synchronized byte[] read() {
        byte[] data = null;

        try {
            fconn = (FileConnection) Connector.open(filename, Connector.READ);
            // #ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            // #endif

            is = fconn.openDataInputStream();
            data = IOUtilities.streamToBytes(is);
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        } finally {
            close();
        }

        return data;
    }

    /**
     * Rename.
     * 
     * @param newFile
     *            the new file
     * @return true, if successful
     */
    public boolean rename(final String newFile) {
        try {
            fconn = (FileConnection) Connector.open(filename,
                    Connector.READ_WRITE);
            // #ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            // #endif

            if (fconn.exists()) {
                fconn.rename(newFile);
                filename = newFile;
            }
        } catch (final IOException e) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            close();
        }
        return true;
    }

    /**
     * Write.
     * 
     * @param message
     *            the message
     * @return true, if successful
     */
    public synchronized boolean write(final byte[] message) {

        try {
            fconn = (FileConnection) Connector.open(filename, Connector.WRITE);
            // #ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            // #endif

            os = fconn.openOutputStream();

            os.write(message);

        } catch (final IOException e) {
            System.out.println(e.getMessage());
            return false;
        } finally {
            close();
        }

        return true;

    }

    /**
     * Write.
     * 
     * @param value
     *            the value
     * @return true, if successful
     */
    public synchronized boolean write(final int value) {
        final byte[] repr = Utils.intToByteArray(value);
        return write(repr);
    }
}
