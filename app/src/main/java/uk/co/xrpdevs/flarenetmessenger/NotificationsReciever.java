package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationsReciever extends BroadcastReceiver {
    Boolean isConnected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        //MyService.subscribeNotifications();


        myLog("NOTIFREC", "action: " + intent.getAction() + " code: " + intent.getIntExtra("id", 0));

        String action = intent.getAction();
        int id = intent.getIntExtra("id", 0);

        RPCServer.authRequests.put(id, action);

    }


}
