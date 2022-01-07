/*
 * Created on 03.06.2008
 * 2.0: 15.08.08
 * 	+ position zeigt nun auf aktuellen Position
 * 1.0: erste Version vom 15.08.08 hatte funktioniert
 * 	+ hier war position der Zeiger auf das nächste zurückzugebende Bild gewesen
 * 		VT: am Anfang auch next verwendbar zum init, NT: rumgegurke und für ausblenden
 * 		von Text brauchts Umstellung!
 */
package kaba_Data;

import java.io.File;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Der ImageStepper erstellt eine geordnete Liste von Bild-Dateinamen zum anzeigen
 * und kann selber durch diese hindurch scrollen, die Bildschirmausgabe vom 
 * ImageStepper beinhaltet noch die ungeordnete Abfolge von Bild-Dateien
 * @author heida
 */
public class ImageStepper {
	String pictureDir;
	final String LOOKFOR = ".jpg"; // zu suchende Dateien
	boolean doWeHaveAJarFileHere;
	List<String> picList = new LinkedList<String>();
	/** Anzahl der Listen-Elemente, 0 bei leerer Liste, sonst > 0 */
	int tokens=0;
	/** Position des Steppers entspricht Bildadresse (beginnend mit 0) und zeigt immmer auf 
	 * aktuelle Bild-Pos. (Vorg. damit x-1, Nachf. x+1) */
	int position=0;
	private int lowQuality=0;

	/** imageStepper Konstruktor für eigenständige Anwendung (Aufruf: JPEGPresenter.main())*/
	public ImageStepper () {
		this (null);
	}

	/** imageStepper Konstruktor für den Aufruf aus Kaba heraus*/
	public ImageStepper (String imagePath) {
		pictureDir = imagePath;
		getImageFiles(); // 1. Versuch: im jar-File suchen
		if (tokens > 0) System.out.println("  -> " + tokens + " Bilder im jar gefunden.");
		else {
			System.out.println("  -> KEINE Bilder gefunden.");
			pictureDir = System.getProperty("user.dir");
			getImageFiles();
			if (tokens > 0) System.out.println("  -> " + tokens + " Bilder im Verzeichnis gefunden.");
			else {
				System.out.println("usage:");
				System.out.println("java -jar jarfile [Bilder-Verzeichnis]");
			} 
		}
		if (tokens > 0) order(picList);
		// position = 0; schon oben im init
	}

	/** sind Bilder aus der jar-Klassen-Datei oder aus externen Verzeichnissen */
	public boolean areWeWorkingWithAJarFile() {
		return doWeHaveAJarFileHere;
	}

	/** hat Bild einen Nachfolger */
	public boolean hasNext() {
		return ( position < (tokens-1) ) ?  true : false;
	}

	/** hat Bild einen Vorgänger */
	public boolean hasBefore() {
		return (position > 0) ?  true : false;
	}

	/** gibt es vom Bild aus gesehen ein Bild mit der Position increment weiter vorne / hinten? */
	public boolean hasStep(int increment) {
		boolean state = true;
		if ((position + increment) < 0) state = false; // untere Grenze
		if ((position + increment) > (tokens-1)) state = false; // obere Grenze
		return state;
	}

	/** gibt die lfd. Nummer vom aktuellen Bild zurück*/ 
	public int getID() {
		return position;
	}

	/** Dateiname vom nächsten Bild oder null wenns kein nächstes gibt*/ 
	public String getNext() {
		return getStep(1);
	}

	/** Dateiname vom vorhergenden Bild oder null wenns kein vorhergehendes gibt*/ 
	public String getBefore() {
		return getStep(-1);
	}

	/** gibt es vom Bild aus gesehen ein Bild mit der Position increment weiter vorne / hinten? */
	public String getStep(int step) {
		if (this.hasStep(step)) {
			position += step;
			return (String) picList.get(position);
		} else return null;
	}

	/** gibt das erste Bild zurück (für init, war es einmal) */
	public String getFirstImage() {
		position = 0;
		if (tokens > 0)
			return (String) picList.get(position);
		else return null;
	}

	/** gibt das erste Bild zurück (für init, war es einmal) */
	public String getLastImage() {
		if (tokens > 0) {
			position = tokens-1;
			return (String) picList.get(position);
		}
		else return null;
	}

	/** gibt den Namen der aktuellen Image-Datei zurück*/
	public String getActualImageFilename() {
		File buffer = new File ((String) picList.get(position));
		return buffer.getName();
	}

	/** wieviele Bilder sind insgesamt vorhanden */
	public int getCountOfPictures () {
		return tokens; 
	}

	/** bei welcher Bild-Nummer sind wir gerade (beginnt mit 1) */
	public int getNumberOfActualPicture () {
		return position+1;
	}

	/** gibt Bild-Nummer und Anzahl zurück - Bequemlichkeitsfunktion der letzten beiden 
	 * Funktionen (für kürzeren Aufruf von extern) */
	public String getFooterLeft() {
		return getNumberOfActualPicture() + " von " + getCountOfPictures() + " = " + getActualImageFilename();
	}

	/** wenn mehr Fotos < 500kB dann true */
	public boolean hasLowQualityFotos () {
		return (lowQuality > 0) ? true : false;
	}

	/** Sucht die Filenames der Image-Dateien zusammen */
	private void getImageFiles() {
		String path = System.getProperty("java.class.path");
		if (pictureDir != null) path = pictureDir;
		String classPathSeparator = File.pathSeparator;
		StringTokenizer st = new StringTokenizer(path, classPathSeparator);
		while (st.hasMoreTokens()) {
			System.out.println("Suche Bilder in: " + path);
			String classPathEntry = st.nextToken();
			File classPathFile = new File(classPathEntry);
			if (classPathFile.exists()) { // TODO wenn jars und dirs gemischt spinnt routine bools...
				if (classPathEntry.toLowerCase().endsWith(".jar")) { // starte aus jar-Datei
					doWeHaveAJarFileHere = true;
					addPicturesFromJar(picList, classPathFile);
				} else if (classPathFile.isDirectory()) {
					doWeHaveAJarFileHere = false;
					addPicturesFromDir(picList, classPathFile, classPathFile);
				}
			}
		}

		/*File[] i = dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				// Only include certain types in the image list
				if (f != null) {
					return(Util.isImageType(f.getAbsolutePath()));
				}
				return(false);
			}
		});*/
	}

	private void addPicturesFromJar (List<String> list, File pathFile) {
		
		try {
			try (ZipFile zipFile = new ZipFile(pathFile)) {
				Enumeration enumeration = zipFile.entries();
				while (enumeration.hasMoreElements()) {
					ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
					String current = zipEntry.getName();
					if (current.toLowerCase().endsWith(LOOKFOR)) {
						list.add("/" + current); //bei jar nur der Name (da ist Verz. im jar enthalten)
						// im jar wird immer mit "/" anstatt File.sparator (\ bei win) gearbeitet
						tokens++;
						System.out.println(current); 
					}
				}
			}
		} catch (Exception ex) {
			System.err.println(
				"Problem opening " + pathFile + " with jar-File.");
		}
	}

	private void addPicturesFromDir (List<String> list, File rootDir, File currentDir) {

		String[] files = currentDir.list();
		for (int i = 0; i < files.length; i++) {
			String current = files[i];
			if (current.toLowerCase().endsWith(LOOKFOR)) {
				String rootPath = rootDir.getPath();
				String currentPath = currentDir.getPath();
				if (! currentPath.startsWith(rootPath)) {
					System.err.println(
						"currentPath doesn't start with rootPath!\n"
							+ "rootPath: "
							+ rootPath
							+ "\n"
							+ "currentPath: "
							+ currentPath
							+ "\n");
				} else {
					list.add(currentPath + File.separator + current);
					tokens++;
					System.out.println(currentPath + File.separator + current); 
				}
			} else {
				// Check if it's a directory to recurse into
				File currentFile = new File(currentDir, current);
				if (currentFile.isDirectory()) {
					addPicturesFromSubDir(list, rootDir, currentFile); // nicht mehr rekursiv Aufruf, dadurch Rekursivität <= 1
				}
			}
		}
	}

	private void addPicturesFromSubDir (List<String> list, File rootDir, File currentDir) {

		String[] files = currentDir.list();
		for (int i = 0; i < files.length; i++) {
			String current = files[i];
			if (current.toLowerCase().endsWith(LOOKFOR)) {
				String rootPath = rootDir.getPath();
				String currentPath = currentDir.getPath();
				if (! currentPath.startsWith(rootPath)) {
					System.err.println(
						"currentPath doesn't start with rootPath!\n"
							+ "rootPath: "
							+ rootPath
							+ "\n"
							+ "currentPath: "
							+ currentPath
							+ "\n");
				} else {
					list.add(currentPath + File.separator + current);
					tokens++;
					System.out.println(currentPath + File.separator + current); 
				}
			}
		}
	}

	/** ordnet die Liste alphanumerisch
	 * @param list
	 */
	private void order(List<String> list) {
		for (int i=0; i<list.size(); i++) {
			int min=i;
			// 3.Zeichen löschen (bei Bildnamen entweder "_" ODER [bei Serienbildern] "b"
			// um Serienbildern bei meiner KODAK Kamera bei den normalen BIldern einzuordnen
			File fMin = new File ((String) list.get(min));
			StringBuffer sbMin = new StringBuffer (fMin.getName());
			if ( (sbMin.toString().startsWith("100_")) || (sbMin.toString().startsWith("100b")) )
				sbMin.deleteCharAt(3);
			for (int j=i+1; j<list.size(); j++) {
				File fList = new File ((String) list.get(j));
				if (fList.length() < 500000) lowQuality++; else lowQuality--;
				StringBuffer sbList = new StringBuffer(fList.getName()); 
				if ( (sbList.toString().startsWith("100_")) || (sbList.toString().startsWith("100b")) ) 
					sbList.deleteCharAt(3);
				if ( sbList.toString().compareTo( sbMin.toString() ) < 0 ) {
					min = j;
					sbMin = sbList;
				} 
			}
			if (min != i) {
				String temp = (String) list.get(i);
				list.set(i, (String) list.get(min) );
				list.set(min, temp);
			}
		}
	}

}
