/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan;

import de.nilswitt.splan.gui.ConsoleGui;

public class Main {

    public static void main(String[] args) {
        boolean cli = false;
        try {
            if(System.getenv("GUI").equals("false")){
                System.out.println("CLI mode");
                cli = true;
            }

        }catch (Exception e){

        }

        if(cli){
            startCLI();
        }else {
            startGUI();
        }
    }

    public static void startGUI(){
        ConsoleGui.launchGui();
    }

    public static void startCLI(){
        CliApplication cliApplication = new CliApplication();

        cliApplication.initApplication();
        cliApplication.resetWatcherThread();
        cliApplication.getWatcherThread().start();
    }

}
