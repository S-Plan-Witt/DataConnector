/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.dataModels;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VertretungsLesson {
    private String date;
    private int lessonNumber;
    private Course course;
    private int weekday;
    private String subject;
    private String teacher;
    private String room;
    private String info;
    private String replacementId;
    private int lessonId;

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public VertretungsLesson() {

    }

    public VertretungsLesson(VertretungsLesson lesson) {
        this.date = lesson.date;
        this.lessonNumber = lesson.lessonNumber;
        this.course = lesson.course;
        this.weekday = lesson.weekday;
        this.subject = lesson.subject;
        this.teacher = lesson.teacher;
        this.room = lesson.room;
        this.info = lesson.info;
        this.replacementId = lesson.replacementId;
        this.lessonId = lesson.lessonId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            formatter = formatter.withLocale(Locale.GERMAN);


            String[] parts = date.split("-");
            if(parts.length > 3){
                if (parts[1].length() < 2){
                    parts[1] = "0".concat(parts[1]);
                }
                if (parts[2].length() < 2){
                    parts[3] = "0".concat(parts[3]);
                }

                this.date = parts[0].concat("-").concat(parts[1]).concat("-").concat(parts[2]);
                LocalDate localDate = LocalDate.parse(this.date, formatter);
                this.weekday = localDate.getDayOfWeek().getValue();
                System.out.println(localDate.getDayOfWeek().getValue());
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING,e.toString());
        }
    }

    public void setDateUntis(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            formatter = formatter.withLocale(Locale.GERMAN);

            this.date = date;
            LocalDate localDate = LocalDate.parse(this.date, formatter);
            this.weekday = localDate.getDayOfWeek().getValue();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING,e.toString());
        }
    }

    public int getWeekday() {
        return weekday;
    }

    public int getLessonNumber() {
        return lessonNumber;
    }

    public void setLessonNumber(int lessonNumber) {
        this.lessonNumber = lessonNumber;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getReplacementId() {
        return replacementId;
    }

    public void genReplacementId() {
        this.replacementId = this.date + "-" + this.lessonId;
    }
}
