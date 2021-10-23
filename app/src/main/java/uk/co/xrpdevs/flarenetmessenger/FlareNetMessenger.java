package uk.co.xrpdevs.flarenetmessenger;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class FlareNetMessenger extends Application {
    private static FlareNetMessenger instance;
    SQLiteDatabase dh;
    public static boolean loggingOn = false;
    Context appContext;
    SQLiteInterface dbi;
    SQLiteDatabase dbh;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
        dbi = new SQLiteInterface(this);

    }

    public static Context getContext() {
        //return instance;
        return instance.getApplicationContext();
    }

}