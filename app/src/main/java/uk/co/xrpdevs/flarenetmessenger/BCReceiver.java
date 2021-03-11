package uk.co.xrpdevs.flarenetmessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import static uk.co.xrpdevs.flarenetmessenger.Utils.isMyServiceRunning;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class BCReceiver extends BroadcastReceiver {
    Boolean isConnected = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        //MyService.subscribeNotifications();
        if(isMyServiceRunning(MyService.class, context)){
            myLog("BCREC", "serbiver is running");
            IBinder binder = peekService(context, new Intent(context, MyService.class));
            if (binder != null) {
                myLog("BCREC", "We're bound");
            }
        } else {
            try {
                Intent serviceIntent = new Intent(context, MyService.class);
                context.startService(serviceIntent);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
/*        if (!isConnected) {
            IBinder binder = peekService(context, new Intent(context, Service.class));
            Toast.makeText(context, "Connected", 1000).show();
            if (binder != null){
                mChatService = ((LocalBinder) binder).getService();
                //.... other code here
            }
        }*/

        myLog("BCREC", "Got a broadcast!");
    }


}
