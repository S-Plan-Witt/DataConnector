/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.gui;

import de.nilswitt.splan.connectors.LoggerConnector;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class Controller {
    @FXML
    private TextArea textArea;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private Button buttonExit;
    @FXML
    private Button buttonReloadConfig;
    @FXML
    private Button buttonWatcher;

    private ConsoleGui consoleGui;

    public void setConsoleGui(ConsoleGui consoleGui) {
        this.consoleGui = consoleGui;
    }

    @FXML
    private void initialize(){
        textArea.setEditable(false);

        GuiLoggingHandler guiLoggingHandler = new GuiLoggingHandler(textArea);
        LoggerConnector.addHandler(guiLoggingHandler);
        LoggerConnector.getLogger().info("Controller DOne");

        textArea.prefWidthProperty().bind(anchorPane.widthProperty());
        textArea.prefHeightProperty().bind(anchorPane.heightProperty().subtract(50));

        buttonExit.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                LoggerConnector.getLogger().info("Exit");
                System.exit(0);
            }
        });

        buttonReloadConfig.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                LoggerConnector.getLogger().info("Reload");
            }
        });

        buttonWatcher.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                buttonWatcher.setText("Stop watcher");
                if(consoleGui.getCustomWatcher().isRunning()){
                    buttonWatcher.setText("Start watcher");
                    try {
                        consoleGui.getCustomWatcher().shutdown();
                    }catch (IOException ex){
                        //ex.printStackTrace();
                    }

                }else {
                    buttonWatcher.setText("Stop watcher");
                    consoleGui.getWatcherThread().start();
                }
            }
        });
    }
}
