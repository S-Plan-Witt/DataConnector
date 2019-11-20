/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

import com.google.gson.Gson;
import de.nils_witt.splan.dataModels.Aufsicht;
import de.nils_witt.splan.dataModels.Course;
import de.nils_witt.splan.dataModels.VertretungsLesson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main(String[] args) {
        //Init workspace and params
        String path;
        Config configRead = null;
        path = getJarPath();

        if (path == null) return;
        Logger logger = initLogger(path);
        if (logger == null) return;
        configRead = loadConfig(logger, path);
        if (configRead == null) return;
        final Config config = configRead;
        initWatchDir(path);

        /*
        if(true){
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputStream in = Files.newInputStream(Paths.get(path.concat("/watchDir/vplan.xml")));
                Document document = builder.parse(in);
                processFile(document,logger,config);

            } catch (Exception e){
                e.printStackTrace();
            }
        }*/

        startWatcher(path, logger, config);

    }

    /**
     * Ermitteln des Pfades zu "dieser" Datei und Rückgabe des Ordners in dem sich diese befindent
     *
     * @return path to jar parent folder
     */
    private static String getJarPath() {
        String path = null;

        try {
            //ermitteln des Path zu dieser Klasse bzw zur Jar Datei
            File f = new File(System.getProperty("java.class.path"));
            //Ordnerpfad als String setzen
            File dir = f.getAbsoluteFile().getParentFile();
            path = dir.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }

    /**
     * Erstellt einen Logger mit Ausgabe in eine Datei (Log.log) im Arbeitsverzeichnis
     *
     * @param path to this jar (or any working directory)
     * @return logger for this program
     */
    @Nullable
    private static Logger initLogger(String path) {
        Logger logger = Logger.getLogger("TextLogger");
        FileHandler fh;

        try {
            //Setzen der Ausgabedatei
            fh = new FileHandler(path + "/Log.log");
            logger.addHandler(fh);
            //Einstellen der Formatierung des Logs
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return logger;
    }

    /**
     * Überprüfen ob das Verzeichnis für die Datein zum einlesen vorhanden ist, ggf. wenn nicht vorhanden wird dieses erstellt
     *
     * @param path to a folder (working directory)
     */
    private static void initWatchDir(@NotNull String path) {
        Path watchDir = Paths.get(path.concat("/watchDir"));

        if (!Files.exists(watchDir)) {
            System.out.println("not found");
            try {
                Files.createDirectory(watchDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Config.json laden
     *
     * @param logger
     * @param path   to config.json
     * @return return config if successful loaded the file
     */
    private static Config loadConfig(Logger logger, String path) {
        Gson gson = new Gson();
        Config config = null;
        try {
            //Laden der Datei und lesen aller Zeilen, die in einem String gespeichert werden, da json erwartet wird.
            InputStream is = new FileInputStream(path + "/config.json");
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();

            while (line != null) {
                sb.append(line).append("\n");
                line = buf.readLine();
            }

            String fileAsString = sb.toString();
            try {
                //String der Datei in in Config Objekt laden.
                config = gson.fromJson(fileAsString, Config.class);
                //Überprüfen ob die config gültig ist.
                if (!verifyBearer(logger, config.getBearer(), config.getUrl())) {
                    //Falls nicht config null setzen.
                    config = null;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error while reading config: ", e);
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Config open failed", e);
        }
        //Wenn die Konfig erfolgreich geladen und validiert wurde, wird diese zurückgegeben, sonst wir null.
        return config;
    }

    /**
     * Überprüfen der Gültigkeit des Zugriffstoken auf die Api
     *
     * @param logger
     * @param bearer token for api access
     * @param url    base api url
     * @return validity of bearer to given url
     */
    private static boolean verifyBearer(Logger logger, String bearer, @NotNull String url) {
        OkHttpClient client = new OkHttpClient();
        boolean isValid = false;
        Request request = new Request.Builder()
                .url(url.concat("/user"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();
        try {
            Response response = client.newCall(request).execute();
            // Api gibt den status 200 zurück, wenn alles  gültig ist.
            if (response.code() == 200) {
                isValid = true;
                logger.info("Bearer valid");
            } else {
                logger.log(Level.WARNING, "Bearer invalid");
            }
        } catch (java.net.UnknownHostException e) {
            //URL der Api ist nicht gültig
            logger.log(Level.WARNING, "Host not found", e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception while verifying Bearer", e);
        }

        return isValid;
    }

    /**
     * Startet die Ünerwachung des Verzeichnisses in das die XMLs gespeichert werden sollen und
     * verarbeitet diese bei einer Änderung
     *
     * @param path   path to data directry where the watcher should start
     * @param logger
     * @param config Object containtig url and bearer
     */
    private static void startWatcher(@NotNull String path, Logger logger, Config config) {
        //Pfad zum Ordner erstellen
        Path watchPath = Paths.get(path.concat("/watchDir"));
        try {
            //Dienset zu Überwachung erstellen
            WatchService watchService = watchPath.getFileSystem().newWatchService();
            //Dienst aktivieren und nur Änderungen an Dateien überwachen
            watchPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            logger.info("Started Watcher");
            while (true) {
                //Laden der Änderungen seit dem letzten Aufruf
                final WatchKey wk = watchService.take();
                //Jede Änderung verarbeiten
                for (WatchEvent<?> event : wk.pollEvents()) {
                    //Datei, die geändert wurde, laden
                    final Path changed = (Path) event.context();

                    //Neues Dokument erstellen, in das die geänderte Datei geladen werden soll
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    InputStream in = Files.newInputStream(Paths.get(path.concat("/watchDir/").concat(changed.getFileName().toString())));
                    //Datei in Dokument laden
                    Document document = builder.parse(in);

                    //Weitere Verarbeitung der Datei/Dokuments
                    processFile(document, logger, config);
                }
                //TODO wat is dat
                wk.reset();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in Watcher: ", e);
        }
    }

    /**
     * Ermitteln des Dokumenttypens und Übergabe an die jeweilige Methode zur Weiterverarbeitung
     *
     * @param document
     * @param logger
     * @param config
     */

    private static void processFile(Document document, Logger logger, Config config) {
        try {
            String nodeName;
            //Laden der base XML node, anhand dieser kann der Inhaltstyp ermittelt werden
            nodeName = document.getLastChild().getNodeName();
            if (config.getTrayNotifications()) {
                displayTrayNotification("Änderung erkannt", "Datei: ".concat(nodeName));
            }

            switch (nodeName) {
                //vp = Vertretungsplan
                case "vp":
                    logger.info("Vplan");
                    vplanFileReader(document, logger, config);
                    break;
                default:
                    logger.info(nodeName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Laden der Daten aus der XML und Übergabe der Json an die Methode zur Übertragung an die API
     *
     * @param document
     * @param logger
     * @param config
     */

    private static void vplanFileReader(Document document, Logger logger, Config config) {
        String currentDate = "";
        List<VertretungsLesson> lessons = new ArrayList<>();
        List<Aufsicht> aufsichten = new ArrayList<>();
        Utils utils = new Utils();
        int length;

        try {
            //Laden der base node Unterelemente
            NodeList nl = document.getLastChild().getChildNodes();

            length = nl.getLength();

            for (int i = 0; i < length; i++) {
                //Ünerprüfen, dass das Element eine Node ist
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nl.item(i);
                    /*
                    Jeder Tag hat drei Nodes Kopf, Haupt und Aufsichen. Der Kopf bestimmt das Datum für die Folgenden Haput und Aufsichent Teile.
                    Die Teile Haupt und Aufsichen haben Unterelement, die die jeweiligen Events beschreiben (ein Event pro Vertretung / Aufsicht)
                     */
                    //Typ der Node bestimmen
                    switch (el.getTagName()) {
                        case "kopf":
                            //Das Datum der XML in das Format yyyy-mm-dd konvertieren, wie es von der Api erfordert wird, mithilfe der Utils Klasse.
                            currentDate = utils.convertDate(el.getElementsByTagName("titel").item(0).getTextContent());
                            break;
                        case "haupt":
                            //Laden aller Unterelement = Vertretungen
                            NodeList aktionen = el.getChildNodes();
                            for (int k = 0; k < aktionen.getLength(); k++) {
                                if (aktionen.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                    //Neues Object laden und Daten aus der Node übertragen
                                    VertretungsLesson lesson = new VertretungsLesson();

                                    Element aktion = (Element) aktionen.item(k);

                                    lesson.setLesson(aktion.getElementsByTagName("stunde").item(0).getTextContent());
                                    lesson.setInfo(aktion.getElementsByTagName("info").item(0).getTextContent());
                                    lesson.setChangedSubject(aktion.getElementsByTagName("vfach").item(0).getTextContent());
                                    String changedTeacher = aktion.getElementsByTagName("vlehrer").item(0).getTextContent();
                                    if ("(".concat(aktion.getElementsByTagName("lehrer").item(0).getTextContent()).concat(")").equals(changedTeacher)) {
                                        lesson.setChangedTeacher("---");
                                    } else {
                                        lesson.setChangedTeacher(changedTeacher);
                                    }

                                    lesson.setChangedRoom(aktion.getElementsByTagName("vraum").item(0).getTextContent());
                                    //Setzen das Datums, das im Kopf ausgelesen wurde
                                    lesson.setDate(currentDate);
                                    //Seperates Splitten der Kursbezeichnung in Stufe, Fach und Gruppe
                                    Course course = new Course();
                                    course.setSubject(aktion.getElementsByTagName("fach").item(0).getTextContent());
                                    course.updateByCourseString(aktion.getElementsByTagName("klasse").item(0).getTextContent());
                                    lesson.setSubject(course.getSubject());
                                    lesson.setGrade(course.getGrade());
                                    lesson.setGroup(course.getGroup());
                                    //Vertretung dem Array aller Vertretungen hinzufügen
                                    lessons.add(lesson);
                                }
                            }
                            break;
                        case "aufsichten":
                            NodeList nodeAufsichtenChilds = el.getChildNodes();
                            for (int k = 0; k < nodeAufsichtenChilds.getLength(); k++) {
                                if (nodeAufsichtenChilds.item(k).getNodeType() == Node.ELEMENT_NODE) {

                                    Element elementAufsicht = (Element) nodeAufsichtenChilds.item(k);
                                    String aufsichtInfo = elementAufsicht.getElementsByTagName("aufsichtinfo").item(0).getTextContent();

                                    String[] parts = aufsichtInfo.split(" - ");
                                    String[] parts2 = parts[1].split(" {2}--> {2}");

                                    Aufsicht aufsicht = new Aufsicht();

                                    aufsicht.setLocation(parts2[0]);
                                    aufsicht.setTeacher(parts2[1]);
                                    aufsicht.setTime(parts[0]);

                                    aufsichten.add(aufsicht);
                                }
                            }
                            break;
                    }
                }
            }

            Gson gson = new Gson();

            //System.out.println(gson.toJson(aufsichten));
            System.out.println(gson.toJson(lessons));
            //Vertretungen Array als Json String an die Uploader Methode übergeben
            uploadToApi(gson.toJson(lessons), "/vertretungen", config, logger);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Die Payload wird an die in der Config angegebene Url, mit dem übergebene Pfad, übertragen
     *
     * @param payload
     * @param urlPath
     * @param config
     * @param logger
     */
    private static void uploadToApi(String payload, String urlPath, @NotNull Config config, @NotNull Logger logger) {
        OkHttpClient client = new OkHttpClient();

        //Setzen des Datentypes der Übertragen wird.
        MediaType mediaType = MediaType.parse("application/json");
        //Erstellen der Daten die Übertragen werden.
        RequestBody body = RequestBody.create(mediaType, payload);

        //Http Request erstellen inkl. Daten und des authorization Tokens
        Request request = new Request.Builder()
                .url("https://api.nils-witt.codes" + urlPath)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(config.getBearer()))
                .build();
        try {
            //Anfrage an Api senden
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                if (config.getTrayNotifications()) {
                    displayTrayNotification("Upload erfolgreich", "Vertretungsplan zur API übertragen");
                }

            }
            logger.info(response.toString());
            response.close();
        } catch (java.io.IOException e) {
            logger.log(Level.WARNING, "Error", e);
        }
    }

    /**
     * Anzeigen einer Traynotification in Windows oder eines Fensters in macOS
     *
     * @param title   Title of the message(short)
     * @param message the message, longer desctipion or message body
     */

    private static void displayTrayNotification(String title, String message) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("info.png");
            TrayIcon trayIcon = new TrayIcon(image, "Vertretungsplan");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Vertretungsplan Import ");
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (java.awt.AWTException e) {
            e.printStackTrace();
        }
    }
}
