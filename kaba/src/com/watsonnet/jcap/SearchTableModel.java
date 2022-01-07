package com.watsonnet.jcap;

// java
import java.io.*;

// swing:
import javax.swing.table.*;

import java.util.*;

public class SearchTableModel extends AbstractTableModel {
	private ArrayList data = new ArrayList();
	private String[] columnNames = { "Filename", "Keywords", "Path" };
	private int rows = 0;
	
	public void addData(SearchResult v) {
		// Add data
		data.add(v);
		rows++;
		Collections.sort(data);
		fireTableDataChanged();
	}
	
	public void clearData() {
		// Clear data
		data.clear();
		rows = 0;
		fireTableDataChanged();
	}
	
	public String getColumnName(int col) { 
		return columnNames[col].toString(); 
	}
	
	public int getRowCount() {
		return rows;
	}
	
	public int getColumnCount() {
		return 3;
	}
	
	public Object getValueAt(int row, int col) {
		File f = new File(((SearchResult)data.get(row)).getPath());
		switch (col) {
			case 0:
				return(Util.getFilenameLabel(f.getName()));
			case 1:
				return(((SearchResult)data.get(row)).getKeywords());
			case 2:
				return(f.getAbsolutePath());
		}
		return null;
	}
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	public void setValueAt(Object value, int row, int col) {
	}
}
