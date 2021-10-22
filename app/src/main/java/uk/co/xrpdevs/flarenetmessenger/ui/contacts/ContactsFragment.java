package uk.co.xrpdevs.flarenetmessenger.ui.contacts;

import static android.app.Activity.RESULT_OK;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.Manifest;
import android.app.FragmentManager;
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

import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import uk.co.xrpdevs.flarenetmessenger.CaptureActivityPortrait;
import uk.co.xrpdevs.flarenetmessenger.ContactsManager;
import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.MyContact;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.ViewContact;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.AddWalletDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.AddressEntryFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;
import uk.co.xrpdevs.flarenetmessenger.ui.messages.MessagesFragment;

public class ContactsFragment extends Fragment implements AddWalletDialogFragment.OnResultListener, AddressEntryFragment.OnResultListener {
    public String contractAddress, token, tAddr;
    boolean isToken = false;
    public SimpleAdapter InboxAdapter;
    public SimpleAdapter simpleAdapter;
    SharedPreferences prefs;
    ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
    ListView lv;
    public int ListType;
    int WITH_ACCOUNTS = 1000;
    int ALL_CONTACTS = 2000;
    int WITH_ACCOUNTS_MESSAGE = 3000;
    int WITH_ACCOUNTS_SENDFUNDS = 4000;
    IntentIntegrator integrator;
    ContactsFragment mThis = this;
    HashMap<String, String> contactItem;
    public View rootView;
    private DashboardViewModel dashboardViewModel;
    Boolean SendMessage = false;
    PleaseWaitDialog dialogActivity;
    AddWalletDialogFragment addWalletDialog;
    AddressEntryFragment addressEntryDialog;
    Bundle tempContactInfo;

    @Override //Handle arguments and setup
    public void onStart() {
        super.onStart();
        myLog("FRAG", "onStart");
        Bundle args = getArguments();
        if (args != null) {

            myLog("FRAG", args.toString());
        }
        if (args != null) {

            if (args.containsKey("ltype")) {
                ListType = args.getInt("ltype");
            }
        }
        if (ListType == WITH_ACCOUNTS) {

            getActionBar().setTitle("Add to Contact");
        }
        if (ListType == WITH_ACCOUNTS_MESSAGE) {
            ListType = WITH_ACCOUNTS;
            SendMessage = true;
        }
        if (ListType == WITH_ACCOUNTS_SENDFUNDS) {
            ListType = WITH_ACCOUNTS;
            if (args.containsKey("token")) {
                isToken = true;
                token = args.getString("token");
                tAddr = args.getString("tAddr");
            }
        }
        if (ListType == WITH_ACCOUNTS) {

            getActionBar().setTitle("Select Contact");
        }
        new Contact_thread().run();
    }

    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }

    // Runs when the view is popped to front of the stack
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contacts, container, false);
        rootView = root;
        lv = root.findViewById(R.id.inbox_list);
        Intent in = mThis.getActivity().getIntent();
        ListType = in.getIntExtra("lType", 1000);
        prefs = this.getActivity().getSharedPreferences("fnm", 0);
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        if ((this.getActivity().checkSelfPermission(Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) ||
                (this.getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) ||
                (this.getActivity().checkSelfPermission(Manifest.permission.WRITE_CONTACTS) !=
                        PackageManager.PERMISSION_GRANTED) ||
                (this.getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) !=
                        PackageManager.PERMISSION_GRANTED)
            //requires android.permission.READ_CONTACTS or android.permission.WRITE_CONTACTS
        ) {
            myLog("TEST", "No camera and storage permission");
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, 50);
        }
        contractAddress = "0x4a1400220373983f3716D4e899059Dda418Fd08A"; // v1 SMSTest2
        contractAddress = MyService.contractAddress;
        setHasOptionsMenu(true);
        myLog("FRAG", "ContactsFragment onCreateView called");
        return root;
    }

    @Override // We have an options menu to inflate
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override // Menu handling code
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_contacts_add_to_existing:
                ListType = ALL_CONTACTS;
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(currentFragment);
                ContactsFragment f = new ContactsFragment();
                Bundle args = new Bundle();
                args.putInt("ltype", ALL_CONTACTS);
                f.setArguments(args);
                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("contacts").commit();
                return true;
            case R.id.menu_contacts_show_associated: // Todo: startActivityForResult add contact then call scanner.
                ListType = WITH_ACCOUNTS;
                Fragment currentFragment2 = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction2 = getFragmentManager().beginTransaction();
                fragmentTransaction2.remove(currentFragment2);
                ContactsFragment f2 = new ContactsFragment();
                Bundle args2 = new Bundle();
                args2.putInt("ltype", WITH_ACCOUNTS);
                f2.setArguments(args2);
                fragmentTransaction2.replace(R.id.nav_host_fragment, f2);
                fragmentTransaction2.addToBackStack("contacts").commit();
                return true;
            case R.id.menu_contacts_add_new:

                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Populate ListView with HashMap contents and set up event listeners
    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) { // lines is initialized here
        myLog("TEST", "FillListView");
        //  ArrayAdapter<String> adapter;
        simpleAdapter = new SimpleAdapter(mThis.getActivity(), lines, R.layout.listitem_contacts, new String[]{"name", "numb", "type", "id"}, new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxAddress);
                TextView myId = view.findViewById(R.id.inboxLastact);
                myLog("pos", position + "");
                if (myId.getText().equals("-5000")) cName.setText("Enter Manually");
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                //myLog("DNSJNI", "item: "+item.toString());
                int unread = lines.size();
                myLog("TEST", "Number of contaxts: " + unread);
                // int unread = 0;
                if (unread > 0) {
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
            if (theItem.get("id").equals("-5000")) {
                // do nothing
            } else {
                ContactsManager.deleteRawContactID(this.getActivity(), Long.parseLong(theItem.getOrDefault("data_id", "0")));
                myLog("TEST", "Long Press -> " + Long.parseLong(theItem.getOrDefault("data_id", "0")));
                maplist = new ArrayList<HashMap<String, String>>();
                new Contact_thread().start();
            }
            return true;
        });


        lv.setOnItemClickListener((parent, v, position, id) -> {
            HashMap<String, String> theItem = lines.get(position);
            if (ListType == WITH_ACCOUNTS) { // use contact

                if (SendMessage) {  // handle click-through from Messages Fragment

                    // todo: handle sending messages for non-contacts for ETH style BC's or XRP
                    TextView cNam = v.findViewById(R.id.inboxAddress);
                    TextView cAddr = v.findViewById(R.id.inboxContent);
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
                    fragmentTransaction.addToBackStack("contacts").commit();
                } else { // open contact in view contact / send funds activity

                    Intent i = new Intent(this.getActivity(),
                            ViewContact.class);
                    Bundle b = new Bundle();

                    // if theItem id = -5000 then popup asking for wallet address

                    if (theItem.get("id").equals("-5000")) {
                        android.app.FragmentManager manager = mThis.getActivity().getFragmentManager();
                        addressEntryDialog("Enter Wallet Address", "Address:", true);

                    } else {

                        b.putString("name", theItem.get("name"));
                        b.putString("addr", theItem.get("numb"));
                        if (isToken) {
                            b.putString("token", token);
                            b.putString("tAddr", tAddr);
                        }
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
            } // add address to contact
            if (ListType == ALL_CONTACTS) {
                myLog("TEST", "Button Pressed");
                Bundle b = new Bundle();
                b.putString("name", theItem.get("name"));
                b.putString("addr", theItem.get("numb"));
                b.putString("id", theItem.get("id"));

                contactItem = lines.get(position);
                // mThis.getApplicationContext().getCurrent
                //integrator = new IntentIntegrator(this.getActivity());
                tempContactInfo = b;
                // doScanner(b);

                addWalletDialog("Scan or Paste", "Please choose", true);


            }
        });

        return simpleAdapter;

    }

    // open the scanner activity to add a public key to a contact
    public void doScanner(Bundle contactinfo) {
        integrator = new IntentIntegrator(getActivity()) {
            @Override
            protected void startActivityForResult(Intent intent, int code) {
                mThis.startActivityForResult(intent, 3000); // REQUEST_CODE override
            }
        };
        //integrator.forSupportFragment(mThis);
        integrator.setPrompt("Scanning WALLET ADDRESS\nQR code will be scanned automatically on focus");
        integrator.addExtra("contactInfo", contactinfo);
        integrator.setCameraId(0);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override // handle result from AddressEntryFragment
    public void onResult(String pinCode, String tag) throws IOException, JSONException {
        Log.d("PINNE", pinCode);
        addressEntryDialog.dismiss();

    }

    // Read contacts and populate the HashMap for the ListView
    class Contact_thread extends Thread {

        @Override
        public void run() {
            maplist.clear();
            // TODO: This could do with being cleaned up a fair bit
            //           - Need to check which blockchain is currently in use in order to populate the
            //              account type field in the custom contact items.
            //           - Each blockchain in 'blockchains.json' needs a unique ID, this is what will be
            //              used to determine the value that is inserted above.

            String yourAccountType = "%";
            Cursor c;
            Cursor d;

            int contactNameColumn, addressColumnIndex, count = 0;
            int addressColumnId;
            List<Long> ctl = new ArrayList<Long>();
            HashMap<String, String> addm = new HashMap<String, String>(); // placeholder for manual address entry

            if (ListType == WITH_ACCOUNTS) {

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

            if (ListType == WITH_ACCOUNTS) {
                addm.put("numb", "Manual");
                addm.put("id", "-5000");
                addm.put("name", "Scan or Paste");
                maplist.add(addm);
            }

            myLog("TEST", "Number of results: " + c.getCount());
            while (c.moveToNext()) {
                HashMap<String, String> tmp = new HashMap<String, String>();

                if (ListType == WITH_ACCOUNTS) {

                    contactNameColumn = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
                    addressColumnIndex = Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID)));

                    // Long contactID = c.getLong(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                    Uri rawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, addressColumnIndex);
                    Uri entityUri = Uri.withAppendedPath(rawContactUri, ContactsContract.RawContacts.Entity.CONTENT_DIRECTORY);
                    String filter = "( mimetype =? AND " + ContactsContract.Data.DATA5 + " = ?) ";
                    d = mThis.getActivity().getContentResolver().query(
                            entityUri,
                            null,
                            filter,
                            // TODO: See above
                            new String[]{"vnd.android.cursor.item/com.sample.profile", MyService.currentChain},
                            null,
                            null
                    );

                    tmp.put("id", String.valueOf(addressColumnIndex));
                    //     tmp.put("id", String.valueOf(bumole));


                    try {
                        while (d.moveToNext()) {
                            count++;
                            String XRPAddress = d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA3));
                            myLog("TEST", "XRP Address: " + XRPAddress);
                            myLog("TEST", "XRP Tag    : " + d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA2)));
                            myLog("TEST", "XRP Info   : " + d.getString(d.getColumnIndex(ContactsContract.RawContacts.Entity.DATA1)));
                            myLog("TEST", "entityURI: " + DatabaseUtils.dumpCurrentRowToString(d));
                            Long contactID = d.getLong(d.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                            Long dataID = d.getLong(d.getColumnIndex(ContactsContract.RawContacts._ID));
                            int bumole = Integer.parseInt(d.getString(d.getColumnIndex("data_id")));
                            ctl.add(contactID);
                            tmp.put("numb", XRPAddress);
                            tmp.put("id", String.valueOf(bumole));
                            tmp.put("data_id", String.valueOf(dataID));
                            tmp.put("name", c.getString(contactNameColumn));
                            maplist.add(tmp);
//                            contactList.add(tmp);
                        }
                    } finally {
                        d.close();
                    }
                    if (count == 0) {

                    }
                    String getRawQuery = ContactsContract.RawContacts.CONTACT_ID + "=" + addressColumnIndex;
                } else {
                    count++;
                    contactNameColumn = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    addressColumnIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
                    tmp.put("numb", c.getString(addressColumnIndex));
                    tmp.put("id", c.getString(addressColumnIndex));
                    tmp.put("name", c.getString(contactNameColumn));
                    maplist.add(tmp);
                }
                //               maplist.add(tmp);

                //  myLog("TEST", "ContactList existing entry: "+tmp.toString());

                contactList.add(c.getString(contactNameColumn));

            }
            c.close();
            if(ctl != null) {
                myLog("TEST", ctl.toString());
            }

            myLog("TEST", maplist.toString());

            int finalCount = count;
            mThis.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // mContactList.setAdapter(cursorAdapter);

                    String noContacts = "It appears that you have no accounts for " + prefs.getString("csbc_name", "") + "! Please add " + prefs.getString("csbc_name", "") + " address to one of your contacts, or manually enter an address.";
                    if (finalCount < 1) {
                        if (ListType != WITH_ACCOUNTS) {
                            noContacts = "You have no contacts in your phone at all. [FNM] relies on your phone's contact database to save wallet addresses to. Please create some contacts and then (in this app) associate those contacts with the corresponding wallet addresses for " + MyService.currentChain;
                        }
                        //     showDialog("No Contacts", noContacts, true);
                    }
                    InboxAdapter = fillListView(maplist);
                    lv.setAdapter(InboxAdapter);
                    myLog("TEST", "Running UI thread");


                }
            });
        }
    }

    @Override // handle result from AddWalletDialogFragment
    public void onResult(String pinCode) {
        addWalletDialog.dismiss();
        if (pinCode.equals("_SCAN_QR")) {
            doScanner(tempContactInfo);
        } else {
            doAddContact(tempContactInfo, pinCode);
            //todo: handle checking and adding from pasted address
        }
    }

    // Convert UNIX epoch to JAVA epoch (*1000) and output human-readable
    public String getDate(Long ts) {
        myLog("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }

    // Show a simple message dialog
    private boolean showDialog(String title, String prompt, Boolean cancelable) {
        FragmentManager manager = mThis.getActivity().getFragmentManager();

        dialogActivity = new PleaseWaitDialog();
        dialogActivity.prompt = prompt;
        dialogActivity.titleText = title;
        dialogActivity.cancelable = cancelable;
        dialogActivity.show(manager, "DialogActivity");
        return true;
    }

    // Ask if user wants to scan a QR code or paste wallet address. TODO: Implement other options like crypto domains, etc.
    private boolean addWalletDialog(String title, String prompt, Boolean cancelable) {
        //FragmentManager manager = mThis.getActivity().getFragmentManager();
        android.app.FragmentManager manager = mThis.getActivity().getFragmentManager();

        // NOTE: When using DialogFragment with an OnResultListener
        //          use newInstance as opposed to dialog = new Dialog form...

        addWalletDialog = new AddWalletDialogFragment().newInstance(this, "meh");
        addWalletDialog.prompt = prompt;
        addWalletDialog.titleText = title;
        addWalletDialog.cancelable = cancelable;
        addWalletDialog.show(manager, "DialogActivity");
        return true;
    }

    // dialog for entering wallet addresses manually
    private boolean addressEntryDialog(String title, String prompt, Boolean cancelable) {
        //FragmentManager manager = mThis.getActivity().getFragmentManager();
        android.app.FragmentManager manager = mThis.getActivity().getFragmentManager();

        // NOTE: When using DialogFragment with an OnResultListener
        //          use newInstance as opposed to dialog = new Dialog form...

        addressEntryDialog = new AddressEntryFragment().newInstance(this, "Enter Address", "EADD");
        addressEntryDialog.show(manager, "Dialog");

        return true;
    }

    @Override // Deal with output from "add wallet to contact" (QR Code Scan)
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 3000) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(resultCode, intent);

            if (resultCode == RESULT_OK) {

                if (scanningResult != null) {
                    myLog("FRAG", "FRAGMENT onActivityResult");
                    String addr = scanningResult.getContents(); // contents of the QR code

                    System.out.println("Address: " + addr);

                    myLog("TEST", "OnactivityResult Contact Item: " + contactItem.toString());

                    Intent myIntent = mThis.getActivity().getIntent();
                    Bundle bundle = myIntent.getExtras();
//                           for (String key : bundle.keySet()) {
//                                Log.e("TEST", "onActivityResult bundleDump " + key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
//                         }
                    doAddContact(bundle, addr);

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

    void doAddContact(Bundle bundle, String addr) {
        // todo - check for invalid data (wrong length)
        //      - if pubkey is scanned or pasted, convert to wallet address
        //      - implement non-hex formats

        //    if (bundle != null) {
        //       for (String key : bundle.keySet()) {
        //            Log.e("TEST", "onActivityResult bundleDump " + key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
        //     }
        MyContact newContact = new MyContact(contactItem.get("name"), addr, "0", Integer.parseInt(Objects.requireNonNull(contactItem.get("id"))));

        String rCID = ContactsManager.addContact(mThis.getActivity(), newContact, MyService.currentChain);
        Log.d("CurrentChain: ", MyService.currentChain);

        String uriString = new StringBuilder().append("content://com.android.contacts/data/").append(rCID).toString();

        Intent abc = new Intent(mThis.getContext(), ViewContact.class);
        Uri myUri = Uri.parse(uriString);
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString("addr", addr);
        abc.putExtra("contactInfo", bundle);
        abc.setData(myUri);
        startActivity(abc);
        //    } else {
        //        Log.d("doAddContact", "Empty bundle :(");
        //    }

    }

    @Override // perhaps update the HashMap for listview here (ie for after "add contact")
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }



}