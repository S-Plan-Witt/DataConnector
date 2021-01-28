/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main extends Application {

    /**
     * JavaFX stageStart
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Logger logger = Logger.getLogger("Logger");

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        GuiLoggingHandler guiLoggingHandler = new GuiLoggingHandler(textArea);

        primaryStage.setScene(new Scene(textArea, 500, 500));
        primaryStage.setTitle("Console");
        primaryStage.show();

        logger.addHandler(guiLoggingHandler);
        logger.info("Gui Init");

        initApplication();
    }

    public static void main(String[] args) {
        launch();
    }

    public void initApplication(){
        //Init workspace and params
        String path;
        Config configRead;
        path = getJarPath();

        if (path == null) return;
        Logger logger = initLogger(path);
        if (logger == null) return;
        configRead = loadConfig(logger, path);
        if (configRead == null) {
            try {
                copyDefaultConfig();
                System.out.println("Created default config.json");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to create default config: ", e);
            }
            return;
        }
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
        StundenplanUntis stundenplanUntis = new StundenplanUntis(logger, api);

        CustomWatcher customWatcher = new CustomWatcher(vertretungsplan, vertretungsplanUntis, stundenplan, stundenplanUntis, klausurenplan, logger, config, path);

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
        Logger logger = Logger.getLogger("Logger");
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
     * creates a new config.json in the working directory
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    private static void copyDefaultConfig() throws IOException, URISyntaxException {
        InputStream in;
        JarURLConnection conn;
        JarFile jarfile;
        URL url;
        BufferedReader inputFileReader;
        File outputFileLocation;
        BufferedWriter outStream;
        String line;

        outputFileLocation = new File(getJarPath() + "/config.json");

        url = new URL("jar:file:" + new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath() + "!/");

        conn = (JarURLConnection) url.openConnection();
        jarfile = conn.getJarFile();

        in = jarfile.getInputStream(jarfile.getEntry("config.json"));

        inputFileReader = new BufferedReader(new InputStreamReader(in));

        outStream = new BufferedWriter(new FileWriter(outputFileLocation));

        while ((line = inputFileReader.readLine()) != null) {
            outStream.write(line);
            outStream.newLine();
        }
        outStream.close();
        in.close();
    }

    /**
     * Anzeigen einer Traynotification in Windows oder eines Fensters in macOS
     *
     * @param title   Title of the message(short)
     * @param message the message, longer description or message body
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
