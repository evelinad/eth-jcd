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

    // constructors

    /**
     * Creates a VDiskFileInputStream by opening the file named by the filePath
     * parameter.
     * 
     * @param filePath
     *            path to the file to open
     * @throws IOException TODO
     */
    public VDiskFileInputStream(String filePath, IVirtualDisk disk)
            throws IOException {

        VDiskFile fileHelper = new VDiskFile(filePath, disk);
        if (!fileHelper.exists() || !fileHelper.isFile()) {
            throw new FileNotFoundException();
        }

        this.file = (IVirtualFile) fileHelper.getDiskEntry();
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
        return (int) this.file.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.file.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        byte[] readBytes = new byte[len];
        int readCount = this.file.read(readBytes);
        
        for(int i = off; i < off + len; i++) {
            b[i] = readBytes[i-off];
        }
        
        return readCount;
    }

    @Override
    public long skip(long n) throws IOException {
        long oldPosition = this.file.getDataPosition();
        long newPosition = oldPosition + n;
        long fileSize = this.file.getSize();
        
        if(newPosition > fileSize) {
            this.file.seek(fileSize - 1);
            return fileSize - oldPosition;
        } else {
            this.file.seek(newPosition);
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
