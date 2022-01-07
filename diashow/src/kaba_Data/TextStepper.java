/*
 * Created on 07.06.2008
 * 2.0: 15.08.08
 * 	+ position zeigt nun auf aktuellen Position
 * 1.0: erste Version vom 15.08.08
 * 	+ hier war position der Zeiger auf die nächste zurückzugebende Story gewesen
 * 		VT: am Anfang auch next verwendbar zum init, NT: rumgegurke und für ausblenden
 * 		von Text brauchts Umstellung!
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package kaba_Data;

import java.io.File;
import java.util.LinkedList;
import java.util.List;



/**
 * @author heida
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextStepper {

	String caption, story;
	List storyList;
	/** Anzahl der Listen-Elemente, 0 bei leerer Liste, sonst > 0 */
	int tokens=0;
	/** Position des Steppers entspricht Textadresse (beginnend mit 0) und zeigt immmer auf 
	 * aktuelle Text-Pos. (Vorg. damit x-1, Nachf. x+1) */
	int position=0;

	public TextStepper (String imageFilename, boolean workWithJar) {
		if (workWithJar) {
// TODO für URL im Internet?
			//getClass().getClassLoader().getResource(Util.getCaptionFilename(imageFilename)) );
			caption = Util.readFile( getClass().getResource(Util.getCaptionFilename(imageFilename)) );
			story = Util.readFile( getClass().getResource(Util.getTextFilename (imageFilename)) );
		} else {
			caption = Util.readFile(new File(Util.getCaptionFilename (imageFilename)));
			story = Util.readFile(new File(Util.getTextFilename (imageFilename)));
		}
		StringTokenizerWithStringDelimiter t;
		t = new StringTokenizerWithStringDelimiter(story, "  "); // delim = Doppel-Leerzeichen
		storyList = new LinkedList();
		while (t.hasMoreTokens()) {
			storyList.add(t.nextToken());
			tokens++;
		}
	}

	/** prüft, ob im TextStepper eine story (description) an aktueller Stelle hinterlegt ist*/
	public boolean hasStory() {
		return ( position <= (tokens-1) ) ?  true : false;
	}

	/** prüft, ob im TextStepper eine weitere story (description) hinterlegt ist*/
	public boolean hasMoreStory() {
		return ( position < (tokens-1) ) ?  true : false;
	}

	/** prüft, ob im TextStepper eine vorherige story (description) hinterlegt ist*/
	public boolean hasLessStory() {
		return (position > 0) ?  true : false;
	}

	public String getNextStory() {
		if (this.hasMoreStory()) {
			position++;
			return (String) storyList.get(position);
		} else return "";
	}

	public String getBeforeStory() {
		if (this.hasLessStory()) {
			position--;
			return (String) storyList.get(position);
		} else return "";
	}

	public String getStory() {
		if (this.hasStory()) {
			return (String) storyList.get(position);
		} else return "";
	}

	public String getCaption() {
		return caption;
	}

}
