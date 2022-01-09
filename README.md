# JCap
Bilder taggen und suchen, Dateienamen editieren, Stories zu Bildern schreiben und diese als Film (in der Loop zB) ablaufen lassen


Das Projekt ist entstanden aus dem Sourcefourge Projekt JCap (ich meine in einer Version 0.7.6 - war mal auf sourceforge damals). Ich habe es ca. 2003 runtergeladen und danach editiert nach meinen Belangen. Lange Zeit nutze ich das Tool schon und ändere immer das, was mir gerade nicht passt. Ich habe es kaba genannt, weil ich es nutze als "**KA**tegrisierbares **B**ilder**A**rchiv". Ich kenne kein Tool, das **mir** so schnell z.B. erlaubt, Bilder so zu bearbeiten, dass man sie an Ausdruckportale (z.B. aldifotos...) hochladen kann - zum Verteilen an weit entfernte Verwandte. An diesem Beispiel möchte ich den Ablauf hier erklären:

![Bildschirmfoto vom 2022-01-07 22-04-53](https://user-images.githubusercontent.com/56628625/148612770-92f349b0-ce67-4343-ad86-91e1e5b0ec5a.png)

Zuerst kann man die Bilder beim gemeinsamen Betrachten verschlagworten z.B. mit 1, 2, oder 3 - nach der Anzahl der Bilder die man bestellen möchte. Dafür alndet der Cursor immer im Schlagwortfeld **Keywords**. Dabei kann man auch z.B. durch Leerzeichen getrennt viele Schlagwörter hinzufügen (taggen). Der Vorgang hält beim Betrachten kaum auf (z.B. mit Zwischenablage) und man kann dazu z.B. alle Familienangehörigen zur gemeinsamen Durchsprache (meist nur die Frau 😃) auf der Couch versammeln). 
  Man kann aber natürlich auch andere Schlagworte verwenden, wie Oma, Handbilder, Hund... , die man später ggf. suchen möchte. Dadurch, dass diese Infos einfach in gleichnamigen .txt-Dateien gespeichert werden, kann man die Infos auch leicht wieder löschen (strippen im Verzeichnisbaum), wenn sie nicht mehr benötigt werden. Ich hatte es damals so eingerichtet, um auf CDs (oder anderen Datenträgern) immer die Daten (nach dem Kopieren) automatisch **BEI** den Bilder zu haben (keine extra Database).
  
![Bildschirmfoto vom 2022-01-07 22-05-57](https://user-images.githubusercontent.com/56628625/148613088-15ddd0a7-30a0-4950-a18c-8439361a36c3.png)

Danach kann man im **search** Dialog (man muss über dem Verzeichnis stehen!) nach den Schlagworten (hier z.B. 2) suchen und die gefundenen Bilder in ein neues Verzeichnis kopieren (Button im Bild), zB nach "nachmachen/2x/". Das macht man dann mit allen Schlagworten 1 & 3 weiter - könnte auch noch ein bisschen leichter gemacht werden in kommenden Versionen, in dem er die manuellen Tätigkeiten für 1-x-fach je nach Vorkommen selbst erledigt.

![Bildschirmfoto vom 2022-01-07 22-06-39](https://user-images.githubusercontent.com/56628625/148613323-53e6d57a-1105-44ef-9600-177ae4844776.png)

Damit man in dem Portal dann anhand der Bild-Dateien schon weiß, welche Bilder 2x gemacht werden sollten, kann man die Dateinamen in den Zielverzeichnissen mit dem **Database Manipulations** Dialog alle Dateien im batch umbenennen zB in "2022-01-07 2x meine Familienbilder 16-07-58.jpg" (Datum und Zeit ist dann für jedes Bild unterschiedlich).
Eigentlich hatte die den DIalog für einen Freund konzipiert, der nach einem Zeltlager verschiedene Kameras mit verschiedenen (auch falsch eingestellten) Datums und Uhrzeiten in einen einnheitlichen chronologischen Ablauf bringen wollte in einem Verzeichnis (daher der Name Achim im leeren Fenster). Also das geht auch damit.
Die Daten für die Zielverzeichnis-Dateien werden aus den exif-Daten der Bilder extrahiert (nicht aus den source Dateinamen). Das Quellfeld dafür ist frei wählbar im Panel ("field")...

Weiterhin kann man Stories in das große Feld **Description** schreiben, mehrere Zeilen Länge (für mehr Infos) kann man durch ein "doppelte Leerzeichen" im Text erzeugen, die danach wie in einem Film auf die Bilder projiziert werden (und zeitlich hintereinander dargestellt werden getrennt an den doppelten Leerzeichen), um diese zB bei einer Party ablaufen zu lassen mit Hintergrundinfos in den Texten: "Hier lacht der Opa über die ersten Laute von Mia..."
Die *caption* (Überschrift) wird oben links im BIld positioniert und die "description" unten dann ähnlich einem Lauftext. Echtzeitverhalten und die Positionen für die Texte sind durch die QUellen dann gut anpassbar, wenn was nicht stimmt.
Die diashow Quelltexte könnte man vielleicht auch für objektorientierte Programmierung als Beispiele hernehmen, weil es entstanden ist nach den Änderungen am JCap selber (also sozusagen auf einer Hochzeit von mir ca. 2008 ). Das gilt für das aus JCap abgeleitete kaba aber eher nicht unbedingt... 😄

Weil das Presenter-Programm diashow nicht alle Klassen benötigt (und auch keine Oberfläche besitzt), habe ich diese Klassen in dem 2. Ordner diashow zusammengefasst als zweites Verzeichnis (im eclipse bei mir 2 Projekte) unabhängig vom kaba lauffähiges Programm (einige Klassen sind aber ähnlich). Mann kann die Bilder (möglichst verkleinert 2k z.B.) sowohl mit den Klassen in ein jar packen lassen (in das Verzeichnis images) wenn man es als eine Datei über das Internet versenden will (dann kriegt man in 20MB gut 50-70 Bilder unter) oder das diashow Programm (ohne Bilder drin) in dem Bilderverzeichnis starten und dann nimmt es alle Bilder, die es vorfindet in dem Verzeichnis (siehe auch Konsolenausgabe von diashow). Probiert es gern auch aus, nach Runterladen des Projektes einfach im image Verzeichnis das jar starten und schaut damit eure eigenen Bilder an. Wenn ihr im Programm diashow h tippt wird auch eine kurze Bedienungshilfe angezeigt. 

Viel Spass mit den sourcen!!
