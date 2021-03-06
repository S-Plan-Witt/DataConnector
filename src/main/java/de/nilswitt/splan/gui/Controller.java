/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.gui;

import de.nilswitt.splan.CliApplication;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Class Controller
 * FX Controller for the mainviewfxml.fxml View
 */
public class Controller {
    private final Logger logger = LogManager.getLogger(Controller.class);
    /**
     * Logging field
     */
    @FXML
    private TextArea textArea;
    /**
     * Main Pane
     */
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button buttonExit;
    @FXML
    private Button buttonReloadConfig;
    @FXML
    private Button buttonWatcher;

    private ConsoleGui consoleGui;
    private CliApplication cliApplication;

    public void setCliApplication(CliApplication cliApplication) {
        this.cliApplication = cliApplication;
    }

    public void setConsoleGui(ConsoleGui consoleGui) {
        this.consoleGui = consoleGui;
    }

    @FXML
    private void initialize() {
        textArea.setEditable(false);
        //Sets the link to the logger
        TextAreaAppender.setTextArea(textArea);

        textArea.prefWidthProperty().bind(anchorPane.widthProperty());
        textArea.prefHeightProperty().bind(anchorPane.heightProperty().subtract(50));

        buttonExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logger.info("Exit (Btn)");
                System.exit(0);
            }
        });

        buttonReloadConfig.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                logger.info("Reload");
                //TODO implement reload Config and component reset
            }
        });

        buttonWatcher.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                buttonWatcher.setText("Stop watcher");
                if (cliApplication.getCustomWatcher().isRunning()) {
                    buttonWatcher.setText("Start watcher");
                    try {
                        cliApplication.getCustomWatcher().shutdown();
                    } catch (IOException ex) {
                        //ex.printStackTrace();
                    }

                } else {
                    cliApplication.resetWatcherThread();
                    buttonWatcher.setText("Stop watcher");
                    cliApplication.getWatcherThread().start();
                }
            }
        });
    }
}
