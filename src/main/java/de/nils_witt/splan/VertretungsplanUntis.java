/*
 * Copyright (c) 2020. Nils Witt
 */

package de.nils_witt.splan;

import com.google.gson.Gson;
import de.nils_witt.splan.dataModels.Course;
import de.nils_witt.splan.dataModels.Lesson;
import de.nils_witt.splan.dataModels.VertretungsLesson;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VertretungsplanUntis {
    private final ArrayList<VertretungsLesson> lessons = new ArrayList<>();
    private final Logger logger;
    private final Api api;
    private final Gson gson = new Gson();

    public VertretungsplanUntis(Logger logger, Api api) {
        this.logger = logger;
        this.api = api;
    }

    public ArrayList<VertretungsLesson> readXslx(String fileLocation) throws IOException {
        Lesson lessonAP = new Lesson();
        lessonAP.setCourse(new Course());
        lessonAP.getCourse().setGroup("L2");
        lessonAP.getCourse().setGrade("Q1");
        lessonAP.getCourse().setSubject("M");
        lessonAP.setRoom("N2.8");
        lessonAP.setLessonNumber(3);
        lessonAP.setDay(5);
        lessonAP.setId(387);

        //Lesson[] lessonsApi = new Lesson[]{lessonAP};
        Lesson[] lessonsApi = api.getLessons();
        if (lessonsApi.length > 0) {
            System.out.println(gson.toJson(lessonsApi[0]));
        }

        //fileLocation = "/Users/nilswitt/SynologyDrive/Splan/S-Plan-DataConnector/out/artifacts/S_Plan_DataConnector_jar/watchDir/Untis.xlsx";
        Logger logger = Logger.getLogger("TextLogger");
        logger.info("Starting XSLX read");
        Iterator rows;
        Iterator cells;
        InputStream excelFileToRead;
        XSSFWorkbook wb;
        XSSFSheet sheet;
        XSSFRow row;
        XSSFCell cell;

        try {
            System.out.println(fileLocation);
            excelFileToRead = new FileInputStream(fileLocation);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while opening File", e);
            return null;
        }

        //Öffnen der Datei
        logger.info("opening File");
        wb = new XSSFWorkbook(excelFileToRead);
        //Erstes Arbeitsblatt öffnen
        logger.info("opening Sheet");
        sheet = wb.getSheetAt(0);
        rows = sheet.rowIterator();

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            cell = row.getCell(0);
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                int eventId = (int) cell.getNumericCellValue();
                String eventType = row.getCell(1).getStringCellValue();
                if (eventType.equals("Pausenaufsicht")) {

                } else {
                    try {

                        String[] lessonNumbers = new String[]{};
                        if (row.getCell(3).getCellType() == CellType.NUMERIC) {
                            lessonNumbers = new String[]{String.valueOf((int) row.getCell(3).getNumericCellValue())};
                        } else if (row.getCell(3).getCellType() == CellType.STRING) {
                            lessonNumbers = row.getCell(3).getStringCellValue().split(" - ");
                        }

                        String date = formatDate(row.getCell(2).getStringCellValue());
                        String[] teachers = row.getCell(5).getStringCellValue().replaceAll("</s>", "").replaceAll("<s>", "").split("→");
                        String[] rooms = row.getCell(6).getStringCellValue().replaceAll("</s>", "").replaceAll("<s>", "").split("→");
                        String[] subjectGroup = row.getCell(7).getStringCellValue().replaceAll("</s>", "").replaceAll("<s>", "").split("-");
                        String grade = row.getCell(4).getStringCellValue();

                        VertretungsLesson lesson = new VertretungsLesson();

                        if (row.getCell(10) != null) {
                            lesson.setInfo(row.getCell(10).getStringCellValue());
                        } else {
                            lesson.setInfo("");
                        }

                        if (eventType.equals("Raum-Vtr.")) {
                            lesson.setTeacher(teachers[0]);
                            lesson.setSubject(subjectGroup[0]);
                        } else if (teachers.length == 1) {
                            lesson.setTeacher("---");
                            lesson.setSubject("---");
                        } else {
                            lesson.setTeacher(teachers[1]);
                            lesson.setSubject(subjectGroup[0]);
                        }
                        if (rooms.length == 1) {
                            lesson.setRoom("---");
                        } else {
                            lesson.setRoom(rooms[1]);
                        }

                        Course course = new Course();
                        course.setSubject(subjectGroup[0]);
                        course.setGrade(grade);
                        String group = subjectGroup[1];


                        if (group.startsWith("GK")) {
                            group = group.substring(2);
                        } else if (group.startsWith("LK")) {
                            group = "L".concat(group.substring(2));
                        }
                        course.setGroup(group);
                        lesson.setCourse(course);

                        if (lesson.getCourse().getSubject().equals("EK")) {
                            lesson.getCourse().setSubject("GO");
                        }

                        lesson.setDateUntis(date);

                        if (lessonNumbers.length == 1) {
                            lesson.setLessonNumber(Integer.parseInt(lessonNumbers[0]));
                            for (Lesson apiLesson : lessonsApi) {
                                if (lesson.getCourse().getGrade().equals(apiLesson.getCourse().getGrade())) {
                                    if (lesson.getCourse().getSubject().equals(apiLesson.getCourse().getSubject())) {
                                        if (lesson.getCourse().getGroup().equals(apiLesson.getCourse().getGroup())) {
                                            if (lesson.getLessonNumber() == apiLesson.getLessonNumber()) {
                                                if (lesson.getWeekday() == apiLesson.getDay()) {
                                                    System.out.println("Found(FIN):" + apiLesson.getId());
                                                    lesson.setLessonId(apiLesson.getId());
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            lesson.genReplacementId();
                            lessons.add(lesson);
                        } else {
                            for (int i = 0; i < lessonNumbers.length; i++) {
                                int lessonNum = Integer.parseInt(lessonNumbers[i]);
                                System.out.println(lessonNum);
                                lesson.setLessonNumber(lessonNum);
                                for (Lesson apiLesson : lessonsApi) {
                                    if (lesson.getCourse().getGrade().equals(apiLesson.getCourse().getGrade())) {
                                        if (lesson.getCourse().getSubject().equals(apiLesson.getCourse().getSubject())) {
                                            if (lesson.getCourse().getGroup().equals(apiLesson.getCourse().getGroup())) {
                                                if (lesson.getLessonNumber() == apiLesson.getLessonNumber()) {
                                                    if (lesson.getWeekday() == apiLesson.getDay()) {
                                                        System.out.println("Found(FIN):" + apiLesson.getId());
                                                        lesson.setLessonId(apiLesson.getId());
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                lesson.genReplacementId();
                                lessons.add(new VertretungsLesson(lesson));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("---------------");

                }


            }
        }

        return lessons;
    }


    private String formatDate(String dateIn) throws ParseException {
        SimpleDateFormat formatIn = new SimpleDateFormat("dd.MM");
        SimpleDateFormat formatOutL = new SimpleDateFormat("MM-dd");
        SimpleDateFormat formatOutY = new SimpleDateFormat("yyyy");
        Date currentDate = new Date();
        Date date = formatIn.parse(dateIn);
        return formatOutY.format(currentDate).concat("-").concat(formatOutL.format(date));
    }

    public void compareVplanLocalWithApi(List<VertretungsLesson> lessons) {
        ArrayList<String> localDates = new ArrayList<>();

        ArrayList<VertretungsLesson> lessonsServer = new ArrayList<>();
        ArrayList<String> lessonsServerIds = new ArrayList<>();
        ArrayList<String> lessonsLocalIds = new ArrayList<>();

        ArrayList<String> removedLessonsIds = new ArrayList<>();
        ArrayList<VertretungsLesson> updatedLessons = new ArrayList<>();

        //Get from all lesson the localDates and add them to unique list
        for (VertretungsLesson lesson : lessons) {
            if (!localDates.contains(lesson.getDate())) {
                localDates.add(lesson.getDate());
            }

            lessonsLocalIds.add(lesson.getReplacementId());
        }

        for (String date : localDates) {
            VertretungsLesson[] vertretungsLesson = api.getVertretungenByDate(date);
            for (VertretungsLesson lesson : vertretungsLesson) {
                lesson.genReplacementId();
                lessonsServerIds.add(lesson.getReplacementId());
                lessonsServer.add(lesson);
            }
        }


        for (VertretungsLesson vertretungsLesson : lessonsServer) {
            //System.out.println(vertretungsLesson.getReplacementId());
            if (!lessonsLocalIds.contains(vertretungsLesson.getReplacementId())) {
                removedLessonsIds.add(vertretungsLesson.getReplacementId());
            } else {
                int pos = lessonsLocalIds.indexOf(vertretungsLesson.getReplacementId());
                VertretungsLesson localLesson = lessons.get(pos);
                if (!(vertretungsLesson.getRoom().equals(localLesson.getRoom()) && vertretungsLesson.getInfo().equals(localLesson.getInfo()) && vertretungsLesson.getSubject().equals(localLesson.getSubject()))) {
                    updatedLessons.add(localLesson);
                }
            }
        }

        ArrayList<VertretungsLesson> addedLessons = new ArrayList<>();
        for (VertretungsLesson lesson : lessons) {
            if (!lessonsServerIds.contains(lesson.getReplacementId())) {
                //System.out.println("added: ".concat(lesson.getReplacementId()).concat(",pos: ").concat(String.valueOf(lessonsLocalIds.indexOf(lesson.getReplacementId()))));
                addedLessons.add(lesson);
                //System.out.println(lesson.getReplacementId());
            }

        }

        logger.info("removed:".concat(gson.toJson(removedLessonsIds)));
        logger.info("added:".concat(gson.toJson(addedLessons)));
        logger.info("updated:".concat(gson.toJson(updatedLessons)));


        for (String lessonId : removedLessonsIds) {
            api.deleteVertretung(lessonId);
        }

        api.addVertretungen(addedLessons);
        api.addVertretungen(updatedLessons);

    }
}
