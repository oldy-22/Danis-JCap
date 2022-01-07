package com.watsonnet.jcap;

import java.awt.Component;

public class SearchResult extends Component implements Comparable {
	private String mPath = null;
	private String mKeywords = null;
	private int mRank = 0;
	
	public SearchResult(String path, String keywords, int rank) {
		mPath = path;
		mKeywords = keywords;
		mRank = rank;
	}
	
	public String toString() {
		return(mPath);
	}
	
	public String getPath() {
		return(mPath);
	}
	
	public String getKeywords() {
		return(mKeywords);
	}
	
	public int getRank() {
		return(mRank);
	}
	
	public void setRank(int r) {
		mRank = r;
	}
	
	public int compareTo(Object o) {
		SearchResult s = (SearchResult)o;
		// Reverse the return values so that bigger values or rank come first when sorted.
		if (mRank > s.getRank()) { return(-1); }
		if (mRank < s.getRank()) { return(1); }
		return(0);
	}
}

