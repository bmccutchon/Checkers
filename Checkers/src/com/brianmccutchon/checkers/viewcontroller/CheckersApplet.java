package com.brianmccutchon.checkers.viewcontroller;

import javax.swing.JApplet;

public class CheckersApplet extends JApplet {
	
	private static final long serialVersionUID = 2794786458523200486L;

	public void start() {
		new CheckersGUI(true);
		//SwingUtilities.invokeLater(CheckersGUI::new);
	}
	
}
