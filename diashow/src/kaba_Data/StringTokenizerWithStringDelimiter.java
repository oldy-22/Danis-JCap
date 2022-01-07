/*
 * Created on 10.06.2008
 * History for Danis FileSync:
 * 0.1: erste Version
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package kaba_Data;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This is a special String Tokenizer. The delimiter here is not a set of
 * char ";#+/" then an String with defined position of each part "  " (like a
 * double char or another string ("dani...") 
 */
public class StringTokenizerWithStringDelimiter implements Enumeration {
	private int currentPosition;
	private int foundEnd;
	private int maxPosition;
	private String str;
	private String delimiter;

	/**
	 * Constructs a string tokenizer for the specified string.
	 * This is a special String Tokenizer. The delimiter here is not a set of
	 * char ";#+/" then an String with defined position of each part "  " (like a
	 * double char or another string ("dani...") 
	 */
	public	StringTokenizerWithStringDelimiter(String str, String delim) {
		currentPosition = 0;
		foundEnd = -1;
		this.str = str;
		maxPosition = str.length();
		delimiter = delim;
	}

	/**
	 * Skips delimiters starting from the specified position. If retDelims
	 * is false, returns the index of the first non-delimiter character at or
	 * after startPos. If retDelims is true, startPos is returned.
	 */
	private int searchDelimiter (int startPos) {
		if (delimiter == null)
			throw new NullPointerException();
		return str.indexOf(delimiter, startPos);
	}


	public boolean hasMoreTokens() {
		/* Temporary store this position and use it in the following nextToken() method only if the 
		 * delimiters have'nt been changed in that nextToken() invocation. */
		// foundEnd = searchDelimiter(currentPosition); //-1 wenn nicht gefunden
		return (currentPosition < maxPosition) ? true : false;
	}
	
	public boolean hasMoreElements() { return hasMoreTokens(); }

	public String nextToken() {
		if (currentPosition >= maxPosition)
				throw new NoSuchElementException();
		int start = currentPosition;
		foundEnd = searchDelimiter(currentPosition);
		if (foundEnd < 0) foundEnd = maxPosition;
		currentPosition = foundEnd+delimiter.length();
		return str.substring(start, foundEnd);
	}

	public Object nextElement() { return nextToken();	}

}
