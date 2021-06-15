package uk.co.xrpdevs.flarenetmessenger.ui.token;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextMenu;
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
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.contracts.ERC20;
import uk.co.xrpdevs.flarenetmessenger.ui.contacts.ContactsFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PinCodeDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.wallets.NotificationsViewModel;

import static uk.co.xrpdevs.flarenetmessenger.Utils.jsonToMap;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

/* TODO:
    Add token dialog. Detect chain wallet is on and offer up lists from resources.
    Tokens list saved as a SharedPreferences item.
    When adding custom tokens, automatically detect coin name, symbol, level of precision.
 */

public class TokensFragment extends Fragment {
    public SimpleAdapter TokensAdaptor;
    public SimpleAdapter simpleAdapter;
    BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<>();
    HashMap<String, String> deets;
    HashMap<String, String> tokens;
    private NotificationsViewModel notificationsViewModel;
    TokensFragment mThis = this;
    ListView lv;
    PinCodeDialogFragment pinDialog;
    private String pinCode;
    Activity mAct;
    BottomNavigationView navView;
    AdapterView.AdapterContextMenuInfo subInfo;


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_tokens, menu);

        super.onCreateOptionsMenu(menu,inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

        myLog("FRAG", "WalletsFragment");

        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_tokens, container, false);
        navView = mThis.getActivity().findViewById(R.id.nav_view);
        navView.getMenu().findItem(R.id.navigation_wallets).setChecked(true);
        lv = root.findViewById(R.id.tokens_list);
        lv.setAdapter(TokensAdaptor);
        registerForContextMenu(lv);

        prefs = this.getActivity().getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        try {
            deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            tokens = jsonToMap(deets.getOrDefault("walletTokens", ""));
        } catch (JSONException e) {
            tokens = null;
//            e.printStackTrace();
        }
//        walletName.setText(deets.getOrDefault("walletName", "Wallet "+prefs.getInt("currentWallet", 0)));
        //  super.onCreate(savedInstanceState);

        mAct = this.getActivity();
        if(prefs.getInt("walletCount", 0) > 0 ) {
            myLog("FRAG", "Wallet count is non zero");

            try {
                ArrayList<HashMap<String, String>> b = getAvailTokens("coston");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                getTokenList();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

       // FloatingActionButton fab = root.findViewById(R.id.fab);
       // fab.setOnClickListener(new View.OnClickListener() {
       //     @Override // TODO: change intent methods to Fragment context switches
       //     public void onClick(View view) {
       //         Intent intent = new Intent(mThis.getActivity(), PKeyScanner.class);
       //         startActivity(intent);
       //     }
       // });

        return root;
    }
    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }
    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setTitle("Assets (" + deets.getOrDefault("walletName", "Wallet " + prefs.getInt("currentWallet", 0)) + ")");
        getActionBar().setIcon(R.mipmap.chain_flare);
        Bundle args = getArguments();
        myLog("FRAG", "onStart");




    }

    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        //myLog("Lines", lines.toString());
        simpleAdapter = new SimpleAdapter(mThis.getActivity(), lines, R.layout.listitem_tokens, new String[]{"Name", "Addressoo", "Type", "lastval"}, new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxContent);
                TextView cType = view.findViewById(R.id.inboxType);
                cType.setText("Coston");
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                Thread updateBalance;
                if(!item.containsKey("primary")) {
                    updateBalance = new getERC20Balance(
                            MyService.getERC20link(
                                    item.get("Address"),
                                    MyService.c,
                                    MyService.rpc),
                            deets.get("walletAddress"),
                            cName);
                } else {
                    updateBalance = new getERC20Balance(
                            null,
                            deets.get("walletAddress"),
                            cName);
                }
                updateBalance.start();
                int unread = lines.size();
                //myLog("TEST", "Number of contaxts: "+unread);

                return view;
            }
        };

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> theItem = lines.get(position);
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.remove(currentFragment);
                ContactsFragment f = new ContactsFragment();
                Bundle args = new Bundle();
                myLog("item", theItem.toString());
                args.putInt("ltype", 4000);
                if (!theItem.containsKey("primary")) {
                    args.putString("token", theItem.get("Name"));
                    args.putString("tAddr", theItem.get("Address"));
                }
                f.setArguments(args);
                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("contacts").commit();

            }
//return false;

        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(mThis.getActivity(),
                        MainActivity.class);
                HashMap<String, String> theItem = lines.get(position);
                //String pooo = theItem.get("num");
                Log.d("AAAA", theItem.toString());
                TextView cBody = view.findViewById(R.id.inboxContent);
                //Fragment currentFragment = getFragmentManager().findFragmentById(R.id.nav_host_fragment);
                //FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
                //fragmentTransaction.remove(currentFragment);
                String addr = theItem.get("Address");

                BigInteger bal = new BigInteger("0");
                Thread updateBalance;
                try {
                    if (theItem.containsKey("primary")) {
                        // bal = new BigInteger("100");
                        updateBalance = new getERC20Balance(
                                null,
                                deets.get("walletAddress"),
                                cBody);
                    } else {

                        updateBalance = new getERC20Balance(
                                MyService.getERC20link(
                                        theItem.get("Address"),
                                        MyService.c,
                                        MyService.rpc),
                                deets.get("walletAddress"),
                                cBody);

                    }
                    updateBalance.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //String balance = bob.balanceOf(deets.get("walletAddress")
                //   BigDecimal bd = new BigDecimal(bal, 18);
                //   cBody.setText(bd.stripTrailingZeros().toPlainString());
                // startActivity(i);
                return true;

            }
        });

        return simpleAdapter;

    }

    public ArrayList<HashMap<String, String>> getAvailTokens(String blockChainName) throws IOException {
        ArrayList<HashMap<String, String>> availTokens = new ArrayList<>();
        HashMap<String, String> primaryAsset = new HashMap<>();
        Log.d("WBALANCE", deets.toString());

        // get asset type from wallets info
        if (deets.containsKey("walletXaddr")) {
            primaryAsset.put("Name", "XRP");
            primaryAsset.put("Address", Utils.getMyXRPBalance(deets.get("walletAddress")).first.toPlainString());
        } else {
            primaryAsset.put("Name", "FLR");
            primaryAsset.put("Address", Utils.getMyBalance(deets.get("walletAddress")).first.toPlainString());
        }
        primaryAsset.put("primary", "1");
        availTokens.add(primaryAsset);
        String json = null;
        if (!deets.containsKey("walletXaddr")) {
            try {
                InputStream is = getActivity().getAssets().open("tokens_" + blockChainName + ".json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);
                //Log.d("JSON", "== "+json);
                JSONObject jo = new JSONObject(json);
                JSONArray key = jo.names();
                for (int i = 0; i < key.length(); ++i) {
                    JSONObject obj = jo.getJSONObject(key.getString(i));
                    HashMap<String, String> tmp = jsonToMap(obj.toString());
                    tmp.put("Name", key.getString(i));
                    availTokens.add(tmp);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //myLog("json", availTokens.toString());

        return availTokens;
    }

    public void getTokenList() throws IOException {
        feedList.clear();
/*
//        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> maplist = getAvailTokens("coston");

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

        }*/

        feedList = getAvailTokens("coston");

        mThis.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                // mContactList.setAdapter(cursorAdapter);

                TokensAdaptor = fillListView(feedList);
                lv.setAdapter(TokensAdaptor);
                //myLog("TEST", "Running UI thread");



            }
        });
        //myLog("feedList", feedList.toString());
//        Collections.reverse(feedList);
        TokensAdaptor = fillListView(feedList);
    }

    public String getDate(Long ts) {
        //myLog("mooo", "val: " + ts);
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }


    class getERC20Balance extends Thread {
        public getERC20Balance(ERC20 contract, String address, TextView updateMe){
            this.contract = contract;
            this.address = address;
            this.updateMe = updateMe;
        }
        ERC20 contract;
        String address;
        TextView updateMe;
        BigInteger balance = new BigInteger("0");
        @Override
        public void run() {
            if (contract != null) {


                try {
                    balance = contract.balanceOf(address).send();
                } catch (Exception e) {
                    Log.d("CONTRACT", e.getMessage());
                    //e.printStackTrace();
                }
            }
            mAct.runOnUiThread(() -> {
                BigDecimal bd = null;
                if (contract != null) {
                    bd = new BigDecimal(balance, 18);
                } else {
                    try {
                        if (deets.containsKey("walletXaddr")) {

                            bd = Utils.getMyXRPBalance(deets.get("walletAddress")).first;
                            //  bd = bd.setScale(6, BigDecimal.ROUND_HALF_DOWN);
                        } else {
                            bd = Utils.getMyBalance(address).first;
                            // get decimal places from definitions file
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                updateMe.setText(bd.stripTrailingZeros().toPlainString());
            });

        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.export_wallets:
                exportWallets();
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
        //
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.tokens_list) {
            MenuInflater inflater = mAct.getMenuInflater();
            inflater.inflate(R.menu.message_item_longclick, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info, info2;
 /*       MenuItem subItem;
        if(item.hasSubMenu()){
            SubMenu abc = item.getSubMenu();
            subItem = abc.getItem();
            info = (AdapterView.AdapterContextMenuInfo) subItem.getMenuInfo();
        } else {*/
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //}
        TextView textView;
        String bod;
        View li;
        //ClipData clip;
        //ClipboardManager cbm = (ClipboardManager) mAct.getSystemService(CLIPBOARD_SERVICE);;
        // int idxOfList = (info!=null) ? info.position : this.mParentContextMenuListIndex;
        switch(item.getItemId()) {
            case R.id.mcm_sendfunds:

                li = info.targetView;
                // add stuff here
                return true;
            case R.id.mcm_submenu:
                subInfo = info;
            case R.id.mcm_copyaddr: case R.id.mcm_copytext:

                Log.d("SUBMENU", "ID: "+item.getItemId());
                switch (item.getItemId()) {
                    /*case R.id.mcm_copytext:
                        Log.d("SUBMENU", "copytext");
                        li = ((View) subInfo.targetView);
                        textView = li.findViewById(R.id.inboxContent);
                        bod = textView.getText().toString();
                        clip = ClipData.newPlainText("Copied", bod);
                        cbm.setPrimaryClip(clip);
                        return true;
                    case R.id.mcm_copyaddr:
                        Log.d("SUBMENU", "copytext");
                        li = ((View) subInfo.targetView);
                        textView = li.findViewById(R.id.inboxAddress);
                        bod = textView.getText().toString();
                        clip = ClipData.newPlainText("Copied", bod);
                        cbm.setPrimaryClip(clip);
                        return true;*/
                    default:
                        return true;
                }
            default:
                return super.onContextItemSelected(item);
        }
    }

}
