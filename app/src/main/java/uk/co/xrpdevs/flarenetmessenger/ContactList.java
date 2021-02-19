package uk.co.xrpdevs.flarenetmessenger;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ContactList extends AppCompatActivity {
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
    ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
    ListView lv;
    int ListType;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        Intent in = getIntent();
        ListType = in.getIntExtra("lType", 1000);
        prefs = this.getSharedPreferences("fnm", 0);
        FlareConnection = MyService.initWeb3j();

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

        contractAddress = "0x4a1400220373983f3716D4e899059Dda418Fd08A"; // v1 SMSTest2

        contractAddress = MyService.contractAddress;
        addresses = (Spinner) findViewById(R.id.spinner);
        try {
            deets = Utils.getPkey(this, prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cgp = new DefaultGasProvider();

        c = Credentials.create(deets.get("walletPrvKey"));

        contract = Smstest3.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT );
        InboxAdapter = fillListView(feedList);
        new Contact_thread().run();

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
                new Contact_thread();
            }
        });
    }

    public SimpleAdapter fillListView(final ArrayList lines) {
        Log.d("TEST", "FillListView");
        ArrayAdapter<String> adapter;
        simpleAdapter = new SimpleAdapter(this, lines, R.layout.inbox_listitem, new String[]{"name", "numb", "type", "date"}, new int[]{R.id.cName, R.id.olUser, R.id.cStatus, R.id.olLastact}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = (TextView) view.findViewById(R.id.cName);
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                //Log.d("DNSJNI", "item: "+item.toString());
                int unread = lines.size();
                Log.d("TEST", "Number of contaxts: "+unread);
                // int unread = 0;
                if(unread>0) {
                    cNtext += " (" + String.valueOf(unread) + ")";
                    cName.setText(cNtext);
                    view.invalidate();
                }
                return view;
            }
        };

        lv = (ListView) findViewById(R.id.inbox_list);
        lv.setAdapter(InboxAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(ContactList.this,
                        MainActivity.class);
                Bundle b = new Bundle();
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);

                b.putString("name", theItem.get("name"));
                b.putString("addr", theItem.get("numb"));
                i.putExtra("contactInfo", b);
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

    private Cursor getContacts() {

     //   Uri rawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId);
    //    Uri entityUri = Uri.withAppendedPath(rawContactUri, ContactsContract.RawContacts.Entity.CONTENT_DIRECTORY);
    //    Cursor c = getContentResolver().query(entityUri,
   //             new String[]{ContactsContract.RawContacts.SOURCE_ID, ContactsContract.RawContacts.Entity.DATA_ID, ContactsContract.RawContacts.Entity.MIMETYPE, ContactsContract.RawContacts.Entity.DATA1},
   //             null, null, null);
   //     try {
   //         while (c.moveToNext()) {
   //             String sourceId = c.getString(0);
    //            if (!c.isNull(1)) {
    //                String mimeType = c.getString(2);
    //                String data = c.getString(3);
    //                //decide here based on mimeType, see comment later
    //            }
    //        }
    //    } finally {
    //        c.close();
    //    }


        // Run query
        Uri uri = ContactsContract.RawContacts.CONTENT_URI;
        String[] projection = new String[] {

        };
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY
                + " COLLATE LOCALIZED ASC";


        return managedQuery(uri, projection, null, selectionArgs, sortOrder);
    }

    class Contact_thread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            // Build adapter with contact entries
          /*  Cursor cursor = getContacts();

            cursor.moveToFirst();
            String[] contactName = new String[cursor.getCount()];
            String[] contactNo = new String[cursor.getCount()];
            String[] contactMime = new String[cursor.getCount()];
            String[][] data;
            boolean[] checkedPosition = new boolean[cursor.getCount()];



            ContentResolver contect_resolver = getContentResolver();
            */

            String yourAccountType = "uk.co.xrpdevs.flarenetmessenger";//ex: "com.whatsapp"
            Cursor c = getContentResolver().query(
                    ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{},
                    ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                    new String[]{yourAccountType},
                    null);

            ArrayList<String> contactList = new ArrayList<String>();
            int contactNameColumn = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
            int addressColumnIndex = c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME);
            while (c.moveToNext()) {
                Log.d("TEST", DatabaseUtils.dumpCurrentRowToString(c));
                // You can also read RawContacts.CONTACT_ID to read the
                // ContactsContract.Contacts table or any of the other related ones.
                HashMap<String, String> tmp = new HashMap<String, String>();

                tmp.put("name", c.getString(contactNameColumn));
                tmp.put("numb", c.getString(addressColumnIndex));

                maplist.add(tmp);

                contactList.add(c.getString(contactNameColumn));
            }
            c.close();
            /*int i = 0;
            Log.d("TEST", "Records: "+String.valueOf(cursor.getCount()));
            if (cursor.getCount() > 0) {
                do {
                    String bob = Arrays.toString(cursor.getColumnNames());
                    Log.d("TEST", "PhoneCur: "+bob);
Log.d("TEST", DatabaseUtils.dumpCurrentRowToString(cursor));
                    //String id = cursor
                     //       .getString(cursor
                    //                .getColumnIndexOrThrow(ContactsContract.RawContacts.SOURCE_ID));

                    i++;

                    Cursor phoneCur = contect_resolver.query(
                            ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//                                    + " = ?", new String[] { id }, null);

          //          String[] tdata[i] = new String[phoneCur.getColumnCount()];
           //         for (int f = 0; f < cursor.getColumnCount(); f++) {
            //            data[i][f] = cursor.getString(f);
            //        }
                    if (phoneCur.moveToFirst()) {
                        contactName[i] = phoneCur
                                .getString(phoneCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        contactNo[i] = phoneCur
                                .getString(phoneCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactMime[i] = phoneCur                                .getString(phoneCur
                                .getColumnIndex(ContactsContract.RawContacts.Entity.MIMETYPE ));

                        Log.d("TEST", "PhoneCur: "+phoneCur.getColumnNames());

                        if(contactMime[i] != null) Log.d("TEST", "Mime: "+contactMime[i]);

                        if (contactName[i] == null) {
                            contactName[i] = "Unknown";
                        }

                    } else {
                        contactName[i] = "Unknown";
                        contactNo[i] = "";
                    }
*/

//                    Object db;
            // db.AddContact(contactName[i], contactNo[i]);

            //                  i++;
            //                phoneCur.close();
            //          } while (cursor.moveToNext());

            Log.d("TEST", maplist.toString());




            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // mContactList.setAdapter(cursorAdapter);

                    InboxAdapter = fillListView(maplist);
                    lv.setAdapter(InboxAdapter);
                    Log.d("TEST", "Running UI thread");



                }
            });
        }
    }

    public String getDate(Long ts) {
        Log.d("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }


}