package uk.co.xrpdevs.flarenetmessenger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPUtil;
import org.spongycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;
import org.web3j.abi.datatypes.Int;
import org.web3j.crypto.Credentials;
import org.web3j.exceptions.MessageDecodingException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.Provider;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Utils {

    public static PublicKey publicKeyParse(byte[] publicKeyBytes) throws IOException, PGPException {
        InputStream pgpIn = PGPUtil.getDecoderStream(new ByteArrayInputStream(publicKeyBytes));
        PGPObjectFactory pgpFact = new PGPObjectFactory(pgpIn, new JcaKeyFingerprintCalculator());
        PGPPublicKeyRing pgpSecRing = (PGPPublicKeyRing) pgpFact.nextObject();
        PGPPublicKey publicKey = pgpSecRing.getPublicKey();
        JcaPGPKeyConverter converter = new JcaPGPKeyConverter();
        Provider bcProvider = new BouncyCastleProvider();
        converter.setProvider(bcProvider);
        return converter.getPublicKey(publicKey);
    }

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

    public static Credentials getCreds(HashMap<String, String>  deets){
        return Credentials.create(deets.get("walletPrvKey"));
    }

    public static Web3j initWeb3j(){

        Web3j myEtherWallet = Web3j.build(
                new HttpService("https://costone.flare.network/ext/bc/C/rpc"));

        return myEtherWallet;
    }

    public static Pair<BigDecimal, String> getMyBalance(String walletAddress) {
        String ErrorMessage = "OK";
        Web3j FlareConnection = MyService.initWeb3j();
        BigDecimal wei;

        BigDecimal FXRP = BigDecimal.valueOf(0);

        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = FlareConnection
                    .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get(10, TimeUnit.SECONDS);
            wei = new BigDecimal(ethGetBalance.getBalance());
            FXRP = Convert.fromWei(wei, Convert.Unit.ETHER);
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
            FXRP=BigDecimal.valueOf(-1);
        } else {

        }

        return new Pair<BigDecimal, String>(FXRP, ErrorMessage);
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

public static void myLog(String tag, String logString){
        boolean loggingOn = true;
        if(loggingOn) {
            String callerClassName = new Exception().getStackTrace()[1].getClassName();
            Log.d(tag, ">\n" + callerClassName + "\n" + logString);
        }
}

}
