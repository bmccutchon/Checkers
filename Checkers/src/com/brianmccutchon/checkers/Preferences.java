package com.brianmccutchon.checkers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.Semaphore;

public class Preferences implements Serializable {

	private static final long serialVersionUID = 9168926646637067344L;
	
	/** The default game preferences. **/
	private static final Preferences DEFAULT_PREFS =
			new Preferences(7, true, false, "Normal");
	
	/** TODO The place where preferences are saved. **/
	private static final String SAVE_LOCATION = "data/prefs";

	public int treeDepth;
	
	public boolean p1IsHuman;
	
	public boolean p2IsHuman;
	
	/** The name of the current variant. "Normal" or "Suicide". **/
	public String modeName;
	
	/**
	 * @param treeDepth
	 * @param p1IsHuman
	 * @param p2IsHuman
	 * @param modeName 
	 */
	public Preferences(int treeDepth, boolean p1IsHuman,
			boolean p2IsHuman, String modeName) {
		this.treeDepth = treeDepth;
		this.p1IsHuman = p1IsHuman;
		this.p2IsHuman = p2IsHuman;
		this.modeName = modeName;
	}
	
	public static Preferences getPrefsGraphically() {
		
		final Semaphore holdForWindowClose = new Semaphore(0);
		
		Preferences originalPrefs = Preferences.load();
		
		// Array to hold one thing - sort of a hack
		// to make it visible to the inner class below
		final Preferences[] prefs = new Preferences[1];
		
		PreferencesGUI gui = new PreferencesGUI(originalPrefs,
				new PreferenceListener() {
			@Override
			public void finished(Preferences endstate) {
				holdForWindowClose.release();
				prefs[0] = endstate;
			}
		});
		
		gui.setVisible(true);
		
		try {
			holdForWindowClose.acquire();
			prefs[0].save();
		} catch (InterruptedException e) {
			System.err.println("Thread interrupted.");
			e.printStackTrace();
		}
		
		return prefs[0];
	}
	
	public void save() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(SAVE_LOCATION));
			oos.writeObject(this);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Preferences load() {
		try {
			ObjectInputStream ois = new ObjectInputStream(
					new FileInputStream(SAVE_LOCATION));
			Preferences p = (Preferences) ois.readObject();
			ois.close();
			return p;
		} catch (Exception e) {
			e.printStackTrace();
			return DEFAULT_PREFS;
		}
	}
	
}
