/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.gui;

import de.nilswitt.splan.CustomWatcher;
import de.nilswitt.splan.FileHandlers.*;
import de.nilswitt.splan.connectors.Api;
import de.nilswitt.splan.connectors.ConfigConnector;
import de.nilswitt.splan.connectors.FileSystemConnector;
import de.nilswitt.splan.connectors.LoggerConnector;
import de.nilswitt.splan.dataModels.Config;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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

    public static void launchGui() {
        launch();
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
