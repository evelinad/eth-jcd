package ch.se.inf.ethz.jcd.batman.browser;

import ch.se.inf.ethz.jcd.batman.model.Entry;

public interface DiskEntryListener {

	void entryAdded(Entry entry);

	void entryDeleted(Entry entry);

	void entryChanged(Entry oldEntry, Entry newEntry);

}
