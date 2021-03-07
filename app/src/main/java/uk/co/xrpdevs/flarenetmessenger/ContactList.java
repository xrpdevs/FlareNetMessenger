package uk.co.xrpdevs.flarenetmessenger;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ContactList extends AppCompatActivity{
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
    public int ListType;
    int WITH_ACCOUNTS = 1000;
    IntentIntegrator integrator;
    Context mThis = this;
    HashMap<String, String> contactItem;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        Intent in = getIntent();
        ListType = in.getIntExtra("lType", 1000);
        prefs = this.getSharedPreferences("fnm", 0);
        FlareConnection = MyService.initWeb3j();

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

        if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))  {
            Log.d("TEST", "No camera and storage permission");
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }

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

    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        Log.d("TEST", "FillListView");
        ArrayAdapter<String> adapter;
        simpleAdapter = new SimpleAdapter(this, lines, R.layout.listitem_inbox, new String[]{"name", "numb", "type", "id"}, new int[]{R.id.inboxName, R.id.inboxAddress, R.id.inboxType, R.id.inboxLastact}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxName);
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                //Log.d("DNSJNI", "item: "+item.toString());
                int unread = lines.size();
                Log.d("TEST", "Number of contaxts: "+unread);
                // int unread = 0;
                if(unread>0) {
                    cNtext += " (" + unread + ")";
                    cName.setText(cNtext);
                    view.invalidate();
                }
                return view;
            }
        };

        lv = findViewById(R.id.inbox_list);
        lv.setAdapter(InboxAdapter);
        lv.setOnItemLongClickListener((parent, v, position, id) -> {
            HashMap<String, String> theItem = lines.get(position);
            ContactsManager.deleteRawContactID(this, Long.parseLong(theItem.getOrDefault("id", "0")));
            Log.d("TEST", "Long Press");
            maplist = new ArrayList<HashMap<String, String>>();
            new Contact_thread().start();
            return true;
        });


        lv.setOnItemClickListener((parent, v, position, id) -> {
            if(ListType == 1000) {
                Intent i = new Intent(ContactList.this,
                        MainActivity.class);
                Bundle b = new Bundle();
                HashMap<String, String> theItem = lines.get(position);

                b.putString("name", theItem.get("name"));
                b.putString("addr", theItem.get("numb"));
                b.putString("id", theItem.get("id"));
                i.putExtra("contactInfo", b);
                String pooo = theItem.get("num");
                Log.d("smscseeker", "name:" + theItem.toString());

                startActivity(i);
            }
            if(ListType == 2000) {
                Log.d("TEST", "Button Pressed");
                Bundle b = new Bundle();
                HashMap<String, String> theItem = lines.get(position);
                b.putString("name", theItem.get("name"));
                b.putString("addr", theItem.get("numb"));
                b.putString("id", theItem.get("id"));

                contactItem = lines.get(position);
               // mThis.getApplicationContext().getCurrent
                integrator = new IntentIntegrator(ContactList.this);
                integrator.setPrompt("Scanning WALLET ADDRESS\nQR code will be scanned automatically on focus");
                integrator.addExtra("contactInfo", b);
                integrator.setCameraId(0);
                integrator.setRequestCode(3000);
                integrator.setOrientationLocked(true);
                integrator.setBeepEnabled(true);
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        return simpleAdapter;

    }




    class Contact_thread extends Thread {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            String yourAccountType = "%";
            Cursor c; Cursor d;

            int contactNameColumn ;
            int addressColumnIndex;
            int addressColumnId;
            List<Long> ctl = new ArrayList<Long>();

            if(ListType == WITH_ACCOUNTS) {
                Log.d("TEST", "ContactList WITH_ACCOUNTS");
                yourAccountType = "uk.co.xrpdevs.flarenetmessenger";//ex: "com.whatsapp"

                c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                        null,
                        ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                        new String[]{yourAccountType},
                        ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);

             //   addressColumnId    = c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME);
            } else {
                Log.d("TEST", "ContactList not WITH_ACCOUNTS");
                c = getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null,
                        null,
                        null,
                        ContactsContract.Contacts.DISPLAY_NAME);
          //      contactNameColumn = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
           //     addressColumnIndex = c.getColumnIndex(ContactsContract.Contacts._ID);

            }
            ArrayList<String> contactList = new ArrayList<String>();



           Log.d("TEST", "Number of results: "+c.getCount());
            while (c.moveToNext()) {
                HashMap<String, String> tmp = new HashMap<String, String>();
                if(ListType == WITH_ACCOUNTS) {

                    contactNameColumn = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
                    addressColumnIndex = Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID)));
                   // Long contactID = c.getLong(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                    Uri rawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, addressColumnIndex);
                    Uri entityUri = Uri.withAppendedPath(rawContactUri, ContactsContract.RawContacts.Entity.CONTENT_DIRECTORY);
                    d = getContentResolver().query(entityUri,
                            null, "mimetype = 'vnd.android.cursor.item/com.sample.profile'", null, null);
                    tmp.put("id", String.valueOf(addressColumnIndex));


                    int count =0;
                    try {
                        while (d.moveToNext()) {
                        count++;
                        String XRPAddress = d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA3));
                            Log.d("TEST", "XRP Address: "+XRPAddress);
                            Log.d("TEST", "XRP Tag    : "+d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA2)));
                            Log.d("TEST", "XRP Info   : "+d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA1)));
                            Log.d("TEST", "entityURI: "+ DatabaseUtils.dumpCurrentRowToString(d));
                            Long contactID = d.getLong(d.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                            ctl.add(contactID);
                            tmp.put("numb", XRPAddress);
                            tmp.put("name", c.getString(contactNameColumn));
                        }
                    } finally {
                        d.close();
                    }
                    String getRawQuery = ContactsContract.RawContacts.CONTACT_ID + "=" + addressColumnIndex;
                } else {
                    contactNameColumn = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    addressColumnIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
                    tmp.put("numb", c.getString(addressColumnIndex));
                    tmp.put("id", c.getString(addressColumnIndex));
                    tmp.put("name", c.getString(contactNameColumn));
                }
                maplist.add(tmp);

                Log.d("TEST", "ContactList existing entry: "+tmp.toString());


                contactList.add(c.getString(contactNameColumn));
            }
            c.close();
            if(ctl != null) {
                Log.d("TEST", ctl.toString());
            }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Toast toasty = Toast.makeText(this, "Content:" +requestCode, Toast.LENGTH_LONG);
        toasty.show();
        if (requestCode == 3000) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(resultCode, intent);

            if (resultCode == RESULT_OK) {
                Toast toasty2 = Toast.makeText(this, "Content:" + scanningResult.toString(), Toast.LENGTH_LONG);
                toasty2.show();
                if (scanningResult != null) {
                    //                        final TextView formatTxt = (TextView)findViewById(R.id.scan_format);
                    //                      final TextView contentTxt = (TextView)findViewById(R.id.scan_content);
                    String scanContent = scanningResult.getContents();
                    String scanFormat = scanningResult.getFormatName();
                    Toast toast = Toast.makeText(this, "Content:" + scanContent + " Format:" + scanFormat, Toast.LENGTH_LONG);
                    Log.d("TEST", "OnActivityResult " + scanContent);

                    //Credentials cs = Credentials.create(scanContent);

                    //String privateKey = cs.getEcKeyPair().getPrivateKey().toString(16);
                    //String publicKey = cs.getEcKeyPair().getPublicKey().toString(16);
                    String addr = scanContent;

                    int wC = prefs.getInt("walletCount", 0); wC++;

                    //System.out.println("Private key: " + privateKey);
                    //System.out.println("Public key: " + publicKey);
                    System.out.println("Address: " + addr);

                    HashMap<String, String> tmp = new HashMap<String, String>();
                //    tmp.put("walletName", wName.getText().toString());
                    //tmp.put("walletPrvKey", scanContent);
                    //tmp.put("walletPubKey", "0x"+publicKey);
                    tmp.put("walletAddress", addr);
                    Log.d("TEST", "OnactivityResult Contact Item: "+contactItem.toString());

                    Intent myIntent = getIntent();
                    Bundle bundle = myIntent.getExtras();
                //    String action = myIntent.getAction();
                //    Bundle data = bundle.getBundle("contactInfo");
                  //  String id = data.getString("id");

                    if (bundle != null) {
                        for (String key : bundle.keySet()) {
                            Log.e("TEST", "onActivityResult bundleDump "+key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                        }
                    }

                    MyContact newContact = new MyContact(contactItem.get("name"), addr, "0", Integer.parseInt(Objects.requireNonNull(contactItem.get("id"))));

                    String rCID = ContactsManager.addContact(this, newContact);
                    //ContactsManager.updateMyContact(this, contactItem.get("name"));

                    String uriString = new StringBuilder().append("content://com.android.contacts/data/").append(rCID).toString();

                    Intent abc = new Intent(this, ViewContact.class);
                    Uri myUri = Uri.parse(uriString);
                    abc.setData(myUri);
                    startActivity(abc);


                   // abc.setData

        //            pEdit.putString("wallet"+String.valueOf(wC), new JSONObject(tmp).toString());
        //            pEdit.putInt("walletCount", wC);
        //            pEdit.putInt("currentWallet", wC);

                    //                        pEdit.putString("walletPrvKey", ""+scanContent);
//                        pEdit.putString("walletPubKey", "0x"+publicKey);
//                        pEdit.putString("walletAddress",""+addr);
        //            pEdit.commit();
         //           pEdit.apply();
                    //       formatTxt.setText("FORMAT: " + scanFormat);
                    //         contentTxt.setText("CONTENT: " + scanContent);
                    //we have a result
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "No scan data received!", Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e("TEST", " Scan unsuccessful");
                }
            } else { //resultCode == RESULT_CANCELED) {
                super.onActivityResult(requestCode, resultCode, intent);
                // Handle cancel
                Log.i("App", "Scan unsuccessful");
            }
        }
    }



    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public void doScan(){

        Log.d("TEST", "Button Pressed");
        integrator = new IntentIntegrator(this);
        integrator.setPrompt("QR code will be scanned automatically on focus");
        integrator.setCameraId(0);
        integrator.setRequestCode(PKeyScanner.PRIV_KEY_REQUEST_CODE);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }


}