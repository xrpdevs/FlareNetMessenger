package uk.co.xrpdevs.flarenetmessenger.ui.messages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.json.JSONException;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.util.encoders.Base64;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import uk.co.xrpdevs.flarenetmessenger.Inbox;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Smstest3;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.ui.wallets.NotificationsViewModel;

import static uk.co.xrpdevs.flarenetmessenger.Utils.deCipherText;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class MessagesFragment extends Fragment {
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
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

            readTheFile2();
        }

  /*      FloatingActionButton fab = this.getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override // TODO: change intent methods to Fragment context switches
            public void onClick(View view) {
           //     Intent intent = new Intent(Wallets.this, PKeyScanner.class);
           //     startActivity(intent);
                try {
            //        contract.clearInbox().send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/

        return root;
    }

    public Boolean IsBase64Encoded(String str)
    {
        try
        {
            // If no exception is caught, then it is possibly a base64 encoded string
            byte[] data = Base64.decode(str);
            // The part that checks if the string was properly padded to the
            // correct length was borrowed from d@anish's solution
            return (str.replace(" ","").length() % 4 == 0);
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
         */

        //ArrayAdapter<String> adapter;
        simpleAdapter = new SimpleAdapter(mThis.getContext(), lines, R.layout.listitem_inbox, new String[]{"cnam", "body", "type", "date"}, new int[]{R.id.inboxName, R.id.inboxAddress, R.id.inboxType, R.id.inboxLastact}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxName);
                TextView inboxAddress = view.findViewById(R.id.inboxAddress);
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
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);
                String pooo = theItem.get("num");
                myLog("smscseeker", "name:" + theItem.toString());

                //startActivity(i);

            }
        });
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                //Intent i = new Intent(mThis.getActivity(),
               //         Inbox.class);
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);

                TextView inboxAddress = v.findViewById(R.id.inboxAddress);
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

    public void readTheFile2() {
        feedList.clear();

        ibSize = inboxSize();

        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
        try {
            //Tuple3<List<byte[]>, List<BigInteger>, List<String>> inbox = contract.receiveMessages().send(); // old contract
            Tuple3<List<BigInteger>, List<String>, List<String>> inbox = contract.receiveMessages().send();
            myLog("TEST", inbox.toString());
            List list1 = inbox.component1(); // timestamp
            List list2 = inbox.component2(); // "ethereum" address
            List list3 = inbox.component3(); // message text
            for(int i=0;i<ibSize;i++){
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("body", (String) list3.get(i));
                map.put("ts", list1.get(i).toString());
                map.put("cnam", list2.get(i).toString());
                map.put("type", "RECD");
                maplist.add(map);
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



            }
        });
        myLog("feedList", feedList.toString());
//        Collections.reverse(feedList);
        InboxAdapter = fillListView(feedList);
    }

    public String getDate(Long ts) {
        myLog("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }
}