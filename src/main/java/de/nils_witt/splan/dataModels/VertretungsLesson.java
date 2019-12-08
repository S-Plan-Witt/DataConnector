/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan.dataModels;

public class VertretungsLesson {
    private String date;
    private String lesson;
    private String subject;
    private String changedSubject;
    private String changedTeacher;
    private String changedRoom;
    private String info;
    private String grade;
    private String group;
    private String vertretungsID;

    public VertretungsLesson() {

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }


    public String getChangedSubject() {
        return changedSubject;
    }

    public void setChangedSubject(String changedSubject) {
        this.changedSubject = changedSubject;
    }

    public String getChangedTeacher() {
        return changedTeacher;
    }

    public void setChangedTeacher(String changedTeacher) {
        this.changedTeacher = changedTeacher;
    }

    public String getChangedRoom() {
        return changedRoom;
    }

    public void setChangedRoom(String changedRoom) {
        this.changedRoom = changedRoom;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVertretungsID() {
        return vertretungsID;
    }

    public void setVertretungsID(String vertretungsID) {
        this.vertretungsID = vertretungsID;
    }
}
