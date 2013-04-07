package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

/**
 * Represents a block that is not used to save useful data.
 * 
 * This interface allows to create an implicit doubly linked list of free blocks
 * in the virtual disk. Large parts, which are normally used to store data (see
 * {@link IDataBlock}), are not used and may contain garbage.
 * 
 * @see IVirtualBlock
 * @see IDataBlock
 */
public interface IFreeBlock extends IVirtualBlock {

    /**
     * Returns the position (address) of the next block inside the implicit
     * doubly linked list of free blocks.
     * 
     * The address is relative to the beginning of the virtual disk. Therefore
     * position 0 is the first byte of the virtual disk.
     * 
     * @return position of the next free block, zero if there is none
     */
    long getNextBlock();

    /**
     * Sets the position of the next free block.
     * 
     * @see #getNextBlock()
     * @param nextBlock
     *            the position of the next free block.
     * @throws IOException
     *             if an I/O error occurs
     */
    void setNextBlock(long nextBlock) throws IOException;

    /**
     * Returns the position (address) of the previous block inside the implicit
     * doubly linked list of free blocks.
     * 
     * The address is relative to the beginning of the virtual disk. Therefore
     * position 0 is the first byte of the virtual disk.
     * 
     * @return position of the previous free block, zero if there is none
     */
    long getPreviousBlock();

    /**
     * Sets the position of the previous free block.
     * 
     * @param previousBlock
     *            the position of the previous free block
     * @throws IOException
     *             if an I/O error occurs
     */
    void setPreviousBlock(long previousBlock) throws IOException;

}
