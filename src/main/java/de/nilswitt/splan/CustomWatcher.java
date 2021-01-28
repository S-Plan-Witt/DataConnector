/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan;

import de.nilswitt.splan.dataModels.VertretungsLesson;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomWatcher {
    private final Vertretungsplan vertretungsplan;
    private final Stundenplan stundenplan;
    private final Klausurplan klausurplan;
    private final Logger logger;
    private final Config config;
    private final Path watchPath;
    private final String path;
    private final VertretungsplanUntis vertretungsplanUntis;
    private final StundenplanUntis stundenplanUntis;

    public CustomWatcher(Vertretungsplan vertretungsplan, VertretungsplanUntis vertretungsplanUntis, Stundenplan stundenplan, StundenplanUntis stundenplanUntis, Klausurplan klausurplan, Logger logger, Config config, String path) {
        this.vertretungsplan = vertretungsplan;
        this.vertretungsplanUntis = vertretungsplanUntis;
        this.stundenplan = stundenplan;
        this.stundenplanUntis = stundenplanUntis;
        this.klausurplan = klausurplan;
        this.logger = logger;
        this.config = config;
        this.watchPath = Paths.get(path.concat("/watchDir"));
        this.path = path;

    }

    public void start() throws IOException, InterruptedException {

        Path watcherPath = Paths.get(this.path.concat("/watchDir"));

        WatchService watchService = watcherPath.getFileSystem().newWatchService();

        watcherPath.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        System.out.println("Watcher started");
        WatchKey key;
        while (true) {
            key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                System.out.println(event.context());
                if(!event.context().toString().startsWith("~$")){
                    fileProccessor(event.context().toString());
                }

            }
            key.reset();
        }
    }

    public void fileProccessor(String changed) {
        try {

            if (changed.endsWith(".xml")) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                InputStream in = Files.newInputStream(Paths.get(path.concat("/watchDir/").concat(changed)));
                Document document = builder.parse(in);

                String nodeName;
                //Laden der base XML node, anhand dieser kann der Inhaltstyp ermittelt werden
                nodeName = document.getLastChild().getNodeName();
                if (config.getTrayNotifications()) {
                    Main.displayTrayNotification("Änderung erkannt", "Datei: ".concat(nodeName));
                }

                switch (nodeName) {
                    case "vp":
                        logger.info("Vplan");
                        vertretungsplan.readDocument(document);
                        break;
                    case "sp":
                        logger.info("Stundenplan");
                        //stundenplanFileReader(document, logger, config);
                        stundenplan.readDocument(document);
                        break;
                    case "dataroot":
                        logger.info("Klausuren");
                        klausurplan.readDocument(document);
                        klausurplan.pushExams();
                        break;
                    default:
                        logger.info(nodeName);
                }

            } else if (changed.endsWith(".xlsx")) {
                logger.log(Level.INFO, "Excel: " + Paths.get(path.concat("/watchDir/").concat(changed)));
                ArrayList<VertretungsLesson> vertretungsLessons = vertretungsplanUntis.readXslx(Paths.get(path.concat("/watchDir/").concat(changed)).toString());
                vertretungsplanUntis.compareVplanLocalWithApi(vertretungsLessons);
            } else if (changed.toLowerCase().endsWith(".txt")) {
                logger.log(Level.INFO, "DIF: " + Paths.get(path.concat("/watchDir/").concat(changed)));
                stundenplanUntis.readDocument(Paths.get(path.concat("/watchDir/").concat(changed)).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
