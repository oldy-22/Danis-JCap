package com.watsonnet.jcap;
import java.awt.*;
import java.util.*;

/*
 * File: MultiLineLabel.java
 * Created for Projekt: DaniBibo
 * Created on 07.11.2007 by Daniel Enke
 */

/**
 * Klasse zum Erzeugen mehrzeiliger Labels mit bestimmten Rändern und Ausrichtungen
 * @author Daniel Enke
 */
public class MultiLineLabel extends Component {

	// vom Benutzer anzugegbende Attribute
	protected String label; //  der noch nicht umbrochene Text
	protected int marginWidth; //  linker / rechter Rand
	protected int marginHeight; //  oberer / unterer Rand
	protected int alignment;
	public static final int LEFT=0, CENTER=1, RIGHT=2; // Werte für Alignment
	
	// berechnete Zustandsvariablen
	protected int numLines;		// Anzahl der Zeilen
	protected String[] lines;			//  der in Zeilen umbrochene Text
	protected int[] lineWidths;		//  Breite der einzelnen Zeilen
	protected int maxWidth;		//  Breite der breitesten Zeile
	protected int lineHeight;		//  Gesamthöhe des Zeichensatzes
	protected int lineAscent;		//  Höhe des Zeichensatzes über der Baseline
	protected boolean measured = false;		// Sind Zeilen schon gemessen worden?	


	public MultiLineLabel ( String label, int marginWidth, int marginHeight, int alignment ) {
		this.label = label; 
		this.marginWidth = marginWidth; 
		this.marginHeight = marginHeight;
		this.alignment = alignment;
		newLabel();		// Text umbrechen
	}
	
	public MultiLineLabel(String label, int marginWidth, int marginHeight) {
		this ( label, marginWidth, marginHeight, LEFT );
	}
	
	public MultiLineLabel( String label, int alignment) {
		this ( label, 10, 10, alignment );
	}
	
	public MultiLineLabel(String label) {this ( label, 10, 10, LEFT ); }
	
	public MultiLineLabel() { this(""); }

	// getters and setters
	public int getAlignment() { return alignment; }
	public String getLabel() { return label; }
	public int getMarginHeight() { return marginHeight; }
	public int getMarginWidth() { return marginWidth; }

	public void setAlignment(int i) { alignment = i; repaint(); }
	public void setMarginHeight(int i) { marginHeight = i; }
	public void setMarginWidth(int i) { marginWidth = i; }

	public void setLabel(String string) {
		this.label = string;
		newLabel();
		measured = false;
		repaint();
	}

	public void setFont (Font f) {
		super.setFont(f);
		measured = false;
		repaint();
	}

	public void setForeground (Color c) {
		super.setForeground(c);
		repaint();
	}

	// Methoden, die vom LayoutManager aufgerufen werden
	public Dimension getPreferredSize () {
		if (!measured) measure();
		return new Dimension (maxWidth + 2*marginWidth, numLines*lineHeight+2*marginHeight);
	}
	
	public Dimension getMinimumSize() {return getPreferredSize(); }
	
	/** Diese Methode zeichnet die Beschirftung neu (die gleiche verwendet auch Applets).
	 * Ränder und Ausrichtung werden berücksichtigt, um Zeichensatz und Farbe kümmert sich
	 * nur die Superklasse und setzt sie im Graphis-Objekt, das wir übergeben bekommen.
	 * @author Daniel Enke */ 
	public void paint (Graphics g) {
		int x, y;
		Dimension size = this.getSize();
		
		if (!measured) measure();
		y = lineAscent + (size.height - numLines*lineHeight)/2;
		for (int i=0; i<numLines; i++, y+=lineHeight) {
			switch (alignment) {
				default:
				case LEFT:			x = marginWidth; break;
				case CENTER:	x = (size.width-lineWidths[i])/2; break;
				case RIGHT:		x = size.width - marginWidth - lineWidths[i]; break;
			}
			g.drawString (lines[i], x, y);
		}
	}

	/** Interne Methode: bricht den angegebenen Beschriftungstext in ein Array von Zeilen um. */
	protected synchronized void newLabel() {
		StringTokenizer t = new StringTokenizer (label, "\n");
		
		numLines = t.countTokens();
		lines = new String [numLines];
		lineWidths = new int[numLines];
		for (int i=0; i<numLines; i++) lines[i] = t.nextToken();
	}

	/** Interne Methode: ermittelt Eigenschaften des Zeichensatzes und daraus die Breite der
	 * breitesten Zeile. */
	protected synchronized void measure () {
		FontMetrics fm = this.getToolkit().getFontMetrics(this.getFont());
		lineHeight = fm.getHeight();
		lineAscent = fm.getAscent();
		maxWidth = 0;
		for  (int i=0; i<numLines; i++) {
			lineWidths[i] = fm.stringWidth(lines[i]);
			if (lineWidths[i] > maxWidth) maxWidth = lineWidths[i];
		}
	}


}
