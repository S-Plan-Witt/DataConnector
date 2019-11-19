/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan.dataModels;

public class Klausur {
    private String date;
    private String from;
    private String to;
    private String grade;
    private String subject;
    private String group;
    private String teacher;
    private Integer students;
    private String room;
    private int display;


    public void setDate(String date) {
        this.date = date;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setStudents(Integer students) {
        this.students = students;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setDisplay(int display) {
        this.display = display;
    }
}
