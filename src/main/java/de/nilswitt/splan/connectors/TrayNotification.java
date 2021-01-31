/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.connectors;

import java.awt.*;

public class TrayNotification {

    /**
     * Anzeigen einer Traynotification in Windows oder eines Fensters in macOS
     *
     * @param title   Title of the message(short)
     * @param message the message, longer description or message body
     */
    public static void display(String title, String message) {
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
