/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.gui;

import de.nilswitt.splan.CustomWatcher;
import de.nilswitt.splan.FileHandlers.*;
import de.nilswitt.splan.connectors.*;
import de.nilswitt.splan.dataModels.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.logging.Logger;

public class ConsoleGui extends Application {

    private Vertretungsplan vertretungsplan;
    private Stundenplan stundenplan;
    private Klausurplan klausurenplan;
    private VertretungsplanUntis vertretungsplanUntis;
    private StundenplanUntis stundenplanUntis;
    private CustomWatcher customWatcher;
    private Config config;
    private Api api;

    private Logger logger;

    private Thread watcherThread;

    public static void launchGui() {
        launch();
    }

    /**
     * JavaFX stageStart
     *
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        this.logger = LoggerConnector.getLogger();
        AnchorPane rootLayout;
        try {
            FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("fxml/mainview.fxml"));
            rootLayout = loader.load();
            Controller controller = loader.getController();
            controller.setConsoleGui(this);

            primaryStage.setScene(new Scene(rootLayout));
            primaryStage.setTitle("Console");
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }

        this.logger.info("Gui Init Done");
        initApplication();
    }

    private void enableSysTray() {

        if (!SystemTray.isSupported()) {
            logger.warning("SystemTray is not supported");
            return;
        }
        logger.info("SystemTray is supported");


        try {


            final PopupMenu popup = new PopupMenu();
            final TrayIcon trayIcon = new TrayIcon(ImageIO.read(FileSystemConnector.getResource("img/TrayIcon.png")));
            final SystemTray tray = SystemTray.getSystemTray();

            // Create a pop-up menu components
            MenuItem aboutItem = new MenuItem("About");
            CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
            CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
            Menu displayMenu = new Menu("Display");
            MenuItem errorItem = new MenuItem("Error");
            MenuItem warningItem = new MenuItem("Warning");
            MenuItem infoItem = new MenuItem("Info");
            MenuItem noneItem = new MenuItem("None");
            MenuItem exitItem = new MenuItem("Exit");

            //Add components to pop-up menu
            popup.add(aboutItem);
            popup.addSeparator();
            popup.add(cb1);
            popup.add(cb2);
            popup.addSeparator();
            popup.add(displayMenu);
            displayMenu.add(errorItem);
            displayMenu.add(warningItem);
            displayMenu.add(infoItem);
            displayMenu.add(noneItem);
            popup.add(exitItem);

            trayIcon.setPopupMenu(popup);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initApplication() {

        FileSystemConnector.createDatadirs();
        Logger logger = LoggerConnector.getLogger();
        LoggerConnector.addJsonHandler();
        if (logger == null) return;

        config = ConfigConnector.loadConfig(logger);
        if (config == null) {
            try {
                ConfigConnector.copyDefaultConfig();
                logger.info("Created default config.json");
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("Failed to create default config.json");
            }
            return;
        }

        if (!Api.verifyBearer(logger, config.getBearer(), config.getUrl())) {
            //Falls nicht config null setzen.
            logger.warning("Api token invalid");

            //return;
        }

        api = new Api(config);

        vertretungsplan = new Vertretungsplan(this.logger, api);
        stundenplan = new Stundenplan(this.logger, api);
        klausurenplan = new Klausurplan(this.logger, api);
        vertretungsplanUntis = new VertretungsplanUntis(this.logger, api);
        stundenplanUntis = new StundenplanUntis(this.logger, api);

        customWatcher = new CustomWatcher(vertretungsplan, vertretungsplanUntis, stundenplan, stundenplanUntis, klausurenplan, logger, config);

        watcherThread = new Thread(customWatcher);
        logger.info("Done initsi");
    }


    public Vertretungsplan getVertretungsplan() {
        return vertretungsplan;
    }

    public void setVertretungsplan(Vertretungsplan vertretungsplan) {
        this.vertretungsplan = vertretungsplan;
    }

    public Stundenplan getStundenplan() {
        return stundenplan;
    }

    public void setStundenplan(Stundenplan stundenplan) {
        this.stundenplan = stundenplan;
    }

    public Klausurplan getKlausurenplan() {
        return klausurenplan;
    }

    public void setKlausurenplan(Klausurplan klausurenplan) {
        this.klausurenplan = klausurenplan;
    }

    public VertretungsplanUntis getVertretungsplanUntis() {
        return vertretungsplanUntis;
    }

    public void setVertretungsplanUntis(VertretungsplanUntis vertretungsplanUntis) {
        this.vertretungsplanUntis = vertretungsplanUntis;
    }

    public StundenplanUntis getStundenplanUntis() {
        return stundenplanUntis;
    }

    public void setStundenplanUntis(StundenplanUntis stundenplanUntis) {
        this.stundenplanUntis = stundenplanUntis;
    }

    public CustomWatcher getCustomWatcher() {
        return customWatcher;
    }

    public void setCustomWatcher(CustomWatcher customWatcher) {
        this.customWatcher = customWatcher;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Thread getWatcherThread() {
        return watcherThread;
    }

    public void setWatcherThread(Thread watcherThread) {
        this.watcherThread = watcherThread;
    }
}
