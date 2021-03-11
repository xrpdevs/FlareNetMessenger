package uk.co.xrpdevs.flarenetmessenger;
/* TODO: Currently mostly Redundant class, though we probably do need to implement a service to deal with certain
*   Intent()s and perhaps for maintaning certain states across context changes, as required.
*
*  TODO: Implement a listener for events coming from the the messaging contract so users get instant notification
*   that they have just recieved a message.
*
* */

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.web3j.abi.EventEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.Contracts.Fsms;

import static org.web3j.crypto.Credentials.create;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class MyService extends Service {
    public MyService() {
        super();
    }

    static BigInteger GAS_LIMIT = BigInteger.valueOf(1670025L);
    static BigInteger GAS_PRICE = BigInteger.valueOf(200000L);

    public static String contractAddress= "0x7884C21E95cBBF12A15F7EaF878224633d6ADF54";
    public static String fsmsContractAddress = "0xba448E96B4D03d4a5AF6faC9CAE12cac5004dBd2";
    static Web3j fsmsLink = Web3j.build(new HttpService("https://costone.flare.network/ext/bc/C/rpc"));
    public static Fsms fsms;
    public static SharedPreferences prefs;
    public SharedPreferences.Editor pEdit;
    static HashMap<String, String> deets;
    static org.web3j.crypto.Credentials c;
    Context mC;


    public static final String TAG = MyService.class.getSimpleName();
    private static boolean started = false;

    public static boolean isStarted() {
        return started;
    }

    /**
     * Factory Method
     */
    public static void start(Context context) {
        if (!isStarted()) {
            context.startService(new Intent(context, MyService.class));
        }
    }

    /**
     * Restart Service Interval
     */
    private static final int REPEAT_TIME_IN_SECONDS = 11 * 1000; // 11 Seconds

    /**
     * Variable for setting Alarm
     */
    private AlarmManager manager;
    private PendingIntent pendingIntent;
    private static boolean isAlarmSet = false;

    public static boolean isAlarmScheduled() {
        return isAlarmSet;
    }

    /**
     * Set the Alarm
     */



    private void setAlarm() {
        //pendingIntent = PendingIntent.getService(this, 0, new Intent(this, BCReceiver.class), 0);
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, BCReceiver.class), 0);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), REPEAT_TIME_IN_SECONDS,
                pendingIntent);
        isAlarmSet = true;
    }

    /**
     * Cancels the Alarm
     */
    private void cancelAlarm() {
        if (pendingIntent != null && manager != null) {
            manager.cancel(pendingIntent);
            pendingIntent.cancel();
            manager = null;
            pendingIntent = null;
        }
        isAlarmSet = false;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "onStartCommand");
        started = true;
        //if (!Utils.isDeviceOnline(getApplicationContext())) {
        //    cancelAlarm();
        //    stopSelf();
        //}
        prefs = getSharedPreferences("fnm", 0);
        try {
            deets = Utils.getPkey(this.getApplicationContext(), prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        c = create(deets.get("walletPrvKey"));
        fsms = Fsms.load(fsmsContractAddress, fsmsLink, c, GAS_PRICE, GAS_LIMIT);

        if (!isAlarmScheduled()) {
            setAlarm();
        }
        mC = getApplicationContext();
        // AsyncTask will take place here to get data from web.
        subscribeNotifications();
        started = false;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        started = false;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("CheckResult")
    public void subscribeNotifications(){
        BGThread bg = new BGThread(true);
        bg.start();

    }
    class BGThread extends Thread {
        Boolean a;
        BGThread(Boolean a){this.a = a;}
        public void run() {
            EthFilter eventFilter = new EthFilter(null, null, fsmsContractAddress);
            eventFilter.addSingleTopic(EventEncoder.encode(Fsms.MESSAGENOTIFICATION_EVENT)); // filter: event type (topic[0])
            eventFilter.addOptionalTopics(null, deets.get("walletAddress")); // filter: event parameters (gameId: no filter, player1: no filter, player2: filter)
            //eventFilter.addOptionalTopics(null, "0x3457834985"); // filter: event parameters (gameId: no filter, player1: no filter, player2: filter)
            myLog("fsms", "running on our owd fred");
            fsms.messageNotificationEventFlowable(eventFilter)
                    .doOnError(
                            error -> System.err.println("The error message is: " + error.getMessage()))
                    .subscribe(messageNotificationEventResponse ->
                    {
                        doAlert(messageNotificationEventResponse);
                        myLog("EVENT", "bob");
                    }, Throwable::printStackTrace);
        }
    }

    public void doAlert(Fsms.MessageNotificationEventResponse mno){
        myLog("EVENT", mno.toString());
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mC, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Web3j initWeb3j(){

        Web3j myEtherWallet = Web3j.build(
                new HttpService("https://costone.flare.network/ext/bc/C/rpc"));

        return myEtherWallet;
    }

}