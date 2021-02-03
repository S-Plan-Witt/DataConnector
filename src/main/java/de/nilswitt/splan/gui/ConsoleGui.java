/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.gui;

import de.nilswitt.splan.CustomWatcher;
import de.nilswitt.splan.FileHandlers.*;
import de.nilswitt.splan.connectors.Api;
import de.nilswitt.splan.connectors.ConfigConnector;
import de.nilswitt.splan.connectors.FileSystemConnector;
import de.nilswitt.splan.dataModels.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;


public class ConsoleGui extends Application {

    private final Logger logger = LogManager.getLogger(ConsoleGui.class);
    private Vertretungsplan vertretungsplan;
    private Stundenplan stundenplan;
    private Klausurplan klausurenplan;
    private VertretungsplanUntis vertretungsplanUntis;
    private StundenplanUntis stundenplanUntis;
    private CustomWatcher customWatcher;
    private Config config;
    private Api api;
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
        Scene scene;
        javafx.scene.control.TextArea area;
        javafx.scene.control.TextField field;
        BorderPane border;

        try {
            FXMLLoader loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("fxml/mainview.fxml"));

            primaryStage.setScene(new Scene(loader.load()));
            Controller controller = loader.getController();
            controller.setConsoleGui(this);
            primaryStage.setTitle("Console");
            primaryStage.sizeToScene();
            primaryStage.show();
            this.logger.info("Controller Done");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.logger.info("Gui Init Done");
        initApplication();
    }

    private void enableSysTray() {

        if (!SystemTray.isSupported()) {
            logger.warn("SystemTray is not supported");
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
                logger.fatal("TrayIcon could not be added.", e);
            }
        } catch (Exception e) {
            logger.fatal(e);
            e.printStackTrace();
        }
    }

    private void initApplication() {

        FileSystemConnector.createDataDirs();

        config = ConfigConnector.loadConfig();
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

        if (!Api.verifyBearer(config.getBearer(), config.getUrl())) {
            //Falls nicht config null setzen.
            logger.warn("Api token invalid");

            //return;
        }

        api = new Api(config);

        vertretungsplan = new Vertretungsplan(api);
        stundenplan = new Stundenplan(api);
        klausurenplan = new Klausurplan(api);
        vertretungsplanUntis = new VertretungsplanUntis(api);
        stundenplanUntis = new StundenplanUntis(api);

        customWatcher = new CustomWatcher(vertretungsplan, vertretungsplanUntis, stundenplan, stundenplanUntis, klausurenplan, config);

        watcherThread = new Thread(customWatcher);
        logger.info("Done initialisation");
    }

    public CustomWatcher getCustomWatcher() {
        return customWatcher;
    }

    public Thread getWatcherThread() {
        return watcherThread;
    }

    public void resetWatcherThread() {
        customWatcher = new CustomWatcher(vertretungsplan, vertretungsplanUntis, stundenplan, stundenplanUntis, klausurenplan, config);
        watcherThread = new Thread(customWatcher);
    }
}
