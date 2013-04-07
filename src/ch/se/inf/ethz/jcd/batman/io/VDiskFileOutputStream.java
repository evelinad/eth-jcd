package ch.se.inf.ethz.jcd.batman.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDisk;
import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualFile;

/**
 * Implements {@link java.io.OutputStream} for files on the Batman Virtual Disk.
 * 
 */
public class VDiskFileOutputStream extends OutputStream {
    // fields
    private final IVirtualFile file;
    private long currentPosition;

    // constructors

    /**
     * Creates a VDiskFileOutputStream by opening the file named by the filePath
     * parameter.
     * 
     * @param filePath
     *            path to the file to open
     * @param disk
     *            TODO
     * @throws IOException
     */
    public VDiskFileOutputStream(String filePath, IVirtualDisk disk)
            throws IOException {
        this(filePath, disk, false);
    }

    /**
     * Creates a VDiskFileOutputStream by opening the file named by the filePath
     * parameter.
     * 
     * @param filePath
     *            path to the file to open
     * @param disk
     *            TODO
     * @param append
     *            true if the written content should be appended to the current
     *            data.
     * @throws IOException
     */
    public VDiskFileOutputStream(String filePath, IVirtualDisk disk,
            boolean append) throws IOException {
        this(new VDiskFile(filePath, disk), append);
    }

    /**
     * Creates a VDiskFileOutputStream by using the given VDiskFile file.
     * 
     * @param file
     *            the file to write into
     * @param append
     *            true if the written content should be appended to the current
     *            data.
     * @throws IOException
     *             TODO
     */
    public VDiskFileOutputStream(VDiskFile file, boolean append)
            throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException();
        }

        this.file = (IVirtualFile) file.getDiskEntry();
        if (append) {
            this.currentPosition = this.file.getSize();
        } else {
            this.currentPosition = 0;
        }
    }

    // public methods
    @Override
    public void close() throws IOException {
        /*
         * not needed as virtual disk has no buffer or anything else that would
         * need to be closed.
         */
    }

    @Override
    public void flush() throws IOException {
        // not needed as virtual disk has no buffer
    }

    @Override
    public void write(int b) throws IOException {
        this.file.seek(currentPosition);
        this.file.write((byte) b);

        this.currentPosition = this.file.getFilePointer();
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.file.seek(currentPosition);
        this.file.write(b);

        this.currentPosition = this.file.getFilePointer();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.file.seek(currentPosition);

        byte[] toBeWritten = Arrays.copyOfRange(b, off, off + len);
        this.file.write(toBeWritten);

        this.currentPosition = this.file.getFilePointer();
    }

    // protected methods
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    // private methods

}
