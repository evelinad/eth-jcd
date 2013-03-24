package ch.se.inf.ethz.jcd.batman.cli.vdisk.pstratetgy;

import java.io.RandomAccessFile;

import ch.se.inf.ethz.jcd.batman.cli.vdisk.VirtualDisk;

public final class PlacementStrategyFactory {

	public static final byte MIXED_STRATEGY = 1;
	
	public static IPlacementStrategy getPlacementStrategy (byte type, VirtualDisk virtualDisk, RandomAccessFile file) {
		if (type == MIXED_STRATEGY)	{
			return new MixedPlacementStrategy(virtualDisk, file);
		}
		throw new IllegalArgumentException("Strategy " + type + " not supported");
	}
	
}
