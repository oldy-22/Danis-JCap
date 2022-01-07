/*
 * Created on 16.08.2008
 * 0.1: erste Version
 *
 */
package JPEG_Data;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

/**
 * @author heida
 *
 */
public class JPEGDataReader {

	final String JPEG_DATE = "Date/Time Original";
	final String JPEG_EXPOSURE_TIME = "Exposure Time";
	final String JPEG_APERTURE = "F-Number";
	final String JPEG_FOCAL_LENGTH = "Focal Length";
	final String JPEG_FOCAL_LENGTH_FOTO = "Unknown tag (0xa405)";

	Date exifDate;
	GregorianCalendar date= new GregorianCalendar(); 
	SimpleDateFormat setterFormat = new SimpleDateFormat("dd.MM.yy - HH:mm:ss");
	SimpleDateFormat getterFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	String diaDate="", diaInfo="";
	StringBuffer sbDate, sbInfo;
	boolean workingWithJarFile;

	public JPEGDataReader(String filename, boolean workWithJarFile) {
		workingWithJarFile = workWithJarFile;
		// Datum ermitteln
		exifDate = retrieveDate(filename);
		if (exifDate != null) {
			date.setTime(exifDate);
			diaDate = setterFormat.format(date.getTime());
		}
		
		// Infos ermitteln
		diaInfo = retrieveInfo(filename);
		
	}

	public String getDate() {
		return  diaDate;
	}

	public String getInfo() {
		return  diaInfo;
	}

	private Date retrieveDate (String filename) {
		String dateTime = getExifData (filename, JPEG_DATE);
		try {
			return getterFormat.parse(dateTime);
		} catch (Exception pe) {return null;}
	}

	private String retrieveInfo (String filename) {
		StringBuffer sbInfo = new StringBuffer();
		sbInfo.append(getExifData (filename, JPEG_EXPOSURE_TIME));
		if (sbInfo.length() < 1) return ""; // wenn keine Infos vorhanden leere Zeichenkette
		sbInfo.append(" - ");
		sbInfo.append(getExifData (filename, JPEG_APERTURE));
		sbInfo.append(" - ");
		sbInfo.append(getExifData (filename, JPEG_FOCAL_LENGTH_FOTO));
		sbInfo.append("/");
		sbInfo.append(getExifData (filename, JPEG_FOCAL_LENGTH));
		
		return sbInfo.toString();
	}

	private String getExifData(String filename, String pattern) {

		try {
			Metadata metadata;
			if (workingWithJarFile)
				metadata = JpegMetadataReader.readMetadata(getClass().getResourceAsStream(filename));
			else
				metadata = JpegMetadataReader.readMetadata(new File (filename));
			
			// iterate through metadata directories
			Iterator directories = metadata.getDirectoryIterator();
			while (directories.hasNext()) {
				Directory directory = (Directory)directories.next();

				// iterate through tags
				Iterator tags = directory.getTagIterator();
				while (tags.hasNext()) {
					Tag tag = (Tag)tags.next();

					// append each tag to stringbuffer
					try {
						//m.addData(tag.getTagName(), tag.getDescription());
						if (tag.getTagName().startsWith(pattern))
							return tag.getDescription();

					} catch (MetadataException ex) {
						System.out.println("ERROR: The EXIF data could not be added to the table.");
						System.out.println(ex.getMessage());
					}
				}
			}
		} catch (java.io.FileNotFoundException ex) {
			System.out.println("File not found: The image could not be found while searching for EXIF data.");
		} catch (JpegProcessingException ex) {
			System.out.println("JPEG error: The JPEG image could not be decoded.");
		}
		return ""; 
	}
	

}
