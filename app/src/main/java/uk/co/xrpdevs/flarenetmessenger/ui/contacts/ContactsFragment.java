package uk.co.xrpdevs.flarenetmessenger.ui.contacts;

import android.Manifest;
import android.content.ContentUris;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import uk.co.xrpdevs.flarenetmessenger.CaptureActivityPortrait;
import uk.co.xrpdevs.flarenetmessenger.ContactsManager;
import uk.co.xrpdevs.flarenetmessenger.MainActivity2;
import uk.co.xrpdevs.flarenetmessenger.MyContact;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.ViewContact;
import uk.co.xrpdevs.flarenetmessenger.ui.messages.MessagesFragment;

import static android.app.Activity.RESULT_OK;
import static java.lang.Long.getLong;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class ContactsFragment extends Fragment {
    public String contractAddress;
    public SimpleAdapter InboxAdapter;
    public SimpleAdapter simpleAdapter;
    SharedPreferences prefs;
    ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
    ListView lv;
    public int ListType;
    int WITH_ACCOUNTS = 1000;
    IntentIntegrator integrator;
    ContactsFragment mThis = this;
    HashMap<String, String> contactItem;
    public View rootView;
    private DashboardViewModel dashboardViewModel;
    Boolean SendMessage = false;

    @Override
    public void onStart() {
        super.onStart();
        myLog("FRAG", "onStart");
        Bundle args = getArguments();
        if (args != null) {

            myLog("FRAG", args.toString());
        }
        if(args != null){

            if(args.containsKey("ltype")){
                ListType = args.getInt("ltype");
            }
        }
        if(ListType == 2000){

            getActionBar().setTitle("Add to Contact");
        }
        if (ListType == 3000) {
            ListType = 1000;
            SendMessage = true;
        }
        if(ListType == 1000){

            getActionBar().setTitle("Select Contact");
        }
        new Contact_thread().run();
    }

    private ActionBar getActionBar() {
        return ((MainActivity2) getActivity()).getSupportActionBar();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);
        rootView = root;
        lv = root.findViewById(R.id.inbox_list);
        Intent in = mThis.getActivity().getIntent();
        ListType = in.getIntExtra("lType", 1000);
        prefs = this.getActivity().getSharedPreferences("fnm", 0);
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        if ((this.getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (this.getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))  {
            myLog("TEST", "No camera and storage permission");
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
        }
        contractAddress = "0x4a1400220373983f3716D4e899059Dda418Fd08A"; // v1 SMSTest2
        contractAddress = MyService.contractAddress;
        setHasOptionsMenu(true);
        myLog("FRAG", "ContactsFragment onCreateView called");
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_contacts_add:
                ListType = 2000;
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(currentFragment);
                ContactsFragment f = new ContactsFragment();
                Bundle args = new Bundle();
                args.putInt("ltype", 2000);
                f.setArguments(args);
                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.commit();
                return true;
            case R.id.menu_contacts_new: // Todo: startActivityForResult add contact then call scanner.
                ListType = 2000;
                Fragment currentFragment2 = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction2 = getFragmentManager().beginTransaction();
                fragmentTransaction2.remove(currentFragment2);
                ContactsFragment f2 = new ContactsFragment();
                Bundle args2 = new Bundle();
                args2.putInt("ltype", 1000);
                f2.setArguments(args2);
                fragmentTransaction2.replace(R.id.nav_host_fragment, f2);
                fragmentTransaction2.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        myLog("TEST", "FillListView");
      //  ArrayAdapter<String> adapter;
        simpleAdapter = new SimpleAdapter(mThis.getActivity(), lines, R.layout.listitem_contacts, new String[]{"name", "numb", "type", "id"}, new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxAddress);
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                //myLog("DNSJNI", "item: "+item.toString());
                int unread = lines.size();
                myLog("TEST", "Number of contaxts: "+unread);
                // int unread = 0;
                if(unread>0) {
                    cNtext += " (" + unread + ")";
                    cName.setText(cNtext);
                    view.invalidate();
                }
                return view;
            }
        };

        lv.setAdapter(InboxAdapter);
        lv.setOnItemLongClickListener((parent, v, position, id) -> {
            HashMap<String, String> theItem = lines.get(position);
            ContactsManager.deleteRawContactID(this.getActivity(), Long.parseLong(theItem.getOrDefault("id", "0")));
            myLog("TEST", "Long Press");
            maplist = new ArrayList<HashMap<String, String>>();
            new Contact_thread().start();
            return true;
        });


        lv.setOnItemClickListener((parent, v, position, id) -> {
            HashMap<String, String> theItem = lines.get(position);
            if(ListType == 1000) {
                if(SendMessage){
                    TextView cNam = v.findViewById(R.id.inboxAddress);     // todo: refactor to InboxAddress
                    TextView cAddr = v.findViewById(R.id.inboxContent);  // todo: refactor to InboxContent


//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
                    //fragmentTransaction.remove(currentFragment);
                    Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.remove(currentFragment);
                    MessagesFragment f = new MessagesFragment();
                    Bundle args = new Bundle();
                    args.putInt("SendMsg", 3000);
                    args.putString("addrTo", cAddr.getText().toString());
                    args.putString("nameTo", cNam.getText().toString());
                    args.putString("selectFragment", "home");
                    f.setArguments(args);

                    fragmentTransaction.replace(R.id.nav_host_fragment, f);
                    fragmentTransaction.commit();
                } else {
                    Intent i = new Intent(this.getActivity(),
                            ViewContact.class);
                    Bundle b = new Bundle();


                    b.putString("name", theItem.get("name"));
                    b.putString("addr", theItem.get("numb"));
                    b.putString("id", theItem.get("id"));
                    i.putExtra("contactInfo", b);
                    String uriString = new StringBuilder().append("content://com.android.contacts/data/").append(theItem.get("id")).toString();

                    Uri myUri = Uri.parse(uriString);
                    i.setData(myUri);
                    String pooo = theItem.get("num");
                    myLog("smscseeker", "name:" + theItem.toString());

                    startActivity(i);
                }
            }
            if(ListType == 2000) {
                myLog("TEST", "Button Pressed");
                Bundle b = new Bundle();
                  b.putString("name", theItem.get("name"));
                b.putString("addr", theItem.get("numb"));
                b.putString("id", theItem.get("id"));

                contactItem = lines.get(position);
                // mThis.getApplicationContext().getCurrent
                //integrator = new IntentIntegrator(this.getActivity());
                integrator = new IntentIntegrator(getActivity()) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        mThis.startActivityForResult(intent, 3000); // REQUEST_CODE override
                    }
                };
                //integrator.forSupportFragment(mThis);
                integrator.setPrompt("Scanning WALLET ADDRESS\nQR code will be scanned automatically on focus");
                integrator.addExtra("contactInfo", b);
                integrator.setCameraId(0);
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
            maplist.clear();
            // TODO Auto-generated method stub

            String yourAccountType = "%";
            Cursor c; Cursor d;

            int contactNameColumn ;
            int addressColumnIndex;
            int addressColumnId;
            List<Long> ctl = new ArrayList<Long>();

            if(ListType == WITH_ACCOUNTS) {
                myLog("TEST", "ContactList WITH_ACCOUNTS");
                yourAccountType = "uk.co.xrpdevs.flarenetmessenger";//ex: "com.whatsapp"

                c = mThis.getActivity().getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                        null,
                        ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                        new String[]{yourAccountType},
                        ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);

                //   addressColumnId    = c.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME);
            } else {
                myLog("TEST", "ContactList not WITH_ACCOUNTS");
                c = mThis.getActivity().getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null,
                        null,
                        null,
                        ContactsContract.Contacts.DISPLAY_NAME);
                //      contactNameColumn = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);
                //     addressColumnIndex = c.getColumnIndex(ContactsContract.Contacts._ID);

            }
            ArrayList<String> contactList = new ArrayList<String>();



            myLog("TEST", "Number of results: "+c.getCount());
            while (c.moveToNext()) {
                HashMap<String, String> tmp = new HashMap<String, String>();
                if(ListType == WITH_ACCOUNTS) {

                    contactNameColumn = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
                    addressColumnIndex = Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID)));

                    // Long contactID = c.getLong(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                    Uri rawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, addressColumnIndex);
                    Uri entityUri = Uri.withAppendedPath(rawContactUri, ContactsContract.RawContacts.Entity.CONTENT_DIRECTORY);
                    d = mThis.getActivity().getContentResolver().query(entityUri,
                            null, "mimetype = 'vnd.android.cursor.item/com.sample.profile'", null, null);
                    tmp.put("id", String.valueOf(addressColumnIndex));
               //     tmp.put("id", String.valueOf(bumole));


                    int count =0;
                    try {
                        while (d.moveToNext()) {
                            count++;
                            String XRPAddress = d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA3));
                            myLog("TEST", "XRP Address: "+XRPAddress);
                            myLog("TEST", "XRP Tag    : "+d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA2)));
                            myLog("TEST", "XRP Info   : "+d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA1)));
                            myLog("TEST", "entityURI: "+ DatabaseUtils.dumpCurrentRowToString(d));
                            Long contactID = d.getLong(d.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                            int bumole = Integer.parseInt(d.getString(d.getColumnIndex("data_id")));
                            ctl.add(contactID);
                            tmp.put("numb", XRPAddress);
                            tmp.put("id", String.valueOf(bumole));
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

              //  myLog("TEST", "ContactList existing entry: "+tmp.toString());


                contactList.add(c.getString(contactNameColumn));
            }
            c.close();
            if(ctl != null) {
                myLog("TEST", ctl.toString());
            }

            myLog("TEST", maplist.toString());

            mThis.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // mContactList.setAdapter(cursorAdapter);

                    InboxAdapter = fillListView(maplist);
                    lv.setAdapter(InboxAdapter);
                    myLog("TEST", "Running UI thread");



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
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        myLog("FRAG", "FRAGMENT onActivityResult");

        Toast toasty = Toast.makeText(mThis.getActivity(), "Content:" +requestCode, Toast.LENGTH_LONG);
        toasty.show();
        if (requestCode == 3000) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(resultCode, intent);

            if (resultCode == RESULT_OK) {
                Toast toasty2 = Toast.makeText(mThis.getActivity(), "Content:" + scanningResult.toString(), Toast.LENGTH_LONG);
                toasty2.show();
                if (scanningResult != null) {
                    //                        final TextView formatTxt = (TextView)findViewById(R.id.scan_format);
                    //                      final TextView contentTxt = (TextView)findViewById(R.id.scan_content);
                    String scanContent = scanningResult.getContents();
                    String scanFormat = scanningResult.getFormatName();
                    Toast toast = Toast.makeText(mThis.getActivity(), "Content:" + scanContent + " Format:" + scanFormat, Toast.LENGTH_LONG);
                    myLog("TEST", "OnActivityResult " + scanContent);

                    String addr = scanContent;

                    int wC = prefs.getInt("walletCount", 0); wC++;

                    System.out.println("Address: " + addr);

                    HashMap<String, String> tmp = new HashMap<String, String>();

                    tmp.put("walletAddress", addr);
                    myLog("TEST", "OnactivityResult Contact Item: "+contactItem.toString());

                    Intent myIntent = mThis.getActivity().getIntent();
                    Bundle bundle = myIntent.getExtras();


                    if (bundle != null) {
                        for (String key : bundle.keySet()) {
                            Log.e("TEST", "onActivityResult bundleDump "+key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
                        }
                    }

                    MyContact newContact = new MyContact(contactItem.get("name"), addr, "0", Integer.parseInt(Objects.requireNonNull(contactItem.get("id"))));

                    String rCID = ContactsManager.addContact(mThis.getActivity(), newContact);


                    String uriString = new StringBuilder().append("content://com.android.contacts/data/").append(rCID).toString();

                    Intent abc = new Intent(mThis.getContext(), ViewContact.class);
                    Uri myUri = Uri.parse(uriString);
                    abc.setData(myUri);
                    startActivity(abc);


                } else {
                    Toast toast = Toast.makeText(mThis.getActivity().getApplicationContext(),
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



}