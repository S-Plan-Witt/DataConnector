/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

import com.google.gson.Gson;
import de.nils_witt.splan.dataModels.Lesson;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class Stundenplan {

    public void hauptToLessons(Element hauptDOM,String grade){
        NodeList lessons = hauptDOM.getChildNodes();
        ArrayList<Lesson> lessonArrayList = new ArrayList<>();
        Gson gson = new Gson();
        int lessonsLenght = lessons.getLength();

        for(int i = 0; i < lessonsLenght; i++){
            if (lessons.item(i).getNodeType() == Node.ELEMENT_NODE) {
                int lessonNumber = 0;

                Element lesson = (Element) lessons.item(i);

                NodeList lessonInfos = lesson.getElementsByTagName("stunde");
                int lessonInfoLenght = lessonInfos.getLength();

                for(int lessonInfoCounter = 0; lessonInfoCounter < lessonInfoLenght; lessonInfoCounter++){
                    if (lessons.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element lessonInfo = (Element) lessonInfos.item(lessonInfoCounter);
                        lessonNumber = Integer.parseInt(lessonInfo.getTextContent());
                    }
                }

                NodeList lessonCourses = lesson.getChildNodes();
                int lessonCoursesLength = lessonCourses.getLength();

                for(int lessonCourseCounter = 0; lessonCourseCounter < lessonCoursesLength; lessonCourseCounter++){
                    if (lessonCourses.item(lessonCourseCounter).getNodeType() == Node.ELEMENT_NODE) {
                        Lesson lessonModel = new Lesson();

                        Element lessonCourse = (Element) lessonCourses.item(lessonCourseCounter);

                        if(lessonCourse.getTagName().substring(0,3).equals("tag")){
                            try {
                                lessonModel.setSubject(lessonCourse.getElementsByTagName("fach").item(0).getTextContent());
                                lessonModel.setLesson(lessonNumber);
                                lessonModel.setDay(Integer.parseInt(lessonCourse.getTagName().substring(3)));
                                lessonModel.setGrade(lessonCourse.getElementsByTagName("klasse").item(0).getTextContent());
                                lessonModel.setGroup(lessonCourse.getElementsByTagName("gruppe").item(0).getTextContent().substring(lessonModel.getSubject().length()).replaceAll("\\s",""));
                                String teacher = lessonCourse.getElementsByTagName("lehrer").item(0).getTextContent().substring(0,3);
                                lessonModel.setTeacher(teacher);
                                lessonModel.setRoom(lessonCourse.getElementsByTagName("raum").item(0).getTextContent());

                                lessonArrayList.add(lessonModel);
                            } catch (Exception e){
                                //e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
        System.out.println(gson.toJson(lessonArrayList));
    }

    public void kopfToLessons(Element lessons){
        System.out.println(lessons.getTagName());
        /*
        int lessonsLenght = lessons.getLength();

        for(int i = 0; i < lessonsLenght; i++){
            if (lessons.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element lesson = (Element)lessons.item(i);
                if(lesson.getTagName() == "title"){
                    System.out.println(lesson.getTextContent());
                }
            }

        }*/
    }
}
