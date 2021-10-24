package uk.co.xrpdevs.flarenetmessenger;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

public class FlareNetMessenger extends Application {
    private static FlareNetMessenger instance;
    public static boolean loggingOn = false;
    public static dbHelper dbH;

    @Override
    public void onCreate() {
        instance = this;
        Log.d("EGGBY", "Is EEEEEEENID");
        super.onCreate();
        try {
            initDB();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        Log.d("EGGBY", "Is diggerZZZZ");
    }

    public static Context getContext() {
        //return instance;
        return instance.getApplicationContext();
    }

    public void initDB() throws IOException, JSONException {

        dbH = new dbHelper(this.getApplicationContext());
        if (dbH.ifTableExists("WAL")) {
            Log.d("EEEK", "Wallets table created!");
        } else {
            Log.d("EEEK", "NO SQLin going on ere! :(");
        }

    }
}