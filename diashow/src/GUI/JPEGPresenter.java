/*
 * Created on 28.05.2008 von dani
 */

package GUI;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.RescaleOp;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import JPEG_Data.JPEGDataReader;
import kaba_Data.ImageStepper;
import kaba_Data.TextStepper;

/** Anzeige für Dias (aus Kaba) mal für den Jahres-Überblick
 * @author heida
 */
public class JPEGPresenter extends JFrame implements KeyListener, MouseListener, MouseWheelListener, Runnable {

	private static final long serialVersionUID = 1L;
	/** Sollen Texte jeglicher Art angezeigt werden? */
	boolean showText=true;
	/** Sollen Bild-Nummer und -anzahl angezeigt werden? */
	boolean showPageNumber=true;
	/** Sollen Exif-Infos des Fotos angezeigt werden? */
	boolean showFotoInfo=false;
	/** Soll das Datum und Uhrzeit der Erzeugung des Bildes angezeigt werden? */
	boolean showDate=true;

	/** Hilfefenster */
	private HelpPanel helpPanel = null;
	public boolean helpPanelVisible = false;

	String workDir, imageDir;
	ImagePanel ip;
	//ImagePanelWithBuffering ipb;
	ImageStepper is;
	TextStepper ts;
	boolean workWithJarFile;
	JPEGDataReader jpegData;

    int picDelay, textDelay, overheadDelay; // variabler Anteil mit +/- veränderbar
    /**
     * Da 20 Zeichen pro sec. wahrscheinlich gut lesbar sind gilt für textDelay
     * Anz Zeichen zB 20 * 50 = 1000 = 1 sec
     */
	private Thread playing = null;
	private double zoom = 1.0;  // zoom factor
	Point oldMousePos, newMousePos;

	public JPEGPresenter() {
		new JPEGPresenter(null);
	}

	public JPEGPresenter(String imagePath) {
		super("Danis Bildbetrachter");
		setIconImage(new ImageIcon(getClass().getResource("/GUI/pingu.png")).getImage());
		try { // 080812 try-Schutz wegen Exception in altem w2k-java
			setDefaultLookAndFeelDecorated(false);
			setUndecorated(true);
			getRootPane().setWindowDecorationStyle(JRootPane.NONE); //set no border
		} catch (NoSuchMethodError e) { /* do nothing */ }
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addKeyListener(this); addMouseListener(this); addMouseWheelListener(this);

		is = new ImageStepper(imagePath);
		workWithJarFile = is.areWeWorkingWithAJarFile();
		ip = new ImagePanel(workWithJarFile);
		// ipb = new ImagePanelWithBuffering();
		getContentPane().add(ip, BorderLayout.CENTER);
		/*getContentPane().getGraphicsConfiguration().getDevice().getFullScreenWindow().getWindows().
		// TODO hieran ändern für Umschaltung auf 2. screen
		public static void showOnScreen( int screen, JFrame frame ) {
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    GraphicsDevice[] gd = ge.getScreenDevices();
		    if( screen > -1 && screen < gd.length ) {
		        frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x, frame.getY());
		    } else if( gd.length > 0 ) {
		        frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, frame.getY());
		    } else {
		        throw new RuntimeException( "No Screens Found" );
		    }
		}

		public static void showOnScreen( int screen, JFrame frame )
		{
		    GraphicsEnvironment ge = GraphicsEnvironment
		        .getLocalGraphicsEnvironment();
		    GraphicsDevice[] gs = ge.getScreenDevices();
		    if( screen > -1 && screen < gs.length )
		    {
		        gs[screen].setFullScreenWindow( frame );
		    }
		    else if( gs.length > 0 )
		    {
		        gs[0].setFullScreenWindow( frame );
		    }
		    else
		    {
		        throw new RuntimeException( "No Screens Found" );
		    }
		}		

		public void moveToScreen() {
		    setVisible(false);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    GraphicsDevice[] screens = ge.getScreenDevices();
		    int n = screens.length;
		    for (int i = 0; i < n; i++) {
		        if (screens[i].getIDstring().contentEquals(settings.getScreen())) {
		            JFrame dummy = new JFrame(screens[i].getDefaultConfiguration());
		            setLocationRelativeTo(dummy);
		            dummy.dispose();
		        }
		    }
		    setVisible(true);
		}
		
		public void showOnScreen(int screen, JFrame frame ) {
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    GraphicsDevice[] gd = ge.getScreenDevices();
		    int width = 0, height = 0;
		    if( screen > -1 && screen < gd.length ) {
		        width = gd[screen].getDefaultConfiguration().getBounds().width;
		        height = gd[screen].getDefaultConfiguration().getBounds().height;
		        frame.setLocation(
		            ((width / 2) - (frame.getSize().width / 2)) + gd[screen].getDefaultConfiguration().getBounds().x, 
		            ((height / 2) - (frame.getSize().height / 2)) + gd[screen].getDefaultConfiguration().getBounds().y
		        );
		        frame.setVisible(true);
		    } else {
		        throw new RuntimeException( "No Screens Found" );
		    }
		}
		//https://stackoverflow.com/questions/4627553/show-jframe-in-a-specific-screen-in-dual-monitor-configuration		
		// TODO bis hier neu

		 */
		pack(); setVisible(true);
		picDelay=3500; //3.5 sec. je Bild zur Betrachtung
		overheadDelay=0;
		
		showImage(is.getFirstImage());
	}


	public static void main (String[] args) {
		
		try {
			UIManager.setLookAndFeel(
				UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Couldn't use the system "
						 + "look and feel: " + e);
		}
		if (args.length > 0) new JPEGPresenter(args[0]);
			else new JPEGPresenter();
	}
  
	public void keyPressed(KeyEvent e) {
		if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
			if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)	goByIncrement(10);
			else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) goByIncrement(-10);
		}
		else if (e.getKeyCode() == KeyEvent.VK_ENTER)	play();
		else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)	goAhead();
		else if (e.getKeyCode() == KeyEvent.VK_SPACE)	goAhead();
		else if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) goBackwards();
		else if (e.getKeyCode() == KeyEvent.VK_END) goToLast();
		else if (e.getKeyCode() == KeyEvent.VK_HOME) goToFirst();
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
		else if (e.getKeyCode() == KeyEvent.VK_T) toggleText();
		else if (e.getKeyCode() == KeyEvent.VK_P) togglePageNumber();
		else if (e.getKeyCode() == KeyEvent.VK_I) toggleFotoInfo();
		else if (e.getKeyCode() == KeyEvent.VK_D) toggleDate();
		else if (e.getKeyCode() == KeyEvent.VK_PLUS) timeLonger(); 
		else if (e.getKeyCode() == KeyEvent.VK_MINUS) timeShorter();
		else if (e.getKeyCode() == KeyEvent.VK_S) toggleScreen();
		else if (e.getKeyCode() == KeyEvent.VK_H) showHelpWindow();
		else if (e.getKeyCode() == KeyEvent.VK_E) ip.endlessLoop= !ip.endlessLoop;
		// TODO brauche ich sowas? Wenn Zahlen eingegeben werden goByIncrement
		
		/*
		else if (e.getKeyCode() == KeyEvent.VK_9) rotateLeft();
		else if (e.getKeyCode() == KeyEvent.VK_8) rotateDown();
		else if (e.getKeyCode() == KeyEvent.VK_7) rotateRight();
		else if (e.getKeyCode() == KeyEvent.VK_0) resetRotation();
		 */
	}
	
	private void toggleScreen() {
		// TODO Auto-generated method stub
		ip.toggleDisplays();
		
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) { 
		int snaps = e.getWheelRotation();
		if (is.hasLowQualityFotos() )
			if (snaps < 0) goBackwards();
			else goAhead();
		else {
			//TODO vergrößern mit Mausrad - nur bei high Quality Fotos
			Point mousePos = e.getLocationOnScreen();
			if (snaps < 0) ip.resizeImage(true, mousePos);
			else ip.resizeImage(false, mousePos);
			ip.repaint();
		}
	}

	private void zoomOut(Point middle) {
		// TODO Auto-generated method stub
		
	}

	private void zoomIn(Point middle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent me) { 
        if(me.getButton() == MouseEvent.BUTTON1) { goAhead();}
        if(me.getButton() == MouseEvent.BUTTON2) {
        	if (ip.isZoomed() )  ip.setZoom (1.0f); else if (!ip.wasZoomed()) play();
        }
        if(me.getButton() == MouseEvent.BUTTON3) { goBackwards();}
	}

	@Override
	public void mouseEntered(MouseEvent me) {	}

	@Override
	public void mouseExited(MouseEvent me) { }

	@Override
	public void mousePressed(MouseEvent me) {
		if (ip.isZoomed()) {
			Point oldMousePos = me.getLocationOnScreen();
		}
		
	}

	volatile private boolean mouseDown = false;

	public void mousePressed(MouseEvent e) {
	    if (e.getButton() == MouseEvent.BUTTON1) {
	        mouseDown = true;
	        initThread();
	    }
	}

	public void mouseReleased(MouseEvent e) {
	    if (e.getButton() == MouseEvent.BUTTON1) {
	        mouseDown = false;
	    }
	}

	volatile private boolean isRunning = false;
	private synchronized boolean checkAndMark() {
	    if (isRunning) return false;
	    isRunning = true;
	    return true;
	}
	private void initThread() {
	    if (checkAndMark()) {
	        new Thread() {
	            public void run() {
	                do {
	                    //do something
	                } while (mouseDown);
	                isRunning = false;
	            }
	        }.start();
	    }
	}

	
	
	@Override
	public void mouseReleased(MouseEvent arg0) { }

	private void play() {
		
		if (playing == null) {
			playing = new Thread (this);
			playing.start(); ip.play=true; ip.repaint();
		} else {
			try {
				playing.interrupt();
				playing = null; ip.play=false; ip.repaint();
			} catch (Exception ex) { }
		}
	}

	@Override
	public void run() {
		int lastPicID=-1;
		while (playing != null) {
			try {
				textDelay = ts.getStory().length()*50 + ts.getCaption().length()*50; 
				// Das Bild muss man ja nur lange anschauen, wenn es neu ist, daher runter mit dem Wert wenn nur ts.stepper 
				// TODO Bildschirm wählbar machen (zB 2. BS)
				int picID = is.getID();
				if (picID == lastPicID) // ohne picDelay war zu heftig bei kurzen Textpassagen
					Thread.sleep((int)(picDelay/3) + textDelay + overheadDelay);
				else Thread.sleep(picDelay + textDelay + overheadDelay);
				lastPicID = picID;
				goAhead();
			} // warten
			catch (InterruptedException ie) {};
		}
		try {
			playing.interrupt();
			playing = null;
		} catch (Exception ex) {}

	}
	
	private void timeLonger() {
		overheadDelay+=500;
	}

	private void timeShorter() {
		if (overheadDelay>-2000) overheadDelay-=500; 
		// da schon picDelay 2500-3500 ist, darf auch ins negative gegangen werden
	}

	/** gehe in der Präsentation vorwärts */
	private void goAhead() {
		if (ts == null) return;
		if ( ts.hasMoreStory() && showText ) {
			ip.setStory(ts.getNextStory());
			ip.repaint();
		} else { 
			if (is.hasNext()) showImage(is.getNext());
			else if (ip.endlessLoop) goToFirst();
		}
	}

	/**gehe in der Präsentation rückwärts */
	private void goBackwards() {
		if (ts == null) return;
		if ( ts.hasLessStory() && showText ) {
			ip.setStory(ts.getBeforeStory());
			ip.repaint();
		} else {
			if (is.hasBefore()) showImage(is.getBefore());
		} 
	}

	/** gehe in der Präsentation relativ vorwärts / rückwärts um incr Bilder */
	private void goByIncrement(int incr) {
		if (is.hasStep(incr)) showImage(is.getStep(incr));
	}

	/** gehe in der Präsentation zum allerersten Bilder */
	private void goToFirst() {
		showImage(is.getFirstImage());
	}

	/** gehe in der Präsentation zum allerletzten Bilder */
	private void goToLast() {
		showImage(is.getLastImage());
	}

	/** schaltet Texte an und aus*/
	private void toggleText() {
		if (showText) { // momentan mal so, besser boolean ins imagePanel und nur zeichnen wenn erf.
			ip.setCaption(""); ip.setStory(""); ip.setPageNumber(""); ip.setFotoInfo(""); ip.setDate("");
			showText= false;
			ip.repaint(); // TODO ins ip rein?
		} else {
			ip.setCaption(ts.getCaption()); ip.setStory(ts.getStory()); 
			if (showPageNumber) ip.setPageNumber(is.getFooterLeft());
			if (showFotoInfo) ip.setFotoInfo(jpegData.getInfo());
			if (showDate) ip.setDate(jpegData.getDate());
			showText= true;
			ip.repaint();
		}
	}

	/** schaltet Bild-Nummern unten links an und aus*/
	private void togglePageNumber() {
		if (showPageNumber) {
			ip.setPageNumber(""); showPageNumber = false; ip.repaint();
		} else {
			ip.setPageNumber(is.getFooterLeft()); showPageNumber = true; ip.repaint();
		}
	}

	/** schaltet Bild-Nummern unten links an und aus*/
	private void toggleFotoInfo() {
		if (showFotoInfo) {
			ip.setFotoInfo(""); showFotoInfo = false; ip.repaint();
		} else {
			ip.setFotoInfo(jpegData.getInfo()); showFotoInfo = true; ip.repaint();
		}
	}

	/** schaltet Bild-Nummern unten links an und aus*/
	private void toggleDate() {
		if (showDate) {
			ip.setDate(""); showDate = false; ip.repaint();
		} else {
			ip.setDate(jpegData.getDate()); showDate = true; ip.repaint();
		}
	}

	/** show the image at the current index*/
	public void showImage(String imageFileName) {
		if (imageFileName != null) {
			ip.setImage(imageFileName);
			ts = new TextStepper (imageFileName, workWithJarFile);
			jpegData = new JPEGDataReader(imageFileName, workWithJarFile);
			if (showText) {
				ip.setCaption(ts.getCaption());
				
				/** 20 Zeichen je 1 sec. (daher 20*50=1000) */
				textDelay=ts.getCaption().length()*50+ts.getStory().length()*50; 
				
				if (ts.hasStory()) ip.setStory(ts.getStory());
				if (showPageNumber) ip.setPageNumber (is.getFooterLeft());
				if (showFotoInfo) ip.setFotoInfo(jpegData.getInfo());
				if (showDate) ip.setDate(jpegData.getDate());
			}
			ip.repaint();
		}
	}

	/** shows a Help Window over the picture for short cuts*/
	private void showHelpWindow() {
		if (!helpPanelVisible) {
			helpPanel = new HelpPanel();
			helpPanel.parent = this;
			helpPanel.setBounds(getBounds().x + getBounds().width/5, getBounds().y + getBounds().height/5, getBounds().width*2/7, getBounds().height*3/5);
			helpPanel.validate();

			// So we only create one search panel.
			helpPanelVisible = true;
			helpPanel.setVisible(true);
		} else {
			helpPanel.setVisible(true);
			helpPanel.setState(Frame.NORMAL); // stelle wieder her, wenn iconified
		}
	}

}
