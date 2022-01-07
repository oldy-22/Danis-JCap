/*
 * Created on 13.04.2007
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.watsonnet.jcap;

// java:
import java.awt.*;
import java.awt.event.*;
import java.io.*;

// swing:
import javax.swing.*;

/**
 * @author dani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ManipulationsPanel extends JFrame implements Runnable {
	JPanel panelMain = new JPanel(new BorderLayout());
	JToolBar toolbarMain = new JToolBar();
	
	JTextField textLocation = new JTextField();
	JButton btnOpenFolder = new JButton("Change Dir...");

	private final String oldExt = "_keywords.txt";
	private final String newExt = "_caption.txt";
	
	JLabel oldExtensions = new JLabel(oldExt);
	JLabel newExtensions = new JLabel(newExt);
	JButton btnRenameTextFiles = new JButton("Rename Text-Files");
	JLabel labelProgress = new JLabel("Ready");
	
	JTextArea log = new JTextArea ();
	JScrollPane logScroll = new JScrollPane(log, 
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel logPanel = new JPanel(new BorderLayout());
	
	private volatile Thread threadSearch = null;
	
	public JCap parent = null;
	
	// constructor
	public ManipulationsPanel() {
		// setModal(true); // does this make sense?
	
		// ---------------------------------------------------------------------
		// Main content panel
		// ---------------------------------------------------------------------
		getContentPane().add(panelMain);
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Title and icon
		// ---------------------------------------------------------------------
		setTitle("DataBase Manipulations");
		setIconImage(new ImageIcon(getClass().getResource("/images/icon.gif")).getImage());
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Database location (including subdirs)
		// ---------------------------------------------------------------------
		// Labels
		JPanel locLabel = new JPanel();
		locLabel.add(new JLabel("Location:", JLabel.RIGHT));
		
		// Fields
		JPanel locText = new JPanel(new GridLayout(0,1));
		locText.add(textLocation);
		if (JCap.prop.getProperty("SearchFolder") != null) {
			textLocation.setText(JCap.prop.getProperty("SearchFolder"));
		} else {
			if (JCap.images != null && JCap.images.length > 0) {
				textLocation.setText(JCap.images[0].getParent());
			}
		}
		JPanel describeAction = new JPanel();
		describeAction.add (oldExtensions);
		JLabel l = new JLabel ( new ImageIcon(getClass().getResource("/images/next_empty.gif")) );
		describeAction.add (l);
		describeAction.add (newExtensions);
		locText.add (describeAction);
		
		// Buttons
		JPanel locButton = new JPanel(new GridLayout (0,1));
		btnOpenFolder.setIcon(new ImageIcon(getClass().getResource("/images/open.gif")));
		//btnOpenFolder.setText(null);
		btnOpenFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				chooseFolder();
			}
		});
		locButton.add(btnOpenFolder);
		btnRenameTextFiles.setIcon(new ImageIcon(getClass().getResource("/images/find.gif")));
		//btnSearch.setText(null);
		btnRenameTextFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				renameFiles();
			}
		});
		locButton.add(btnRenameTextFiles);
		getRootPane().setDefaultButton(btnRenameTextFiles);
		
		// Main panel
		JPanel actPanel = new JPanel(new BorderLayout());
		actPanel.add(locLabel, BorderLayout.WEST);
		actPanel.add(locText, BorderLayout.CENTER);
		actPanel.add(locButton, BorderLayout.EAST);
		
		// Add border
		actPanel.setBorder(BorderFactory.createTitledBorder("Manipulate Text Files"));
		
		// Add the whole thing to the main panel
		panelMain.add(actPanel, BorderLayout.NORTH);
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Log panel
		// ---------------------------------------------------------------------
		logPanel.setBorder(BorderFactory.createTitledBorder("Protocol"));
		logScroll.setWheelScrollingEnabled(true);
		
		log.setLineWrap(true);
		log.setWrapStyleWord(true);

		logPanel.add(logScroll, BorderLayout.CENTER);
		panelMain.add(logPanel, BorderLayout.CENTER);
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Progress
		// ---------------------------------------------------------------------
		labelProgress.setBorder(BorderFactory.createTitledBorder("Status"));
		panelMain.add(labelProgress, BorderLayout.SOUTH);
		
		// ---------------------------------------------------------------------
		// Add window listener to trap closing event
		// ---------------------------------------------------------------------
		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent e) {
				// So we know to create a new search panel next time
				parent.manipulationsPanelVisible = false;
				
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
		textLocation.setCaretPosition(textLocation.getText().length());
		textLocation.requestFocus();
		// ---------------------------------------------------------------------
	}
	
	public void start() {
		if (threadSearch == null) {
			threadSearch = new Thread(this);
			threadSearch.start();
		} else {
			try {
				threadSearch.interrupt();
				threadSearch = null;
			} catch (Exception ex) {}
		}
	}
	
	public void run() {
		if (threadSearch != null) {
			// Remove current results
			log.setText("");
			
			// Update the headlines
			int i = search(textLocation.getText());
			
			// Done
			labelProgress.setText("Done: " + i + " Files found and renamed.");
			
			// stop the thread
			threadSearch = null;
		}
	}
	
	// Search
	private int search(String f) {
		int found = 0;
		File folder = new File(f);
		
		if (folder.exists()) {
			String file[] = folder.list();
			for(int i=0; i<=file.length-1; i++) {
				// abort if thread was interrupted
				if (threadSearch == null) { return -1; }
				String filename = folder.getAbsolutePath() + File.separator + file[i];
				
				File subfile = new File(filename);
				labelProgress.setText("Searching " + subfile.getAbsolutePath());
				
				if (subfile.isDirectory()) {
					// recursively search sub-folders
					int foundInSub = search(subfile.getAbsolutePath());
					found = found + foundInSub;
				} else {
					if (Util.imageExistsForFilename(filename)) {
						
						// Dani inserted for database change on 070409
						// filename changes and something else usefull things are possible here
						if (filename.endsWith(oldExt)) {
							String destFilename = filename.substring(0, filename.indexOf(oldExt)) + newExt;
							File fdest = new File (destFilename);
							boolean check = true;
							
							// here goes action - recomment if necessary, but be careful :-)
							// check = subfile.renameTo(fdest);

							log.append(filename + " -> " + destFilename + "\n");
							
							if (!check) found = -1000000; // something was going wrong when negative value
								else found++;
						}
					}
				}
			}
		}
		return found;
	}
	
	protected void renameFiles() {
		File f = new File(textLocation.getText());
		if (f.exists() || threadSearch != null) {
			// start new thread and do searching.
			// if a thread is already running, this will stop it.
			start();
		} else {
			JOptionPane.showMessageDialog(this, "The folder you have chosen does not exist.", 
				"Folder does not exist", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	protected void chooseFolder() {
		StringBuffer loc = new StringBuffer();
		
		if (Util.chooseFolder(loc, textLocation.getText(), panelMain, false)) {
			textLocation.setText(loc.toString());
		}
	}
	
	// Close window
	protected void closeWindow() {
		// Save search settings
		JCap.prop.setProperty("SearchFolder", textLocation.getText());
		
		// Close window
		dispose();
	}

}
