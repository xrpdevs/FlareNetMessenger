package uk.co.xrpdevs.flarenetmessenger;

import android.app.Application;
import android.content.Context;

public class FlareNetMessenger extends Application {
    private static FlareNetMessenger instance;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static Context getContext() {
        //return instance;
        return instance.getApplicationContext();
    }
}