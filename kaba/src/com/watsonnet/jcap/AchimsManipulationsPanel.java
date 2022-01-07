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
import com.drew.metadata.*;
import com.drew.imaging.jpeg.*;
import java.util.*;
import java.text.*;

// swing:
import javax.swing.*;

/**
 * @author dani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AchimsManipulationsPanel extends JFrame implements ActionListener, Runnable {
	JPanel panelMain = new JPanel(new BorderLayout());
	JToolBar toolbarMain = new JToolBar();
	String[] actionMenuTitles, optionMenuTitles;
	JMenu optionMenu;
	
	JTextField textLocation = new JTextField();

	private final String oldExt = "*.jpg";
	private final String newExt = "YY-MM-DD 'freier Text' hh-mm-ss.jpg";

	private final String oldFileExt = ".jpg";
	
	/** zählt mehrfache gleiche Dateinamen */
	int counterDouble; 
	String userFileInsertion = "";
	/** das müssen nur die Beginn-Zeichen sein*/
	String dateFieldWork, dateFieldUser = "";
	final String dateFieldDefault = "Date"; 
	
	JLabel oldExtensions = new JLabel(oldExt);
	JLabel newExtensions = new JLabel(newExt);
	JLabel labelProgress = new JLabel("Ready");
	JCheckBox testOnly = new JCheckBox();
	
	ExifTableModel exifData = new ExifTableModel();
	JTable tableExif = new JTable(exifData);

	JTextArea log = new JTextArea ();
	JScrollPane logScroll = new JScrollPane(log, 
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel logPanel = new JPanel(new BorderLayout());
	
	private volatile Thread threadSearch = null;
	
	public JCap parent = null;
	
	SimpleDateFormat getterFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	SimpleDateFormat setterDateFormat = new SimpleDateFormat("yy-MM-dd");
	SimpleDateFormat setterTimeFormat = new SimpleDateFormat("HH-mm-ss");
	Date exifDate;
	GregorianCalendar date= new GregorianCalendar(); 
	/** year, month, day, hours, minutes*/
	int dateOffset[] = {0,0,0,0,0};

	public AchimsManipulationsPanel() {
	
		// Main content panel
		getContentPane().add(panelMain);
		
		// Title and icon
		setTitle("DataBase Manipulations");
		setIconImage(new ImageIcon(getClass().getResource("/images/icon.gif")).getImage());

		// Menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		// File menu
		menuBar.add(makeJMenu("Actions", KeyEvent.VK_A, 
			actionMenuTitles = new String[] {"Select location...", "Rename files!", "---", "Quit"}, 
			new int[] {KeyEvent.VK_S, KeyEvent.VK_R, 0, KeyEvent.VK_Q},
			new int[] {0,0,0,0}, new int[] {0,0,0,0}, this));
		
		// Option menu
		menuBar.add(optionMenu = makeJMenu("Options", KeyEvent.VK_O, 
			optionMenuTitles = new String[] {
				"field name: ", "---",
				"years offset: ", "months offset: ", "days offset: ", "hours offset: ", "minutes offset: ", 
				"---", "reset offsets", "---", "free text: "}, 
			new int[] {
				KeyEvent.VK_N, 0, 
				KeyEvent.VK_Y, KeyEvent.VK_O, KeyEvent.VK_D, KeyEvent.VK_H, KeyEvent.VK_M, 0, KeyEvent.VK_R, 0, 
				KeyEvent.VK_F},
			new int[] {0,0,0,0,0,0,0,0,0,0,0}, new int[] {0,0,0,0,0,0,0,0,0,0,0}, this));
		
		// Labels
		JPanel locLabel = new JPanel(new GridLayout(0,1));
		locLabel.add(new JLabel("Location: ", JLabel.RIGHT));
		locLabel.add(new JLabel("Action: ", JLabel.RIGHT));
		locLabel.add (new JLabel ("Test It before!  ", JLabel.RIGHT));
		
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
		JPanel describeAction = new JPanel(new FlowLayout(FlowLayout.LEADING));
		describeAction.add (oldExtensions);
		JLabel l = new JLabel ( new ImageIcon(getClass().getResource("/images/next_empty.gif")) );
		describeAction.add (l);
		describeAction.add (newExtensions);
		locText.add (describeAction);
		
		/* Dieses Panel und die 3 Zeilen sind dafür da, dass nur bei Klicken IN die CheckBox
		  eine Änderung erfolgt - ansonsten passiert das auch beim Klicken weit dahinter. */ 
		JPanel testPanel = new JPanel (new FlowLayout(FlowLayout.LEADING));
		testPanel.add (testOnly); testOnly.setSelected(true);
		locText.add (testPanel);

		// Main panel
		JPanel actPanel = new JPanel(new BorderLayout());
		actPanel.add(locLabel, BorderLayout.WEST);
		actPanel.add(locText, BorderLayout.CENTER);
		
		// Add border
		actPanel.setBorder(BorderFactory.createTitledBorder("Manipulate JPEG Files"));
		
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
			public void windowClosing(WindowEvent e) { closeWindow(); }
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
		
		log.append("Hi Achim,\nerster Punkt im Optionen-Menu ist dein gesuchter Punkt,\n"+
			"um andere Feldnamen für den Zeitstempel des JPEG-Bildes einzutragen.\n"+
			"Steht nichts drin, nimmt er das 1. Feld, welches mit -> Date <- beginnt.\n"+
			"Du musst nur soviel Text eintragen, bis das Feld eindeutig wird (nicht den kpl. Namen).\n"+
			"Das ist die Bedienbarkeit, die ich momentan am einfachsten umsetzen konnte.\n"+
			"Hoffentlich tut es - ich konnte es leider nicht mehr testen - Gruss Dani!");
	}
	
	/** A convenience method to create a Menu from an array of items */
	private JMenu makeJMenu(String menuName, int menuKeyEvent, 
		String[] itemNames, int[] mnemonics, int[] accelerators, int[] masks, ActionListener listener) {
		
		JMenu m = new JMenu(menuName);
		m.setMnemonic(menuKeyEvent);

		JMenuItem item;
		for(int i = 0; i < itemNames.length; i++) {
			if (itemNames[i] == "---") m.addSeparator();
			else {
				item = new JMenuItem(itemNames[i]);
				item.addActionListener(listener);
				item.setActionCommand(itemNames[i]);  // needed in actionPerformed
				if (mnemonics[i] != 0) item.setMnemonic(mnemonics[i]);
				if (accelerators[i] != 0) item.setAccelerator(KeyStroke.getKeyStroke(accelerators[i], masks[i]));
				m.add(item);
			} 
		}
		return m;
	}

	/** This method handles the items in the menubars */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals(actionMenuTitles[0])) chooseFolder();
		else if (cmd.equals(actionMenuTitles[1])) renameFiles();
		else if (cmd.equals(actionMenuTitles[3])) closeWindow();

		else if (cmd.equals(optionMenuTitles[0])) setFieldName (optionMenuTitles, 0);
		else if (cmd.equals(optionMenuTitles[2])) setDateOffset(optionMenuTitles, 2);
		else if (cmd.equals(optionMenuTitles[3])) setDateOffset(optionMenuTitles, 3);
		else if (cmd.equals(optionMenuTitles[4])) setDateOffset(optionMenuTitles, 4);
		else if (cmd.equals(optionMenuTitles[5])) setDateOffset(optionMenuTitles, 5);
		else if (cmd.equals(optionMenuTitles[6])) setDateOffset(optionMenuTitles, 6);
		else if (cmd.equals(optionMenuTitles[8])) resetDateOffsets(optionMenuTitles);
		else if (cmd.equals(optionMenuTitles[10])) setFreeText (optionMenuTitles, 10);
	}

	private void setDateOffset (String[] labelHeaders, int menuPosition) {
		String buffer = JOptionPane.showInputDialog(this, labelHeaders[menuPosition], 
			new Integer(dateOffset[menuPosition-2])); // 080728 2 Punkte vorne eingeschoben
		try {
			dateOffset[menuPosition-2] = Integer.parseInt(buffer);
		} catch (NumberFormatException nfe) {} // wenn int nicht lesbar, bleibt alte stehen

		if (dateOffset[menuPosition-2] == 0)
			buffer = labelHeaders[menuPosition];
		else buffer = labelHeaders[menuPosition] + dateOffset[menuPosition-2];
		optionMenu.getItem(menuPosition).setText(buffer);
	}

	private void resetDateOffsets (String[] labelHeaders) {
		for (int i=0; i<5; i++) {
			dateOffset[i] = 0;
			optionMenu.getItem(i+2).setText(labelHeaders[i+2]);
		}
	}

	private void setFreeText (String[] labelHeaders, int menuPosition) {
		userFileInsertion =JOptionPane.showInputDialog(this, labelHeaders[menuPosition], userFileInsertion);
		optionMenu.getItem(menuPosition).setText(labelHeaders[menuPosition] + userFileInsertion);
	}

	private void setFieldName (String[] labelHeaders, int menuPosition) {
		dateFieldUser =JOptionPane.showInputDialog(this, labelHeaders[menuPosition], dateFieldUser);
		optionMenu.getItem(menuPosition).setText(labelHeaders[menuPosition] + dateFieldUser);
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
			// Inits
			log.setText("");
			dateFieldWork = dateFieldDefault;
			if (dateFieldUser.length() > 0) dateFieldWork = dateFieldUser;
			
			// Update the headlines
			counterDouble=1;
			int i = search(textLocation.getText());
			
			// Done
			if (i >= 0)
				labelProgress.setText("Done: " + i + " Files found and renamed.");
			else
				labelProgress.setText("Error during rename: Filecounter = " + i );
			
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
				
				if ( Util.supportsEXIF(filename)){
					exifDate = getDate(filename);
					if (exifDate == null) {
						log.append(filename + ":  No EXIF-data present!\n");
						continue;
					}
					date.setTime(exifDate);

					StringBuffer sb = new StringBuffer();
					
					//Date arithmetic hier
					date.add(Calendar.YEAR, dateOffset[0]);
					date.add(Calendar.MONTH, dateOffset[1]);
					date.add(Calendar.DAY_OF_MONTH, dateOffset[2]);
					date.add(Calendar.HOUR_OF_DAY, dateOffset[3]);
					date.add(Calendar.MINUTE, dateOffset[4]);
					
					sb.append( setterDateFormat.format(date.getTime()) );
					if (userFileInsertion.length() > 0) sb.append(" " + userFileInsertion);
					sb.append( " " + setterTimeFormat.format(date.getTime()) );

					String destFilename = folder.getAbsolutePath() + File.separator + sb.toString() + oldFileExt;
					File fdest = new File (destFilename);
					boolean check = true;
							
					// here goes action - recomment if necessary, but be careful :-)
					if ( ! testOnly.isSelected() ) {
						check = subfile.renameTo(fdest);

						if ( ! check ) { //2nd service - falls Datei schon existierte
							destFilename = folder.getAbsolutePath() + File.separator + sb.toString() 
								+ "_" + counterDouble + oldFileExt;
							fdest = new File (destFilename);
							check = subfile.renameTo(fdest);
							counterDouble ++;
						}
					}

					log.append(filename + " -> " + destFilename + "\n");
							
					if (!check) found = -1000000; // something was going wrong when negative value
						else found++;
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
	
	private Date getDate (String filename) {
		String dateTime = getExifData (filename, dateFieldWork);
		try {
			return getterFormat.parse(dateTime);
		} catch (Exception pe) {return null;}
	}

	private String getExifData(String filename, String pattern) {
		if (!Util.supportsEXIF(filename)) { return ""; }
		
		try {
			Metadata metadata = JpegMetadataReader.readMetadata(new File (filename));
			
			// iterate through metadata directories
			Iterator directories = metadata.getDirectoryIterator();
			while (directories.hasNext()) {
				Directory directory = (Directory)directories.next();
				
				// iterate through tags
				Iterator tags = directory.getTagIterator();
				while (tags.hasNext()) {
					Tag tag = (Tag)tags.next();
					
					// append each tag to stringbuffer
					try {
						//m.addData(tag.getTagName(), tag.getDescription());
						if (tag.getTagName().startsWith(pattern))
							return tag.getDescription();

					} catch (com.drew.metadata.MetadataException ex) {
						JOptionPane.showMessageDialog(this, "The EXIF data could not be added to the table.", "Error", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		} catch (java.io.FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "The image could not be found\nwhile searching for EXIF data.", "File not found", JOptionPane.WARNING_MESSAGE);
		} catch (com.drew.imaging.jpeg.JpegProcessingException ex) {
			JOptionPane.showMessageDialog(this, "The JPEG image could not be decoded.", "JPEG error", JOptionPane.WARNING_MESSAGE);
		}
	return ""; 
	}
	
	// Close window
	protected void closeWindow() {
		// Save search settings
		JCap.prop.setProperty("SearchFolder", textLocation.getText());
		
		// So we know to create a new search panel next time
		parent.manipulationsPanelVisible = false;

		// Close window
		dispose();
	}

}
