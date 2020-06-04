/*
 * Copyright (c) 2020. Nils Witt
 */

package de.nils_witt.splan;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        Api api = new Api(logger, config.getUrl());
        if(!api.verifyBearer(config.getBearer())){
            return;
        }

        Vertretungsplan vertretungsplan = new Vertretungsplan(logger, api);
        Stundenplan stundenplan = new Stundenplan(logger, api);
        Klausurplan klausurenplan = new Klausurplan(logger, api);
        VertretungsplanUntis vertretungsplanUntis = new VertretungsplanUntis(logger, api);

        CustomWatcher customWatcher = new CustomWatcher(vertretungsplan, vertretungsplanUntis, stundenplan, klausurenplan, logger, config, path);

        try {
            customWatcher.start();
        } catch (Exception e){
            e.printStackTrace();
        }
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
     * Anzeigen einer Traynotification in Windows oder eines Fensters in macOS
     *
     * @param title   Title of the message(short)
     * @param message the message, longer desctipion or message body
     */
    static void displayTrayNotification(String title, String message) {
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
