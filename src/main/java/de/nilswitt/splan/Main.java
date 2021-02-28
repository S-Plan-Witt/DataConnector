/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan;

import de.nilswitt.splan.exceptions.InvalidCredentialsException;
import de.nilswitt.splan.gui.ConsoleGui;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private final static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        boolean cli = false;
        try {
            if (System.getenv("GUI").equals("false")) {
                System.out.println("CLI mode");
                cli = true;
            }

        } catch (Exception e) {

        }

        if (cli) {
            startCLI();
        } else {
            startGUI();
        }
    }

    public static void startGUI() {
        ConsoleGui.launchGui();
    }

    public static void startCLI() {
        CliApplication cliApplication = new CliApplication();
        try {
            cliApplication.initApplication();
            cliApplication.resetWatcherThread();
            cliApplication.getWatcherThread().start();
            cliApplication.getCustomWatcher().fileProcessor("Untis.xlsx");
        } catch (InvalidCredentialsException invalidCredentialsException) {
            logger.error("Exited(error)");
        }
    }

}
