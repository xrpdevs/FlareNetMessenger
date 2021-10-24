package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.MyService.xrplClient;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Arrays;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.exceptions.MessageDecodingException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.transactions.Address;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;

@SuppressWarnings("UnstableApiUsage")
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String pinHash(String pin) {
        byte[] encoded = SCrypt.generate(pin.getBytes(), "DEADFEED".getBytes(), 16, 16, 16, 16);
        BigInteger bi = new BigInteger(encoded);
        return (bi.toString(16));
    }

    public static ECPublicKey rawToEncodedECPublicKey(String curveName, byte[] rawBytes) throws GeneralSecurityException {
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("EC", secP);
//        KeyFactory kf = KeyFactory.getInstance("EC");
        byte[] x = Arrays.copyOfRange(rawBytes, 0, rawBytes.length / 2);
        byte[] y = Arrays.copyOfRange(rawBytes, rawBytes.length / 2, rawBytes.length);
        // ECPoint w = new ECPoint(new BigInteger(1,x), new BigInteger(1,y));
        ECPoint w = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
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
            return "DECRYPTION FAILED: " + e.getMessage();
        }
    }

    public static byte[] toByte(String s) {
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

        String wD = prefs.getString("wallet" + wN, "");
        HashMap<String, String> bob;

        if (wD.contains("wallet")) {
            bob = jsonToMap(wD);
        } else {
            return null;
        }

        return bob;
    }

    public static Credentials getCreds(HashMap<String, String> deets) {
        return Credentials.create(deets.get("walletPrvKey"));
    }

    public static Pair<BigDecimal, String> getMyBalance(String walletAddress) throws IOException {
        String ErrorMessage = "OK";
        Web3j FlareConnection = MyService.initWeb3j();
        BigDecimal wei;

        BigDecimal FLR = BigDecimal.valueOf(0);

        EthGetBalance ethGetBalance;
        try {
            ethGetBalance = FlareConnection
                    .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get(200, TimeUnit.SECONDS);
            wei = new BigDecimal(ethGetBalance.getBalance());
            FLR = Convert.fromWei(wei, Convert.Unit.ETHER);
        } catch (MessageDecodingException e) {
            if (e.toString().contains("Value must be in format")) {
                ErrorMessage = "Not a valid Flare/Coston Adddress";
            }
            e.printStackTrace();
        } catch (Exception e) {
            ErrorMessage = "Error: " + e.toString();
            e.printStackTrace();
        }


        if (!ErrorMessage.equals("OK")) {
            myLog("TEST", ErrorMessage);
            FLR = BigDecimal.valueOf(-1);
        }

        return new Pair<>(FLR, ErrorMessage);
    }

    public static Pair<BigDecimal, String> getMyXRPBalance(String walletAddress) throws IOException {
        String ErrorMessage = "OK";
        BigDecimal XRP;
        AccountInfoRequestParams requestParams =
                AccountInfoRequestParams.of(Address.of(walletAddress));
        AccountInfoResult accountInfoResult;
        //AccountTransactionsResult eek;


        try {
            // AccountTransactionsRequestParams bob = AccountTransactionsRequestParams.builder()
            //         .account(Address.of(walletAddress))
            //         .limit(UnsignedInteger.valueOf(20))
            //         .build();


            //  eek = xrplClient.accountTransactions(bob);

            //   Iterator<AccountTransactionsTransactionResult<? extends Transaction>> itr=eek.transactions().iterator();

            //   while(itr.hasNext())
            //   {
            //    System.out.println(itr.next());
            //   }

            accountInfoResult = xrplClient.accountInfo(requestParams);
            BigInteger drops = new BigInteger(accountInfoResult.accountData().balance().toString());
            XRP = new BigDecimal(drops, 6);
        } catch (JsonRpcClientErrorException | NullPointerException e) {
            XRP = new BigDecimal("-1");
            e.printStackTrace();
        }

        return new Pair<>(XRP, ErrorMessage);
    }

    public static HashMap<String, String> jsonToMap(String t) throws JSONException {
        HashMap<String, String> map = new HashMap<>();
        JSONObject jObject = new JSONObject(t);
        Iterator<?> keys = jObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = jObject.getString(key);
            map.put(key, value);
        }
        return map;
        //System.out.println("json : "+jObject);
        //   System.out.println("map : "+map);
    }

    public static Pair<BigInteger, String> xorStrings(BigInteger pKey, String messageText) {

        //String XORpKey;
        String pKeyTmpHEX = pKey.toString(16);
        int pkLen = pKeyTmpHEX.length();

        String hexMessage = new BigInteger(messageText.getBytes()).toString(16);
        //int msgLen = hexMessage.length();
        StringBuilder XORStringTmp = new StringBuilder();
        while (XORStringTmp.length() < pkLen) XORStringTmp.append(hexMessage);
        myLog("XOR", "Exited for loop");
        XORStringTmp = new StringBuilder(XORStringTmp.substring(0, pkLen));
        myLog("XOR", "pkeyTmp: " + pKeyTmpHEX);
        myLog("XOR", " msgTmp: " + hexMessage);
        myLog("XOR", " XORMsg: " + XORStringTmp);
        //XORStringTmp = XORStringTmp.substring(0,pkLen);

        BigInteger a = new BigInteger(XORStringTmp.toString(), 16);
        BigInteger c;

        c = pKey.xor(a);

        myLog("XOR", " PUBKEY: " + c.toString(16));
        Pair<BigInteger, String> rv;
        rv = new Pair<>(c, c.toString(16));

        return (rv);
    }

    public static void myLog(String tag, String logString) {
        boolean compactLog = true;
        String sep = "\n";

        if (compactLog) {
            sep = " :: ";
        }
        // if (FlareNetMessenger.loggingOn) {
        String callerClassName = new Exception().getStackTrace()[1].getClassName();
        Log.d(tag, sep + callerClassName + sep + logString);
        // }
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return manager.getRunningServices(Integer.MAX_VALUE).stream().anyMatch(service -> serviceClass.getName().equals(service.service.getClassName()));
    }

    public static ArrayList<HashMap<String, String>> getAvailChains() {
        ArrayList<HashMap<String, String>> availTokens = new ArrayList<>();

        String json;
        try {
            InputStream is = FlareNetMessenger.getContext().getAssets().open("blockchains.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
            //myLog("JSON", "== "+json);
            JSONArray jo = new JSONArray(json);
            //JSONArray key = jo.names();
            for (int i = 0; i < json.length(); ++i) {
                JSONObject obj = jo.getJSONObject(i);
                HashMap<String, String> tmp = jsonToMap(obj.toString());
                tmp.put("bcid", String.valueOf(i));
                availTokens.add(tmp);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // TODO: Append custom chains to this list before returning "availTokens"
        //myLog("json", availTokens.toString());

        return availTokens;
    }

    public static String dump(Bundle bundle) {
        if (bundle == null) {
            return "null";
        }//from  w  w w.  jav  a2s.  c  o m

        final StringBuilder builder = new StringBuilder("Bundle{");

        boolean isFirst = true;
        Object val;

        for (String key : bundle.keySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                builder.append(", ");
            }
            builder.append(key).append(": ");
            val = bundle.get(key);
            if (val instanceof Bundle) {
                builder.append(dump((Bundle) val));
            } else {
                builder.append(val);
            }
        }
        builder.append("}");
        return builder.toString();
    }

    public static String dumpMap(Map<String, ?> prefs) {
        String out = "";
        for (String key : prefs.keySet()) {
            Object pref = prefs.get(key);
            String printVal = "";
            if (pref instanceof Boolean) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Float) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Integer) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Long) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof String) {
                printVal = key + " : " + pref;
            }
            if (pref instanceof Set<?>) {
                printVal = key + " : " + pref;
            }
            // Every new preference goes to a new line
            out = out + printVal + "\n\n";
        }
        return out;
    }

    public static Pair<BigInteger, BigInteger> getKeyPair() {
        ECKeyPairGenerator keyGen = new ECKeyPairGenerator();
        X9ECParameters params = SECNamedCurves.getByName("secp256k1");
        ECKeyGenerationParameters keygenParams =
                new ECKeyGenerationParameters(
                        new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH()),
                        new SecureRandom());

        //ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.init(keygenParams);
        AsymmetricCipherKeyPair keypair = keyGen.generateKeyPair();
        ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
        BigInteger priv = privParams.getD();
        BigInteger pub = new BigInteger(pubParams.getQ().getEncoded());


        return new Pair<>(priv, pub);
    }

    public static String newKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {

        Pair<BigInteger, BigInteger> pair = getKeyPair();

        HashMap<String, String> newKeys = new HashMap<>();

        String sPrivatekeyInHex = pair.first.toString(16);
        String sPublickeyInHex = pair.second.toString(16);

        Credentials cs = Credentials.create("0x" + sPrivatekeyInHex);

        String privateKey = cs.getEcKeyPair().getPrivateKey().toString(16);
        String publicKey = cs.getEcKeyPair().getPublicKey().toString(16);
        String addr = cs.getAddress();

        newKeys.put("prv", sPrivatekeyInHex);
        newKeys.put("pub", sPublickeyInHex);
        newKeys.put("add", addr);
        newKeys.put("d_prv", privateKey);
        newKeys.put("d_pub", publicKey);

        myLog("KEYZ", Utils.dumpMap(newKeys));


        return "0x" + sPrivatekeyInHex;
    }

    public static PublicKey getPublicKey(byte[] pk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pk);
        KeyFactory kf = KeyFactory.getInstance("ECDSA", secP);
        return kf.generatePublic(publicKeySpec);
    }

    public static PrivateKey getPrivateKey(byte[] privk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privk);
        KeyFactory kf = KeyFactory.getInstance("EC", secP);
        //      ParameterSpe
        return kf.generatePrivate(privateKeySpec);
    }

    public static PublicKey getPublicKeyFromBytes(byte[] pubKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyFactory kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
        ECNamedCurveSpec params = new ECNamedCurveSpec("secp256k1", spec.getCurve(), spec.getG(), spec.getN());
        ECPoint point = ECPointUtil.decodePoint(params.getCurve(), pubKey);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
        return kf.generatePublic(pubKeySpec);
    }

    public static byte[] toByte2(String s) {
        myLog("TOBYTE", s + " len: " + s.length());

        int len = s.length();
        if ((len & 1) == 1) s = "0" + s;
        myLog("TOBYTE", s + " len: " + s.length());

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
      /*  public static HashMap<String, String> walletAddressesToWalletNamesOrContactsToHashMap(Context mC) throws JSONException {
        return walletAddressesToWalletNamesOrContactsToHashMap(mC, null);
    }
    public static HashMap<String, String> walletAddressesToWalletNamesOrContactsToHashMap(Context mC, @Nullable int _bcid) throws JSONException {
        SharedPreferences prefs = mC.getSharedPreferences("fnm", 0);
        String pKey;

        HashMap<String, String> wAddrs = new HashMap<String, String>();
        int wC = prefs.getInt("walletCount", 0);
        wC++;
        for (int i = 1; i < wC; i++) { // wallets are numbered starting at 1
            String wD = prefs.getString("wallet" + i, "");
            HashMap<String, String> bob;
            myLog("WD-", wD);
            if (wD.contains("wallet")) {
                bob = jsonToMap(wD);
                if (bcType == null) {
                    bcType = "ALL";
                }
                if (bob.containsKey("walletType") && bob.get("walletType").equals("XRPL")) {
                    myLog("SUCKA", bob.get("walletName") + " wC = " + wC);
                }
                switch (bcType) {
                    case "XRPL":  // in future just use tge blockchain ID and select matching wallets from DB
                        if (bob.containsKey("walletXaddr") || (bob.containsKey("walletType") && bob.get("walletType").equals("XRPL"))) {
                            myLog("WALLET_" + bcType, bob.toString());
                            String cWname = bob.getOrDefault("walletName", "Wallet " + i);
                            if (bob.containsKey("walletAddress")) {
                                wAddrs.put(bob.get("walletAddress"), cWname);
                            }
                        }
                        break;
                    case "ALL": // all
                        myLog("XWALLET_" + bcType, bob.toString());
                        String cWname = bob.getOrDefault("walletName", "Wallet " + i);
                        if (bob.containsKey("walletAddress")) {
                            wAddrs.put(bob.get("walletAddress"), cWname);
                            String wpk = bob.get("walletPrvKey").replace("Optional[", "").replace("]", "");
                            boolean b = FlareNetMessenger.dbH.addWallet(
                                    bob.get("walletName"),
                                    Integer.valueOf(bob.getOrDefault("bcid", "0")),
                                    bob.get("walletPubKey"), //bob.get("walletPubKey"),
                                    wpk,
                                    bob.get("walletAddress"),
                                    bob.getOrDefault("walletXaddr", bob.getOrDefault("walletAltAddress", "")), 0, "0");
                        }
                        break; // note: don't forget those break statements or you might want to break something lol
                }
            }
            }
        return wAddrs;
    }*/
}
