package com.watsonnet.jcap;

// java:
import java.awt.*;

// swing:
import javax.swing.*;

public class ImagePanel extends JPanel {
	private Image image = null;
	private boolean loading = false;

	// set the image
	public void setImage(Image img) {
		image = null;
		image = img;
		
		// Set loading to true so that we always show the "Loading..." text
		// even if the image is loaded very quickly.
		if (image != null) {
			loading = true;
		}
		
		repaint();
	}
	
	// return the image
	public Image getImage() {
		return(image);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// draw this first.  if the image loads fast enough then both of these
		// draws will happen in the same call.  if that happens and these are
		// in the wrong order then you end up with a picture with "Loading..."
		// written on it.
		if (loading) {
			g.drawString("Loading...", this.getWidth()/2, this.getHeight()/2);
		}
		
		// draw the image, scaled
		if (image != null) {
			int w, h, sw, sh;
			float ratio;
			
			w = image.getWidth(this);
			h = image.getHeight(this);
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
			g.drawImage(image, sw, sh, w, h, this);
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
}

