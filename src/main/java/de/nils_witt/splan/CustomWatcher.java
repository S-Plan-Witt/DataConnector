/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Timer;
import java.util.logging.Logger;

public class CustomWatcher {
    private Vertretungsplan vertretungsplan;
    private Stundenplan stundenplan;
    private Klausurplan klausurplan;
    private Logger logger;
    private Config config;
    private Path watchPath;
    private String path;
    private Timer timer;

    public CustomWatcher(Vertretungsplan vertretungsplan, Stundenplan stundenplan, Klausurplan klausurplan, Logger logger, Config config, String path) {
        this.vertretungsplan = vertretungsplan;
        this.stundenplan = stundenplan;
        this.klausurplan = klausurplan;
        this.logger = logger;
        this.config = config;
        this.watchPath = Paths.get(path.concat("/watchDir"));
        this.path = path;

        timer = new Timer(true);
    }

    public void start() throws IOException, InterruptedException, ParserConfigurationException {

        WatchService watchService = watchPath.getFileSystem().newWatchService();
        watchPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        logger.info("Started Watcher");
        while (true) {
            final WatchKey wk = watchService.take();
            for (WatchEvent<?> event : wk.pollEvents()) {
                final Path changed = (Path) event.context();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                try {
                    InputStream in = Files.newInputStream(Paths.get(path.concat("/watchDir/").concat(changed.getFileName().toString())));
                    Document document = builder.parse(in);

                    timer.schedule(new TimerProccess(document, this), 2000);
                }catch (IOException e){
                    System.out.println("In subdir");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            wk.reset();
        }
    }

    public void fileProccessor(Document document) {
        try {
            String nodeName;
            //Laden der base XML node, anhand dieser kann der Inhaltstyp ermittelt werden
            nodeName = document.getLastChild().getNodeName();
            if (config.getTrayNotifications()) {
                Main.displayTrayNotification("Änderung erkannt", "Datei: ".concat(nodeName));
            }

            switch (nodeName) {
                //vp = Vertretungsplan
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
                    break;
                default:
                    logger.info(nodeName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
