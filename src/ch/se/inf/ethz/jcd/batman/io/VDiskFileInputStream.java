/**
 * 
 */
package ch.se.inf.ethz.jcd.batman.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;

/**
 * Implements {@link java.io.InputStream} for files on the Batman Virtual Disk
 * 
 */
public class VDiskFileInputStream extends InputStream {
    // fields
    private final IVirtualFile file;
    private long currentPosition;

    // constructors

    /**
     * Creates a VDiskFileInputStream by opening the file named by the filePath
     * parameter.
     * 
     * @param filePath
     *            path to the file to open
     * @throws IOException
     *             TODO
     */
    public VDiskFileInputStream(String filePath, IVirtualDisk disk)
            throws IOException {
        this(new VDiskFile(filePath, disk));

    }

    /**
     * Creates a VDiskFileInputStream by opening the file represented by
     * VDiskFile file.
     * 
     * @param file
     *            the file to open
     * @throws IOException
     *             TODO
     */
    public VDiskFileInputStream(VDiskFile file) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException();
        }

        this.file = (IVirtualFile) file.getDiskEntry();
        this.currentPosition = 0;
    }

    // public methods

    @Override
    public int available() throws IOException {
        long bytesToGo = this.file.getSize() - this.file.getDataPosition();

        if (bytesToGo > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) bytesToGo;
        }
    }

    @Override
    public void close() throws IOException {
        // we do not need to close because of the underlying disk
    }

    @Override
    public int read() throws IOException {
        if (currentPosition < this.file.getSize()) {
            this.file.seek(currentPosition);
            int readValue = this.file.read() & 0xFF;
            this.currentPosition = this.file.getFilePointer();
            
            return readValue;
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        this.file.seek(currentPosition);
        int readAmount = this.file.read(b);
        this.currentPosition = this.file.getFilePointer();

        return readAmount;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        this.file.seek(currentPosition);

        byte[] readBytes = new byte[len];
        int readCount = this.file.read(readBytes);

        for (int i = off; i < off + len; i++) {
            b[i] = readBytes[i - off];
        }

        this.currentPosition = this.file.getFilePointer();

        return readCount;
    }

    @Override
    public long skip(long n) throws IOException {
        long oldPosition = this.currentPosition;
        long newPosition = oldPosition + n;
        long fileSize = this.file.getSize();

        if (newPosition > fileSize) {
            this.file.seek(fileSize);
            this.currentPosition = this.file.getFilePointer();

            return fileSize - oldPosition;
        } else {
            this.file.seek(newPosition);
            this.currentPosition = this.file.getFilePointer();

            return n;
        }
    }

    /**
     * Using {@link #mark(int)} and {@link #reset()} is not supported. As
     * specified it will always return false.
     * 
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Not supported.
     * 
     * @see #markSupported()
     */
    @Override
    public synchronized void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     * 
     * @see #markSupported()
     */
    @Override
    public synchronized void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    // protected methods

    // private methods

}
