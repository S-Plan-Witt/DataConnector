/*
 * Copyright (c) 2021.
 */

package de.nilswitt.splan.connectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileSystemConnector {
    private static final Logger logger = LogManager.getLogger(FileSystemConnector.class);

    /**
     * Copy a file from source to destination.
     *
     * @param source      the source
     * @param destination the destination
     * @return True if succeeded , False if not
     */
    public static boolean copy(InputStream source, String destination) {
        boolean succeess = true;

        logger.info("Copying ->" + source + "\n\tto ->" + destination);

        try {
            Files.copy(source, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
            // logger.log(Level.WARNING, "", ex);
            succeess = false;
        }

        return succeess;

    }

    public static String getWorkingDir() {
        return Paths.get("").toAbsolutePath().toString();
    }

    public static void createDataDirs() {
        File dataDir = new File(getWorkingDir() + "/data/");
        dataDir.mkdir();
        File logDir = new File(getWorkingDir() + "/data/logs");
        logDir.mkdir();
        File watchDir = new File(getWorkingDir() + "/data/watcher");
        watchDir.mkdir();
    }

    public static URL getResource(String name) {
        return Thread.currentThread().getContextClassLoader().getResource(name);
    }
}
