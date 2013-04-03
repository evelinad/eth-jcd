package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.IOException;

public interface IFreeBlock extends IVirtualBlock {

	long getNextBlock();

	void setNextBlock(long nextBlock) throws IOException;

	long getPreviousBlock();
	
	void setPreviousBlock(long previousBlock) throws IOException;
	
}
