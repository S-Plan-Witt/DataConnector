/*
 * Copyright (c) 2021. Nils Witt
 */

package de.nilswitt.splan.connectors;

import com.google.gson.Gson;
import de.nilswitt.splan.dataModels.Config;
import de.nilswitt.splan.dataModels.Klausur;
import de.nilswitt.splan.dataModels.Lesson;
import de.nilswitt.splan.dataModels.VertretungsLesson;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Api {
    private final Logger logger = LoggerConnector.getLogger();
    private final OkHttpClient client;
    private final Config config;
    private final Gson gson = new Gson();
    private final MediaType mediaType = MediaType.parse("application/json");


    public Api(Config config) {
        this.config = config;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Überprüfen der Gültigkeit des Zugriffstoken auf die Api
     *
     * @param logger Filelogger
     * @param bearer token for api access
     * @param url    base api url
     * @return validity of bearer to given url
     */
    public static boolean verifyBearer(@NotNull Logger logger, @NotNull String bearer, @NotNull String url) {
        OkHttpClient client = new OkHttpClient();
        boolean isValid = false;
        Request request = new Request.Builder()
                .url(url.concat("/user"))
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();
        try {
            Response response = client.newCall(request).execute();
            // Api gibt den status 200 zurück, wenn alles  gültig ist.
            if (response.code() == 200) {
                isValid = true;
                logger.info("Bearer valid");
            } else {
                logger.log(Level.WARNING, "Bearer invalid");
            }
        } catch (java.net.UnknownHostException e) {
            //URL der Api ist nicht gültig
            logger.log(Level.WARNING, "Host not found");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception while verifying Bearer");
        }

        return isValid;
    }

    public VertretungsLesson[] getVertretungenByDate(String date) {
        Request request = new Request.Builder()
                .url(this.config.getUrl().concat("/replacementLessons/date/").concat(date))
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .build();

        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String json = response.body().string();
            response.close();
            return gson.fromJson(json, VertretungsLesson[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Lesson[] getLessons() {
        Request request = new Request.Builder()
                .url(this.config.getUrl().concat("/timeTable/lessons"))
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .build();

        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String json = response.body().string();
            response.close();
            return gson.fromJson(json, Lesson[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void deleteVertretung(String id) {
        Request request = new Request.Builder()
                .url(this.config.getUrl().concat("/replacementLessons/id/").concat(id))
                .delete()
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addVertretungen(ArrayList<VertretungsLesson> vertretungen) {

        RequestBody body = RequestBody.create(mediaType, gson.toJson(vertretungen));
        Request request = new Request.Builder()
                .url(this.config.getUrl().concat("/replacementLessons"))
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addLessons(ArrayList<Lesson> lessons) {

        RequestBody body = RequestBody.create(mediaType, gson.toJson(lessons));
        Request request = new Request.Builder()
                .url(this.config.getUrl().concat("/timetable/lessons"))
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VertretungsLesson[] getReplacementLessonByFilter(String info) {

        RequestBody body = RequestBody.create(mediaType, "{\"info\":\"" + info + "\"}");

        Request request = new Request.Builder()
                .url(this.config.getUrl().concat("/replacementLessons/find/"))
                .post(body)
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .build();
        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String json = response.body().string();
            response.close();
            return gson.fromJson(json, VertretungsLesson[].class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addExams(ArrayList<Klausur> exams) {
        RequestBody body = RequestBody.create(mediaType, gson.toJson(exams));
        Request request = new Request.Builder()
                .url(this.config.getUrl().concat("/exams"))
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(this.config.getBearer()))
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
