package uk.co.xrpdevs.flarenetmessenger;

import android.app.Application;
import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;

public class FlareNetMessenger extends Application {
    private static FlareNetMessenger instance;
    //public static boolean loggingOn = false;
    public static dbHelper dbH;
    public static HashMap<String, String> deets;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            initDB();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    public static Context getContext() {
        //return instance;
        return instance.getApplicationContext();
    }

    public void initDB() throws IOException, JSONException {
        dbH = new dbHelper(this);
    }
}