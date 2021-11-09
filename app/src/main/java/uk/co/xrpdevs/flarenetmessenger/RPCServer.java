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
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.tx.RawTransactionManager;
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
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import fi.iki.elonen.NanoHTTPD;

public class RPCServer extends NanoHTTPD {

    public static boolean RUNNING = false;
    public static int serverPort = 8545;

    public static int authRequestCount = 0;

    public static HashMap<Integer, String> authRequests = new HashMap<>(); // ID , [0 = nothing, 1 = allow, 2 = deny]

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
        String responseBody = "";
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

                        if (amethod.equals("eth_accounts")) {
                            String id = jo.optString("id");
                            String myAddress = FlareNetMessenger.deets.get("ADDRESS");
                            responseBody = "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"result\":[\"" + myAddress + "\"]}";
                        }

                        if (amethod.equals("eth_sendTransaction")) { // intercept sendTransaction
                            authRequestCount++;
                            int arc_local = authRequestCount;
                            //authRequests.put(authRequestCount, 0);
                            long start = System.currentTimeMillis();
                            decodeMessage("unsigned", jo.toString(), mcx, origin);
                            while (authRequests.getOrDefault(arc_local, "nothing").equals("nothing")) {
                                Thread.sleep(100);
                                if ((System.currentTimeMillis() - start) > 15000) {
                                    // auto cancel
                                    authRequests.put(arc_local, "Deny");
                                }
                                //Log.d("DECISION: ", "Sleep time in ms = " + (System.currentTimeMillis() - start));

                            }
                            // deal with user decision here
                            if (authRequests.get(arc_local).equals("Allow")) {
                                Log.d("DECISION1: ", authRequests.get(arc_local));
                                // get the nonce
                                EthSendTransaction moo = signAndSend(jo);

                                Log.d("TX_RECEIPT", moo.getTransactionHash());

                                responseBody = "{\"id\": " + jo.optString("id") + ", \"jsonrpc\": \"2.0\"," +
                                        "\"result\": \"" + moo.getTransactionHash() + "\"}";

                                Log.d("RESP", responseBody);

                            }
                            Log.d("DECISION2: ", authRequests.get(arc_local));
                        }

                        if (amethod.equals("eth_sendRawTransaction")) { // intercept sendRawTransaction
                            authRequestCount++;
                            String paramsHex = new JSONArray(jo.optString("params")).getString(0).replace("0x", "");
                            // Log.d("Outgoing Method: ", "params[0]: "+paramsHex);
                            decodeMessage("signed", paramsHex.replace("0x", ""), mcx, origin);
                        }
                    }
                }
            } catch (JSONException | InterruptedException e) {
                Log.d("JSON ERROR: ", e.getMessage());
            }

            if (responseBody.equals("")) { // pass request through unaltered if not processed above
                responseBody = performPostCall(FlareNetMessenger.deets.get("RPC"), files.get("postData"));
            }

            Log.d("BC_ANSWE", responseBody);

            r = newFixedLengthResponse(responseBody);
            r.addHeader("Access-Control-Allow-Origin", origin);
            r.addHeader("Access-Control-Allow-Headers", ac_ht);

        } else if (Method.OPTIONS.equals(method)) { // deal with CORS OPTIONS requests
            Log.d("HEADERS", "he: " + session.getHeaders().toString());
            ac_ht = hm.get("access-control-request-headers");
            r = newFixedLengthResponse("");
            r.addHeader("Access-Control-Allow-Origin", origin);
            r.addHeader("Access-Control-Allow-Headers", ac_ht);
            //Log.d("Response [" + method + "]", "Access-Control-Allow-Origin: " + origin);
            //Log.d("Response [" + method + "]", "Access-Control-Allow-Headers: " + ac_ht);
        } else {
            r = newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "Method not allowed");
        }


        r.setMimeType("application/json");

        //  r.setChunkedTransfer(true);
        Log.d("RESP-HEAD", r.toString());
        return r;
    }

    private static void decodeMessage(String type, String txData, Context mcx, String origin) {
        //样例 https://ropsten.etherscan.io/tx/0xfd8acd10d72127f29f0a01d8bcaf0165665b5598781fe01ca4bceaa6ab9f2cb0
        BigInteger value = new BigInteger("0");
        String contentText = "";
        if (type.equals("signed")) {
            try {
                System.out.println(txData);
                System.out.println("Decode start " + System.currentTimeMillis());
                RlpList rlpList = RlpDecoder.decode(Numeric.hexStringToByteArray(txData));
                List<RlpType> values = ((RlpList) rlpList.getValues().get(0)).getValues();
                BigInteger nonce = Numeric.toBigInt(((RlpString) values.get(0)).getBytes());
                BigInteger gasPrice = Numeric.toBigInt(((RlpString) values.get(1)).getBytes());
                Log.d("DECRAW", "Gasprice: " + gasPrice.toString());
                BigInteger gasLimit = Numeric.toBigInt(((RlpString) values.get(2)).getBytes());
                Log.d("DECRAW", "GasLimit: " + gasLimit.toString());
                String to = Numeric.toHexString(((RlpString) values.get(3)).getBytes());
                Log.d("DECRAW", "To: " + to);
                value = Numeric.toBigInt(((RlpString) values.get(4)).getBytes());
                Log.d("DECRAW", "Value: " + value.toString());
                String data = Numeric.toHexString(((RlpString) values.get(5)).getBytes());
                Log.d("DECRAW", "Data: " + data);

                contentText = "An app at " + origin + " is trying to access your wallet";

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
                RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);

            } catch (Exception e) {
                Log.d("DECRAW", "Error" + e.getMessage());
                e.printStackTrace();
            }
        } else if (type.equals("unsigned")) {
            contentText = "An app at " + origin + " is trying to access your wallet";
        }

        Intent i = new Intent(mcx, NotificationsReciever.class);
        i.putExtra("id", authRequestCount);
        Intent noReceive = new Intent(mcx, NotificationsReciever.class);
        noReceive.setAction("Deny");
        noReceive.putExtra("id", authRequestCount);
        Intent yesReceive = new Intent(mcx, NotificationsReciever.class);
        yesReceive.putExtra("id", authRequestCount);
        yesReceive.setAction("Allow");
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(mcx, authRequestCount, noReceive, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(mcx, authRequestCount, yesReceive, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mcx, authRequestCount, i, PendingIntent.FLAG_ONE_SHOT);
        String CHANNEL_ID = "channel_name";// The id of the channel.
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mcx, CHANNEL_ID)
                .setSmallIcon(R.mipmap.chain_xrp)
                .setContentTitle("Authorize Transaction (ID=" + authRequestCount + ")")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(R.mipmap.chain_xrp, "Allow",
                        pendingIntentYes)
                .addAction(R.mipmap.chain_xrp, "Deny",
                        pendingIntentNo)
                    .setGroup("group ya mum")
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) mcx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Channel Namel";// The user-visible name of the channel.
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                notificationManager.createNotificationChannel(mChannel);
            }
        notificationManager.notify(authRequestCount, notificationBuilder.build());


        Log.d("DECRAW", "Decode end " + System.currentTimeMillis());

    }

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

        return response;
    }


    public void setContext(Context context) {
        this.context = FlareNetMessenger.getContext();
    }

    public Context getContext() {
        return context;
    }

    public RawTransactionManager getRawTxManager() {
        String cid = FlareNetMessenger.deets.get("CHAINID");
        RawTransactionManager transactionManager = new RawTransactionManager(
                MyService.initConnection(
                        FlareNetMessenger.deets.get("RPC"),
                        Integer.decode(cid)),
                Utils.getCreds(FlareNetMessenger.deets),
                Integer.decode(cid),
                null);
        return transactionManager;
    }

    public EthSendTransaction signAndSend(JSONObject jo) {
        String value = "0x0";
        try {
            RawTransactionManager rtxm = getRawTxManager();
            EthGetTransactionCount ethGetTransactionCount = MyService.initConnection(
                    FlareNetMessenger.deets.get("RPC"),
                    Integer.decode(Objects.requireNonNull(FlareNetMessenger.deets.get("CHAINID")))).ethGetTransactionCount(
                    FlareNetMessenger.deets.get("ADDRESS"), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            if (jo.optString("value").length() > 0) {
                value = jo.optString("value");
            }
            Log.e("CONTRACT CALL DATA", "D: " + jo.optString("data"));
            JSONObject params = jo.getJSONArray("params").getJSONObject(0);


            Log.e("PARAMS", params.toString());
            EthSendTransaction transactionResponse = rtxm.sendTransaction(
                    MyService.GAS_PRICE,
                    new BigInteger("8000000"),
                    params.optString("to"),
                    params.optString("data"), //.encodeFunctionCall(),
                    new BigInteger(
                            String.valueOf(
                                    Integer.decode(
                                            value))));
        /*    RawTransaction rawTransaction =
                    RawTransaction.createTransaction(
                            nonce,
                            MyService.GAS_PRICE,
                            new BigInteger("1000000"),
                            jo.optString("to"),
                            new BigInteger(
                                    String.valueOf(
                                            Integer.decode(
                                                    value))),
                            jo.optString("data"));*/
            return transactionResponse; //EthSendTransaction.signAndSend(rawTransaction);
        } catch (Exception e) {
            Log.e("ERROR", "Error in signAndSend: " + e.getMessage() + "\n" + e.getStackTrace().toString());
            e.printStackTrace();
            return null;
        }
    }

}