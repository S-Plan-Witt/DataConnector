/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.gui;

import de.nilswitt.splan.CliApplication;
import de.nilswitt.splan.CustomWatcher;
import de.nilswitt.splan.FileHandlers.*;
import de.nilswitt.splan.connectors.Api;
import de.nilswitt.splan.connectors.ConfigConnector;
import de.nilswitt.splan.connectors.FileSystemConnector;
import de.nilswitt.splan.dataModels.Config;
import de.nilswitt.splan.exceptions.InvalidCredentialsException;
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
    private CliApplication cliApplication = new CliApplication();

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
            controller.setCliApplication(cliApplication);
            primaryStage.setTitle("Console");
            primaryStage.sizeToScene();
            primaryStage.show();
            this.logger.info("Controller Done");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.logger.info("Gui Init Done");
        cliApplication.initApplication();
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

}
