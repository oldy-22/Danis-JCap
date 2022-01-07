package kaba_Data;

import java.awt.Component;
// java:
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

// swing:
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class Util {
	public static final String DESCRIPTION_EXT = "_description.txt";
	public static final String KEYWORDS_EXT = ".txt";
	public static final String CAPTION_EXT = "_caption.txt";
	public static final String TEXT_EXT = ".txt";
	public static final String FOLDERINFO_FILENAME = "_folderInfo.txt";
	
	/**	Read the contents of a text file got by URL (jar)*/
	public static String readFile(URL url) {
		if (url == null ) return "";
		try {
			InputStream i = url.openStream();
			StringBuffer sb = new StringBuffer();
			String line;
			BufferedReader br = new BufferedReader(new InputStreamReader(i, "UTF-8"));
			while ((line = br.readLine()) != null) {
				if (sb.length() > 0) { sb.append("\n"); }
				sb.append(line);
			}
			return sb.toString();
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(null, "The file " + url.getFile() + " could not be read.", "Read error", JOptionPane.WARNING_MESSAGE);
			return("");
		}
	}
	
	/**	Read the contents of a text file */
	public static String readFile(File f) {
		try {
			StringBuffer sb = new StringBuffer();
			if (f.canRead()) {
				String line;
				BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "UTF-8"));
				while ((line = br.readLine()) != null) {
					if (sb.length() > 0) { sb.append("\n"); }
					sb.append(line);
				}
			}
			return sb.toString();
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(null, "The file " + f.getName() + " could not be read.", "Read error", JOptionPane.WARNING_MESSAGE);
			return("");
		}
	}
	
	/** Save text data to the given file */
	public static void saveFile(File f, String data) {
		// Save data
		try {
			f.createNewFile();
			if (f.canWrite()) {
				FileWriter fw = new FileWriter(f);
				fw.write(data);
				fw.flush();
				fw.close();
			}
		} catch (java.io.IOException ex) {
			JOptionPane.showMessageDialog(null, "The file " + f.getName() + " could not be saved.", "Save error", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/** Get filenames */
	public static String getImageFilename(String filename) {
		return(getFilenameLabel(filename) + ".jpg");
	}
	
	/** Get filenames */
	public static String getTextFilename(String filename) {
		return(getFilenameLabel(filename) + DESCRIPTION_EXT);
	}
	
	/** Get the filename of the txt file associated with each image */
	public static String getKeywordsFilename(String filename) {
		return(getFilenameLabel(filename) + KEYWORDS_EXT);
	}
	
	/** Get the filename of the txt file associated with each image */
	public static String getCaptionFilename(String filename) {
		return(getFilenameLabel(filename) + CAPTION_EXT);
	}
	
	/** Determines if the given base filename (without extension) has the same
	 *	name as an image file in the same folder. 
	 * @param filename
	 * @return
	 */
	public static boolean imageExistsForFilename(String filename) {
		String filelabel = getFilenameLabel(filename);
		ArrayList ext = getImageTypesList();
		
		for(int i=0; i <= ext.size()-1; i++) {
			File f = new File(filelabel + (String)ext.get(i));
			if (f.exists()) {
				return(true);
			}
		}
		
		return(false);
	}
	
	/** Determines if the given filename is a supported image type */
	public static boolean isImageType(String filename) {
		ArrayList ext = getImageTypesList();
		
		for(int i=0; i <= ext.size()-1; i++) {
			if (filename.toLowerCase().endsWith(((String)ext.get(i)).toLowerCase())) {
				return(true);
			}
		}
		
		return(false);
	}
	
	/** An arraylist of valid image types */
	public static ArrayList getImageTypesList() {
		ArrayList data = new ArrayList();
		data.add(".jpg");
		data.add(".jpeg");
		// data.add(".gif");
		// data.add(".png");
		return(data);
	}
	
	/** Gets the filename without the extension.  The purpose of this is
	 *	to get the raw image filename without extensions. 
	 * @param path
	 * @return
	 */
	public static String getFilenameLabel(String path) {
		String retval = path;
		int dot = -1;
		
		// look for special filenames first
		if (dot == -1) { dot = path.lastIndexOf(DESCRIPTION_EXT); }
		if (dot == -1) { dot = path.lastIndexOf(CAPTION_EXT); }
		if (dot == -1) { dot = path.lastIndexOf(KEYWORDS_EXT); }
		
		// if those aren't found then just look for the extension
		if (dot == -1) { dot = path.lastIndexOf('.'); }
		
		if (dot > -1) {
			retval = path.substring(0, dot);
		}
		return retval;
	}
	
	// Returns true if this image type supports EXIF data
	public static boolean supportsEXIF(String path) {
		return(path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg"));
	}

	/** Open Directory Dialog
	 * @return true, if APPLY was pressed, CANCEL results in false
	 */
	public static boolean chooseFolder(StringBuffer output, String input, Component parent, 
			boolean saveWindow) {
		// folder chooser
		JFileChooser fc;

		if (input == null) input=System.getProperty("user.home");
		fc = new JFileChooser(input);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		int status;
		if (saveWindow) status = fc.showSaveDialog(parent);
		else  status = fc.showOpenDialog(parent);
		
		if (status == JFileChooser.APPROVE_OPTION) {
			output.replace(0, 0, fc.getSelectedFile().getAbsolutePath());
			return true;
		} else { 
			return false;
		}
	}

	/**
	 * Copy source file to destination file.
	 *
	 */
	public static void copyFile (String srcFilename, String destFilename) {
		FileInputStream in; FileOutputStream out;
		byte[] bytes = new byte[512];
		int len = 0;
		try {
			in = new FileInputStream(new File (srcFilename));
			out = new FileOutputStream(new File (destFilename));
			while ((len = in.read(bytes)) != -1) {
				out.write(bytes, 0, len);
			}
		} catch (FileNotFoundException exc) {
			System.err.println("File not found: " + destFilename);
		} catch (IOException exc) {
			System.err.println("I/O Error on file: " + destFilename);
		} catch (SecurityException exc) {
			System.err.println("You don´t have enough rights on file: " + destFilename);
		}
	}

}

