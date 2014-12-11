package com.brianmccutchon.checkers;

/**
 * Describes a callback to be executed when the
 * preferences window is closed.
 * @author Brian McCutchon
 */
public interface PreferenceListener {
	
	public void finished(Preferences endstate);
	
}
