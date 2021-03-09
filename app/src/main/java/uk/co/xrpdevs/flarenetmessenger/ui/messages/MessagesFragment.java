package uk.co.xrpdevs.flarenetmessenger.ui.messages;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import uk.co.xrpdevs.flarenetmessenger.ContactsManager;
import uk.co.xrpdevs.flarenetmessenger.EnterMsgDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.Inbox;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.PleaseWaitDialog;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Smstest3;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.ui.contacts.ContactsFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.home.HomeFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.wallets.NotificationsViewModel;

import static uk.co.xrpdevs.flarenetmessenger.ContactsManager.getPubKey;
import static uk.co.xrpdevs.flarenetmessenger.Utils.deCipherText;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;
import static uk.co.xrpdevs.flarenetmessenger.Utils.toByte;

public class MessagesFragment extends Fragment implements EnterMsgDialogFragment.OnResultListener {
    public SimpleAdapter InboxAdapter;
    public SimpleAdapter simpleAdapter;
    Smstest3 contract;
    BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    Web3j FlareConnection;
    String contractAddress;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> deets;
    private NotificationsViewModel notificationsViewModel;
    MessagesFragment mThis = this;
    ListView lv;
    View root;
    Credentials c;
    DefaultGasProvider cgp;
    int ibSize = 0;
    HashMap<String, String> namesCache;
    EnterMsgDialogFragment EMDialog;
    String message, destination;
    TransactionReceipt receipt;
    Activity mAct;
    Thread sendMessageThread;
    Thread getMessagesThread;
    PleaseWaitDialog dialogActivity;
    Boolean encReqd = false;
    String pubKeyTmp = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        /* TODO: Public Key Exchange
                 When first message is sent, encryption not available due to no pub key.
                 Pub Key is sent as a message, XOR'ed with a secret known to the app
                 this is to prevent people looking at the blockchain and finding the
                 keys. It's low risk, at worst if someone does find the XOR key then
                 they might be able to sign messages using someone's key.

           TODO: Perhaps the XOR can be rotated, or based on a the hash of the content of the first
                 message a user sends to another.
                 We can use one of the DATA[n] fields in the contacts to store the pubkey for the user
         */

        getMessagesThread = new getInbox();
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        myLog("FRAG", "MessagesFragment");
        root = inflater.inflate(R.layout.fragment_messages, container, false);
        lv = root.findViewById(R.id.inbox_list);
        lv.setAdapter(InboxAdapter);
        prefs = mThis.getActivity().getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        FlareConnection = MyService.initWeb3j();
        contractAddress = MyService.contractAddress;
        try {
            deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cgp = new DefaultGasProvider();
        c = Credentials.create(deets.get("walletPrvKey"));
        contract = Smstest3.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT );

        //ibSize = inboxSize();
        if(prefs.getInt("walletCount", 0) > 0 ) {
            myLog("FRAG", "Wallet count is non zero");
            getMessagesThread.start();

        }

        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override // TODO: change intent methods to Fragment context switches
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
                //fragmentTransaction.remove(currentFragment);
                ContactsFragment f = new ContactsFragment();
                Bundle args = new Bundle();
                args.putInt("ltype", 3000);
                args.putString("selectFragment", "home");
                f.setArguments(args);

                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.commit();
            }
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAct = mThis.getActivity();
        sendMessageThread = new sendMessage();
        getMessagesThread = new getInbox();
        Bundle args = getArguments();
        if (args != null) {

            myLog("FRAG", args.toString());
        }
        if(args != null){

            if(args.containsKey("SendMsg")){
                destination = args.getString("addrTo");
                String who = args.getString("nameTo");
                showEditDialog("Mesage to "+who, "New message", true);
            }
        }
        c = Credentials.create(deets.get("walletPrvKey"));
        contract = Smstest3.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT );

    }
    public Boolean IsBase64Encoded(String str) {
        try {
            // If no exception is caught, then it is possibly a base64 encoded string
            byte[] data = Base64.decode(str);
            // The part that checks if the string was properly padded to the
            // correct length was borrowed from d@anish's solution
            Boolean isDiv4 = str.replace(" ","").length() % 4 == 0;
            //myLog("B64", new String(data, StandardCharsets.UTF_8));
            return (isDiv4 & (data.length>50));
        }
        catch (Exception e)
        {
            // If exception is caught, then it is not a base64 encoded string
            return false;
        }
    }
    public SimpleAdapter fillListView(final ArrayList lines) {

        /* todo: hashmap of wallet addresses vs contact names in order to prevent repeated cursor lookups.
                 if hashmap empty, check cursor, if not, check cursor and if match add to hashmap

           todo: done
         */

        //ArrayAdapter<String> adapter;
        simpleAdapter = new SimpleAdapter(mThis.getContext(), lines, R.layout.listitem_inbox, new String[]{"cnam", "body", "type", "date"}, new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxAddress);
                TextView inboxAddress = view.findViewById(R.id.inboxContent);
                if(namesCache==null){
                    namesCache = new HashMap<>();
                }
                String listName = cName.getText().toString();
                if(namesCache.containsKey(listName)){
                    cName.setText(namesCache.get(listName));
                } else {
//myLog("fuckoff", "listname: "+listName);
                    String filter = ContactsContract.Data.DATA3 + "=?";
                    Uri uri = ContactsContract.Data.CONTENT_URI;
                    String[] projection    = new String[] {ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Data.DATA3,
                            ContactsContract.Data.MIMETYPE};
                    String[] filterParams = new String[]{listName};
                    Cursor cursor = getContext().getContentResolver().query(uri, projection, filter, filterParams, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                            String data3 = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA3));
                            String mimetype = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
                            myLog("temp", name + " " + data3 + " " + mimetype);
                            cName.setText(name);
                            namesCache.put(listName, name);
                        }
                        cursor.close();
                    } else {
                        myLog("temp", "Cursor = null");
                    }
                }
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....

                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                //myLog("DNSJNI", "item: "+item.toString());

                // int unread = 0;
                if(ibSize>0) {
                    String bob = inboxAddress.getText().toString();
                    if(IsBase64Encoded(bob)){
                        inboxAddress.setText("* Encrypted *");
                    }
                    //cNtext += " (" + String.valueOf(unread) + ")";
                    //cName.setText(cNtext);
                    view.invalidate();
                }
                return view;
            }
        };

        ListView lv = root.findViewById(R.id.inbox_list);
        lv.setAdapter(simpleAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(mThis.getActivity(),
                        Inbox.class);
                TextView cNam = v.findViewById(R.id.inboxAddress);     // todo: refactor to InboxAddress
                TextView cBod = v.findViewById(R.id.inboxContent);  // todo: refactor to InboxContent

                String who = cNam.getText().toString();
                String bod = cBod.getText().toString();
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);
                destination = theItem.get("cnam");
               // String pooo = theItem.get("num");
                myLog("smscseeker", "name:" + theItem.toString());

                showEditDialog("Reply to "+who, bod, true);

                //startActivity(i);

            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                //Intent i = new Intent(mThis.getActivity(),
               //         Inbox.class);
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);

                TextView inboxAddress = v.findViewById(R.id.inboxContent);
                String b64 = theItem.get("body");

                if(IsBase64Encoded(b64)){
                    byte[] barr = Base64.decode(b64);
                    inboxAddress.setText(deCipherText(c, barr));
                }


                String pooo = theItem.get("num");
                myLog("smscseeker", "name:" + theItem.toString());

                //startActivity(i);

                return true;
            }
        });

        return simpleAdapter;

    }

    public String decryptWithPrivateKey(String... inputs){

        return "";
    }




    public int inboxSize() {
        int mCount = 0;
        try {
            Tuple2<BigInteger, BigInteger> messageCount = contract.getMyInboxSize().send();

            myLog("TEST", "Inbox count: " + messageCount);
            String msgCount = messageCount.getValue2().toString();
            mCount = Integer.parseInt(msgCount);
            //  inbox.setText("Inbox: " + msgCount + " messages");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCount;
    }

//    public void readTheFile2() {
    class getInbox extends Thread {
        @Override
        public void run() {
            feedList.clear();

            ibSize = inboxSize();
            int pkPos = -1;
            ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
            try {
                //Tuple3<List<byte[]>, List<BigInteger>, List<String>> inbox = contract.receiveMessages().send(); // old contract
                Tuple3<List<BigInteger>, List<String>, List<String>> inbox = contract.receiveMessages().send();
                myLog("TEST", inbox.toString());
                List list1 = inbox.component1(); // timestamp
                List list2 = inbox.component2(); // "ethereum" address
                List list3 = inbox.component3(); // message text
                for (int i = 0; i < ibSize; i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    String body = (String) list3.get(i);
                    map.put("body", (String) list3.get(i));
                    map.put("ts", list1.get(i).toString());
                    map.put("cnam", list2.get(i).toString());
                    map.put("type", "RECD");
                    if(body.startsWith("RZ")){
                        pkPos = i+1;
                        pubKeyTmp = body;
                    } else {
                        if(i==pkPos){
                            String XORpKey = new String(Base64.decode(pubKeyTmp.substring(2)));
                            Log.d("RXOR", XORpKey);

                            //if(ContactsManager.getPubKey(mThis.getContext(), list2.get(i).toString()) == null) {
                                BigInteger a = new BigInteger(XORpKey, 16);
                                String HexPubKey = Utils.xorStrings(a, body).second;
                                Log.d("RXOR", HexPubKey);
                                ContactsManager.updatePubKey(mThis.getContext(), list2.get(i).toString(), HexPubKey);
//                            XORpKey = "RZ"+new String(Base64.encode(b.xor(a).toByteArray()));
//                            BigInteger rtrvdPub =
                            //}
                        }
                        maplist.add(map);
                    }
                }
                myLog("TEST", inbox.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            String dtemp;
            for (int j = 0; j < maplist.size(); j++) {
                HashMap<String, String> poo = maplist.get(j);
                dtemp = getDate(Long.parseLong(poo.get("ts")));
                myLog("PooPoos", poo.toString());
                poo.remove("ts");
                poo.put("date", dtemp);
                // TODO: Local database of names associated with Coston addresses.
                //      poo.put("cnam", dbHelper.getContactName(this, poo.get("num")));


                feedList.add(poo);
            }

    //        Collections.reverse(feedList);

            mThis.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // mContactList.setAdapter(cursorAdapter);

                    InboxAdapter = fillListView(feedList);
                    lv.setAdapter(InboxAdapter);
                    myLog("TEST", "Running UI thread");
                   // myLog("feedList", feedList.toString());
                    //        Collections.reverse(feedList);
                   // InboxAdapter = fillListView(feedList);

                }
            });

        }
    }
    public String getDate(Long ts) {
        myLog("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }

    @Override
    public void onResult(String pinCode, Boolean enc) throws GeneralSecurityException {
        EMDialog.dismiss();
        // todo: check here that we have the destination address' public key!
        //byte[] ct = Utils.encryptTextWithPubKey(pinCode, deets.get("walletPubKey"));

        message = pinCode;
        encReqd = enc;

        showDialog("Sending", "\nPlease wait 3-5 seconds for block validation", false);

        sendMessageThread = new sendMessage();
        sendMessageThread.start();

//        myLog("Message Send:", new String(ct));

        //setPIN(pinCode);
    }

    private boolean showEditDialog(String title, String prompt, Boolean cancelable) {
        FragmentManager manager = mThis.getActivity().getFragmentManager();
        EMDialog = new EnterMsgDialogFragment().newInstance(this, title, prompt, true);
        EMDialog.prompt = prompt;
        EMDialog.cancelable = cancelable;
        EMDialog.show(manager, null);
        return true;
    }

    private boolean showDialog(String title, String prompt, Boolean cancelable) {
        FragmentManager manager = mThis.getActivity().getFragmentManager();

        dialogActivity = new PleaseWaitDialog();
        dialogActivity.prompt = prompt;
        dialogActivity.titleText = title;
        dialogActivity.cancelable = cancelable;
        dialogActivity.show(manager, "DialogActivity");
        return true;
    }

    class sendMessage extends Thread {

        @Override
        public void run() {
            myLog("TEST", "Running SendMSG thread");
            String rawString = message;
            //byte[] bytes = rawString.getBytes(StandardCharsets.UTF_8);
            //String walletPrvKey = deets.get("walletPrvKey");
            String walletPubKey = deets.get("walletPubKey");
            //walletPrvKey = walletPrvKey.replace("0x", "");
            //walletPubKey = walletPubKey.replace("0x", "");
            //PublicKey x509key;
            String remotePubKey = null;
            myLog("walletPubKey", walletPubKey);
            //wpk="6RLj4k7CmA7RLsphpi/LwyXNaSsc1MbYmCa3iPcIzLk8jgaPCq3EqeyhJcmpOzzeHjnXnwbK6J9yF8RozFiuvQ==";
            //byte[] wpkBytes = toByte(walletPubKey);
            // wpk=Base64.decode()

            String base64_encoded_ciphertext;
            byte[] ciphertext = new byte[]{0}; //ciphertext[0]=0;
            String text = "";

            try {
                if (encReqd) {
                    if ((remotePubKey = getPubKey(getContext(), destination)) != null) {
                        byte[] ct = Utils.encryptTextWithPubKey(message, remotePubKey);
/*
               // x509key = Utils.rawToEncodedECPublicKey("secp256k1", wpkBytes); //.decode(wpk));
               // myLog("KeyInfo:", x509key.getFormat());
               // Cipher iesCipher = Cipher.getInstance("ECIES");
               // iesCipher.init(Cipher.ENCRYPT_MODE, x509key);

               // ciphertext = iesCipher.doFinal(rawString.getBytes());





                //b = new String(ciphertext, StandardCharsets.UTF_8);*/
                        String hexStr = Hex.toHexString(ct);
                        //String hexStr2 = hexStr.substring(2);
                        byte[] hexByt = toByte(hexStr);
                        base64_encoded_ciphertext = new String(Base64.toBase64String(ct).getBytes(), StandardCharsets.UTF_8);

                        myLog("Ciphered: ",
                                "Hex: " + hexStr + "\n" +
                                        "B64: " + Base64.toBase64String(ciphertext) + "\n" +
                                        "Len: " + base64_encoded_ciphertext.length());


//                receipt = contract.sendMessage(addresses.getSelectedItem().toString(), utf8EncodedString).send();
                        receipt = contract.sendMessage(destination, base64_encoded_ciphertext).send();
                        text = "Message sent!\n\nGas used:" + receipt.getGasUsed().toString();
                    } else {
                        showDialog("Error", "No public key available for address", true);
                    }
                } else {
                    Log.d("TXOR", c.getEcKeyPair().getPublicKey().toString(16));
                    String xorTmp = Utils.xorStrings(c.getEcKeyPair().getPublicKey(), message).first.toString(16);
                    Log.d("TXOR", xorTmp);
                    String xorTst = Utils.xorStrings(new BigInteger(xorTmp, 16), message).first.toString(16);
                    Log.d("TXOR", xorTst);

                    String XORpKey = "RZ" + new String(Base64.encode(xorTmp.getBytes()));
                    receipt = contract.sendMessage(destination, XORpKey).send();
                    text = "Pubkey Sent!\nGas Used: " + receipt.getGasUsed().toString() + "\n\n";
                    receipt = contract.sendMessage(destination, message).send();
                    text = text + "Message sent!\nGas used:" + receipt.getGasUsed().toString();
                }
            } catch (Exception e) {
                if (receipt != null) {
                    text = "FAILED\n\nReason:\n" + e.getMessage() + "\n\nGas used: " + receipt.getGasUsed().toString();
                    text = text + "\n\nD: " + destination + "\nM: " + message;

                } else {
                    text = "FAILED\n\nReason:\n" + e.getMessage() + "\n\n";
                    text = text + "\n\nD: " + destination + "\nM: " + message;

                }
                e.printStackTrace();

            }

    /*        try {
                myLog("PRIVATE KEY", "Len (Hex ) "+walletPrvKey.length()+"\nLen (Byte) "+(walletPrvKey.length()/2)+"\nKey: "+walletPrvKey);
                Cipher iesDecipher = Cipher.getInstance("ECIES");

                //BigInteger s = new BigInteger(walletPrvKey, 16);
                //PrivateKey X509_priv = Utils.getPrivateKeyFromECBigIntAndCurve(s, "secp256k1");

                ECKeyPair pair = c.getEcKeyPair();

                PrivateKey X509_priv = Utils.getPrivateKeyFromECBigIntAndCurve(pair.getPrivateKey(), "secp256k1");
                //PrivateKey X509_priv = (PrivateKey) pair.getPublicKey();

                //java.security.KeyFactory keyFactory = KeyFactory.getInstance("EC", secP);
                //PrivateKey X509_priv = Utils.gPK(toByte(walletPrvKey));
                iesDecipher.init(Cipher.DECRYPT_MODE, X509_priv);
                myLog("DECIPHERED TEXT", "" + new String(iesDecipher.doFinal(ciphertext)));
            } catch (Exception e){
                myLog("DECRYPTION FAILED", e.getMessage());
                e.printStackTrace();
            }
            if(receipt.isStatusOK()){
                text = "Message sent!\n"+receipt.getGasUsed().toString();
            } else {
                text = "Message sending failed";
            }
*/
            //String poo = Utils.deCipherText(c, ciphertext);
            String finalText = text;
            mAct.runOnUiThread(() -> {
                dialogActivity.dismiss();
                showDialog("Status", finalText, true);
                try {

                    getMessagesThread = new getInbox();
                    getMessagesThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
    }
}