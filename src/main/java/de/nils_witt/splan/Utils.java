/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    private List<String> months = new ArrayList<String>();

    public Utils() {
        months.add("Januar");
        months.add("Februar");
        months.add("März");
        months.add("April");
        months.add("Mai");
        months.add("Juni");
        months.add("Juli");
        months.add("August");
        months.add("September");
        months.add("Oktober");
        months.add("November");
        months.add("Dezember");
    }

    public String convertDate(String oldDate) {
        String[] parts = oldDate.split(" ");
        int month = months.indexOf(parts[2]) + 1;

        return parts[3] + "-" + month + "-" + parts[1].substring(0, parts[1].length() - 1);
    }
}
