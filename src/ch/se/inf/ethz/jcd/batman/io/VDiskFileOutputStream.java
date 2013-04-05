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

    // constructors

    /**
     * Creates a VDiskFileOutputStream by opening the file named by the filePath
     * parameter.
     * 
     * If the file already exists it will overwrite the current content. If the
     * file does not yet exist the file will be created.
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
     * If the file already exists, the written content will be appended to the
     * current data. If the file dies not yet exist the file will be created.
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

        VDiskFile fileHelper = new VDiskFile(filePath, disk);
        if (!fileHelper.exists() || !fileHelper.isFile()) {
            throw new FileNotFoundException();
        }

        this.file = (IVirtualFile) fileHelper.getDiskEntry();
        if(append) {
            this.file.seek(this.file.getSize());
        } else {
            this.file.seek(0);
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
        this.file.write((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.file.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] toBeWritten = Arrays.copyOfRange(b, off, len);
        this.file.write(toBeWritten);
    }

    // protected methods
    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    // private methods

}
