package com.watsonnet.jcap;

// java:
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

// swing:
import javax.swing.*;

// exif
import com.drew.metadata.*;
import com.drew.imaging.jpeg.*;

/* 
 *	 IDEAS:
 *	- TODO Serienbilder 100b6650.jpg anders einzuordnen - in dias bereits erledigt?
- Laden von Bildern im Hintergrund bei Listenanzeige (viell. mit Cursor o. andere -Anim.)
- Präs.modus Vollbild mit Texten und möglicher Positionierung dieser, z:b. in description
	#20 230 200 260 zu Beginn 
	für Kaba Präsentation:
	Vorlage für undekorierten Voll-Bildschirm in freemind: FreeMindSplash.java

- weitere actions (ren, mv, Textliste ...) aller gefundenen Dateien (SearchPanel) impl.
- Hilfe mit Beschreibung der Funktionen
*/

public class JCap extends JFrame implements ActionListener, DropTargetListener {
	public static final String APP_TITLE = "KaBa";
	public static final String APP_VERSION = " 2.0.8";
	
	private String iniFilename ="";
	private static final String INI_FILE = "kaba.ini";
	
	public static Properties prop = new Properties();
	public static File[] images;
	public static int imageIndex = 0;
	
	// Main Panel
	String[] fileMenuTitles, imageMenuTitles, searchMenuTitles, databaseMenuTitles, helpMenuTitles;
	
	// Search panel
	private SearchPanel searchPanel = null;
	public boolean searchPanelVisible = false;
	
	// Database Manipulations Panel
	private ManipulationsPanel manipulationsPanel = null;
	public boolean manipulationsPanelVisible = false;
	public static final boolean showDatabaseChangesMenu = true;
		
	private AchimsManipulationsPanel manipulations01Panel = null;
	public boolean manipulations01PanelVisible = false;
		
	
	// Help panel
	private InfoPanel infoPanel = null;
	private HelpPanel helpPanel = null;
	public boolean helpPanelVisible = false;
	private DX6340_InfoPanel camInfos = null;
	public boolean camInfoPanelVisible = false;
	
	JPanel panelMain = new JPanel();
	JPanel panelTop = new JPanel(new BorderLayout());
	JPanel panelBottom = new JPanel(new BorderLayout());
	
	JToolBar toolbarMain = new JToolBar();
	JButton btnChooser = new JButton();
	JButton btnNext = new JButton();
	JButton btnPrev = new JButton();
	JButton btnExif = new JButton();
	JButton btnNextEmpty = new JButton();
	JButton btnZoom = new JButton();
	JButton btnFolderInfo = new JButton();
	JButton btnSearch = new JButton();
	JButton btnApplyKeywords = new JButton();
	
	JPanel panelExif = new JPanel(new BorderLayout());
	ExifTableModel exifData = new ExifTableModel();
	JTable tableExif = new JTable(exifData);
	JScrollPane scrollExif = new JScrollPane(tableExif);
	
	JPanel panelImageInfo = new JPanel(new BorderLayout());
	ImagePanel panelImage = new ImagePanel();
	ImagePanelWithBuffering panelBufferedImage = new ImagePanelWithBuffering();
	JLabel labelFilename = new JLabel();
	JTextField textPath = new JTextField();
	
	JPanel panelText = new JPanel(new BorderLayout());
	JTextArea textDescription = new JTextArea();
	JScrollPane scrollDescription = new JScrollPane(textDescription);
	JTextField inputKeywords = new JTextField();
	JTextField inputCaption = new JTextField();
	
	JSplitPane splitComment;
	JSplitPane splitImageExif;
	
	// constructor
	public JCap() {
/*		+----------------------------+
		| toolbar                  
		+-------------+-------------+
		|   image            exif     
		|                         
	    +-------------+------------+
		| comment text area        
		+---------------------------+	*/
		
		// Load ini file
		loadIni();
		
		// ---------------------------------------------------------------------
		// Main panel
		// ---------------------------------------------------------------------
		panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.Y_AXIS));
		setTitle(APP_TITLE + " " + APP_VERSION);
		getContentPane().add(panelMain);
		setIconImage(new ImageIcon(getClass().getResource("/images/icon.gif")).getImage());
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Exif table
		// ---------------------------------------------------------------------
		panelExif.add(scrollExif, BorderLayout.CENTER);
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Text areas
		// ---------------------------------------------------------------------
		inputKeywords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				inputCaption.setCaretPosition(0);
				inputCaption.requestFocus();
			}
		});
		
		inputCaption.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				textDescription.setCaretPosition(0);
				textDescription.requestFocus();
			}
		});
		
		textPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openFolder(textPath.getText());
			}
		});
		
		textDescription.setLineWrap(true);
		textDescription.setWrapStyleWord(true);
		textDescription.setFont(Font.getFont("SansSerif"));
		scrollDescription.getVerticalScrollBar().setUnitIncrement(textDescription.getFontMetrics(textDescription.getFont()).getHeight());
		scrollDescription.getHorizontalScrollBar().setUnitIncrement(textDescription.getFontMetrics(textDescription.getFont()).stringWidth("W"));
		scrollDescription.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel panelKeywordsCaptionLabels = new JPanel();
		panelKeywordsCaptionLabels.setLayout(new GridLayout(0,1));
		panelKeywordsCaptionLabels.add(new JLabel("Keywords :  ", JLabel.RIGHT));
		panelKeywordsCaptionLabels.add(new JLabel("Caption :  ", JLabel.RIGHT));
		
		JPanel panelKeywordsAndButton = new JPanel(new BorderLayout());
		panelKeywordsAndButton.add(inputKeywords, BorderLayout.CENTER);
		panelKeywordsAndButton.add(btnApplyKeywords, BorderLayout.EAST);
		inputKeywords.setBackground(new Color (255, 255, 222));
		
		JPanel panelKeywordsCaptionFields = new JPanel();
		panelKeywordsCaptionFields.setLayout(new GridLayout(0,1));
		panelKeywordsCaptionFields.add(panelKeywordsAndButton);
		panelKeywordsCaptionFields.add(inputCaption);
		
		JPanel panelCaptionKeywords = new JPanel(new BorderLayout());
		panelCaptionKeywords.add(panelKeywordsCaptionLabels, BorderLayout.WEST);
		panelCaptionKeywords.add(panelKeywordsCaptionFields, BorderLayout.CENTER);
		
		JPanel panelDescription = new JPanel(new BorderLayout());
		panelDescription.setBorder(BorderFactory.createTitledBorder("Description"));
		panelDescription.add(scrollDescription, BorderLayout.CENTER);
		
		panelText.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		panelText.add(panelCaptionKeywords, BorderLayout.NORTH);
		panelText.add(panelDescription, BorderLayout.CENTER);
		panelBottom.add(panelText, BorderLayout.CENTER);
		//panelBottom.setBorder(BorderFactory.createEtchedBorder());
		// ---------------------------------------------------------------------
		
		// ---------------------------------------------------------------------
		// Menu
		// ---------------------------------------------------------------------
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		// File menu
		menuBar.add(makeJMenu("File", KeyEvent.VK_F, 
			fileMenuTitles = new String[] {"Open folder ...", "---", "Quit"}, 
			new int[] {KeyEvent.VK_O, 0, KeyEvent.VK_Q},
			new int[] {KeyEvent.VK_O, 0, KeyEvent.VK_Q}, 
			new int[] {ActionEvent.CTRL_MASK, 0, ActionEvent.CTRL_MASK}, 
			this));
		
		// Image menu
		menuBar.add(makeJMenu("Image", KeyEvent.VK_I, 
			imageMenuTitles = new String[] {"view 10 images Up", "view Previous image", "view Next image", "view 10 images Down", 
				"view next image without Keywords", 
				"---",  "view First image", "view Last image", "view image by Address",
				"---",  "show image full siZe", "append EXIF data to comment", "update folder Information"}, 
			new int[] {KeyEvent.VK_U, KeyEvent.VK_P, KeyEvent.VK_N, KeyEvent.VK_D, KeyEvent.VK_K, 
				0, KeyEvent.VK_F, KeyEvent.VK_L, KeyEvent.VK_A,
				0, KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_I},
			new int[] {KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN, KeyEvent.VK_PAGE_DOWN, 0, 
				0, KeyEvent.VK_HOME, KeyEvent.VK_END, 0,
				0, KeyEvent.VK_Z, 0, 0}, 
			new int[] {ActionEvent.SHIFT_MASK, 0, 0, ActionEvent.SHIFT_MASK, 0, 0, 0, 0, 0, 0, ActionEvent.CTRL_MASK, 0, 0}, 
			this));
		
		// Search menu
		menuBar.add(makeJMenu("Search", KeyEvent.VK_S, 
			searchMenuTitles = new String[] {"search for Pictures ..."}, 
			new int[] {KeyEvent.VK_P},
			new int[] {KeyEvent.VK_F}, 
			new int[] {ActionEvent.CTRL_MASK}, 
			this));
		
		// Database Manipulations 
		if (showDatabaseChangesMenu){
			JMenu jmn;
			menuBar.add(jmn=makeJMenu("Database Manipulations", KeyEvent.VK_D, 
				databaseMenuTitles = new String[] {"rename Jpeg-files for Achim..."}, 
				new int[] {KeyEvent.VK_A},
				new int[] {0}, 
				new int[] {0}, 
				this));
			jmn.setToolTipText ("HANDLE WITH CARE AND TEST BEFORE ACTION!!!");

			/*			war mal erster Eintrag im Databases-Menu	

						menuItem = new JMenuItem("Rename text-files");
							menuItem.setMnemonic(KeyEvent.VK_R);
							menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
							menuItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent evt) {
									renameAllTextFiles();
								}
							});
							menu.add(menuItem);
			*/
		}

		// Help menu 
		menuBar.add(makeJMenu("Help", KeyEvent.VK_H, 
			helpMenuTitles = new String[] {"show DX6340 Infos", "show Helptext", "show Info"}, 
			new int[] {KeyEvent.VK_D, KeyEvent.VK_H, KeyEvent.VK_I},
			new int[] {0, 0, 0}, 
			new int[] {0, 0, 0}, 
			this));
		

		// ---------------------------------------------------------------------
			
		// ---------------------------------------------------------------------
		// Toolbar TODO: wie Menus machen ist kürzer 
		// ---------------------------------------------------------------------
		panelTop.add(toolbarMain, BorderLayout.NORTH);
		
		// Folder button
		btnChooser.setToolTipText("Choose a folder to browse");
		btnChooser.setIcon(new ImageIcon(getClass().getResource("/images/open.gif")));
		btnChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				chooseFolder();
			}
		});
		toolbarMain.add(btnChooser);
		
		// Previous button
		btnPrev.setToolTipText("View previous image");
		btnPrev.setIcon(new ImageIcon(getClass().getResource("/images/previous.gif")));
		btnPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				getImageByIncrement(-1, true, false);
			}
		});
		toolbarMain.add(btnPrev);
		
		// Next button
		btnNext.setToolTipText("View next image");
		btnNext.setIcon(new ImageIcon(getClass().getResource("/images/next.gif")));
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				getImageByIncrement(1, true, false);
			}
		});
		toolbarMain.add(btnNext);
		
		// Next without text button
		btnNextEmpty.setToolTipText("View next image without keywords");
		btnNextEmpty.setIcon(new ImageIcon(getClass().getResource("/images/next_empty.gif")));
		btnNextEmpty.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				getNextImageWithNoKeyword();
			}
		});
		toolbarMain.add(btnNextEmpty);
		
		// Zoom into image
		btnZoom.setToolTipText("Show image full size");
		btnZoom.setIcon(new ImageIcon(getClass().getResource("/images/zoom.gif")));
		btnZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showFullSize(imageIndex);
			}
		});
		toolbarMain.add(btnZoom);
		
		// Append EXIF button
		btnExif.setToolTipText("Append EXIF data to comment");
		btnExif.setIcon(new ImageIcon(getClass().getResource("/images/appendexif.gif")));
		btnExif.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				copyExifToComment(imageIndex);
			}
		});
		toolbarMain.add(btnExif);
		
		// Folder information button
		btnFolderInfo.setToolTipText("Update folder information");
		btnFolderInfo.setIcon(new ImageIcon(getClass().getResource("/images/folderinfo.gif")));
		btnFolderInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				updateFolderInfo();
			}
		});
		toolbarMain.add(btnFolderInfo);
		
		// Search
		btnSearch.setToolTipText("Search");
		btnSearch.setIcon(new ImageIcon(getClass().getResource("/images/find.gif")));
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				showSearchDialog();
			}
		});
		toolbarMain.add(btnSearch);
		
		// ---------------------------------------------------------------------
		// Other buttons
		// ---------------------------------------------------------------------
		// Automatic Use
		btnApplyKeywords.setToolTipText("Apply to all in folder");
		btnApplyKeywords.setIcon(new ImageIcon(getClass().getResource("/images/saveall.gif")));
		btnApplyKeywords.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				applyKeywordsToAll();
			}
		});
		
		// Create split panes
		splitComment = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelTop, panelBottom);
		splitComment.setOneTouchExpandable(true);
		splitComment.setContinuousLayout(true);
		splitComment.setDividerSize(8);
		//splitImageExif = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelImage, panelExif);
		splitImageExif = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelBufferedImage, panelExif);
		splitImageExif.setOneTouchExpandable(true);
		splitImageExif.setContinuousLayout(true);
		splitImageExif.setDividerSize(8);
		
		// Image panel
		//panelImage.setBorder(BorderFactory.createEtchedBorder());
		panelImageInfo.add(splitImageExif, BorderLayout.CENTER);
		JPanel panelPath = new JPanel(new BorderLayout());
		panelPath.add(labelFilename, BorderLayout.WEST);
		panelPath.add(textPath, BorderLayout.CENTER);
		panelImageInfo.add(panelPath, BorderLayout.NORTH);
		labelFilename.setBorder(BorderFactory.createEtchedBorder());
		panelTop.add(panelImageInfo, BorderLayout.CENTER);
		
		// Add panels
		panelMain.add(splitComment, BorderLayout.CENTER);
		
		// Enable drag and drop
		this.setDropTarget(new DropTarget(this, this));
		
		// Validate
		panelMain.validate();
		
		// Add window listener to trap closing event
		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent e) {
				closeApplication();
			}
			public void windowClosed(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowActivated(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
		});
		
		// Load window position
		try {
			int x = Integer.parseInt(prop.getProperty("WindowX"));
			int y = Integer.parseInt(prop.getProperty("WindowY"));
			int w = Integer.parseInt(prop.getProperty("WindowW"));
			int h = Integer.parseInt(prop.getProperty("WindowH"));
			setBounds(x, y, w, h);
		} catch (NumberFormatException ex) {
			setBounds(0, 0, 600, 450);
		}
		
		// Load split locations
		try {
			int p1 = Integer.parseInt(prop.getProperty("Split1"));
			int p2 = Integer.parseInt(prop.getProperty("Split2"));
			splitImageExif.setDividerLocation(p1);
			splitComment.setDividerLocation(p2);
		} catch (NumberFormatException ex) {
			splitImageExif.setDividerLocation(400);
			splitComment.setDividerLocation(300);
		}
		
		// Make frame visible
		setVisible(true);
		
		// Open images in last folder opened
		showFirstTimeImage();
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
		if (cmd.equals(fileMenuTitles[0])) chooseFolder();
		else if (cmd.equals(fileMenuTitles[2])) closeApplication();

		else if (cmd.equals(imageMenuTitles[0])) getImageByIncrement(-10, false, false);
		else if (cmd.equals(imageMenuTitles[1])) getImageByIncrement(-1, true, true);
		else if (cmd.equals(imageMenuTitles[2])) getImageByIncrement(1, true, true);
		else if (cmd.equals(imageMenuTitles[3])) getImageByIncrement(10, false, false);
		else if (cmd.equals(imageMenuTitles[4])) getNextImageWithNoKeyword();
		else if (cmd.equals(imageMenuTitles[6])) getImageByAddress(0);
		else if (cmd.equals(imageMenuTitles[7])) getImageByAddress(10000); // TODO zu korr. bei Gelegenheit
		else if (cmd.equals(imageMenuTitles[8])) getImageByAddress(0); // TODO zu impl.
		else if (cmd.equals(imageMenuTitles[10])) showFullSize(imageIndex);
		else if (cmd.equals(imageMenuTitles[11])) copyExifToComment(imageIndex);
		else if (cmd.equals(imageMenuTitles[12])) updateFolderInfo();

		else if (cmd.equals(searchMenuTitles[0])) showSearchDialog();

		else if (cmd.equals(databaseMenuTitles[0])) renameAllJPEGFiles();

		else if (cmd.equals(helpMenuTitles[0])) showCameraInfos();
		else if (cmd.equals(helpMenuTitles[1])) showHelpDialog();
		else if (cmd.equals(helpMenuTitles[2])) showInfoDialog();
	}


	
	// Search
	// TODO alle DialogFenster vereinheitlichen in HTMLInfoFrame
	// TODO diese Methoden dafür ebenfalls vereinheitlichen 
	protected void showSearchDialog() {
		if (!searchPanelVisible) {
			searchPanel = new SearchPanel();
			searchPanel.parent = this;
			searchPanel.setBounds(getBounds().x + getBounds().width/5, getBounds().y + getBounds().height/5, getBounds().width*3/5, getBounds().height*3/5);
			searchPanel.validate();
			
			// So we only create one search panel.
			searchPanelVisible = true;
		} else {
			searchPanel.textKeywords.requestFocus();
			searchPanel.show();
			searchPanel.setState(Frame.NORMAL); // stelle wieder her, wenn iconified
		}
	}

	// Database Manipulations
	
	/** rename the TextFile-Label-Extensions im ManipulationsPanel- 
	 * 	genutzt für _caption.txt --> _keywords.txt*/
	protected void renameAllTextFiles() {
		if (!manipulationsPanelVisible) {
			manipulationsPanel = new ManipulationsPanel();
			manipulationsPanel.parent = this;
			manipulationsPanel.setBounds(getBounds().x + getBounds().width/5, getBounds().y + getBounds().height/5, getBounds().width*3/5, getBounds().height*3/5);
			manipulationsPanel.validate();
			
			// So we only create one search panel.
			manipulationsPanelVisible = true;
		} else {
			manipulationsPanel.show();
			manipulationsPanel.setState(Frame.NORMAL); // stelle wieder her, wenn iconified
		}
					
	}

	/** rename the JPEGs im ManipulationsPanel- 
	 * 	genutzt für *.jpg --> date_in_jpg.jpg*/
	protected void renameAllJPEGFiles() {
		if (!manipulations01PanelVisible) {
			manipulations01Panel = null;
			manipulations01Panel = new AchimsManipulationsPanel();
			manipulations01Panel.parent = this;
			manipulations01Panel.setBounds(getBounds().x + getBounds().width/5, getBounds().y + getBounds().height/5, getBounds().width*3/5, getBounds().height*3/5);
			manipulations01Panel.validate();
			
			// So we only create one search panel.
			manipulations01PanelVisible = true;
		} else {
			manipulations01Panel.show();
			manipulations01Panel.setState(Frame.NORMAL); // stelle wieder her, wenn iconified
		}
		manipulations01Panel.testOnly.setSelected(true);			
	}

	// Help
	// Help
	protected void showCameraInfos() {
		if (! camInfoPanelVisible) {
			camInfos = new DX6340_InfoPanel();
			camInfos.parent = this;
			camInfos.setBounds(getBounds().x + getBounds().width/20, getBounds().y + getBounds().height/20, getBounds().width*9/10, getBounds().height*9/10);
			camInfos.validate();
			
			// So we only create one search panel.
			camInfoPanelVisible = true;
		} else {
			camInfos.show();
			camInfos.setState(Frame.NORMAL); // stelle wieder her, wenn iconified
		}
					
	}
	
	protected void showHelpDialog() {
		if (!helpPanelVisible) {
			helpPanel = new HelpPanel();
			helpPanel.parent = this;
			helpPanel.setBounds(getBounds().x + getBounds().width/5, getBounds().y + getBounds().height/5, getBounds().width*3/5, getBounds().height*3/5);
			helpPanel.validate();
			
			// So we only create one search panel.
			helpPanelVisible = true;
		} else {
			helpPanel.show();
			helpPanel.setState(Frame.NORMAL); // stelle wieder her, wenn iconified
		}
					
	}
	
	protected void showInfoDialog() {
		infoPanel = new InfoPanel(this);
		Point start = new Point (getBounds().x + getBounds().width/4, getBounds().y + getBounds().height/4);
		Rectangle r = new Rectangle (start, infoPanel.getPreferredSize());
		infoPanel.setBounds(r);
		infoPanel.show();
	}
	
	// on start of application: get images and show first
	private void showFirstTimeImage() {
		String lastFolderName = getFolder("LastFolder");
		if (lastFolderName == null) { return; }
		getImages(lastFolderName);
		if (images == null || images.length <= 0) { return; }

		imageIndex = 0;
		
		String lastImageNameLabel = getLastImage();
		if (lastImageNameLabel != null) {
			for (int i=0; i<images.length; i++) {
				if ( (Util.getFilenameLabel (images[i].getName())).equalsIgnoreCase(lastImageNameLabel) ) {
					imageIndex = i;
					break;
				} 
			} 
		}

		showImage(Toolkit.getDefaultToolkit().getImage(images[imageIndex].getAbsolutePath()));
	}
	
	// get images and show first one
	private void showFirstImage() {
		String folderName = getFolder("LastFolder");
		if (folderName == null) { return; }
		getImages(folderName);
		if (images == null || images.length <= 0) { return; }
		imageIndex = 0;

		showImage(Toolkit.getDefaultToolkit().getImage(images[imageIndex].getAbsolutePath()));
	}
	
	// show the image at the current index
	public void showImage(Image img, boolean focus) {
		if (images != null && images.length > 0) {
			panelBufferedImage.setImage(img);
			panelBufferedImage.repaint();
			loadText(imageIndex, focus);
		}
	}
	
	// show the image at the current index
	public void showImagePrepareNext (Image img, Image imgNext) {
		if (images != null && images.length > 0) {
			panelBufferedImage.setImage(img, imgNext);
			panelBufferedImage.repaint();
			loadText(imageIndex, true);
			// setImage so weit als mgl. von prepare entfernen, damit gezeigtes image schneller lädt
			panelBufferedImage.prepareNextImage(imgNext);
		}
	}
	
	public void showImage(Image img) {
		showImage(img, true);
	}
	
	// Show the image full size
	protected void showFullSize(int n) {
		ImagePanel ip = new ImagePanel();
		ip.setImage(Toolkit.getDefaultToolkit().getImage(images[n].getAbsolutePath()));
		
		JFrame f = new JFrame(APP_TITLE + " " + APP_VERSION + " " + images[n].getName());
		f.setIconImage(new ImageIcon(getClass().getResource("/images/icon.gif")).getImage());
		f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		//f.setBounds(getBounds().x, getBounds().y, getBounds().width, getBounds().height); // Set frame to same size and position as main window
		f.setSize(getSize()); // Set frame to same size as main window
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(ip, BorderLayout.CENTER);
		f.setVisible(true);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH); // maximize
	}
	
	// show image of list with the given increment
	protected void getImageByIncrement(int increment, boolean rollOverEnd, boolean loadNextPicture) {
		if (images == null || images.length <= 0) { return; }
		
		// update text
		saveText(imageIndex);
		
		// update image pointer
		imageIndex += increment;
		
		// Zähler manipulieren, am Anfang / Ende der Bildfolge
		if (imageIndex < 0) {
			if (rollOverEnd) imageIndex = images.length-1; 
			else imageIndex = 0;
		} 
		else	if (imageIndex > images.length-1) {
			if (rollOverEnd) imageIndex = 0;
			else imageIndex = images.length-1;
		}
		
		Image imageToLoad = null;
		imageToLoad = Toolkit.getDefaultToolkit().getImage(images[imageIndex].getAbsolutePath());

		// Zähler für vorzubereitende Bilder manipulieren, 1 Bild vor Anfang / Ende der Bildfolge
		if (loadNextPicture) {
			Image imageToPrepare = null;
			int imageToPrepareIndex;

			imageToPrepareIndex = imageIndex + increment;
			if (imageToPrepareIndex < 0) {
				imageToPrepareIndex = images.length-1; // boolean rollOver noch nicht berücksichtigt 
			}
			else	if (imageToPrepareIndex > images.length-1) {
				imageToPrepareIndex = 0;
			}
			imageToPrepare = Toolkit.getDefaultToolkit().getImage(images[imageToPrepareIndex].getAbsolutePath());
			showImagePrepareNext(imageToLoad, imageToPrepare);
		} else {
			showImage(imageToLoad); 
		}
	}
	
	// show image of list with the given address
	protected void getImageByAddress(int address) {
		if (images == null || images.length <= 0) { return; }
		saveText(imageIndex); 		// update text
		imageIndex = address; // update image pointer
		
		// Zähler manipulieren, am Anfang / Ende der Bildfolge
		if (imageIndex < 0) imageIndex = 0; 
		else	if (imageIndex > images.length-1) imageIndex = images.length-1;
		
		Image imageToLoad = null;
		imageToLoad = Toolkit.getDefaultToolkit().getImage(images[imageIndex].getAbsolutePath());

		showImage(imageToLoad); 
	}
	
	// show the next image without keywords
	protected void getNextImageWithNoKeyword() {
		if (images == null || images.length <= 0) { return; }
		
		// update text
		saveText(imageIndex);
		
		// find next image without comments
		int i = imageIndex+1;
		if (i > images.length-1) { i = 0; }
		int start = imageIndex;
		boolean found = false;
		while (true) {
			File f = new File(getCaptionFileName(i));
			if (!f.exists() || f.length() <= 0) {
				imageIndex = i;
				found = true;
				break;
			}
			if (i == start) { break; }
			i += 1;
			if (i > images.length-1) { i = 0; }
		}
		
		if (found) {
			// show image
			showImage(Toolkit.getDefaultToolkit().getImage(images[imageIndex].getAbsolutePath()));
		} else {
			// message box, everything is commented
			JOptionPane.showConfirmDialog(this, "All images in this folder have keywords.", "Not found", JOptionPane.OK_CANCEL_OPTION);
		}
	}
	
	// Apply current keywords to all images in this folder after confirmation
	protected void applyKeywordsToAll() {
		if (images == null || images.length <= 0) { return; }
		
		if (JOptionPane.showConfirmDialog(this, "All existing keywords will be erased.\nAre you sure you want to apply these keywords\nto all images in this folder?") == JOptionPane.YES_OPTION) {
			String keywords = inputCaption.getText();
			for(int i=0;i<=images.length-1;i++) {
				saveFile(new File(getKeywordsFileName(i)), keywords);
			}
			JOptionPane.showConfirmDialog(this, "Keywords applied to all images.", "Done", JOptionPane.OK_CANCEL_OPTION);
		}
	}
	
	/** get the filename of the txt file associated with each image */
	private String getTextFileName(int n) {
		return(Util.getTextFilename(images[n].getAbsolutePath()));
	}
	
	/** get the filename of the txt file associated with each image */
	private String getKeywordsFileName(int n) {
		return(Util.getKeywordsFilename(images[n].getAbsolutePath()));
	}
	
	/** get the filename of the txt file associated with each image */
	private String getCaptionFileName(int n) {
		return(Util.getCaptionFilename(images[n].getAbsolutePath()));
	}
	
	/** Loads the txt file associated with the image and displays the text
	 *	in the textArea box. 
	 * @param n
	 * @param focus
	 */
	private void loadText(int n, boolean focus) {
		// Clear existing text
		clearText();
		
		// Get descriptions
		inputKeywords.setText(Util.readFile(new File(getKeywordsFileName(n))));
		inputCaption.setText(Util.readFile(new File(getCaptionFileName(n))));
		textDescription.setText(Util.readFile(new File(getTextFileName(n))));
		
		// Move caret to top and get focus
		if (focus) {
			inputKeywords.setCaretPosition(0);
			inputKeywords.requestFocus();
		}
		
		// Get EXIF data
		addExifData(exifData, n);
		
		// Update path
		textPath.setText(images[n].getParent());
		textPath.setCaretPosition(textPath.getText().length()-1);
		
		// File index and max number of images
		String index = (new Integer(n+1)).toString() + "/" + (new Integer(images.length)).toString();
		
		// Update label
		labelFilename.setText(Util.getFilenameLabel(images[n].getName()) + " (" + index + ")");
	}
	
	private void loadText(int n) {
		loadText(n, true);
	}
	
	// Clear the text areas
	private void clearText() {
		textDescription.setText("");
		inputCaption.setText("");
		inputKeywords.setText("");
		labelFilename.setText("");
		exifData.clearData();
	}
	
	/**	Save the given data to the given file */
	private void saveFile(File f, String data) {
		// Don't create txt files if the DeleteEmpty option is on and there is no comment
		if (prop.getProperty("DeleteEmpty").equalsIgnoreCase("1") && data.length() <= 0) {
			if (f.exists()) { f.delete(); }
			return;
		}
		
		Util.saveFile(f, data);
	}
	
	/** Save text of the current picture to <picture name>.txt */
	public void saveText(int n) {
		saveFile(new File(getTextFileName(n)), textDescription.getText());
		saveFile(new File(getCaptionFileName(n)), inputCaption.getText());
		saveFile(new File(getKeywordsFileName(n)), inputKeywords.getText());
	}
	
	// appends the exif data to the comment textarea
	protected void copyExifToComment(int n) {
		if (images == null || images.length <= 0) { return; }
		
		StringBuffer sb = new StringBuffer();
		
		// get current text
		sb.append(textDescription.getText());
		sb.append("\n");
		
		// get exif
		sb.append(getExifData(n));
		
		// set text
		textDescription.setText(sb.toString());
	}
	
	// gets the exif data and returns it as a string
	private String getExifData(int n) {
		if (images == null || images.length <= 0) { return(""); }
		if (!Util.supportsEXIF(images[n].getAbsolutePath())) { return(""); }
		
		try {
			StringBuffer sb = new StringBuffer();
			Metadata metadata = JpegMetadataReader.readMetadata(images[n]);
			
			// iterate through metadata directories
			Iterator directories = metadata.getDirectoryIterator();
			while (directories.hasNext()) {
				Directory directory = (Directory)directories.next();
				
				// iterate through tags
				Iterator tags = directory.getTagIterator();
				while (tags.hasNext()) {
					Tag tag = (Tag)tags.next();
					
					// append each tag to stringbuffer
					if (sb.length() > 0) { sb.append("\n"); }
					sb.append(tag.toString());
				}
			}
			
			// return text
			return(sb.toString());
		} catch (java.io.FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "The image could not be found\nwhile searching for EXIF data.", "File not found", JOptionPane.WARNING_MESSAGE);
			return("");
		} catch (com.drew.imaging.jpeg.JpegProcessingException ex) {
			JOptionPane.showMessageDialog(this, "The JPEG image could not be decoded.", "JPEG error", JOptionPane.WARNING_MESSAGE);
			return("");
		}
	}
	
	// Add exif data to table mode
	private void addExifData(ExifTableModel m, int n) {
		if (images == null || images.length <= 0) { return; }
		if (!Util.supportsEXIF(images[n].getAbsolutePath())) { return; }
		
		try {
			Metadata metadata = JpegMetadataReader.readMetadata(images[n]);
			
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
						m.addData(tag.getTagName(), tag.getDescription());
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
	}
	
	// choose a folder
	protected void chooseFolder() {
		StringBuffer dest = new StringBuffer();
		String oldDir;

		if (images != null && images.length > 0) {
			oldDir = images[imageIndex].getPath();
		} else {
			oldDir = getFolder("LastFolder");
		}
		
		if (Util.chooseFolder (dest, oldDir, panelMain, false)) {
			openFolder(dest.toString());
		}
	}
	
	// open the chosen folder and start showing images
	protected void openFolder(String folder) {
		// save last folder
		setFolder("LastFolder", folder);
		
		// get images and show first one
		showFirstImage();
	}
	
	// save folders in Properties
	void setFolder(String folderKey, String folder) {
		prop.setProperty(folderKey, folder);
	}
	
	// get last folder
	String getFolder(String folderKey) {
		return(prop.getProperty(folderKey));
	}
	
	// save last Image shwon
	private void setLastImage (String ImageName) {
		prop.setProperty("LastImage", ImageName);
	}
	
	// get last images shown
	private String getLastImage() {
		return(prop.getProperty("LastImage"));
	}
	
	// update folder information
	protected void updateFolderInfo() {
		if (images == null || images.length <= 0) { return; }
		
		// Get path for title
		String path = images[0].getParent();
		if (path.length() > 40) {
			// find closest File.separator
			int p = path.indexOf(File.separator, path.length()-40);
			if (p < 0) { p = 0; }
			path = "..." + path.substring(p, path.length());
		}
		
		// Frame (defined below)
		// Changed to JDialog so I could get modal (was JFrame)
		final JDialog myframe = new JDialog(this, APP_TITLE + " " + APP_VERSION + " " + path, true);
		
		// Text area
		final JTextArea textInfo = new JTextArea();
		JScrollPane scrollInfo = new JScrollPane(textInfo);
		
		textInfo.setLineWrap(true);
		textInfo.setWrapStyleWord(true);
		textInfo.setEnabled(true);
		textInfo.setFont(Font.getFont("SansSerif"));
		// scrollInfo.setWheelScrollingEnabled(true);
		scrollInfo.getVerticalScrollBar().setUnitIncrement(textInfo.getFontMetrics(textInfo.getFont()).getHeight());
		scrollInfo.getHorizontalScrollBar().setUnitIncrement(textInfo.getFontMetrics(textInfo.getFont()).stringWidth("W"));
		
		JPanel panelText = new JPanel(new BorderLayout());
		panelText.setBorder(BorderFactory.createTitledBorder("Description"));
		panelText.add(scrollInfo, BorderLayout.CENTER);
		
		// Ok and Cancel buttons
		JButton btnOK = new JButton("Ok");
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Save text and close window
				try {
					File f = new File(images[0].getParent(), Util.FOLDERINFO_FILENAME);
					f.createNewFile();
					if (f.canWrite()) {
						FileWriter fw = new FileWriter(f);
						fw.write(textInfo.getText());
						fw.flush();
						fw.close();
					}
				} catch (java.io.IOException ex) {
					JOptionPane.showMessageDialog(myframe, "Could not save folder information.", "Save error", JOptionPane.WARNING_MESSAGE);
				}
				myframe.dispose();
			}
		});
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// just close window
				myframe.dispose();
			}
		});
		JPanel panelButtons = new JPanel(new FlowLayout());
		panelButtons.add(btnOK);
		panelButtons.add(btnCancel);
		
		// Load text
		try {
			File f = new File(images[0].getParent(), Util.FOLDERINFO_FILENAME);
			StringBuffer sb = new StringBuffer();
			if (f.canRead()) {
				String line;
				BufferedReader br = new BufferedReader(new FileReader(f));
				while ((line = br.readLine()) != null) {
					if (sb.length() > 0) { sb.append("\n"); }
					sb.append(line);
				}
			}
			textInfo.setText(sb.toString());
			
			// Move caret to top and get focus
			inputKeywords.setCaretPosition(0);
			inputKeywords.requestFocus();
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(this, "Could not read folder information.", "Read error", JOptionPane.WARNING_MESSAGE);
		}
		
		// New frame
		//myframe.setIconImage(new ImageIcon(getClass().getResource("/images/icon.gif")).getImage()); // method of JFrame
		myframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); // same as cancel
		// set frame to same size and position as main window
		myframe.setBounds(getBounds().x + getBounds().width/5, getBounds().y + getBounds().height/5, getBounds().width*3/5, getBounds().height*3/5);
		// set frame to same size as main window
		//f.setSize(getSize());
		myframe.getContentPane().setLayout(new BorderLayout());
		myframe.getContentPane().add(panelText, BorderLayout.CENTER);
		myframe.getContentPane().add(panelButtons, BorderLayout.SOUTH);
		myframe.setVisible(true);
	}
	
	// Get a File[] of images in the given folder
	public void getImages(String folder) {
		File f = new File(folder);
		images = f.listFiles(new FileFilter() {
			public boolean accept(File f) {
				// Only include certain types in the image list
				if (f != null) {
					return(Util.isImageType(f.getAbsolutePath()));
				}
				return(false);
			}
		});
		
		// sort file list
		// TODO anzupassen an DX6340-FileNames z.B. Nachfrage if Exif-Kamertyp.startsWith(KODAK)
		if (images != null && images.length > 0) {
			Arrays.sort(images);
		}
	}
	
	// Get the user specific ini filename based on INI_FILE
	private String getIniFilename() {
		
		String homeName = System.getProperty("user.home") + File.separator + "KaBa";
		String dirName = System.getProperty("user.dir");
		
		File fileInHome = new File(homeName); // lediglich Verz.-Test nicht Datei-Test auf "kaba.ini"
		
		if ( ! fileInHome.exists()) {
			File fileInDir = new File(dirName + File.separator + INI_FILE);
			if ( ! fileInDir.exists()) {
				int yes = JOptionPane.showConfirmDialog( this,
					"Initialisierungs-Datei nicht gefunden.\n" + "Datei erzeugen?",
					"File not found", JOptionPane.YES_NO_OPTION);
				if (yes == 0) {
					fileInHome.mkdirs();
					return(homeName + File.separator + INI_FILE);
				}
				return null;
			}
			return(dirName + File.separator + INI_FILE);
		}
		return(homeName + File.separator + INI_FILE);
	}
	
	// load settings
	private void loadIni() {
		try {
			iniFilename = getIniFilename();
			File f = new File (iniFilename);
			if (f.exists()) {
				prop.load(new FileInputStream(iniFilename));
			} else {
				f.createNewFile();
			}
			
			// Defaults
			if (prop.getProperty("DeleteEmpty") == null) {
				prop.setProperty("DeleteEmpty", "1");
			}
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "Settings file not found.", "Read error", JOptionPane.WARNING_MESSAGE);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Could not read settings.", "Read error", JOptionPane.WARNING_MESSAGE);
		} catch (NullPointerException ex) { // wird erreicht, wenn ohne ini gearbeitet wird
		}
	}
	
	// save settings
	public void saveIni() {
		try {
			// Window size and position
			Rectangle r = getBounds();
			prop.setProperty("WindowX", (new Integer(r.x)).toString());
			prop.setProperty("WindowY", (new Integer(r.y)).toString());
			prop.setProperty("WindowW", (new Integer(r.width)).toString());
			prop.setProperty("WindowH", (new Integer(r.height)).toString());
			
			// Splitter positions
			prop.setProperty("Split1", (new Integer(splitImageExif.getDividerLocation())).toString());
			prop.setProperty("Split2", (new Integer(splitComment.getDividerLocation())).toString());
			
			// set last shown Image
			if (images != null && images.length > 0 )
				setLastImage(Util.getFilenameLabel(images[imageIndex].getName()));
			
			// Save settings
			if (iniFilename != null) // wenn ohne ini gearbeitet werden soll
				prop.store(new FileOutputStream(getIniFilename()), "KaBa - settings");
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, "Settings file not found.", "Save error", JOptionPane.WARNING_MESSAGE);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Could not save settings.", "Save error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	// quit
	protected void closeApplication() {
		if (images != null && images.length > 0) {
			// save text currently being worked on
			saveText(imageIndex);
		}
		saveIni();
		System.exit(0);
	}
	
	// -------------------------------------------------------------------------
	// DropTargetListener interface
	// -------------------------------------------------------------------------
	// Called while a drag operation is ongoing, when the mouse pointer enters
	// the operable part of the drop site for the DropTarget registered with
	// this listener.
	public void dragEnter(DropTargetDragEvent dtde) {
	}
	// Called while a drag operation is ongoing, when the mouse pointer has
	// exited the operable part of the drop site for the DropTarget registered
	// with this listener.
	public void dragExit(DropTargetEvent dte) {
	}
	// Called when a drag operation is ongoing, while the mouse pointer is still
	// over the operable part of the drop site for the DropTarget registered
	// with this listener.
	public void dragOver(DropTargetDragEvent dtde) {
	}
	// Called when the drag operation has terminated with a drop on the operable
	// part of the drop site for the DropTarget registered with this listener.
	public void drop(DropTargetDropEvent dtde) {
		if ((dtde.getSourceActions() & DnDConstants.ACTION_COPY) != 0) {
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
		} else {
			dtde.rejectDrop();
			return;
		}
		
		// Get the file list and attempt to open the folder that the first file
		// is in.
		try {
			java.util.List files = (java.util.List)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			dtde.dropComplete(true);
			File f = (File)files.get(0);
			if (f.exists()) {
				if (f.isFile()) {
					setFolder("LastFolder", f.getParent());
					getImages(f.getParent());
					for(int i = 0; i <= images.length-1; i++) {
						if (Util.getFilenameLabel(images[i].getAbsolutePath()).equals(Util.getFilenameLabel(f.getAbsolutePath()))) {
							imageIndex = i;
							break;
						}
					}
					showImage(Toolkit.getDefaultToolkit().getImage(images[imageIndex].getAbsolutePath()), false);
				} else {
					openFolder(f.getAbsolutePath());
				}
			} else {
				JOptionPane.showMessageDialog(this, "Drag a file or folder to open it.", "Can't open file", JOptionPane.WARNING_MESSAGE);
			}
			return;
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(this, "Unknown IO error during drag and drop.", "Drag and drop error", JOptionPane.WARNING_MESSAGE);
		} catch (UnsupportedFlavorException ex) {
			JOptionPane.showMessageDialog(this, "The data you tried to drop here is not supported.", "Drag and drop error", JOptionPane.WARNING_MESSAGE);
		}
		dtde.dropComplete(false);
	}
	// Called if the user has modified the current drop gesture.
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}
	// -------------------------------------------------------------------------
	
	// main
	public static void main(String[] args) {
		// Set native look and feel
		// Get the native look and feel class name
		String nativeLF = UIManager.getSystemLookAndFeelClassName();
		
		// Install the look and feel
		try {
			UIManager.setLookAndFeel(nativeLF);
		} catch (InstantiationException e) {
		} catch (ClassNotFoundException e) {
		} catch (UnsupportedLookAndFeelException e) {
		} catch (IllegalAccessException e) {
		}
		
		// Start JCap
		new JCap();
	}
}

