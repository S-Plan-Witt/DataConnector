/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

public class Config {
    private String bearer;
    private String url;
    private Boolean trayNotifications;

    public String getBearer() {
        return bearer;
    }

    public String getUrl() {
        return url;
    }

    public Boolean getTrayNotifications() {
        return trayNotifications;
    }
}
