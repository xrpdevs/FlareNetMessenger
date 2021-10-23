package uk.co.xrpdevs.flarenetmessenger;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FlareNetMessenger extends Application {
    private static FlareNetMessenger instance;
    SQLiteDatabase dh;
    public static boolean loggingOn = false;
    Context appContext;
    static dbHelper dbH;
    //SQLiteDatabase dbh;

    @Override
    public void onCreate() {

        instance = this;


        //dbi.myhelper.


        Log.d("EGGBY", "Is EEEEEEENID");
        super.onCreate();
        try {
            initDB();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
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
        InputStream is = FlareNetMessenger.getContext().getAssets().open("blockchains.json");
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, StandardCharsets.UTF_8);
        //myLog("JSON", "== "+json);
        JSONArray jo = new JSONArray(json);
        dbH.setupblk(jo);
    }
}