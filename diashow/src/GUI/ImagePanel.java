package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JPanel;

public class ImagePanel extends JPanel /* auch mal JFrame probiert aber viele Fehler */ {

	private static final long serialVersionUID = 1L;
	private Dimension screenSize;
	private Image image = null;
	private boolean loading = false;
	private String story, caption, pageNumber, fotoInfo, date;
	boolean workingWithJarFile;
	// private MultiLineLabel ml;
	private Font captionFont, storyFont, pageNumberFont;
	private Point storyStartPoint, captionStartPoint, pageNumberStartPoint, fotoInfoStartPoint,
		dateStartPoint, playStartPoint, endlessStartPoint;
	private int shadowSize, screenResolution;
	public boolean play=false, endlessLoop=false;
	private float zoom= (float) 1.0;
	private boolean wasZoomed=false;
	private double ratioX, ratioY;
	private int mouseX, mouseY;

	public ImagePanel (boolean workWithJarFile) {
		workingWithJarFile = workWithJarFile;
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		//int taskBarHeight = screenSize.height - winSize.height;

		screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
		//TODO weiterforschen hier: Insets i = Toolkit.getDefaultToolkit().getScreenInsets();
		double cfSize =  screenSize.getWidth()/25.6;
		captionFont = new Font ("Monospaced",Font.ITALIC, (int) cfSize);
		captionStartPoint = new Point ( (int) (screenSize.getWidth()/20.5), (int) (screenSize.getHeight()/7.7) );
		double sfSize =  screenSize.getWidth()/34;
		storyFont = new Font ("SansSerif",Font.PLAIN, (int) sfSize);
		storyStartPoint = new Point ( (int) (screenSize.getWidth()/51.2), (int) (screenSize.getHeight()/1.07) );
		
		int groundLineY = (int) Math.round (winSize.getHeight()/1.01); 
			//vorher screenSize anstatt winSize, dann wird bei Windows mit Taskleiste die unterste Zeile verdeckt
		double pnfSize =  screenSize.getWidth()/75;  //pagteNumberFont
		pageNumberFont = new Font ("SansSerif",Font.PLAIN, (int) pnfSize);
		pageNumberStartPoint = new Point ( (int) (screenSize.getWidth()/100), groundLineY);
		fotoInfoStartPoint = new Point ( (int) (screenSize.getWidth()*0.3), groundLineY);
		dateStartPoint = new Point ( (int) (screenSize.getWidth()*0.85), groundLineY);
		playStartPoint = new Point ( (int) (screenSize.getWidth()*0.975), groundLineY);
		endlessStartPoint = new Point ( (int) (screenSize.getWidth()*0.986), groundLineY);
		
		shadowSize = (int) (screenSize.getWidth() + screenSize.getHeight()) / ( 1500) + 1;
	}
	
	// set the image
	public void setImage(String  imageFilename) {
		image = null;
		if (workingWithJarFile) {
			image = Toolkit.getDefaultToolkit().createImage(this.getClass().getResource(imageFilename));
		}
		else
			image = Toolkit.getDefaultToolkit().getImage(imageFilename);
		
		// Set loading to true so that we always show the "Loading..." text
		// even if the image is loaded very quickly.
		if (image != null) {
			loading = true;
		}
		setZoom(1f);
		setStory(""); setCaption(""); setPageNumber(""); setFotoInfo(""); setDate("");
		repaint();
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
		//setResizable(false);
		//pack();
		//setVisible(true);	
	}

	public void resizeImage(boolean sizeIn, Point mousePointer) {
		if (sizeIn) zoom += 0.1; else if (zoom>1.0) zoom-=0.1;
		// ratio berechnen
		if (image != null ) { 
			mouseX=mousePointer.x;
			mouseY=mousePointer.y;
		}
		repaint();
	}
	
	public void moveImage(Point old, Point newer) {
		if (image != null ) { 
			mouseX=mouseX + (newer.x +10);
			mouseY=mouseY + (newer.y - old.y);
		}
		repaint();
	}

	public boolean isZoomed() {	
		return (zoom > 1) ? true : false;
	}

	/** soll das Zoom um einen Click verlängern, damit nicht gleich bei 2x MittelmouseClick play aktiviert wird*/
	public boolean wasZoomed() {	
		return (wasZoomed = !wasZoomed);
	}

	public void setZoom(float newZoom) { 
		zoom = newZoom;
		repaint();
	}
	
	public void setCaption (String caption) {
			this.caption = caption;
	}

	public void setStory (String story) {
			this.story = story;
			//ml.setLabel(story);
	}

	public void setPageNumber (String pageNumber) {
			this.pageNumber = pageNumber;
	}

	public void setFotoInfo (String fotoInfo) {
			this.fotoInfo = fotoInfo;
	}

	public void setDate (String date) {
			this.date = date;
	}

	// return the image
	public Image getImage() {
		return(image);
	}
	
	public void paintComponent(Graphics g) {
		//super.paint(g);
		//super.paintComponent(g);
		
		// draw this first.  if the image loads fast enough then both of these
		// draws will happen in the same call.  if that happens and these are
		// in the wrong order then you end up with a picture with "Loading..."
		// written on it.
		if (loading) {
			// g.setColor(Color.DARK_GRAY);
			g.drawString("Loading...", this.getWidth()/2, this.getHeight()/2);
		}
		
		// draw the image, scaled
		if (image != null) {
			int w, h, sw, sh;
			float ratio;
			
			w = image.getWidth(this);
			h = image.getHeight(this);
			ratio = (float)w/(float)h;
			
			//if (w > this.getWidth()) { // 080812 if Bedingung vorr.-gehend rausgenommen
				// für Bildschirmfüllende Bilder
				w = this.getWidth();
				h = (int)(w/ratio);
			//}
			if (h > this.getHeight()) {
				h = this.getHeight();
				w = (int)(h*ratio);
			}
			
			// left upper coordinates ohne zoom
			sw = (int) ((Math.abs(this.getWidth())-w)/2); // *ratioX/zoom);
			sh = (int) ((Math.abs(this.getHeight())-h)/2); //*ratioY/zoom);
			ratioX = (mouseX-sw) / (double) w;
			ratioY = (mouseY-sh) / (double) h;
			if (zoom > 1) {
				sw = (int) (mouseX - (mouseX-sw)*zoom); //TODO HIER!
				sh = (int) (mouseY - (mouseY-sh)*zoom);
			}
			//TODO: Zoom muss noch auf das aktuell gezeigte Bild bezogen werden und nicht auf das gesamte Bild
			
			// draw
			g.setColor(Color.black); g.fillRect(0, 0, screenSize.width, screenSize.height);
			//g.drawImage(image, sw, sh, w, h, this); // ohne zoom
			g.drawImage(image, sw, sh, (int) (w*zoom), (int) (h*zoom), this);
			// final Color NEONGELB = new Color(240, 255, 0);
			// Stories mit Schatten zeichnen
			g.setColor(Color.black);
			for (int i = -shadowSize; i <= shadowSize; i+=2) {
				for (int j = -shadowSize; j <= shadowSize; j+=2) {
					g.setFont(storyFont); g.drawString (story, (storyStartPoint.x+i), (storyStartPoint.y+j));
					g.setFont(captionFont); g.drawString (caption, (captionStartPoint.x+i), (captionStartPoint.y+j));
				}
			}
			g.setColor(Color.yellow);
			g.setFont(storyFont); g.drawString (story, storyStartPoint.x, storyStartPoint.y);
			g.setFont(captionFont); g.drawString (caption, captionStartPoint.x, captionStartPoint.y);
			
			g.setFont(pageNumberFont); 
			g.drawString (pageNumber, pageNumberStartPoint.x, pageNumberStartPoint.y);
			g.drawString (fotoInfo, fotoInfoStartPoint.x, fotoInfoStartPoint.y);
			g.drawString (date, dateStartPoint.x, dateStartPoint.y);
			//210914 gelöst Todo verbessern, dass wenn Taskleiste dargestellt, die unteren Texte höher wandern
			if (play) {
				g.drawString ("\u25b6", playStartPoint.x, playStartPoint.y);
				if (endlessLoop) g.drawString ("\u21BB", endlessStartPoint.x, endlessStartPoint.y);
			}
		}
	}
	
	// wait for the image to be completely loaded then repaint the panel
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if ((infoflags & ALLBITS) == ALLBITS) {
			loading = false;
			repaint();
		} else if ((infoflags & ABORT) == ABORT) {
			loading = false;
			repaint();
		} else {
			loading = true;
		}
		return(loading);
	}
	
	public Dimension getPreferredSize() {
		return screenSize;
	}
	
	public String toggleDisplays() {
		
		// https://stackoverflow.com/questions/31717724/java-swing-detect-which-display-a-jframe-is-displayed-on
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    ge.getDefaultScreenDevice();
	    
	    GraphicsDevice device = this.getGraphicsConfiguration().getDevice();
	    
	    GraphicsDevice[] gs = ge.getScreenDevices();
	    //gs[0].setFullScreenWindow();
	    GraphicsConfiguration gc[] = gs[1].getConfigurations();
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < gs.length; i++) {
	        DisplayMode dm = gs[i].getDisplayMode();
	        sb.append(i + ", width: " + dm.getWidth() + ", height: " + dm.getHeight() + "\n");
	    }    
	    return sb.toString();
	}
}

