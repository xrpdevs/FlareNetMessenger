package uk.co.xrpdevs.flarenetmessenger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.RawTransaction;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import fi.iki.elonen.NanoHTTPD;

public class RPCServer extends NanoHTTPD {

    public static boolean RUNNING = false;
    public static int serverPort = 8545;

    int authRequestCount = 0;

    public static Map<Integer, Boolean> authRequests;

    Context context;


    private ServerSocket serverSocket;

    public void initialise() throws IOException {
        new RPCServer();
    }

    public RPCServer() throws IOException {
        super(serverPort);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    public Response serve(IHTTPSession session) {
        Context mcx = context;
        Response r;
        final HashMap<String, String> files = new HashMap<String, String>();
        try {

            session.parseBody(files);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        Method method = session.getMethod();
        String origin = "*";
        HashMap<String, String> hm = (HashMap<String, String>) session.getHeaders();
        //files = session.getParms();
        Log.d("PARAMS", files.toString());
        Log.d("METHOD", session.getMethod().toString());
        origin = hm.getOrDefault("origin", "*");
        String postBody = "";
        String ac_ht = "";
        if (Method.POST.equals(method)) {// || Method.OPTIONS.equals(method) || Method.GET.equals(method)){
            Log.d("HEADERS", "he: " + session.getHeaders().toString());
            ac_ht = hm.get("Access-Control-Request-Headers");
            //session.parseBody(files);
            Log.d("BC_REQUES", "po: " + files.get("postData"));
            postBody = files.get("postData");


            // try {
            try {
                JSONObject jo = new JSONObject(postBody);
                String amethod = jo.optString("method", "");
                if (amethod.length() > 0) {
                    if (!amethod.equals("eth_blockNumber")) {
                        Log.d("Outgoing Method: ", jo.optString("method"));

                        if (amethod.equals("eth_sendTransaction")) {
                            long start = System.currentTimeMillis();
                            Thread.sleep(20000);
                            System.out.println("Sleep time in ms = " + (System.currentTimeMillis() - start));

                        }

                        if (amethod.equals("eth_sendRawTransaction")) {

                            String paramsHex = new JSONArray(jo.optString("params")).getString(0).replace("0x", "");
                            // Log.d("Outgoing Method: ", "params[0]: "+paramsHex);
                            decodeMessage(paramsHex.replace("0x", ""), mcx);
                        }
                    }
                }
            } catch (JSONException | InterruptedException e) {
                Log.d("JSON ERROR: ", e.getMessage());
            }

            //          if(jo.optString("method", "").length() >0){
            //              Log.d("Outgoing Method: ", jo.optString("method"));
            //          }


            // } catch (JSONException e) {
            //     e.printStackTrace();
            // }

            // or you can access the POST request's parameters
//            String postParameter = session.getParms().get("parameter");

            postBody = performPostCall(FlareNetMessenger.deets.get("RPC"), files.get("postData"));

            Log.d("BC_ANSWE", postBody);

            r = newFixedLengthResponse(postBody);
            //r = newFixedLengthResponse(null);
            r.addHeader("Access-Control-Allow-Origin", origin);
            //r.addHeader("Access-Control-Max-Age", "3628800");
            // r.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
            //r.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
            //r.addHeader("Access-Control-Allow-Headers", "Content-Type");
            r.addHeader("Access-Control-Allow-Headers", ac_ht);
            //r.addHeader("Access-Control-Allow-Headers", "Authorization");

        } else if (Method.OPTIONS.equals(method)) {
            Log.d("HEADERS", "he: " + session.getHeaders().toString());
            ac_ht = hm.get("access-control-request-headers");
            r = newFixedLengthResponse("");
            r.addHeader("Access-Control-Allow-Origin", origin);
            Log.d("Response [" + method + "]", "Access-Control-Allow-Origin: " + origin);
            //r.addHeader("Access-Control-Max-Age", "3628800");
            // r.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
            //r.addHeader("Access-Control-Allow-Headers", "X-Requested-With");
            //r.addHeader("Access-Control-Allow-Headers", "Content-Type");
            r.addHeader("Access-Control-Allow-Headers", ac_ht);
            //r.addHeader("Access-Control-Allow-Headers", "Authorization");
            Log.d("Response [" + method + "]", "Access-Control-Allow-Headers: " + ac_ht);
        } else {
            r = newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "Method not allowed");
        }


        r.setMimeType("application/json");

        //  r.setChunkedTransfer(true);
        Log.d("RESP-HEAD", r.toString());
        return r;
    }

    private static void decodeMessage(String signedData, Context mcx) {
        //样例 https://ropsten.etherscan.io/tx/0xfd8acd10d72127f29f0a01d8bcaf0165665b5598781fe01ca4bceaa6ab9f2cb0
        try {
            System.out.println(signedData);
            System.out.println("Decode start " + System.currentTimeMillis());
            RlpList rlpList = RlpDecoder.decode(Numeric.hexStringToByteArray(signedData));
            List<RlpType> values = ((RlpList) rlpList.getValues().get(0)).getValues();
            BigInteger nonce = Numeric.toBigInt(((RlpString) values.get(0)).getBytes());
            BigInteger gasPrice = Numeric.toBigInt(((RlpString) values.get(1)).getBytes());
            Log.d("DECRAW", "Gasprice: " + gasPrice.toString());
            BigInteger gasLimit = Numeric.toBigInt(((RlpString) values.get(2)).getBytes());
            Log.d("DECRAW", "GasLimit: " + gasLimit.toString());
            String to = Numeric.toHexString(((RlpString) values.get(3)).getBytes());
            Log.d("DECRAW", "To: " + to);
            BigInteger value = Numeric.toBigInt(((RlpString) values.get(4)).getBytes());
            Log.d("DECRAW", "Value: " + value.toString());
            String data = Numeric.toHexString(((RlpString) values.get(5)).getBytes());
            Log.d("DECRAW", "Data: " + data);

            Intent i = new Intent(FlareNetMessenger.getContext(), BCReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(mcx, 7, i, PendingIntent.FLAG_ONE_SHOT);
            String CHANNEL_ID = "channel_name";// The id of the channel.
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mcx, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.chain_xrp)
                    .setContentTitle("Transaction Value: " + value.toString())
                    .setContentText("Click below to allow")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("Allow that"))
                    .setAutoCancel(false)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .addAction(R.mipmap.chain_xrp, "23.498372",
                            pendingIntent)
                    .setGroup("group ya mum")
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) mcx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Channel Namel";// The user-visible name of the channel.
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
            notificationManager.notify(7, notificationBuilder.build());

            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
            //RlpString v = (RlpString) values.get(6);
            //RlpString r = (RlpString) values.get(7);
            //RlpString s = (RlpString) values.get(8);
            //Sign.SignatureData signatureData = new Sign.SignatureData(
            //        v.getBytes()[0],
            //        Numeric.toBytesPadded(Numeric.toBigInt(r.getBytes()), 32),
            //        Numeric.toBytesPadded(Numeric.toBigInt(s.getBytes()), 32));
            //BigInteger pubKey = Sign.signedMessageToKey(TransactionEncoder.encode(rawTransaction), signatureData);
            //Log.d("DECRAW", "publicKey " + pubKey.toString(16));
            //String address = Numeric.prependHexPrefix(Keys.getAddress(pubKey));
            //Log.d("DECRAW", "address " + address);
            Log.d("DECRAW", "Decode end " + System.currentTimeMillis());
        } catch (Exception e) {
            Log.d("DECRAW", "Error" + e.getMessage());
            e.printStackTrace();
        }
    }

    /*public Response serve(IHTTPSession session) {
        if (session.getMethod() == Method.POST) {
            Map<String, String> files = new HashMap<String, String>();
            try {
                session.parseBody(files);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            //this prints {file=C:\path-to-java-tmp-files\NanoHTTPD-4635244586997909485}
            //the number is always different
            System.out.println(files.toString());
        } else {
            //page containing the index.html including the form
            return page;
        }
    }
*/


    public void runServer() {

        RUNNING = false;
    }

    public synchronized void startServer() {
        RUNNING = true;
        runServer();
    }

    public synchronized void stopServer() {
        RUNNING = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String performPostCall(String requestURL,
                                  String postData) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);
            HttpURLConnection conn = null;
            if (requestURL.contains("https://")) {
                conn = (HttpsURLConnection) url.openConnection();
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", "application/json");

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(postData);

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = conn.getResponseMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Log.d("POST  IN", response);
        //  try {
        // JSONObject ji = new JSONObject(response);
        //  } catch (JSONException e) {
        //      e.printStackTrace();
        //   }
        return response;
    }


    public void setContext(Context context) {
        this.context = FlareNetMessenger.getContext();
    }

    public Context getContext() {
        return context;
    }
}