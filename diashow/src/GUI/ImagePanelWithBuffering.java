package GUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JPanel;

public class ImagePanelWithBuffering extends JPanel {
	private static final long serialVersionUID = 1L;
	private String calledFilename, calledBufferFilename;
	private Image calledImage = null, calledBufferImage = null; // Bild-Anforderungen aus GUI
	private Image imageToShow = null;
	// TODO erhalte immer noch OutOfMemory aus sun.awt.image.ImageFetcher 
	// statt mit Images mal mit BufferedImages ausprobieren, 080209 mit img=null dazwischen wird es 
	// glaube ich besser
	
	private boolean weAreLoading = false; // TODO muss wieder private werden
	private boolean weWereOverrun = false; 
	private boolean directionHasChanged = true; 
	
	static final Color TENDER_GREEN = new Color(230, 245, 212);
	static final Color TENDER_YELLOW = new Color(245,  245, 212);
	static final Color TENDER_RED = new Color(200, 130, 130);
	static final Color TENDER_GRAY = new Color(192, 192, 192);
	
	/** set an image before showing on screen without setting an image to prepare*/
	public void setImage(String iFilename) {
		setImage(iFilename, null);
	}
	
	/** set an image before showing on screen and set a image to prepare*/
	public void setImage(String iFilename, String iFilenameToPrepare) {
		
		calledFilename = iFilename;
		
		if (calledFilename.equals(calledBufferFilename)) directionHasChanged = false;
		else directionHasChanged = true;
		calledBufferFilename = iFilenameToPrepare;
		
		if (! isImagingEngingBusy()) {
			calledImage = Toolkit.getDefaultToolkit().getImage(calledFilename);
			setImageToShow(calledImage);
			
			// nur wenn engineNotBusy wird Buffer-Bild gesetzt
			calledBufferImage = Toolkit.getDefaultToolkit().getImage(calledBufferFilename);

			// Set loading to true so that we always show the "Loading..." text
			// even if the image is loaded very quickly.
			if (imageToShow != null) {
				// weAreLoading = true; darf hier nicht drinstehen, sonst lädt er keine Bild mehr 
				// sobald Bild im Speicher war
				//repaint();
			}
		} else {
			weWereOverrun = true;
		}
	}
	
	/** set the next image to get imagedata*/
	public void prepareNextImage(Image imageToPrepare) {
		
		// Bdi.1: true, wenn !isImagingEngingBusy(); 
		if ( imageToPrepare.equals(calledBufferImage) && (calledBufferImage != null)
			&& ( ! directionHasChanged ) ) { 
			// nur true, wenn setzen BufferImage erfolgte (bei engineNotBusy)
			// flush hilft bei OutOfMemory
			if ( prepareImage(calledBufferImage, this) ) calledBufferImage.flush();
			
			setBackground(TENDER_YELLOW); 
		} 
	}
	
	/** return the image*/
	public Image getImage() {
		return(calledImage);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.drawString("Loading...", this.getWidth()/2, this.getHeight()/2);
		
		// draw the image, scaled
		if (imageToShow != null) {
			int w, h, sw, sh;
			float ratio;
			
			w = imageToShow.getWidth(this);
			h = imageToShow.getHeight(this);
			ratio = (float)w/(float)h;
			
			if (w > this.getWidth()) {
				w = this.getWidth();
				h = (int)(w/ratio);
			}
			if (h > this.getHeight()) {
				h = this.getHeight();
				w = (int)(h*ratio);
			}
			
			// centering coordinates
			sw = (Math.abs(this.getWidth())-w)/2;
			sh = (Math.abs(this.getHeight())-h)/2;
			
			// draw
			g.drawImage(imageToShow, sw, sh, w, h, this);
		
		}	
	}
	
	// wait for the image to be completely loaded then repaint the panel
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if ((infoflags & ALLBITS) == ALLBITS) {
			notifyPicture();
			repaint();
		} else if ((infoflags & ABORT) == ABORT) {
			notifyPicture();
			repaint();
		} else if ((infoflags & ERROR) == ERROR) {
			notifyPicture();
			repaint();
		} else {
			if (weWereOverrun) setBackground(TENDER_RED);
			else {
				if (directionHasChanged) setBackground(TENDER_GREEN);
				else  setBackground(TENDER_YELLOW);
			} 
			weAreLoading = true;
		}
		return(weAreLoading);
	}

	public void notifyPicture() {
		setBackground(TENDER_GRAY);
		weAreLoading = false;

		if (weWereOverrun) huntToOverrun();
	}
	
	/** only call if not loading - (! isImagingEngingeBusy) */
	private void huntToOverrun() {
		weWereOverrun = false;
		setImage(calledFilename); // Buffer = null, zum schnelleren Laden des callImage
		repaint();
	}

	private boolean isImagingEngingBusy() {
		return weAreLoading;
	}
	
	private void setImageToShow(Image i) {
		if (imageToShow != null) {
			imageToShow.flush();
			imageToShow = null; // alles Versuche, Speicher zu leeren
		} 
		imageToShow = i;
	}

}

