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
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.web3j.abi.EventEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.contracts.ERC20;
import uk.co.xrpdevs.flarenetmessenger.contracts.Fsms;

import static org.web3j.crypto.Credentials.create;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class MyService extends Service {
    public MyService() {
        super();
    }

    static BigInteger GAS_LIMIT = BigInteger.valueOf(8000000L);
    static BigInteger GAS_PRICE = BigInteger.valueOf(470000000000L);

    static String rpc = "https://api.avax-test.network/ext/bc/C/rpc";

    public static String contractAddress= "0x7884C21E95cBBF12A15F7EaF878224633d6ADF54";
//    public static String fsmsContractAddress = "0x0A51d135316cc52A0CBac6f0D92c927A69bf6E27";  //COSTON
    public static String fsmsContractAddress = "0xdA451F4feBBbdDdAb8A80a606B45146d0ac1C4fa";    // AVAXTEST
//    public static String fCoinAddr = "0x068412C17fc66f73CC24048c334A1DBA994e8fED"; // COSTON
    public static String fCoinAddr = "0x94e0e1f82c99dBC11271DB7E39c1Af5E379aF8e0";              // AVAXTEST
    //    static Web3j fsmsLink = Web3j.build(new HttpService("https://costone.flare.network/ext/bc/C/rpc"));
//    static Web3j fCoinLink = Web3j.build(new HttpService("https://costone.flare.network/ext/bc/C/rpc"));
    static Web3j fsmsLink = initConnection(rpc, 0xa869);
    static Web3j fCoinLink = initConnection(rpc, 0xa869);
    public static Fsms fsms;
    public static ERC20 fcoin;
    public static SharedPreferences prefs;
    public SharedPreferences.Editor pEdit;
    static HashMap<String, String> deets;
    public static org.web3j.crypto.Credentials c;
    Context mC;
    public static String currentChain = "Coston";
    String cChain = currentChain;


    public static final String TAG = MyService.class.getSimpleName();
    private static boolean started = false;

    public static boolean isStarted() {
        return started;
    }

    /**
     * Factory Method
     */


    public static ERC20 getERC20link(String contractAddress, Credentials c, String RPCendPoint){
        //Web3j RPC = Web3j.build(new HttpService(RPCendPoint));
        ERC20 aToken = ERC20.load(contractAddress, fCoinLink, MyService.c, GAS_PRICE, GAS_LIMIT);
        return aToken;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

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

    public static void initialiseContracts(){
        fsms = Fsms.load(fsmsContractAddress, fsmsLink, c, GAS_PRICE, GAS_LIMIT);
        fcoin = ERC20.load(fCoinAddr, fsmsLink, c, GAS_PRICE, GAS_LIMIT);
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
        //   fsms = uk.co.xrpdevs.flarenetmessenger.contracts.Fsms.load(fsmsContractAddress, fsmsLink, c, GAS_PRICE, GAS_LIMIT);
        //    fcoin = uk.co.xrpdevs.flarenetmessenger.contracts.ERC20.load(fCoinAddr, fsmsLink, c, GAS_PRICE, GAS_LIMIT);
        initialiseContracts();

        //fcoin.transfer("")

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

            try {
                BigInteger bal = fcoin.balanceOf(c.getAddress()).send();
                String cn = fcoin.name().send();
                Log.d("uk.co.xrpdevs.flarenetmessenger.contracts.ERC20-name", cn);
                Log.d("BALANCE", bal.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            EthFilter eventFilter = new EthFilter(null, null, fsmsContractAddress);
            eventFilter.addSingleTopic(EventEncoder.encode(Fsms.MESSAGENOTIFICATION_EVENT));
            //TODO: Filter isn't working yet. Needs investigation!
            // think it is because walletAddress for both _to and _from will match walletAddress.
            // it doesn't check by position, only if the hash exists.
            // think we need to alter our contract to only emit (a hash of?) _to as having both _from and _to in Events makes
            // it easier for an outsider to collect metadata
            //
            // Modified smart contract with same results. Frustrating. Is this the best way of doing this?
            // We could store a "lastReceivedMessageTimeStamp" in wallet info, then just query the contract for any new messages
            // since that time. Since the HashMap that messages are stored in is static, we could add new messages to it from the thread
            // in the Service if there are activities open. Is it worth user's data if we're having to listen to EVERY messageNotificationEvent
            // and then filter in the app - presumably this is what the Flowable has to do anyway, seems like a waste of resources.                                        vcvv

            eventFilter.addOptionalTopics(null, deets.get("walletAddress"));

            myLog("fsms", "running on our owd fred, "+deets.get("walletAddress").substring(2));
        /*    Disposable bob = fsms.messageNotificationEventFlowable(eventFilter)
                    .doOnError(
                            error -> System.err.println("The error message is: " + error.getMessage()))
                    .subscribe(messageNotificationEventResponse ->
                    {
                        doAlert(messageNotificationEventResponse);
                        myLog("EVENT", deets.get("walletAddress")+" "+messageNotificationEventResponse._to);
                    }, Throwable::printStackTrace);*/

        }
    }
    public void doAlert(Fsms.MessageNotificationEventResponse mno){
        myLog("EVENT", "_to   = walletaddress: "+mno._to.equals(deets.get("walletAddress")));
        if(mno._to.equals(deets.get("walletAddress"))) {
            //    mno.
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(mC, notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "uk.co.xrpdevs.flarenetmessenger";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_wallet)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public boolean stopService(Intent name) {
        // TODO Auto-generated method stub
        cancelAlarm();
        stopSelf();
        return super.stopService(name);

    }

    public static Web3j initConnection(String rpc, int chainID){

        Web3j myEtherWallet = Web3j.build(

                new HttpService(rpc));
//                new HttpService("https://costone.flare.network/ext/bc/C/rpc"));
        myEtherWallet.ethChainId().setId(chainID);
        return myEtherWallet;
    }

    public static Web3j initWeb3j(){

        Web3j myEtherWallet = Web3j.build(

                new HttpService("https://api.avax-test.network/ext/bc/C/rpc"));
//                new HttpService("https://costone.flare.network/ext/bc/C/rpc"));
        myEtherWallet.ethChainId().setId(0xa869);
        return myEtherWallet;
    }

}