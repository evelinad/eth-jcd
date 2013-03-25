package ch.se.inf.ethz.jcd.batman.vdisk;

import java.io.RandomAccessFile;


public final class PlacementStrategyFactory {

	public static final byte MIXED_STRATEGY = 1;
	
	public static IPlacementStrategy getPlacementStrategy (byte type, VirtualDisk virtualDisk, RandomAccessFile file) {
		if (type == MIXED_STRATEGY)	{
			return new MixedPlacementStrategy(virtualDisk, file);
		}
		throw new IllegalArgumentException("Strategy " + type + " not supported");
	}
	
}
