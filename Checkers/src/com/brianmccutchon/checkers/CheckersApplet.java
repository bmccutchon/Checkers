package com.brianmccutchon.checkers;

import javax.swing.JApplet;

public class CheckersApplet extends JApplet {
	
	private static final long serialVersionUID = 2794786458523200486L;

	public void init() {
		new CheckersGUI();
	}
	
}
