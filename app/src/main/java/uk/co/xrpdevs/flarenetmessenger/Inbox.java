package uk.co.xrpdevs.flarenetmessenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/* TODO: Redundant class, check dependencies and remove */

public class Inbox extends AppCompatActivity {
    public Web3j FlareConnection;
    private Object TextView;
    TextView myTV;
    EthBlockNumber bob;
    Button refresh;
    Button sendMsg;
    TextView myBalance;
    android.widget.TextView message;
    public String walletAddress;
    public String contractAddress;
    public String walletPrivateKey;
    Credentials c;
    ContractGasProvider cgp;
    TransactionReceipt receipt;
    Spinner addresses;
    Button inbox;
    public SimpleAdapter InboxAdapter;
    public SimpleAdapter simpleAdapter;
    Smstest3 contract;
    BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    SharedPreferences prefs;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> deets;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefs = this.getSharedPreferences("fnm", 0);
        FlareConnection = MyService.initWeb3j();

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

        contractAddress = "0x4a1400220373983f3716D4e899059Dda418Fd08A"; // v1 SMSTest2

        contractAddress = MyService.contractAddress;
        addresses = findViewById(R.id.spinner);
        try {
            deets = Utils.getPkey(this, prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cgp = new DefaultGasProvider();

        c = Credentials.create(deets.get("walletPrvKey"));

        contract = Smstest3.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT );

        readTheFile2();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Inbox cleared", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    contract.clearInbox().send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                readTheFile2();

            }
        });
    }

    public SimpleAdapter fillListView(final ArrayList lines) {
        ArrayAdapter<String> adapter;
        simpleAdapter = new SimpleAdapter(this, lines, R.layout.listitem_inbox, new String[]{"cnam", "body", "type", "date"}, new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxAddress);
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                //Log.d("DNSJNI", "item: "+item.toString());
                int unread = inboxSize();
                // int unread = 0;
                if(unread>0) {
                    cNtext += " (" + unread + ")";
                    cName.setText(cNtext);
                    view.invalidate();
                }
                return view;
            }
        };

        ListView lv = findViewById(R.id.inbox_list);
        lv.setAdapter(simpleAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(Inbox.this,
                        Inbox.class);
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);
                String pooo = theItem.get("num");
                Log.d("smscseeker", "name:" + theItem.toString());

                startActivity(i);

            }
        });

        return simpleAdapter;

    }

    public int inboxSize() {
        int mCount = 0;
        try {
            Tuple2<BigInteger, BigInteger> messageCount = contract.getMyInboxSize().send();

            Log.d("TEST", "Inbox count: " + messageCount);
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

        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
        try {
            //Tuple3<List<byte[]>, List<BigInteger>, List<String>> inbox = contract.receiveMessages().send(); // old contract
            Tuple3<List<BigInteger>, List<String>, List<String>> inbox = contract.receiveMessages().send();
            Log.d("TEST", inbox.toString());
            List list1 = inbox.getValue1(); // timestamp
            List list2 = inbox.getValue2(); // "ethereum" address
            List list3 = inbox.getValue3(); // message text
            for(int i=0;i<inboxSize();i++){
                HashMap<String, String> map = new HashMap<String, String>();
                //byte[] bytes = (byte[]) list1.get(i); old contract
                //map.put("body", new String(bytes, StandardCharsets.UTF_8)); old contract
                map.put("body", (String) list3.get(i));
                map.put("ts", list1.get(i).toString());
                map.put("cnam", list2.get(i).toString());
                map.put("type", "RECD");
                maplist.add(map);

             //   Log.d("TEST", "INBOX ["+i+"] : "+ new String(bytes, StandardCharsets.UTF_8));
            }
            Log.d("TEST", inbox.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        String dtemp;
        for (int j = 0; j < maplist.size(); j++) {
            HashMap<String, String> poo = maplist.get(j);
            dtemp = getDate(Long.parseLong(poo.get("ts")));
            Log.d("PooPoos", poo.toString());
            poo.remove("ts");
            poo.put("date", dtemp);
        // TODO: Local database of names associated with Coston addresses.
      //      poo.put("cnam", dbHelper.getContactName(this, poo.get("num")));



            feedList.add(poo);
        }

//        Collections.reverse(feedList);
        InboxAdapter = fillListView(feedList);
    }

    public String getDate(Long ts) {
        Log.d("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }


}