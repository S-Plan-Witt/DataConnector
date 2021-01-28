/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan;

import com.google.gson.Gson;
import de.nilswitt.splan.dataModels.Klausur;
import de.nilswitt.splan.dataModels.Lesson;
import de.nilswitt.splan.dataModels.VertretungsLesson;
import okhttp3.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Api {
    private final Logger logger;
    private final String backend;
    private String bearer;
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private final MediaType mediaType = MediaType.parse("application/json");


    public Api(Logger logger, String backend) {
        this.logger = logger;
        this.backend = backend;
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
    }


    public boolean verifyBearer(String bearer) {
        boolean isValid = false;

        Request request = new Request.Builder()
                .url(backend.concat("/user"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                isValid = true;
                this.bearer = bearer;
                logger.info("Bearer valid");
            } else {
                //bearer invalid
                logger.log(Level.WARNING, "Bearer invalid");
            }
            response.close();
        } catch (java.net.UnknownHostException e) {
            //URL invalid
            logger.log(Level.WARNING, "API-Host not found");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception while verifying Bearer", e);
        }

        return isValid;
    }

    public VertretungsLesson[] getVertretungenByDate(String date){
        Request request = new Request.Builder()
                .url(backend.concat("/replacementLessons/date/").concat(date))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String json = response.body().string();
            response.close();
            return gson.fromJson(json, VertretungsLesson[].class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public Lesson[] getLessons(){
        Request request = new Request.Builder()
                .url(backend.concat("/timeTable/lessons"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String json = response.body().string();
            response.close();
            return gson.fromJson(json, Lesson[].class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public void deleteVertretung(String id){
        Request request = new Request.Builder()
                .url(backend.concat("/replacementLessons/id/").concat(id))
                .delete()
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addVertretungen(ArrayList<VertretungsLesson> vertretungen){

        RequestBody body = RequestBody.create(mediaType, gson.toJson(vertretungen));
        Request request = new Request.Builder()
                .url(backend.concat("/replacementLessons"))
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.code());
            response.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addLessons(ArrayList<Lesson> lessons){

        RequestBody body = RequestBody.create(mediaType, gson.toJson(lessons));
        Request request = new Request.Builder()
                .url(backend.concat("/timetable/lessons"))
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.code());
            response.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public VertretungsLesson[] getReplacementLessonByFilter(String info){

        RequestBody body = RequestBody.create(mediaType, "{\"info\":\"" + info + "\"}");

        Request request = new Request.Builder()
                .url(backend.concat("/replacementLessons/find/"))
                .post(body)
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String json = response.body().string();
            response.close();
            return gson.fromJson(json, VertretungsLesson[].class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void addExams(ArrayList<Klausur> exams){
        RequestBody body = RequestBody.create(mediaType, gson.toJson(exams));
        System.out.println(gson.toJson(exams));
        Request request = new Request.Builder()
                .url(backend.concat("/exams"))
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.code());
            response.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
