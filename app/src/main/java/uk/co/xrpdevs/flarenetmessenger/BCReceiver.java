package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.isMyServiceRunning;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BCReceiver extends BroadcastReceiver {
    Boolean isConnected = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        //MyService.subscribeNotifications();
        if(isMyServiceRunning(MyService.class, context)) {
            myLog("BCREC", "serbiver is running");
            IBinder binder = peekService(context, new Intent(context, MyService.class));
            // update stuff here]
            JSONObject j;
            try {
                j = readJsonFromUrl("https://api.binance.com/api/v3/avgPrice?symbol=XRPUSDT");
                FlareNetMessenger.prices.put("XRPUSDT", j.optString("price"));
                j = readJsonFromUrl("https://www.bitrue.com/api/v1/ticker/price?symbol=SGBUSDT");
                FlareNetMessenger.prices.put("SGBUSDT", j.optString("price"));
                myLog("prices", FlareNetMessenger.prices.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
