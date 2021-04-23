package uk.co.xrpdevs.flarenetmessenger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Arrays;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.exceptions.MessageDecodingException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

public class Utils {

    static Provider secP = new org.bouncycastle.jce.provider.BouncyCastleProvider();

    public static byte[] encryptTextWithPubKey(String text, String pubKeyHexString) throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
        pubKeyHexString = pubKeyHexString.replace("0x", "");
        myLog("KEYKEY", pubKeyHexString);
        byte[] wpkBytes = toByte(pubKeyHexString);
        byte[] ciphertext = new byte[]{0};
        try {
            PublicKey x509key = rawToEncodedECPublicKey("secp256k1", wpkBytes); //.decode(wpk));
            myLog("KeyInfo:", x509key.getFormat());
            Cipher iesCipher = Cipher.getInstance("ECIES");
            iesCipher.init(Cipher.ENCRYPT_MODE, x509key);

            ciphertext = iesCipher.doFinal(text.getBytes());

            return ciphertext;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static ECPublicKey rawToEncodedECPublicKey(String curveName, byte[] rawBytes) throws GeneralSecurityException {
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("EC", secP);
//        KeyFactory kf = KeyFactory.getInstance("EC");
        byte[] x = Arrays.copyOfRange(rawBytes, 0, rawBytes.length/2);
        byte[] y = Arrays.copyOfRange(rawBytes, rawBytes.length/2, rawBytes.length);
        // ECPoint w = new ECPoint(new BigInteger(1,x), new BigInteger(1,y));
        ECPoint w = new ECPoint(new BigInteger(1,x), new BigInteger(1,y));
        return (java.security.interfaces.ECPublicKey) kf.generatePublic(new java.security.spec.ECPublicKeySpec(w, ecParameterSpecForCurve(curveName)));
    }

    public static java.security.spec.ECParameterSpec ecParameterSpecForCurve(String curveName) throws java.security.GeneralSecurityException {
        AlgorithmParameters params = AlgorithmParameters.getInstance("EC", secP);
        params.init(new ECGenParameterSpec(curveName));
        return params.getParameterSpec(java.security.spec.ECParameterSpec.class);
    }

    public static String deCipherText(Credentials c, byte[] ciphertext) {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
        myLog("PRIVATEKEY--", c.getEcKeyPair().getPrivateKey().toString(16));
        try {
            Cipher iesDecipher = Cipher.getInstance("ECIES");
           ECKeyPair pair = c.getEcKeyPair();
            PrivateKey X509_priv = Utils.getPrivateKeyFromECBigIntAndCurve(pair.getPrivateKey(), "secp256k1");
            iesDecipher.init(Cipher.DECRYPT_MODE, X509_priv);
            String deCipheredText = new String(iesDecipher.doFinal(ciphertext));
            myLog("DECIPHERED TEXT", "" + deCipheredText);
            return deCipheredText;
        } catch (Exception e) {
            myLog("DECRYPTION FAILED", e.getMessage());
            e.printStackTrace();
            return "DECRYPTION FAILED: "+e.getMessage();
        }
    }

    public static byte[] toByte2(String s) {
        Log.d("TOBYTE", s+" len: "+s.length());

        int len = s.length();
        if ( (len & 1) == 1 ) s="0"+s;
        Log.d("TOBYTE", s+" len: "+s.length());

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] toByte(String s){
        if (s.length() == 127) {  // make sure we add back leading zeroes. These need to be saved properly in contact details.
            s = "0" + s;
        }
        return Hex.decode(s);
    }

    public static PrivateKey getPrivateKeyFromECBigIntAndCurve(BigInteger s, String curveName) {
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec(curveName);

        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(s, ecParameterSpec);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC", secP);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> getPkey(Context mC, int wN) throws JSONException {
        SharedPreferences prefs = mC.getSharedPreferences("fnm", 0);
        String pKey;

        int wC = prefs.getInt("walletCount", 0);
        String wD = prefs.getString("wallet"+ wN, "");

        HashMap<String, String> bob = jsonToMap(wD);

      //  HashMap<String, String> bob = new HashMap<String, String>();

     //   bob.put("walletPrvKey", prefs.getString("walletPrvKey", null));
     //   bob.put("walletPubKey", prefs.getString("walletPubKey", null));
     //   bob.put("walletAddress", prefs.getString("walletAddress", null));
        return bob;
    }

    public static Credentials getCreds(HashMap<String, String>  deets){
        return Credentials.create(deets.get("walletPrvKey"));
    }

    public static Web3j initWeb3j() {

        Web3j myEtherWallet = Web3j.build(

                //   new HttpService("https://api.avax-test.network/ext/bc/C/rpc"));
                new HttpService("https://coston.flare.network/ext/bc/C/rpc"));
        myEtherWallet.ethChainId().setId(0x11);

        return myEtherWallet;
    }

    public static Pair<BigDecimal, String> getMyBalance(String walletAddress) {
        String ErrorMessage = "OK";
        Web3j FlareConnection = MyService.initWeb3j();
        BigDecimal wei;

        BigDecimal FLR = BigDecimal.valueOf(0);

        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = FlareConnection
                    .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get(10, TimeUnit.SECONDS);
            wei = new BigDecimal(ethGetBalance.getBalance());
            FLR = Convert.fromWei(wei, Convert.Unit.ETHER);
        } catch (MessageDecodingException e) {
            if(e.toString().contains("Value must be in format")){
                ErrorMessage = "Not a valid Flare/Coston Adddress";
            }
            e.printStackTrace();
        } catch (Exception e){
            ErrorMessage = "Error: "+e.toString();
            e.printStackTrace();
        }



        if(!ErrorMessage.equals("OK")){
            myLog("TEST", ErrorMessage);
            FLR=BigDecimal.valueOf(-1);
        } else {

        }

        return new Pair<BigDecimal, String>(FLR, ErrorMessage);
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

    public static Pair<BigInteger, String> xorStrings(BigInteger pKey, String messageText){

        String XORpKey;
        String pKeyTmpHEX = pKey.toString(16);
        int pkLen = pKeyTmpHEX.length();

        String hexMessage = new BigInteger(messageText.getBytes()).toString(16);
        int msgLen = hexMessage.length();
        String XORStringTmp = "";
        while(XORStringTmp.length() < pkLen){
            XORStringTmp = XORStringTmp+hexMessage;
        }
        Log.d("XOR", "Exited for loop");
        XORStringTmp = XORStringTmp.substring(0,pkLen);
        Log.d("XOR", "pkeyTmp: "+pKeyTmpHEX);
        Log.d("XOR", " msgTmp: "+hexMessage);
        Log.d("XOR", " XORMsg: "+XORStringTmp);
        //XORStringTmp = XORStringTmp.substring(0,pkLen);

        BigInteger a = new BigInteger(XORStringTmp, 16);
        BigInteger b = pKey;
        BigInteger c;

            c = b.xor(a);

        Log.d("XOR", " PUBKEY: "+c.toString(16));
        Pair<BigInteger, String> rv;
        rv=new Pair<>(c, c.toString(16));

        return(rv);
    }

public static void myLog(String tag, String logString){
        boolean loggingOn = true;
        if(loggingOn) {
            String callerClassName = new Exception().getStackTrace()[1].getClassName();
            Log.d(tag, ">\n" + callerClassName + "\n" + logString);
        }
}

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<HashMap<String, String>> getAvailChains() {
        ArrayList<HashMap<String, String>> availTokens = new ArrayList<>();

        String json = null;
        try {
            InputStream is = FlareNetMessenger.getContext().getAssets().open("blockchains.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            //Log.d("JSON", "== "+json);
            JSONObject jo = new JSONObject(json);
            JSONArray key = jo.names();
            for (int i = 0; i < key.length(); ++i) {
                JSONObject obj = jo.getJSONObject(key.getString(i));
                HashMap<String, String> tmp = jsonToMap(obj.toString());
                tmp.put("Name", key.getString(i));
                availTokens.add(tmp);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //myLog("json", availTokens.toString());

        return availTokens;
    }


}
