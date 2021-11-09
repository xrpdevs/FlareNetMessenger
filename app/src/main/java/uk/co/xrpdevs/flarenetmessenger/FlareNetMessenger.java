package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.readJsonFromUrl;

import android.app.Application;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class FlareNetMessenger extends Application {
    private static FlareNetMessenger instance;
    //public static boolean loggingOn = false;
    public static dbHelper dbH;
    public static HashMap<String, String> deets;

    //org.web3j.protocol.core.

    public static class prices {
        public static HashMap<String, String> _prices = new HashMap<>();
        public static HashMap<String, String> priceURLs = new HashMap<>();
        public static String[] pairs;
        public static long lastUpdate = 0L;

        public prices() {
            priceURLs.put("XRPUSDT", "https://api.binance.com/api/v3/avgPrice?symbol=XRPUSDT");
            priceURLs.put("SGBUSDT", "https://www.bitrue.com/api/v1/ticker/price?symbol=SGBUSDT");
        }

        public static String getPrice(String pair) throws IOException, JSONException {
            if (priceURLs.isEmpty()) new prices();
            String priceURL = priceURLs.getOrDefault(pair, "https://api.binance.com/api/v3/avgPrice?symbol=" + pair);
            JSONObject j = readJsonFromUrl(priceURL);
            return j.optString("price");
        }

        public static HashMap<String, String> get() {
            return _prices;
        }

        public static String get(String pair) throws IOException, JSONException {
            return _prices.getOrDefault(pair, updateNow(pair));
        }

        public static void put(String tokPair, String price) {
            _prices.put(tokPair, price);
        }

        public static void updateNow() throws IOException, JSONException {
            if ((System.currentTimeMillis() / 1000L) > (lastUpdate + 300L)) { // fetch coin values every 5 mins.
                if (pairs == null)
                    pairs = new String[]{"XRPUSDT", "SGBUSDT", "AVAUSDT", "VTHOUSDT", "VETUSDT", "BTCUSDT", "BNBUSDT", "XLMUSDT"};
                for (int i = 0; i < pairs.length; i++) {
                    updateNow(pairs[i]);
                }
                lastUpdate = System.currentTimeMillis() / 1000L;
            }
            // update all tokens
        }

        public static void updateNow(String tok, String against) throws IOException, JSONException {
            updateNow(tok + against);
        }

        public static String updateNow(String pair) throws IOException, JSONException {
            // update only "tok"
            String price = getPrice(pair);
            _prices.put(pair, price);
            return price;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            initDB();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }

    public static Context getContext() {
        //return instance;
        return instance.getApplicationContext();
    }

    public void initDB() throws IOException, JSONException {
        dbH = new dbHelper(this);
    }
}