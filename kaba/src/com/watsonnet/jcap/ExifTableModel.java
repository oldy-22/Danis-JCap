package com.watsonnet.jcap;

// swing:
import javax.swing.table.*;

import java.util.*;

public class ExifTableModel extends AbstractTableModel {
	private ArrayList name = new ArrayList();
	private ArrayList value = new ArrayList();
	private String[] columnNames = { "Name", "Value" };
	private int exifRows = 0;
	
	public void addData(String n, String v) {
		// Add data
		name.add(n);
		value.add(v);
		exifRows++;
		fireTableDataChanged();
	}
	
	public void clearData() {
		// Clear data
		name.clear();
		value.clear();
		exifRows = 0;
		fireTableDataChanged();
	}
	
	public String getColumnName(int col) { 
		return columnNames[col].toString(); 
	}
	
	public int getRowCount() {
		return exifRows;
	}
	
	public int getColumnCount() {
		return 2;
	}
	
	public Object getValueAt(int row, int col) { 
		if (col == 0) {
			return(name.get(row));
		} else {
			return(value.get(row));
		}
	}
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	public void setValueAt(Object value, int row, int col) {
	}
}
