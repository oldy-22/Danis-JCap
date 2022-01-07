package com.watsonnet.jcap;

// java:
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.text.*;

// swing:
import javax.swing.*;

// watsonnet

public class InfoPanel extends CloseableDialog {
	MultiLineLabel label;

	// constructor
	public InfoPanel( JCap parentWindow ) {
		
		super (parentWindow, "Info-Text", true);
		this.setLayout( new BorderLayout(0, 0) );
		
		
		DateFormat df = DateFormat.getDateInstance();
		URL pClass = getClass().getResource("JCap.class");
		String versionDate = df.format(new Date (new File (pClass.getFile()).lastModified()));
		
		String message = "Das ist " + 
			JCap.APP_TITLE + JCap.APP_VERSION + "!\n\n" +
			"Ein Programm zum \nArchivieren, Beschriften und Betrachten von Digi-Bildern. \n \n " +
			" \nVersions-Datum:   " + versionDate + " (compiled)" +
			" \nQuelle:   Internet www.sourceforge.net" +
			"\nUmgeschrieben von:   Daniel Enke";
			 
		label = new MultiLineLabel (message, MultiLineLabel.CENTER);
		this.add ("Center", label);

		JButton readyButton = new JButton("Ready");

		JPanel panelReadyButton = new JPanel();
		panelReadyButton.add(readyButton);
		
		readyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				hide(); dispose();
			}
		});
		
		this.add (panelReadyButton, BorderLayout.SOUTH);
		
	}
	
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		Dimension dl = label.getPreferredSize();
		
		Dimension e = new Dimension ((d.width+dl.width)*3/6, (d.height+dl.height)*5/6);
		
		return e;
	}
}

