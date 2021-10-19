package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.MyService.xrplClient;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.primitives.UnsignedInteger;

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

public class TransactionsActivity extends Fragment {
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;

    MyRecyclerView mylist;

    FragmentManager manager;
    BottomNavigationView navView;
    TransactionsActivity mThis = this;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.activity_transactions, container, false);
        navView = mThis.getActivity().findViewById(R.id.nav_view);
        navView.getMenu().findItem(R.id.navigation_wallets).setChecked(true);
        manager = mThis.getActivity().getFragmentManager();

        myLog("TRANSACTIONS", "onCreateCalled");

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        prefs = this.getActivity().getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        // BottomNavigationView navView = findViewById(R.id.nav_view);

        if (!prefs.contains("walletCount")) {
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        } else {
            //  setContentView(R.layout.activity_transactions);
        }
        TextView tv = root.findViewById(R.id.textView4);
        RecyclerView rv = root.findViewById(R.id.rv1);
        getActionBar().setTitle("Transaction History");
        String myAddress = "rJCdNPsfemPexCzCeZbr7Jin83LtJmpmNn";
        ArrayList<HashMap<String, String>> poo = getXRPtransactions(myAddress);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());

        rv.setLayoutManager(mLayoutManager);
        Log.d("OUTPUT", poo.toString());

        String[] data = new String[]{"My name", "is geoffery", "and i live in", "a tree", "I sell condoms", "for 59 pee"};
        mylist = new MyRecyclerView(poo, myAddress, manager);
        //   rv.setHasFixedSize(true);

        rv.setAdapter(mylist);
        mylist.notifyDataSetChanged();
        Log.d("pooo", mylist.getItemCount() + "_");

        return root;
    }

    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }

    public ArrayList<HashMap<String, String>> getXRPtransactions(String address) {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        String ErrorMessage = "OK";
        BigDecimal XRP;
        AccountInfoRequestParams requestParams =
                AccountInfoRequestParams.of(Address.of(address));
        AccountInfoResult accountInfoResult;
        AccountTransactionsResult eek;


        try {
            AccountTransactionsRequestParams bob = AccountTransactionsRequestParams.builder()
                    .account(Address.of(address))
                    .limit(UnsignedInteger.valueOf(200))
                    .build();


            eek = xrplClient.accountTransactions(bob);

            Iterator<AccountTransactionsTransactionResult<? extends Transaction>> itr = eek.transactions().iterator();

            String tmp;

            // list.add(items);
            while (itr.hasNext()) {
                HashMap<String, String> items = new HashMap<>();
                HashMap mapl = toHashMap(itr.next().transaction());
                list.add(mapl);

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

    public HashMap<String, String> toHashMap(Object obj) {
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

    public static Map<String, String> parameters(Object obj) {
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

        }
        return map;
    }


}
