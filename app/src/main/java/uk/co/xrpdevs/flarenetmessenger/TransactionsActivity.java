package uk.co.xrpdevs.flarenetmessenger;

import static android.content.Context.CLIPBOARD_SERVICE;
import static uk.co.xrpdevs.flarenetmessenger.MyService.xrplClient;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.primitives.UnsignedInteger;

import org.json.JSONException;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsResult;
import org.xrpl.xrpl4j.model.client.accounts.AccountTransactionsTransactionResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TransactionsActivity extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static long theID = 0;
    private static ObjectMapper mapper;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;

    MyRecyclerView mylist;
    HashMap<String, String> deets;
    FragmentManager manager;

    Activity mAct;
    RecyclerView rv;
    BottomNavigationView navView;
    TransactionsActivity mThis = this;
    public static String myAddress = "";
    static int thepos = 0;
    static ArrayList<HashMap<String, String>> poo = new ArrayList<HashMap<String, String>>();
    SwipeRefreshLayout mSwipeRefreshLayout;


    // SwipeRefreshLayout

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.activity_transactions, container, false);
        navView = mThis.getActivity().findViewById(R.id.nav_view);
        navView.getMenu().findItem(R.id.navigation_wallets).setChecked(true);
        manager = mThis.getActivity().getFragmentManager();

        myLog("TRANSACTIONS", "onCreateCalled");
        mSwipeRefreshLayout = root.findViewById(R.id.swipe_container);

        mSwipeRefreshLayout.setOnRefreshListener(this);
//        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        prefs = this.getActivity().getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        // BottomNavigationView navView = findViewById(R.id.nav_view);
        mAct = this.getActivity();
        if (!prefs.contains("walletCount")) {
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        } else {
            //  setContentView(R.layout.activity_transactions);
        }
        try {
            deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView tv = root.findViewById(R.id.textView4);
        rv = root.findViewById(R.id.rv1);
        getActionBar().setTitle("Transaction History");


        return root;
    }

    private ActionBar getActionBar() {
        return ((MainActivity) mThis.getActivity()).getSupportActionBar();
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        myLog("FRAG", "onStart");
        mAct = mThis.getActivity();

        if (args != null) {
            if (args.containsKey("wAddr")) {
                myAddress = args.getString("wAddr");
            }
        }
        try {
            update();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void update() throws JsonProcessingException, IllegalAccessException {
        if (prefs.contains("currentWallet") && deets != null) {
            try {
                deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (deets.containsKey("walletXaddr")) {

            } else {
                //c = org.web3j.crypto.Credentials.create(deets.get("walletPrvKey"));

            }

            myLog("DEETS", deets.toString());
            if (myAddress.equals("")) {
                myAddress = deets.get("walletAddress");
                Log.d("Address: ", myAddress);
            }
        }
        poo = getXRPtransactions(myAddress);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());

        rv.setLayoutManager(mLayoutManager);
        Log.d("OUTPUT", poo.toString());


        try {
            mylist = new MyRecyclerView(poo, myAddress, manager);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //   rv.setHasFixedSize(true);
        rv.setAdapter(mylist);
        //registerForContextMenu(rv);
        mylist.notifyDataSetChanged();


    }

    /*   public ArrayList<HashMap<String, String>> getETHtransactions(String Address) {
           return null;
       }

       public ArrayList<HashMap<String, String>> getERC20transactions(String Address, String contract) {
           return null;
       }*/
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerForContextMenu(view);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //menuInfo.toString();
        // inflate menu here
        ///PopupMenu popup = new PopupMenu(mAct, v);
        //MenuInflater inflater = popup.getMenuInflater();
        //inflater.inflate(R.menu.transactions_item_context, popup.getMenu());
        // popup.show();
        MenuInflater inflater = mAct.getMenuInflater();
        //PopupMenu pu
        inflater.inflate(R.menu.transactions_item_context, menu);
        // If you want the position of the item for which we're creating the context menu (perhaps to add a header or something):
        //int itemIndex = (ContextMenu.ContextMenuInfo ) menuInfo.
        //int itemIndex = ((ContextMenuRecyclerView.RecyclerViewContextMenuInfo) menuInfo).position;
        //Log.d("MENU", menuInfo.toString()+"");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        ContextMenuRecyclerView.RecyclerViewContextMenuInfo info = (ContextMenuRecyclerView.RecyclerViewContextMenuInfo) item.getMenuInfo();
        // handle menu here - get item index or ID from info
        //item.get
        //long id = info.id;

        //int pos = info.position;
        Log.d("MENU", item.toString() + " Position: " + thepos + " ID " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.tic_copyaddress:
                if (poo.get(thepos).containsKey("isrx")) {
                    Log.d("MENU: ", "Copied  sender's  Address: " + poo.get(thepos).get("account"));
                    ClipboardManager clipboard = (ClipboardManager) mAct.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied", poo.get(thepos).get("account"));
                    clipboard.setPrimaryClip(clip);
                } else {
                    Log.d("MENU: ", "Copied reciever's Address: " + poo.get(thepos).get("destination"));
                    ClipboardManager clipboard = (ClipboardManager) mAct.getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied", poo.get(thepos).get("destination"));
                    clipboard.setPrimaryClip(clip);

                }
                return super.onContextItemSelected(item);
            case R.id.tic_transactions:
                String wa;
                Log.d("poo debug: ", poo.get(thepos).toString());
                if (poo.get(thepos).containsKey("isrx")) {
                    wa = poo.get(thepos).get("o_acco");
                } else {
                    wa = poo.get(thepos).get("o_dest");
                }
                try {
                    poo = getXRPtransactions(wa);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                try {
                    mylist = new MyRecyclerView(poo, myAddress, manager);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                myAddress = wa;
                rv.setAdapter(mylist);
                mylist.notifyDataSetChanged();
                return super.onContextItemSelected(item);
            default:
                return super.onContextItemSelected(item);
        }


    }

    public ArrayList<HashMap<String, String>> getXRPtransactions(String address) throws JsonProcessingException, IllegalAccessException {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        ArrayList<String> jlist = new ArrayList<>();
        String ErrorMessage = "OK";
        BigDecimal XRP;
        AccountInfoRequestParams requestParams =
                AccountInfoRequestParams.of(Address.of(address));
        AccountInfoResult accountInfoResult;
        AccountTransactionsResult eek;
        mapper = new ObjectMapper();

        try {
            AccountTransactionsRequestParams bob = AccountTransactionsRequestParams.builder()
                    .account(Address.of(address))
                    .limit(UnsignedInteger.valueOf(200))
                    .build();


            eek = xrplClient.accountTransactions(bob);

            Iterator<AccountTransactionsTransactionResult<? extends Transaction>> itr = eek.transactions().iterator();



            // list.add(items);
            while (itr.hasNext()) {
                HashMap<String, String> items = new HashMap<>();
                AccountTransactionsTransactionResult tmp = itr.next();
                try {
                    //  Log.d("META", mapper.writeValueAsString(tmp.transaction().memos().get(0).memo().memoData().get()));
                    tmp.transaction().memos().size();
                    for (int z = 0; z < tmp.transaction().memos().size(); z++) {
                        Log.d("MEMOZ " + (z + 1), tmp.transaction().memos().get(z).memo().memoData().get());
                    }
                } catch (Exception e) {

                }
                HashMap mapl = toHashMap(tmp.transaction());
                list.add(mapl);
                String tjson = mapper.writeValueAsString(tmp);
                jlist.add(tjson);
                Log.d("JSON output: ", tjson);

                // HashMap<String, String> map =
                //        (HashMap<String, String>) Arrays.asList(tmp.split(",")).stream().map(s -> s.split(":")).collect(Collectors.toMap(e -> e[0], e -> e[1]));
                //itr.next(
                //items.put("fee", tmp.fee().toString());
                //    items.putAll(toMap(itr.next().transaction()));
                // i//tems.put("dest", tmp.
                //   Log.d("TRANS", tmp);
                // list.add(map);

                //   }

                // accountInfoResult = xrplClient.accountInfo(requestParams);
                //   BigInteger drops = new BigInteger(accountInfoResult.accountData().balance().toString());
                // XRP = new BigDecimal(drops, 6);
                //  } catch (JsonRpcClientErrorException | NullPointerException e) {
                //    XRP = new BigDecimal("-1");
                //      e.printStackTrace();
                //    }

            }
        } catch (JsonRpcClientErrorException e) {
            e.printStackTrace();
        }

        return list;
    }

    public HashMap<String, String> toHashMap(Object obj) throws JsonProcessingException, IllegalAccessException {
        HashMap<String, String> tmp = new HashMap<>();
        Map<String, String> map = parameters(obj);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            tmp.putAll(map);

            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        // ObjectMapper oMapper = new ObjectMapper();


        // object -> Map
        // Map map = oMapper.convertValue(obj, Map.class);
        // Map<String, String> oot = null;
        // Iterator it = map.entrySet().iterator();
        //  while (it.hasNext()) {
        //      Map.Entry pair = (Map.Entry)it.next();
        //      System.out.println(pair.getKey() + " = " + pair.getValue());
        //      Map.Entry vals = (Map.Entry) oMapper.convertValue(pair.getValue(), Map.Entry.class);
        //     vals.getValue();
        //      oot.put((String) pair.getKey(), (String) vals.getValue());
        //      it.remove(); // avoids a ConcurrentModificationException
        //   }

        //  System.out.println(map);
        return tmp;

    }

    public static Map<String, String> parameters(Object obj) throws IllegalAccessException, JsonProcessingException {
        Map<String, String> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            String n = "", f = "";
            try {
                n = field.getName();
                f = field.get(obj).toString();

//                map.put(field.getName(), field.get(obj).toString());
            } catch (Exception e) {
            }

            if (n.equals("fee") || n.equals("amount")) {
                BigDecimal bd = new BigDecimal(f).movePointLeft(6).stripTrailingZeros();
                Log.d("Bigint", bd + " dd");
                f = bd.toPlainString();
            }
            map.put(n, f);
            if (n.equals("memos")) {
                f = mapper.writeValueAsString(field.get(obj));
                map.put("_" + n, f);
            }
        }
        return map;
    }


    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                //    if(mSwipeRefreshLayout != null) {
                //      mSwipeRefreshLayout.setRefreshing(true);
                //   }
                try {
                    poo = getXRPtransactions(myAddress);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                try {
                    mylist = new MyRecyclerView(poo, myAddress, manager);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // myAddress = wa;
                rv.setAdapter(mylist);
                mylist.notifyDataSetChanged();
                // TODO Fetching data from server
                Log.d("REFRESH", "SSSSS");
            }
        });

        //  Log.d("REFRESH", "SSSSS");
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
