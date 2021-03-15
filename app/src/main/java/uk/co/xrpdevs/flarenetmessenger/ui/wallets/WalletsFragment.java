package uk.co.xrpdevs.flarenetmessenger.ui.wallets;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import net.lingala.zip4j.exception.ZipException;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.PKeyScanner;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PinCodeDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.Zipper;
import uk.co.xrpdevs.flarenetmessenger.ui.home.HomeFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.token.TokensFragment;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class WalletsFragment extends Fragment implements PinCodeDialogFragment.OnResultListener {

    /* TODO:
        - Context menu: Remove wallet, rename wallet, view tokens, transfer, export private key (QR code)
        - Import wallet(s) from ZIP file created by app - check format of outputted JSON in export wallets, too.
     */

    public SimpleAdapter WalletsAdaptor;
    //public SimpleAdapter simpleAdapter;
    //Smstest3 contract;
    //BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    //BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<>();
    //HashMap<String, String> deets;
    //private NotificationsViewModel notificationsViewModel;
    WalletsFragment mThis = this;
    ListView lv;
    PinCodeDialogFragment pinDialog;
    //private String pinCode;
    Activity mAct;

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_wallets, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myLog("FRAG", "WalletsFragment");
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_wallets, container, false);
        lv = root.findViewById(R.id.wallets_list);
        lv.setAdapter(WalletsAdaptor);

        prefs = this.getActivity().getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
      //  super.onCreate(savedInstanceState);

        mAct = this.getActivity();
        if(prefs.getInt("walletCount", 0) > 0 ) {
            myLog("FRAG", "Wallet count is non zero");

            getWalletList();
        }

       FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override // TODO: change intent methods to Fragment context switches
            public void onClick(View view) {
                Intent intent = new Intent(mThis.getActivity(), PKeyScanner.class);
                startActivity(intent);
            }
        });

        return root;
    }

    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        myLog("Lines", lines.toString());
        SimpleAdapter simpleAdapter = new SimpleAdapter(mThis.getActivity(), lines, R.layout.listitem_wallets, new String[]{"walletName", "walletAddress", "type", "lastval"}, new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxAddress);
                TextView cType = view.findViewById(R.id.inboxType);
                cType.setText("Coston");
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                int unread = lines.size();
                myLog("TEST", "Number of contaxts: "+unread);

                return view;
            }
        };

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(mThis.getActivity(),
                        MainActivity.class);
                HashMap<String, String> theItem = lines.get(position);
                String pooo = theItem.get("num");
                myLog("smscseeker", "name:" + theItem.toString());
                pEdit.putInt("currentWallet", (position +1 ));
                pEdit.commit();

                //Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
                //fragmentTransaction.remove(currentFragment);
                HomeFragment f = new HomeFragment();
                Bundle args = new Bundle();
                args.putInt("ltype", 2000);
                args.putString("selectFragment", "home");
                f.setArguments(args);
                Intent myService = new Intent(mAct, MyService.class);
                mAct.stopService(myService);
                mAct.startService(myService);

                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("wallets").commit();

               // startActivity(i);

            }
        });

        return simpleAdapter;

    }


    public void getWalletList() {
        feedList.clear();

        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
        try {

            myLog("TEST", "Number of wallets: "+prefs.getInt("walletCount", 0));

            for(int i=0;i<prefs.getInt("walletCount", 0);i++){
                HashMap<String, String> map = Utils.getPkey(this.getContext(), (i+1));
                if(!map.containsKey("walletName")){ map.put("walletName", "Wallet "+ (i + 1));}

                maplist.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        String dtemp;
        for (int j = 0; j < maplist.size(); j++) {
            HashMap<String, String> poo = maplist.get(j);
            // dtemp = getDate(Long.parseLong(poo.get("ts")));
            myLog("PooPoos", poo.toString());
            poo.remove("ts");
            //   poo.put("date", dtemp);
            // TODO: Local database of names associated with Coston addresses.
            //      poo.put("cnam", dbHelper.getContactName(this, poo.get("num")));



            feedList.add(poo);

        }

        mThis.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // mContactList.setAdapter(cursorAdapter);

                WalletsAdaptor = fillListView(feedList);
                lv.setAdapter(WalletsAdaptor);
                myLog("TEST", "Running UI thread");



            }
        });
        myLog("feedList", feedList.toString());
//        Collections.reverse(feedList);
        WalletsAdaptor = fillListView(feedList);
    }

    public String getDate(Long ts) {
        myLog("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.export_wallets:
                exportWallets();
                return true;
            case R.id.tokens:
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
                //fragmentTransaction.remove(currentFragment);
                Fragment f = new TokensFragment();
                Bundle args = new Bundle();
                args.putInt("ltype", 2000);
                args.putString("selectFragment", "home");
                f.setArguments(args);


                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("wallets").commit();
                return true;
     /*       case R.id.theNav:
                Intent intent2 = new Intent(mThis.getContext(), FirstRun.class);
                //intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                //intent.putExtra("lType", 2000);
                startActivity(intent2);
            case R.id.ma2:
                Intent intent3 = new Intent(mThis.getContext(), MainActivity.class);
                //intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                //intent.putExtra("lType", 2000);
                startActivity(intent3);
*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void exportWallets(){
        FragmentManager manager = mThis.getActivity().getFragmentManager();
        pinDialog = new PinCodeDialogFragment().newInstance(this, "Enter PIN:");
        pinDialog.show(manager, "1");
    }

    @Override
    public void onResult(String pinCode) throws ZipException {
        if(pinCode.equals(prefs.getString("pinCode", "asas"))){
            pinDialog.dismiss();
            Zipper zipArchive = new Zipper(prefs.getString("pinCode", "0000"), mThis.getContext());
            zipArchive.pack("/sdcard/Downloads/wallets.zip");

            // TODO: Save as a passworded ZIP file in default location on phone.
        }
    }
}
