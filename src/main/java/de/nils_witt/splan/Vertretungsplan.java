/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

import com.google.gson.Gson;
import de.nils_witt.splan.dataModels.Aufsicht;
import de.nils_witt.splan.dataModels.Course;
import de.nils_witt.splan.dataModels.VertretungsLesson;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Vertretungsplan {
    private Utils utils = new Utils();
    private List<VertretungsLesson> lessons = new ArrayList<>();
    private List<Aufsicht> aufsichten = new ArrayList<>();
    private Logger logger;
    private Api api;
    private Gson gson = new Gson();

    public Vertretungsplan(Logger logger, Api api) {
        this.logger = logger;
        this.api = api;
    }

    public void readDocument(Document document) {
        String currentDate = "";
        int length;

        try {
            //Laden der base node Unterelemente
            NodeList nl = document.getLastChild().getChildNodes();

            length = nl.getLength();

            for (int i = 0; i < length; i++) {
                //Ünerprüfen, dass das Element eine Node ist
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) nl.item(i);
                    /*
                    Jeder Tag hat drei Nodes Kopf, Haupt und Aufsichen. Der Kopf bestimmt das Datum für die Folgenden Haput und Aufsichent Teile.
                    Die Teile Haupt und Aufsichen haben Unterelement, die die jeweiligen Events beschreiben (ein Event pro Vertretung / Aufsicht)
                     */
                    //Typ der Node bestimmen
                    switch (el.getTagName()) {
                        case "kopf":
                            //Das Datum der XML in das Format yyyy-mm-dd konvertieren, wie es von der Api erfordert wird, mithilfe der Utils Klasse.
                            currentDate = utils.convertDate(el.getElementsByTagName("titel").item(0).getTextContent());
                            break;
                        case "haupt":
                            //Laden aller Unterelement = Vertretungen
                            NodeList aktionen = el.getChildNodes();
                            for (int k = 0; k < aktionen.getLength(); k++) {
                                if (aktionen.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                    //Neues Object laden und Daten aus der Node übertragen
                                    VertretungsLesson lesson = new VertretungsLesson();

                                    Element aktion = (Element) aktionen.item(k);

                                    lesson.setLesson(aktion.getElementsByTagName("stunde").item(0).getTextContent());
                                    lesson.setInfo(aktion.getElementsByTagName("info").item(0).getTextContent());
                                    lesson.setChangedSubject(aktion.getElementsByTagName("vfach").item(0).getTextContent());
                                    String changedTeacher = aktion.getElementsByTagName("vlehrer").item(0).getTextContent();
                                    if ("(".concat(aktion.getElementsByTagName("lehrer").item(0).getTextContent()).concat(")").equals(changedTeacher)) {
                                        lesson.setChangedTeacher("---");
                                    } else {
                                        lesson.setChangedTeacher(changedTeacher);
                                    }

                                    lesson.setChangedRoom(aktion.getElementsByTagName("vraum").item(0).getTextContent());
                                    //Setzen das Datums, das im Kopf ausgelesen wurde
                                    lesson.setDate(currentDate);
                                    //Seperates Splitten der Kursbezeichnung in Stufe, Fach und Gruppe
                                    Course course = new Course();
                                    course.setSubject(aktion.getElementsByTagName("fach").item(0).getTextContent());
                                    course.updateByCourseString(aktion.getElementsByTagName("klasse").item(0).getTextContent());
                                    lesson.setSubject(course.getSubject());
                                    lesson.setGrade(course.getGrade());
                                    lesson.setGroup(course.getGroup());
                                    //Vertretung dem Array aller Vertretungen hinzufügen
                                    lessons.add(lesson);
                                }
                            }
                            break;
                        case "aufsichten":
                            NodeList nodeAufsichtenChilds = el.getChildNodes();
                            for (int k = 0; k < nodeAufsichtenChilds.getLength(); k++) {
                                if (nodeAufsichtenChilds.item(k).getNodeType() == Node.ELEMENT_NODE) {

                                    Element elementAufsicht = (Element) nodeAufsichtenChilds.item(k);
                                    String aufsichtInfo = elementAufsicht.getElementsByTagName("aufsichtinfo").item(0).getTextContent();

                                    String[] parts = aufsichtInfo.split(" - ");
                                    String[] parts2 = parts[1].split(" {2}--> {2}");

                                    Aufsicht aufsicht = new Aufsicht();

                                    aufsicht.setLocation(parts2[0]);
                                    aufsicht.setTeacher(parts2[1]);
                                    aufsicht.setTime(parts[0]);

                                    aufsichten.add(aufsicht);
                                }
                            }
                            break;
                    }
                }
            }
            //Vertretungen Array als Json String an die Vergleichs Methode übergeben
            compareVplanLocalWithApi(lessons);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void compareVplanLocalWithApi(List<VertretungsLesson> lessons){
        ArrayList<String> dates = new ArrayList<>();

        ArrayList<VertretungsLesson> lessonsServer = new ArrayList<>();
        ArrayList<String> lessonsServerId = new ArrayList<>();
        ArrayList<String> lessonsLocal = new ArrayList<>();

        ArrayList<String> removedLessons = new ArrayList<>();
        ArrayList<VertretungsLesson> updatedLessons = new ArrayList<>();

        //Get from all lesson the dates and add them to unique list
        for (VertretungsLesson lesson : lessons) {
            if(!dates.contains(lesson.getDate())){
                dates.add(lesson.getDate());
            }
            LocalDate date = LocalDate.parse(lesson.getDate());

            String id = lesson.getGrade().concat("-").concat(lesson.getSubject()).concat("-").concat(lesson.getGroup()).concat("-").concat(lesson.getLesson()).concat("-").concat(String.valueOf(date.getDayOfWeek().getValue())).concat("-").concat(lesson.getDate());
            lessonsLocal.add(id);
            lesson.setVertretungsID(id);
        }

        //Load vertretungen for each day
        for (String date : dates){

            VertretungsLesson[] vertretungsLesson = api.getVertretungenByDate(date);
            for (VertretungsLesson lesson : vertretungsLesson) {
                lessonsServerId.add(lesson.getVertretungsID());
                lessonsServer.add(lesson);
            }
        }


        for (VertretungsLesson vertretungsLesson : lessonsServer) {
            if(!lessonsLocal.contains(vertretungsLesson.getVertretungsID())) {
                //System.out.println("removed: ".concat(vertretungsLesson.getVertretungsID()));
                removedLessons.add(vertretungsLesson.getVertretungsID());
            }else {
                int pos = lessonsLocal.indexOf(vertretungsLesson.getVertretungsID());
                VertretungsLesson localLesson = lessons.get(pos);
                if(!(vertretungsLesson.getChangedRoom().equals(localLesson.getChangedRoom()) && vertretungsLesson.getChangedTeacher().equals(localLesson.getChangedTeacher()) && vertretungsLesson.getChangedSubject().equals(localLesson.getChangedSubject() ))){
                    //System.out.println("updated: ".concat(vertretungsLesson.getVertretungsID()));
                    updatedLessons.add(localLesson);
                }
            }
        }
        ArrayList<VertretungsLesson> addedLessons = new ArrayList<>();
        for (VertretungsLesson lesson : lessons) {
            if(!lessonsServerId.contains(lesson.getVertretungsID())){
                //System.out.println("added: ".concat(lesson).concat(",").concat(String.valueOf(lessonsLocal.indexOf(lesson))));
                addedLessons.add(lesson);
                System.out.println(lesson.getVertretungsID());
            }

        }

        System.out.println("removed:".concat(gson.toJson(removedLessons)));
        System.out.println("added:".concat(gson.toJson(addedLessons)));
        System.out.println("updated:".concat(gson.toJson(updatedLessons)));

        for (String lesson : removedLessons) {
            api.deleteVertretung(lesson);
        }
        /*
        for (VertretungsLesson lesson : updatedLessons) {
           // api.addVertretungen(lesson);
        }
        */
        api.addVertretungen(addedLessons);
        api.addVertretungen(updatedLessons);

    }
}
