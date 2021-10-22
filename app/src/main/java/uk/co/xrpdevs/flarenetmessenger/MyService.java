package uk.co.xrpdevs.flarenetmessenger;
/* TODO: Currently mostly Redundant class, though we probably do need to implement a service to deal with certain
*   Intent()s and perhaps for maintaning certain states across context changes, as required.
*
*  TODO: Implement a listener for events coming from the the messaging contract so users get instant notification
*   that they have just recieved a message.
*
* */

import static org.web3j.crypto.Credentials.create;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.EventEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.http.HttpService;
import org.xrpl.xrpl4j.client.JsonRpcClient;
import org.xrpl.xrpl4j.client.XrplClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocketListener;
import uk.co.xrpdevs.flarenetmessenger.contracts.ERC20;
import uk.co.xrpdevs.flarenetmessenger.contracts.Fsms;


public class MyService extends Service {
    public MyService() {
        super();
    }

    private final IBinder mBinder = new MyBinder();
    private Messenger outMessenger;
    static BigInteger GAS_LIMIT = BigInteger.valueOf(8000000L);
    static BigInteger GAS_PRICE = BigInteger.valueOf(470000000000L);
    public static boolean isXRPL = false;
    public static XrplClient xrplClient;
    public XrplClient _xrplClient;
    HttpUrl rippledUrl;
    //    static int tmpCID = 0x11;

//    public static String rpc = "https://coston.flare.network/ext/bc/C/rpc";

    //  public static String contractAddress= "0x7884C21E95cBBF12A15F7EaF878224633d6ADF54";
//    public static String fsmsContractAddress = "0x21dd8FAa568b05Fd260e998D2d0adc12b5f36b1E";  //COSTON
    public static String fsmsContractAddress = "0xBb5E6dC37Fe620E71A9394bC1dE446D4ED11C7eb"; //local testnet
    public static String contractAddress = fsmsContractAddress;
    //    public static String fsmsContractAddress = "0xdA451F4feBBbdDdAb8A80a606B45146d0ac1C4fa";    // AVAXTEST
//    public static String fCoinAddr = "0xd15942e499186AA173A082ED0Bc90Aa3Ab93bd73"; // COSTON
    public static String fCoinAddr = "0x933FDA928386bce4021FC472b4115C427df06612"; // local testnet

    //    public static String fCoinAddr = "0x94e0e1f82c99dBC11271DB7E39c1Af5E379aF8e0";              // AVAXTEST
    //    static Web3j fsmsLink = Web3j.build(new HttpService("https://costone.flare.network/ext/bc/C/rpc"));
//    static Web3j fCoinLink = Web3j.build(new HttpService("https://costone.flare.network/ext/bc/C/rpc"));

    public static SharedPreferences prefs;
    public SharedPreferences.Editor pEdit;

    public static int tmpCID = 16;
    public static String rpc = "https://testnet.xrpdevs.co.uk:9650/ext/bc/C/rpc";
    public static Web3j fsmsLink = initConnection(rpc, tmpCID);
    public static Web3j fCoinLink = initConnection(rpc, tmpCID);
    public static Fsms fsms;
    public static ERC20 fcoin;

    static HashMap<String, String> deets;
    public static org.web3j.crypto.Credentials c;
    Context mC;
    public static String currentChain = "Coston";
    String cChain = currentChain;

    HashMap<String, String> addresses;

    public static final String TAG = MyService.class.getSimpleName();
    private static boolean started = false;

    public static boolean isStarted() {
        return started;
    }

    public boolean isFirstRun = true;

    /**
     * Factory Method
     */


    public static ERC20 getERC20link(String contractAddress, Credentials c, String RPCendPoint){
        //Web3j RPC = Web3j.build(new HttpService(RPCendPoint));
        ERC20 aToken = ERC20.load(contractAddress, fCoinLink, MyService.c, GAS_PRICE, GAS_LIMIT);
        return aToken;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mC = getApplicationContext();


        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

        HttpUrl rippledUrl = HttpUrl
                .get("https://s.altnet.rippletest.net:51234/");
        xrplClient = new XrplClient(rippledUrl);
        JsonRpcClient bob = xrplClient.getJsonRpcClient();
        String jsub = "{\"id\": \"76\"," +
                "\"command\": \"subscribe\"," +
                "\"accounts\": [";
        try {
            addresses = Utils.walletAddressesToWalletNamesOrContactsToHashMap(mC, "XRPL");
        } catch (JSONException e) {
            myLog("WAT", "Error: " + e);
            e.printStackTrace();
        }
        int end = addresses.size();
        int a = 0;


        for (Map.Entry<String, String> pair : addresses.entrySet()) {
            //System.out.format("key: %s, value: %d%n", pair.getKey(), pair.getValue());

            jsub = jsub + "\"" + pair.getKey() + "\"";
            if (a != (end - 1)) jsub += ", ";
            a++;
        }

        jsub += "]}";

        myLog("FUCKYOU", addresses.toString());
        myLog("FUCKYOU", jsub);

        WSock socket = WSock.Builder.with("wss://s.altnet.rippletest.net/").build().connect();
        socket.sendOnOpen("76", jsub);
        socket.onEventResponse("76", oevrl);

        // .createSocket("ws://localhost/endpoint");
        // bob.
        //   _xrplClient = xrplClient;

  /*    //  if (Build.VERSION.SDK_INT >= 28) {

          //  StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

         //   StrictMode.setVmPolicy(builder.build());
//
    //    }
/*

// Create a Wallet using a WalletFactory
        WalletFactory walletFactory = DefaultWalletFactory.getInstance();
        Wallet testWallet = walletFactory.randomWallet(true).wallet();

// Get the Classic and X-Addresses from testWallet
        Address classicAddress = testWallet.classicAddress();
        XAddress xAddress = testWallet.xAddress();

        System.out.println("Classic Address: " + classicAddress);
        System.out.println("X-Address: " + xAddress);

// Fund the account using the testnet Faucet
        FaucetClient faucetClient = FaucetClient
                .construct(HttpUrl.get("https://faucet.altnet.rippletest.net"));
        faucetClient.fundAccount(FundAccountRequest.of(classicAddress));


// Look up your Account Info
        AccountInfoRequestParams requestParams =
                AccountInfoRequestParams.of(classicAddress);
        AccountInfoResult accountInfoResult =
                null;
        try {
            accountInfoResult = xrplClient.accountInfo(requestParams);
        } catch (JsonRpcClientErrorException e) {
            e.printStackTrace();
        }

// Print the result
        myLog("XRPAccountInfo", accountInfoResult.toString());
*/

        prefs = getSharedPreferences("fnm", 0);
        Log.d("PREFS", Utils.dumpMap(prefs.getAll()));


        if (prefs.contains("csbc_rpc") && prefs.contains("csbc_cid")) {
            tmpCID = Integer.decode(prefs.getString("csbc_cid", "1"));
            rpc = prefs.getString("csbc_rpc", rpc);
        }

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

    WSock.OnEventResponseListener oevrl = new WSock.OnEventResponseListener() {
        @Override
        public void onMessage(String event, String data) {
            try {
                doAlert(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

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
        Log.d("PREFS", Utils.dumpMap(prefs.getAll()));
        if (prefs.contains("walletCount") && prefs.getInt("walletCount", 0) > 0) {
            try {
                deets = Utils.getPkey(this.getApplicationContext(), prefs.getInt("currentWallet", 0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (deets.containsKey("walletType") && !deets.get("walletType").equals("XRPL")) {
                c = create(deets.get("walletPrvKey"));
                //   fsms = uk.co.xrpdevs.flarenetmessenger.contracts.Fsms.load(fsmsContractAddress, fsmsLink, c, GAS_PRICE, GAS_LIMIT);
                //    fcoin = uk.co.xrpdevs.flarenetmessenger.contracts.ERC20.load(fCoinAddr, fsmsLink, c, GAS_PRICE, GAS_LIMIT);
                initialiseContracts();

                //fcoin.transfer("")
            }
            if (!isAlarmScheduled()) {
                setAlarm();
            }
            mC = getApplicationContext();


            // AsyncTask will take place here to get data from web.
            subscribeNotifications();
        }
        started = false;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        started = false;
    }

    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d("SERVICE", "Message recieved: " + intent.getStringExtra("message"));
        //   Bundle extras = arg0.getExtras();
        Log.d("service", "onBind");
        // Get messager from the Activity
        // if (extras != null) {
        //     Log.d("service","onBind with extra");
        //     outMessenger = (Messenger) extras.get("MESSENGER");
        //  }
        return mBinder;
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
            //      @ReflectionSupport(ReflectionSupport.Level.FULL);
            //     XrplClient fucker = new XrplClient(rippledUrl);

            if (deets.containsKey("walletPrvKey")) {
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
            }
            myLog("fsms", "running on our owd fred, ");

            //+deets.get("walletAddress").substring(2));
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

    public void doAlert(String XRPLJSONdata) throws JSONException {
        JSONObject data = new JSONObject(XRPLJSONdata);
        JSONObject transaction = data.getJSONObject("transaction");
        String account = transaction.optString("Account");
        String destination = transaction.optString("Destination");
        String amount = transaction.optString("Amount");
        amount = new BigDecimal(amount).movePointLeft(6).stripTrailingZeros().toPlainString();
        String acc_name = addresses.getOrDefault(account, account);
        String des_name = addresses.getOrDefault(destination, destination);
        myLog("EVENT", "address: " + account);
        String title = "FNM";
        if (addresses.containsKey(destination)) {
            title = "FNM: You've got XRP!";
        } else {
            title = "FNM: You sent XRP!";
        }
        JSONArray memos = transaction.getJSONArray("Memos");
        String memo = null;
        if (!memos.isNull(0)) {
            JSONObject memoData = memos.getJSONObject(0);
            memo = MyRecyclerView.hexToAscii(memoData.getJSONObject("Memo").optString("MemoData"));
        }
        //if(mno._to.equals(deets.get("walletAddress"))) {
        //    mno.
        int reqCode = 1;
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        String message = acc_name + " sent " + amount + " XRP to " + des_name;
        if (memo != null) {
            message += " \nMemo: " + memo;
        }
        showNotification(this, title, message, intent, reqCode);

    }

    //public void doAlert
    public void doAlert(Fsms.MessageNotificationEventResponse mno) {
        myLog("EVENT", "_to   = walletaddress: " + mno._to.equals(deets.get("walletAddress")));
        if (mno._to.equals(deets.get("walletAddress"))) {
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
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "uk.co.xrpdevs.flarenetmessenger";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_wallet)
                .setContentTitle("FlareNetMessenger Service Is Active")
                .setContentIntent(pendingIntent)
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

    public static Web3j initConnection(String rpc, long chainID) {

        myLog("CHAINID_PRE: ", "" + chainID);

        Web3j myEtherWallet = Web3j.build(
                new HttpService(rpc));
        // myEtherWallet.ethChainId().setId(chainID);
        //   myEtherWallet.netVersion().setId(chainID);
        return myEtherWallet;
    }

    public static Web3j initWeb3j() {

        Web3j myEtherWallet = Web3j.build(new HttpService(rpc));
        //  myEtherWallet.ethChainId().setId(tmpCID);
        return myEtherWallet;
    }

    public final class WebSocketResponse extends WebSocketListener {
        OkHttpClient client;
        String url;

        private void setURL(String _url) {
            url = _url;
        }

        private void run() {
            client = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url("ws://echo.websocket.org")
                    .build();
            client.newWebSocket(request, this);

            // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
            client.dispatcher().executorService().shutdown();
        }
    }

    /**
     * @param context
     * @param title   --> title to show
     * @param message --> details to show
     * @param intent  --> What should happen on clicking the notification
     * @param reqCode --> unique code for the notification
     */

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode) {
        //SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(context);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.chain_xrp)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

      /*  NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                 .setSmallIcon(R.mipmap.chain_xrp)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);*/
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationManager.notify(reqCode, notificationBuilder.build()); // 0 is the request code, it should be unique id

        Log.d("showNotification", "showNotification: " + reqCode);
    }
}
