package ch.se.inf.ethz.jcd.batman.vdisk.util;

import java.io.IOException;
import java.util.Iterator;

import ch.se.inf.ethz.jcd.batman.vdisk.IVirtualDiskEntry;

public class VirtualEntryIterator implements Iterator<IVirtualDiskEntry> {
    
    IVirtualDiskEntry curEntry;
    
    public VirtualEntryIterator(IVirtualDiskEntry startEntry) {
        curEntry = startEntry;
    }

    @Override
    public boolean hasNext() {
        IVirtualDiskEntry nextEntry;
        try {
            nextEntry = curEntry.getNextEntry();
        } catch (IOException e) {
            return false; // TODO this is very bad, we need some clean solution
        }
        
        return nextEntry != null;
    }

    @Override
    public IVirtualDiskEntry next() {
        try {
            curEntry = curEntry.getNextEntry();
            return curEntry;
        } catch (IOException e) {
            return null; // TODO this is very bad, we need some clean solution
        }
        
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    

}
