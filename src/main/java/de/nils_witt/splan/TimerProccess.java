/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

import org.w3c.dom.Document;
import java.util.TimerTask;

public class TimerProccess extends TimerTask {
    private Document document;
    private CustomWatcher customWatcher;

    public TimerProccess(Document document, CustomWatcher customWatcher ) {
        this.document = document;
        this.customWatcher = customWatcher;

    }

    @Override
    public void run()
    {
        customWatcher.fileProccessor(document);
    }

}
