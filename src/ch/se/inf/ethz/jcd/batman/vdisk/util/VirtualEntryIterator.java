package ch.se.inf.ethz.jcd.batman.vdisk.util;

import java.io.IOException;
import java.util.Iterator;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;

public class VirtualEntryIterator implements Iterator<IVirtualDiskEntry> {
    
    private IVirtualDiskEntry nextEntry;
    
    public VirtualEntryIterator(IVirtualDiskEntry startEntry) {
        nextEntry = startEntry;
    }

    @Override
    public boolean hasNext() {
        return nextEntry != null;
    }

    @Override
    public IVirtualDiskEntry next() {
        try {
            IVirtualDiskEntry toReturn = nextEntry;
            nextEntry = nextEntry.getNextEntry();
            
            return toReturn;
        } catch (IOException e) {
            return null; // TODO this is very bad, we need some clean solution
        }
        
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    

}
