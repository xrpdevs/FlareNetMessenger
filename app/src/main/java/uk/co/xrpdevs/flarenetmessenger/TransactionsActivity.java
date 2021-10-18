package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.MyService.xrplClient;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

public class TransactionsActivity extends AppCompatActivity {
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    private Object Payment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (!Utils.isMyServiceRunning(MyService.class, this)) {
            try {
                Intent serviceIntent = new Intent(this, MyService.class);
                startService(serviceIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        myLog("TRANSACTIONS", "onCreateCalled");

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        BottomNavigationView navView = findViewById(R.id.nav_view);

        if (!prefs.contains("walletCount")) {
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        } else {
            setContentView(R.layout.activity_transactions);
        }
        TextView tv = findViewById(R.id.textView4);
        setTitle("Transaction History");
        ArrayList<HashMap<String, String>> poo = getXRPtransactions("rJCdNPsfemPexCzCeZbr7Jin83LtJmpmNn");

        Log.d("OUTPUT", poo.toString());
        tv.setText(poo.toString());


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
                    .limit(UnsignedInteger.valueOf(20))
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
            try {
                map.put(field.getName(), field.get(obj).toString());
            } catch (Exception e) {
            }
        }
        return map;
    }
}
