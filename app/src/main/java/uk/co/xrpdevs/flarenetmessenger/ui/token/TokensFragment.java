package uk.co.xrpdevs.flarenetmessenger.ui.token;

import static uk.co.xrpdevs.flarenetmessenger.Utils.jsonToMap;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.util.Pair;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.FlareNetMessenger;
import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.MyService;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.Utils;
import uk.co.xrpdevs.flarenetmessenger.contracts.ERC20;
import uk.co.xrpdevs.flarenetmessenger.contracts.WNat;
import uk.co.xrpdevs.flarenetmessenger.ui.contacts.ContactsFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PinCodeDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.WrapUnWrapDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.transactions.TransactionsFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.wallets.NotificationsViewModel;

/* TODO:
    Add token dialog. Detect chain wallet is on and offer up lists from resources.
    Tokens list saved as a SharedPreferences item.
    When adding custom tokens, automatically detect coin name, symbol, level of precision.
 */

public class TokensFragment extends Fragment implements WrapUnWrapDialogFragment.OnResultListener, SwipeRefreshLayout.OnRefreshListener {
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
    WrapUnWrapDialogFragment wuw;
    PleaseWaitDialog pwd;
    SwipeRefreshLayout mSwipeRefreshLayout;


    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_tokens, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        //try {
        //    deets = Utils.getPkey(mThis.getActivity(), prefs.getInt("currentWallet", 0));
        //} catch (JSONException e) {
        //    e.printStackTrace();
        //}
        deets = FlareNetMessenger.deets;
        //try {
        //    tokens = jsonToMap(deets.getOrDefault("walletTokens", ""));
        //} catch (JSONException e) {
        //    tokens = null;
        //}
        mAct = this.getActivity();
        if (prefs.getInt("walletCount", 0) > 0) {
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
        mSwipeRefreshLayout = root.findViewById(R.id.swipe_container_tokens);


        mSwipeRefreshLayout.setOnRefreshListener(this);
//        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
        mSwipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        return root;
    }

    private ActionBar getActionBar() {
        return ((MainActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setTitle("Assets (" + deets.getOrDefault("NAME", "Wallet " + prefs.getInt("currentWallet", 0)) + ")");
        getActionBar().setIcon(R.mipmap.chain_flare);
        Bundle args = getArguments();
        myLog("FRAG", "onStart");
    }

    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        myLog("Lines", lines.toString());
        String bcname = deets.get("BCNAME");
        simpleAdapter = new SimpleAdapter(
                mThis.getActivity(), lines, R.layout.listitem_tokens,
                new String[]{"NAME", "ADDRESS", "NAME", "LEDGER_BALANCE"},
                new int[]{R.id.inboxAddress, R.id.inboxContent, R.id.inboxType, R.id.inboxLastact}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView cName = view.findViewById(R.id.inboxContent);
                TextView cType = view.findViewById(R.id.inboxType);

                cType.setText(bcname);
                String cNtext = cName.getText().toString();
                @SuppressWarnings("all") // we know its a hashmap....
                HashMap<String, String> item = (HashMap<String, String>) getItem(position);

                Thread updateBalance;
                if (!item.containsKey("primary")) {
                    Log.d("abcdef", deets.get("RPC"));
                    updateBalance = new getERC20Balance(
                            MyService.getERC20link(
                                    item.get("Address"),
                                    Credentials.create(FlareNetMessenger.deets.get("PRIVKEY")),
                                    deets.get("RPC"),
                                    deets.get("CHAINID")),
                            deets.get("ADDRESS"),
                            cName, position);
                } else {
                    updateBalance = new getERC20Balance(
                            null,
                            deets.get("ADDRESS"),
                            cName, position);
                }
                updateBalance.start();
                //  int unread = lines.size();
                return view;
            }
        };

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> theItem = lines.get(position);
                FragmentManager fm = getFragmentManager();
                assert fm != null;
                Fragment currentFragment = fm.findFragmentById(R.id.nav_host_fragment);
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                assert currentFragment != null;
                fragmentTransaction.remove(currentFragment);
                ContactsFragment f = new ContactsFragment();
                Bundle args = new Bundle();
                myLog("item", theItem.toString());
                args.putInt("ltype", 4000); // send funds to contact
                if (!theItem.containsKey("primary")) { // indicate that we're dealing with an ETH style contract
                    args.putString("token", theItem.get("Name"));
                    args.putString("tAddr", theItem.get("Address")); // the Address of the smart contract for the token
                }
                f.setArguments(args);
                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("contacts").commit();

            }
        });

        return simpleAdapter;

    }

    public ArrayList<HashMap<String, String>> getAvailTokens(String blockChainName) throws IOException {
        ArrayList<HashMap<String, String>> availTokens = new ArrayList<>();
        HashMap<String, String> primaryAsset = new HashMap<>();


        // get asset type from wallets info
        if (deets.get("TYPE").equals("XRPL")) {
            primaryAsset.put("NAME", "XRP");
            primaryAsset.put("TYPE", "XRPL");
            primaryAsset.put("ADDRESS", Utils.getMyXRPBalance(deets.get("ADDRESS"), deets.get("RPC")).first.toPlainString());
        } else {
            myLog("WBALANCE", deets.toString());
            primaryAsset.put("NAME", deets.get("TOKNAME"));
            primaryAsset.put("Address", Utils.getMyBalance(deets.get("ADDRESS")).first.toPlainString());
        }
        primaryAsset.put("primary", "1");
        availTokens.add(primaryAsset);
        String json = null;
        //pain in the arse logic statement.. roll on simple SQL lookups!
        if (!deets.get("TYPE").equals("XRPL")) {

            try { // much simpler with SQL.
                InputStream is = getActivity().getAssets().open("tokens_" + blockChainName + ".json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                json = new String(buffer, StandardCharsets.UTF_8);
                //myLog("JSON", "== "+json);
                JSONObject jo = new JSONObject(json);
                JSONArray key = jo.names();
                for (int i = 0; i < key.length(); ++i) {
                    JSONObject obj = jo.getJSONObject(key.getString(i));
                    HashMap<String, String> tmp = jsonToMap(obj.toString());
                    tmp.put("NAME", key.getString(i));
                    availTokens.add(tmp);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            myLog("DEBUG", "We are XRPL2!" + prefs.getString("csbc_type", ""));
        }

        //myLog("json", availTokens.toString());

        return availTokens;
    }

    public void getTokenList() throws IOException {
        feedList.clear();
        feedList = getAvailTokens("coston");
        mThis.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TokensAdaptor = fillListView(feedList);
                lv.setAdapter(TokensAdaptor);

            }
        });
        //TokensAdaptor = fillListView(feedList);
    }

    public String getDate(Long ts) {
        Date df = new Date(ts * 1000);
        String rc = new SimpleDateFormat("dd MMM yy").format(df);
        return (rc);
    }

    @Override
    public void onResult(String humanAmount, BigInteger inWei, String percentage, String tag) throws IOException, JSONException {
        Log.d("WRAPUNWRAP", humanAmount);
        wuw.dismiss();
        // create another dialog whilst waiting for wrap/unwrap completion
        pwd = new PleaseWaitDialog();
        pwd.prompt = "Waiting a few seconds for confirmation..";
        pwd.cancelable = false;
        pwd.titleText = tag;
        pwd.show(mAct.getFragmentManager(), "pwd");
        final TextView[] updateText = new TextView[1];
        if (tag.equals("Wrap SGB")) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() { // delay 200ms to wait for FragmentDialog to be init'd
                @Override
                public void run() {
                    String result = wrapSGB("0x02f0826ef6aD107Cfc861152B32B52fD11BaB9ED", inWei);
                    updateText[0] = pwd.getDialog().findViewById(R.id.textview_pwd);
                    updateText[0].setText(result);
                    pwd.setCancelable(true);
                    try {
                        getTokenList();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 200);
        }

        if (tag.equals("Unwrap SGB")) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() { // delay 200ms to wait for FragmentDialog to be init'd
                @Override
                public void run() {
                    String result = UnWrapSGB("0x02f0826ef6aD107Cfc861152B32B52fD11BaB9ED", inWei);
                    updateText[0] = pwd.getDialog().findViewById(R.id.textview_pwd);
                    updateText[0].setText(result);
                    pwd.setCancelable(true);
                    try {
                        getTokenList();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }, 200);
        }
    }

    class getERC20Balance extends Thread {
        private final int position;

        public getERC20Balance(ERC20 contract, String address, TextView updateMe, int _position) {
            this.contract = contract;
            this.address = address;
            this.updateMe = updateMe;
            this.position = _position;
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
                    myLog("CONTRACT", e.getMessage());
                }
            }
            mAct.runOnUiThread(this::run2);
        }

        private void run2() {
            BigDecimal bd = null;
            if (contract != null) {
                bd = new BigDecimal(balance, 18);
                updateMe.setText(bd.stripTrailingZeros().toPlainString());
                feedList.get(position).put("_balance", bd.stripTrailingZeros().toPlainString());
            } else {
                Pair<BigDecimal, String> out;
                try {
                    if (deets.get("TYPE").equals("XRPL")) {
                        out = Utils.getMyXRPBalance(deets.get("ADDRESS"), deets.get("RPC"));
                        bd = out.first;
                        //bd = Utils.getMyXRPBalance(deets.get("ADDRESS")).first;
                    } else {
                        out = Utils.getMyBalance(address);
                        bd = out.first;
                        // get decimal places from definitions file
                    }
                    if (bd.equals(new BigDecimal("-1"))) {
                        updateMe.setText("Error:" + out.second);
                        feedList.get(position).put("_balance", "0");
                    } else {
                        updateMe.setText(bd.stripTrailingZeros().toPlainString());
                        feedList.get(position).put("_balance", bd.stripTrailingZeros().toPlainString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            //updateMe.setText(bd.stripTrailingZeros().toPlainString());
        }
    }

    @Override // THIS IS FOR THE MENU AT TOP OF ACTIVITY/FRAGMENT
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

    public void exportWallets() {
        //
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.tokens_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            HashMap<String, String> theItem = feedList.get(info.position);
            String fCoin = "0x3AEcBA5b8e701ab9DF8E0812467C61479b25eE8A";

            String wsgb = "0x02f0826ef6aD107Cfc861152B32B52fD11BaB9ED";
            if (theItem.get("Address").equalsIgnoreCase(wsgb)) {
                menu.add("Unwrap");
                menu.add("Delegate");
            }
            if (theItem.get("NAME").equals("SGB") && theItem.get("primary").equals("1")) {
                menu.add(R.string.wrap_sgb);
            }
            MenuInflater inflater = mAct.getMenuInflater();
            inflater.inflate(R.menu.assets_item_context, menu);
        }
    }

    @Override // T
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info, info2;
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        TextView textView;
        String bod;
        // View li;

        // if is WSGB


        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View li = info.targetView;
        HashMap<String, String> theItem = feedList.get(info.position);
        // Handle item selection

        Log.d("THEITEM", theItem.toString());


        TextView cBody = li.findViewById(R.id.inboxContent);
        myLog("MITEM", item.toString() + " ");
        switch (item.getItemId()) {
            case R.id.aic_updatebalance:
                String addr = theItem.get("ADDRESS");
                BigInteger bal = new BigInteger("0");
                Thread updateBalance;
                try {
                    if (theItem.containsKey("primary")) {
                        // bal = new BigInteger("100");
                        updateBalance = new getERC20Balance(
                                null,
                                deets.get("ADDRESS"),
                                cBody, info.position);
                    } else {
                        updateBalance = new getERC20Balance(
                                MyService.getERC20link(
                                        theItem.get("ADDRESS"),
                                        MyService.c,
                                        MyService.rpc),
                                deets.get("ADDRESS"),
                                cBody, info.position);

                    }
                    updateBalance.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.aic_sendfunds:
                li = info.targetView;
                // add stuff here
                return true;
            case R.id.aic_transactions:
                // open the "Transactions" activity.
                // Todo: keep track of balances of primary assets and tokens
                //      these can be periodically updated by the service, and a
                //      notification can be generated if amounts have changed.
                // Todo: Activity will show transactions only for the currently
                //      selected blockchain. This is a resource intensive process
                //      so makes sense to keep context relevant. General updates
                //      can be managed by the previous entry, with a link to the
                //      transactions activity from the notifications.
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//                fragmentTransaction.setCustomAnimations(R.animator.
//                        R.anim.slide_in,  // enter
//                        R.anim.slide_out // exi
                //fragmentTransaction.remove(currentFragment);
                Fragment f = new TransactionsFragment();
                Bundle args = new Bundle();
                args.putString("wAddr", deets.get("ADDRESS"));
                args.putString("wBcid", deets.get("BCID"));
                // args.putString("selectFragment", "home");
                f.setArguments(args);


                fragmentTransaction.replace(R.id.nav_host_fragment, f);
                fragmentTransaction.addToBackStack("tokens").commit();
                //------------------

                //   Bundle b = new Bundle(); // store info for transaction activity
                ///   Intent i = new Intent(this.getContext(), TransactionsActivity.class);
                ///   i.putExtra("data", b);
                ///   startActivity(i);
                ///   //    subInfo = info;
                return true;

            default:
                // deal with wrap/unwrap/delegate here
                if (item.toString().equals(getString(R.string.wrap_sgb))) {
                    android.app.FragmentManager manager = mThis.getActivity().getFragmentManager();
                    wuw =
                            new WrapUnWrapDialogFragment().newInstance(this,
                                    "Wrap How Much?",
                                    "Wrap SGB",
                                    theItem.get("Address"),
                                    true);

                    wuw.show(mAct.getFragmentManager(), "wrapsgb");
                }
                if (item.toString().equals("Unwrap")) {
                    android.app.FragmentManager manager = mThis.getActivity().getFragmentManager();
                    wuw =
                            new WrapUnWrapDialogFragment().newInstance(this,
                                    "UnWrap How Much?",
                                    "Unwrap SGB",
                                    feedList.get(info.position).get("_balance"),
                                    false);

                    wuw.show(mAct.getFragmentManager(), "wrapsgb");
                }

                Log.d("MENUX", item.toString());
                return super.onContextItemSelected(item);
        }
    }

    private BigInteger getNetworkGasPrice(Web3j mWeb3j) {
        BigInteger gasPrice = BigInteger.ONE;
        try {
            Request<?, EthGasPrice> rs = mWeb3j.ethGasPrice();
            EthGasPrice eGasPrice = rs.sendAsync().get();
            gasPrice = eGasPrice.getGasPrice();
        } catch (Exception e) {
            System.out.println("" + e);
        }
        return gasPrice;
    }

    private String wrapSGB(String cAddr, BigInteger amount) {
        TransactionReceipt transactionReceipt;
        WNat wsgb = MyService.getWNatlink(cAddr, MyService.c, deets.get("RPC"));
        try {
            Web3j web3j = Web3j.build(
                    new HttpService(deets.get("RPC")));
            web3j.ethChainId().setId(Integer.parseInt(deets.get("CHAINID")));
            TransactionManager txManager = new RawTransactionManager(web3j, MyService.c, Integer.decode(deets.get("CHAINID")));
            EthSendTransaction transactionResponse = txManager.sendTransaction(
                    getNetworkGasPrice(web3j),
                    new BigInteger("8000000"),
                    cAddr,
                    wsgb.deposit().encodeFunctionCall(),
                    amount);
            String txHash = transactionResponse.getTransactionHash();
            myLog("TXHASH1", txHash);
            if (transactionResponse.hasError()) {
                myLog("TXHASH1", transactionResponse.getError().getMessage());
                myLog("TXHASH1", transactionResponse.getError().getData());
                return (transactionResponse.getError().getMessage());
            }
            TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(
                    web3j,
                    TransactionManager.DEFAULT_POLLING_FREQUENCY,
                    TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);
            transactionReceipt = receiptProcessor.waitForTransactionReceipt(txHash);
            String TAG = "RECEIPT";
            if (!transactionReceipt.isStatusOK()) {
                myLog(TAG, "transactionReceipt: Error: " + transactionReceipt.getStatus());
                return (transactionReceipt.getStatus());
            } else {
                BigDecimal _amount = new BigDecimal(transactionReceipt.getGasUsed(), 18);
                String disp = _amount.setScale(14, RoundingMode.FLOOR).stripTrailingZeros().toPlainString();
                return ("Success!\nTx Hash: " +
                        transactionReceipt.getTransactionHash() +
                        "\nGas: " +
                        disp + " SGB");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return (e.getMessage());
        }
    }

    private String UnWrapSGB(String cAddr, BigInteger amount) {
        TransactionReceipt transactionReceipt;
        WNat wsgb = MyService.getWNatlink(cAddr, MyService.c, deets.get("RPC"));
        try {
            Web3j web3j = Web3j.build(
                    new HttpService(deets.get("RPC")));
            web3j.ethChainId().setId(Integer.parseInt(deets.get("CHAINID")));
            TransactionManager txManager = new RawTransactionManager(web3j, MyService.c, Integer.decode(deets.get("CHAINID")));
            EthSendTransaction transactionResponse = txManager.sendTransaction(
                    getNetworkGasPrice(web3j),
                    new BigInteger("8000000"),
                    cAddr,
                    wsgb.withdraw(amount).encodeFunctionCall(),
                    BigInteger.ZERO);
            String txHash = transactionResponse.getTransactionHash();
            myLog("TXHASH1", txHash);
            if (transactionResponse.hasError()) {
                myLog("TXHASH1", transactionResponse.getError().getMessage());
                myLog("TXHASH1", transactionResponse.getError().getData());
                return (transactionResponse.getError().getMessage());
            }
            TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(
                    web3j,
                    TransactionManager.DEFAULT_POLLING_FREQUENCY,
                    TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);
            transactionReceipt = receiptProcessor.waitForTransactionReceipt(txHash);
            String TAG = "RECEIPT";
            if (!transactionReceipt.isStatusOK()) {
                myLog(TAG, "transactionReceipt: Error: " + transactionReceipt.getStatus());
                return (transactionReceipt.getStatus());
            } else {
                BigDecimal _amount = new BigDecimal(transactionReceipt.getGasUsed(), 18);
                String disp = _amount.setScale(14, RoundingMode.FLOOR).stripTrailingZeros().toPlainString();
                return ("Success!\nTx Hash: " +
                        transactionReceipt.getTransactionHash() +
                        "\nGas: " +
                        disp + " SGB");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return (e.getMessage());
        }
    }

    private String DelegateSGB(String cAddr, BigInteger amount, String delegationAddress) {
        TransactionReceipt transactionReceipt;
        WNat wsgb = MyService.getWNatlink(cAddr, MyService.c, deets.get("RPC"));
        try {
            Web3j web3j = Web3j.build(
                    new HttpService(deets.get("RPC")));
            web3j.ethChainId().setId(Integer.parseInt(deets.get("CHAINID")));
            TransactionManager txManager = new RawTransactionManager(web3j, MyService.c, Integer.decode(deets.get("CHAINID")));
            EthSendTransaction transactionResponse = txManager.sendTransaction(
                    getNetworkGasPrice(web3j),
                    new BigInteger("8000000"),
                    cAddr,
                    wsgb.delegateExplicit(delegationAddress, amount).encodeFunctionCall(),
                    BigInteger.ZERO);
            String txHash = transactionResponse.getTransactionHash();
            myLog("TXHASH1", txHash);
            if (transactionResponse.hasError()) {
                myLog("TXHASH1", transactionResponse.getError().getMessage());
                myLog("TXHASH1", transactionResponse.getError().getData());
                return (transactionResponse.getError().getMessage());
            }
            TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(
                    web3j,
                    TransactionManager.DEFAULT_POLLING_FREQUENCY,
                    TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);
            transactionReceipt = receiptProcessor.waitForTransactionReceipt(txHash);
            String TAG = "RECEIPT";
            if (!transactionReceipt.isStatusOK()) {
                myLog(TAG, "transactionReceipt: Error: " + transactionReceipt.getStatus());
                return (transactionReceipt.getStatus());
            } else {
                BigDecimal _amount = new BigDecimal(transactionReceipt.getGasUsed(), 18);
                String disp = _amount.setScale(14, RoundingMode.FLOOR).stripTrailingZeros().toPlainString();
                return ("Success!\nTx Hash: " +
                        transactionReceipt.getTransactionHash() +
                        "\nGas: " +
                        disp + " SGB");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return (e.getMessage());
        }
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
                    getTokenList();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        TokensAdaptor.notifyDataSetChanged();
                        simpleAdapter.notifyDataSetChanged();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        //  Log.d("REFRESH", "SSSSS");

    }

}
