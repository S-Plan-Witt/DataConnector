/*
 * Copyright (c) 2019. Nils Witt
 */

package de.nils_witt.splan;

import com.google.gson.Gson;
import de.nils_witt.splan.dataModels.Klausur;
import de.nils_witt.splan.dataModels.Lesson;
import de.nils_witt.splan.dataModels.LessonRequest;
import de.nils_witt.splan.dataModels.VertretungsLesson;
import okhttp3.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Api {
    private Logger logger;
    private String backend;
    private String bearer;
    private OkHttpClient client;
    private Gson gson = new Gson();
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
        } catch (java.net.UnknownHostException e) {
            //URL invalid
            logger.log(Level.WARNING, "Host not found", e);
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
            String json = response.body().string();
            response.close();
            return gson.fromJson(json, VertretungsLesson[].class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public void deleteVertretung(String id){
        Request request = new Request.Builder()
                .url(backend.concat("/vertretungen/id/").concat(id))
                .delete()
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
            return;
        }catch (Exception e){
            e.printStackTrace();
        }
        return;
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
        return;
    }

    public void updateVertretungen(VertretungsLesson vertretung){

        RequestBody body = RequestBody.create(mediaType, gson.toJson(vertretung));
        Request request = new Request.Builder()
                .url(backend.concat("/vertretungen/id/".concat(vertretung.getVertretungsID())))
                .put(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ".concat(bearer))
                .build();

        try {
            Response response = client.newCall(request).execute();
            response.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return;
    }

    public void addLessons(ArrayList<Lesson> lessons){

        RequestBody body = RequestBody.create(mediaType, gson.toJson(lessons));
        Request request = new Request.Builder()
                .url("https://api.nils-witt.de".concat("/lessons"))
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
        return;
    }
}
