package com.watsonnet.jcap;

// java:
import java.awt.*;
import java.awt.event.*;
import java.io.*;

// swing:
import javax.swing.*;

// watsonnet

public class HelpPanel extends JFrame {

	public JCap parent = null;
	
	// constructor
	public HelpPanel() {
		
		JPanel panelMain = new JPanel(new BorderLayout());
	
		JButton readyButton = new JButton("Ready");

		try {
			JEditorPane je = new JEditorPane (getClass().getResource("/images/help.html"));

			JScrollPane scrollHelpText = new JScrollPane (je);

			panelMain.add (scrollHelpText, BorderLayout.CENTER);

		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "HelpText file not found.", "Read error", JOptionPane.WARNING_MESSAGE);
		} 
		
	
		// Create elements
		/*
		+---------------------------+
		| helpText               |
		+---------------------------+
		| readyButton          |
		+---------------------------+
		*/
		
		// ---------------------------------------------------------------------
		// Main content panel
		// ---------------------------------------------------------------------
		getContentPane().add(panelMain);
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Title and icon
		// ---------------------------------------------------------------------
		setTitle("Help");
		setIconImage(new ImageIcon(getClass().getResource("/images/icon.gif")).getImage());
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Keywords/search button/location
		// ---------------------------------------------------------------------
		// Labels
		JPanel panelReadyButton = new JPanel();
		panelReadyButton.add(readyButton);
		
		readyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				closeWindow ();
			}
		});
		
		panelMain.add (panelReadyButton, BorderLayout.SOUTH);
		
		// ---------------------------------------------------------------------
		// Add window listener to trap closing event
		// ---------------------------------------------------------------------
		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
			public void windowClosed(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Make frame visible
		// ---------------------------------------------------------------------
		panelMain.validate();
		setVisible(true);
		// ---------------------------------------------------------------------
	}
	
	// Close window
	protected void closeWindow() {
		// So we know to create a new search panel next time
		parent.helpPanelVisible = false;
		
		// Close window
		dispose();
	}
}

