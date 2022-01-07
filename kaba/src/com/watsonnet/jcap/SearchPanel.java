package com.watsonnet.jcap;

// java:
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

// swing:
import javax.swing.*;
import javax.swing.event.*;

public class SearchPanel extends JFrame implements Runnable {

	// interesting object for use
	JTextField textKeywords = new JTextField();
	JTextField textLocation = new JTextField();

	JPanel panelMain = new JPanel(new BorderLayout());
	JToolBar toolbarMain = new JToolBar();
	
	JButton btnSearch = new JButton("Search");
	JButton btnOpenFolder = new JButton("Open...");
	JLabel labelProgress = new JLabel("Ready");
	
	JButton btnCopyAll = new JButton("copy images...");
	JButton btnEraseAll = new JButton("erase images!!");
	JButton btnEraseKeywords = new JButton("erase keywords!!");
	
	JPanel panelResults = new JPanel(new BorderLayout());
	SearchTableModel resultsData = new SearchTableModel();
	JTable tableResults = new JTable(resultsData);
	JScrollPane scrollResults = new JScrollPane(tableResults);
	
	private volatile Thread threadSearch = null;
	
	public JCap parent = null;
	
	// constructor
	public SearchPanel() {
		// setModal(true); // does this make sense?
		
		// Create elements
		/*
		+---------------------------+
		| toolbar                   |
		+----------------------+----+
		| keywords input       | go |
		+----------------------+----+
		| search progress           |
		+---------------------------+
		| results area              |
		|                           |
		|                           |
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
		setTitle("Search");
		setIconImage(new ImageIcon(getClass().getResource("/images/icon.gif")).getImage());
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Keywords/search button/location
		// ---------------------------------------------------------------------
		// Labels
		JPanel panelKeywordsLabels = new JPanel(new GridLayout(0,1));
		panelKeywordsLabels.add(new JLabel("Location:", JLabel.RIGHT));
		panelKeywordsLabels.add(new JLabel("Keywords:", JLabel.RIGHT));
		
		// Fields
		JPanel panelKeywordsFields = new JPanel(new GridLayout(0,1));
		panelKeywordsFields.add(textLocation);
		panelKeywordsFields.add(textKeywords);
		if (JCap.prop.getProperty("SearchFolder") != null) {
			textLocation.setText(JCap.prop.getProperty("SearchFolder"));
		} else {
			if (JCap.images != null && JCap.images.length > 0) {
				textLocation.setText(JCap.images[0].getParent());
			}
		}
		
		// Buttons
		JPanel panelKeywordsButtons = new JPanel(new GridLayout(0,1));
		btnOpenFolder.setIcon(new ImageIcon(getClass().getResource("/images/open.gif")));
		//btnOpenFolder.setText(null);
		btnOpenFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				StringBuffer searchFolder = new StringBuffer();
				if (Util.chooseFolder (searchFolder, textLocation.getText(), panelMain, false)) {
					textLocation.setText(searchFolder.toString());
				}
			}
		});
		panelKeywordsButtons.add(btnOpenFolder);

		btnSearch.setIcon(new ImageIcon(getClass().getResource("/images/find.gif")));
		//btnSearch.setText(null);
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				startSearch();
			}
		});

		panelKeywordsButtons.add(btnSearch);
		getRootPane().setDefaultButton(btnSearch);

		// Buttons for multiple actions on found occurences 
		JPanel panelActionButtons = new JPanel(new GridLayout(0,3));
		
		btnCopyAll.setIcon(new ImageIcon(getClass().getResource("/images/saveall.gif")));
		btnCopyAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionCopyAllImages();
			}
		});
		btnCopyAll.setEnabled(false);
		panelActionButtons.add(btnCopyAll);

		btnEraseAll.setIcon(new ImageIcon(getClass().getResource("/images/remove.gif")));
		btnEraseAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionEraseAllImages();
			}
		});
		btnEraseAll.setEnabled(false);
		panelActionButtons.add(btnEraseAll);
		
		btnEraseKeywords.setIcon(new ImageIcon(getClass().getResource("/images/remove.gif")));
		btnEraseKeywords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionEraseAllKeywords();
			}
		});
		btnEraseKeywords.setEnabled(false);
		panelActionButtons.add(btnEraseKeywords);

		// Main panel
		JPanel panelKeywords = new JPanel(new BorderLayout());
		panelKeywords.add(panelKeywordsLabels, BorderLayout.WEST);
		panelKeywords.add(panelKeywordsFields, BorderLayout.CENTER);
		panelKeywords.add(panelKeywordsButtons, BorderLayout.EAST);
		panelKeywords.add(panelActionButtons, BorderLayout.SOUTH);
		
		// Add border
		panelKeywords.setBorder(BorderFactory.createTitledBorder("Search"));
		
		// Add the whole thing to the main panel
		panelMain.add(panelKeywords, BorderLayout.NORTH);
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Results panel
		// ---------------------------------------------------------------------
		panelResults.setBorder(BorderFactory.createTitledBorder("Results"));
		scrollResults.setWheelScrollingEnabled(true);
		tableResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = tableResults.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				//Ignore extra messages.
				if (e.getValueIsAdjusting()) return;
				
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (lsm.isSelectionEmpty()) {
					// No rows are selected
				} else {
					// save actual
					parent.saveText(JCap.imageIndex);
					
					int selectedRow = lsm.getMinSelectionIndex();
					// selectedRow is now selected
					String filename = (String)resultsData.getValueAt(selectedRow, 2);
					parent.getImages((new File(filename)).getParent());
					for(int i = 0; i <= JCap.images.length-1; i++) {
						if (Util.getFilenameLabel(JCap.images[i].getAbsolutePath()).equals(Util.getFilenameLabel(filename))) {
							JCap.imageIndex = i;
							break;
						}
					}
					parent.showImage(Toolkit.getDefaultToolkit().getImage(JCap.images[JCap.imageIndex].getAbsolutePath()), false);
				}
			}
		});
		panelResults.add(scrollResults, BorderLayout.CENTER);
		panelMain.add(panelResults, BorderLayout.CENTER);
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
				parent.searchPanelVisible = false;
				
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
		textKeywords.setCaretPosition(0);
		textKeywords.requestFocus();
		// ---------------------------------------------------------------------
	}
	
	public void start() {
		if (threadSearch == null) {
			threadSearch = new Thread(this);
			threadSearch.start();
		} else {
			try {
				// .stop() is deprecated
				// http://java.sun.com/j2se/1.4.1/docs/guide/misc/threadPrimitiveDeprecation.html
				//threadSearch.stop();
				threadSearch.interrupt();
				threadSearch = null;
			} catch (Exception ex) {}
		}
	}
	
	public void run() {
		if (threadSearch != null) {
			// Remove current results
			resultsData.clearData();
			btnCopyAll.setEnabled(false);
			
			// Update the headlines
			int i = search(textLocation.getText());
			
			// Done
			labelProgress.setText("Done: " + i + " Occurences found.");
			if (i>0) btnCopyAll.setEnabled(true);
			
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
			Pattern p = Pattern.compile(textKeywords.getText(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			for(int i=0; i<=file.length-1; i++) {
				// abort if thread was interrupted
				if (threadSearch == null) { return found; }
				String filename = folder.getAbsolutePath() + File.separator + file[i];
				
				File subfile = new File(filename);
				labelProgress.setText("Searching " + subfile.getAbsolutePath());
				
				if (subfile.isDirectory()) {
					// recursively search sub-folders
					int foundInSub = search(subfile.getAbsolutePath());
					found = found + foundInSub;
				} else {
					if (Util.imageExistsForFilename(filename)) {
						// search within txt files
						if (subfile.getAbsolutePath().toLowerCase().endsWith(Util.TEXT_EXT)) {
							String text = Util.readFile(subfile);
							Matcher m = p.matcher(text);
							int matchcount = 0;
							while (m.find()) {
								matchcount += 1;
							}
							if (matchcount > 0) {
								// found a match
								SearchResult sr = new SearchResult(subfile.getAbsolutePath(), Util.readFile(new File(Util.getKeywordsFilename(subfile.getAbsolutePath()))), matchcount);
								resultsData.addData(sr);
								found ++;
							}
						}
					}
				}
			}
		}
		return found;
	}
	
	protected void startSearch() {
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
	
	protected void actionCopyAllImages() {
		StringBuffer dest = new StringBuffer();
		String oldDir=parent.getFolder("CopyAllLastFolder");
		if (oldDir == null) oldDir = System.getProperty("user.home");

		if (Util.chooseFolder (dest, oldDir, panelMain, true)) {
			copyImages(dest.toString());
			parent.setFolder("CopyAllLastFolder", dest.toString());
		}
	}
	
	/** Kopiert die Bilder in das angegebene Verzeichnis
	 * @param Ziel-Verzeichnis
	 */
	private void copyImages(String destFolder) {

		for (int i=0; i<resultsData.getRowCount(); i++) {
			String oldTextFilename = (String) resultsData.getValueAt(i, 2);
			File text = new File (oldTextFilename);
			Util.copyFile (oldTextFilename, destFolder + File.separator + text.getName());

			String oldImageFilename = Util.getImageFilename (oldTextFilename);
			File image = new File (oldImageFilename);
			// momentan werden Bilder im Zielverz. unter Umständen überschrieben
			Util.copyFile (oldImageFilename, destFolder + File.separator + image.getName());
		}
		labelProgress.setText("All Copies are ready from list above!");
	}

	protected void actionEraseAllImages() {
		// TODO Auto-generated method stub
				
	}
	
	protected void actionEraseAllKeywords() {
		// TODO Auto-generated method stub
				
	}
	
	// Close window
	protected void closeWindow() {
		// Save search settings
		parent.setFolder("SearchFolder", textLocation.getText());
		
		// Close window
		dispose();
	}
}

