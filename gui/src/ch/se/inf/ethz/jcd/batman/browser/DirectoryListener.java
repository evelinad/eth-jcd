package ch.se.inf.ethz.jcd.batman.browser;

import ch.se.inf.ethz.jcd.batman.model.Directory;

public interface DirectoryListener {

	void directoryChanged(Directory directory);
	
}
