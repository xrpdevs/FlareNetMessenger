package uk.co.xrpdevs.flarenetmessenger.ui.wallets;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.ContactList;
import uk.co.xrpdevs.flarenetmessenger.FirstRun;
import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.PKeyScanner;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Smstest3;
import uk.co.xrpdevs.flarenetmessenger.Utils;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class WalletsFragment extends Fragment {

    public SimpleAdapter InboxAdapter;
    public SimpleAdapter simpleAdapter;
    Smstest3 contract;
    BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    public ArrayList<HashMap<String, String>> feedList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> deets;
    private NotificationsViewModel notificationsViewModel;
    WalletsFragment mThis = this;
    ListView lv;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_wallets, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        myLog("FRAG", "WalletsFragment");
        setHasOptionsMenu(true);
        View root = inflater.inflate(R.layout.fragment_wallets, container, false);
        lv = root.findViewById(R.id.wallets_list);
       // myLog("LV", lv.getParent().getParent().getParent().getParent().getClass().getName());
        lv.setAdapter(InboxAdapter);
   /*     notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        final TextView textView = root.findViewById(R.id.text_notifications);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
*/
        prefs = this.getActivity().getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
      //  super.onCreate(savedInstanceState);


        if(prefs.getInt("walletCount", 0) > 0 ) {
            myLog("FRAG", "Wallet count is non zero");

            readTheFile2();
        }

       FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override // TODO: change intent methods to Fragment context switches
            public void onClick(View view) {
                Intent intent = new Intent(mThis.getActivity(), PKeyScanner.class);
                startActivity(intent);
               // try {
               //     contract.clearInbox().send();
              //  } catch (Exception e) {
             //       e.printStackTrace();
              //  }
            }
        });

        return root;
    }

    public SimpleAdapter fillListView(final ArrayList lines) {
myLog("Lines", lines.toString());
        simpleAdapter = new SimpleAdapter(mThis.getActivity(), lines, R.layout.listitem_wallets, new String[]{"walletName", "walletAddress", "type", "lastval"}, new int[]{R.id.inboxName, R.id.inboxAddress, R.id.inboxType, R.id.inboxLastact}){
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = (TextView) view.findViewById(R.id.inboxName);
                TextView cType = (TextView) view.findViewById(R.id.inboxType);
                cType.setText("Coston");
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                        HashMap<String, String> item = (HashMap<String, String>) getItem(position);
                //myLog("DNSJNI", "item: "+item.toString());
               // int unread = inboxSize();
                // int unread = 0;
                int unread = lines.size();
                myLog("TEST", "Number of contaxts: "+unread);
               // if(unread>0) {
             //       cNtext += " (" + String.valueOf(unread) + ")";
               //     cName.setText(cNtext);
               //     view.invalidate();
             //   }
                return view;
            }
        };

     //   ListView lv = (ListView) this.getActivity().findViewById(R.id.inbox_list);
       // lv.setAdapter(InboxAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(mThis.getActivity(),
                        MainActivity.class);
                HashMap<String, String> theItem = (HashMap<String, String>) lines.get(position);
                String pooo = theItem.get("num");
                myLog("smscseeker", "name:" + theItem.toString());
                pEdit.putInt("currentWallet", (position +1 ));
                pEdit.commit();
                //dumper(theItem);
                //   if (session.loggedin) {
                //                  i.putExtra("sid", 0);
                //    }
//                i.putExtra("name", pooo.toString());
                startActivity(i);

            }
        });

        return simpleAdapter;

    }


    public void readTheFile2() {
        feedList.clear();

        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
        try {

            myLog("TEST", "Number of wallets: "+prefs.getInt("walletCount", 0));

            for(int i=0;i<prefs.getInt("walletCount", 0);i++){
                HashMap<String, String> map = Utils.getPkey(this.getContext(), (i+1));
                if(!map.containsKey("walletName")){ map.put("walletName", "Wallet "+String.valueOf(i+1));}

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

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
}
