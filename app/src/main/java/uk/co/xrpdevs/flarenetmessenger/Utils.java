package uk.co.xrpdevs.flarenetmessenger;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.datatypes.Int;

import java.util.HashMap;
import java.util.Iterator;

public class Utils {

    public static HashMap<String, String> getPkey(Context mC, int wN) throws JSONException {
        SharedPreferences prefs = mC.getSharedPreferences("fnm", 0);
        String pKey;

        int wC = prefs.getInt("walletCount", 0);
        String wD = prefs.getString("wallet"+String.valueOf(wN), "");

        HashMap<String, String> bob = jsonToMap(wD);

      //  HashMap<String, String> bob = new HashMap<String, String>();

     //   bob.put("walletPrvKey", prefs.getString("walletPrvKey", null));
     //   bob.put("walletPubKey", prefs.getString("walletPubKey", null));
     //   bob.put("walletAddress", prefs.getString("walletAddress", null));
        return bob;
    }

    public static HashMap<String, String> jsonToMap(String t) throws JSONException {

        HashMap<String, String> map = new HashMap<String, String>();
        JSONObject jObject = new JSONObject(t);
        Iterator<?> keys = jObject.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
            String value = jObject.getString(key);
            map.put(key, value);

        }
        return map;
        //System.out.println("json : "+jObject);
     //   System.out.println("map : "+map);
    }

}
