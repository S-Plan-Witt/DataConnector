/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan.dataModels;

public class Klausuraufsicht {
    private String teacher = null;
    private String date = null;
    private String lesson = null;
    private String id = null;

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        if(teacher.length() > 3){
            teacher = teacher.substring(0,3);
        }
        this.teacher = teacher;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLesson() {
        return lesson;
    }

    public void setLesson(String lesson) {
        this.lesson = lesson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
