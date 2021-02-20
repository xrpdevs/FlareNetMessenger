package uk.co.xrpdevs.flarenetmessenger;


import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import org.json.JSONException;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java9.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    // NOTE: The credentials here are from the testnet.. this is also VERY hacky at the moment!
    // NOTE: Code quality can, and will, improve!

    public Web3j FlareConnection;
    private Object TextView;
    TextView myTV;
    EthBlockNumber bob;
    Button refresh;
    Button sendMsg;
    Button settings;
    TextView myBalance;
    EditText message;
    public String walletAddress = null;
    public String contractAddress = null;
    public String walletPrivateKey = null;
    public String walletPubKey = null;
    Credentials c;
    ContractGasProvider cgp;
    TransactionReceipt receipt;
    Spinner addresses;
    Button inbox;
    private Intent serviceIntent;
    Smstest3 contract;
    BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    HashMap<String, String> deets;
    Context mC = this;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TEST", String.valueOf(requestCode));
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case 100:
                    Cursor cursor = null;
                    try {
                        String phoneNo = null;
                        String name = null;
                        Uri uri = data.getData();
                        cursor = getContentResolver().query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        phoneNo = cursor.getString(phoneIndex);
                        Log.d("TEST", phoneNo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.select:
               // TypedValue outValue = new TypedValue();
               // getBaseContext().getTheme().resolveAttribute(R.style.popupOverlay, outValue, true);
               // int theme = outValue.resourceId;
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final View myView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.contacts_dialog, (ViewGroup) findViewById(android.R.id.content), false);
                final EditText input = (EditText) myView.findViewById(R.id.EditText01);
                builder.setTitle("Choose Contact");
                int PICK_CONTACT = 100;
                builder.setMessage("Enter destination or select from contacts");
                builder.setView(myView);
                builder.setNegativeButton("Contacts", new DialogInterface.OnClickListener() {


                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       Intent intent = new Intent(getApplicationContext(), ContactList.class);
                        intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                        intent.putExtra("lType", 1000);
                        startActivity(intent);
                    }
                });
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent(getApplicationContext(),
                                MainActivity.class);

                        String out = input.getText().toString();
                        i.putExtra("name", out);
                        startActivity(i);


                    }
                });
                final AlertDialog alert = builder.create();

                alert.show();
                input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                });
                input.requestFocus();
               // newGame();
                return true;
            case R.id.add_existing:
                Intent intent = new Intent(getApplicationContext(), ContactList.class);
                //intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                intent.putExtra("lType", 2000);
                startActivity(intent);


            //    showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
      //  EasyLock.checkPassword(this);
        if(!prefs.contains("pinCode")) {
        }
        if(!prefs.contains("walletCount")){
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        }

      //  final String s = ContactsManager.deleteAllAppContacts(this);
//        Log.d("TEST", "output "+s);

        //addNewAccount(AccountGeneral.ACCOUNT_TYPE, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS);
        //ContactsManager.updateMyContact(this, "Jamie Prince");
      //  ContactsManager.addContact(this, new MyContact("Jamie", "Prince", "0x3495230492345092387"));
	//			if (serviceIntent == null)
	//				serviceIntent = new Intent(this, ContactUpdateService.class);
		//		stopService(serviceIntent);
	//			startService(serviceIntent);

        FlareConnection = MyService.initWeb3j();
        bob = getBlockNumber();
        Log.d("TEST", bob.getBlockNumber().toString());

        myBalance = findViewById(R.id.balance);
        myTV = findViewById(R.id.text1);
        message = findViewById(R.id.message);
        try {
            deets = Utils.getPkey(this, prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

     //   Log.d("TEST", deets.toString());


      //  walletAddress    = MyService.wallet; // "0x24423475227b49376d72E863bB6c5b6cB4E60Cea";
      //  walletPrivateKey = MyService.walletPkey; //"0xc6d66f9d9cd4c607e742d27c5fb9a9140465226c19578c02a13931f1fd0c8ef2";
        //contractAddress  = "0xa49D5f1f6e63406E9dd6BF6BbAC5A9ac085527e7";

        contractAddress = "0x4a1400220373983f3716D4e899059Dda418Fd08A"; // SMSTest2 v1
        //contractAddress = "0xFC9c505590D29E2400b1fc6243A019435e24FD40"; // SmsTest2 v2
        contractAddress = MyService.contractAddress;
        String contractPkey    = "";

        //button definitions
        inbox = findViewById(R.id.button3);
        refresh = findViewById(R.id.button);
        sendMsg = findViewById(R.id.button2);
        settings = findViewById(R.id.Settings);

        addresses = (Spinner) findViewById(R.id.spinner);
        cgp = new DefaultGasProvider();



        if(deets != null) {
            Log.d("TEST", deets.get("walletAddress"));

            //   c = FlareConnection.getCredentialsFromPrivateKey("private-key");
            //   cgp = FlareConnection.ethGasPrice();

        }

        refresh.setOnClickListener(new View.OnClickListener() {
                                       public void onClick(View v) {
                                           try {
                                               new update().start();
                                           } catch (Exception e) {
                                               e.printStackTrace();
                                           }
                                       }
                                   }
        );
        inbox.setOnClickListener(new View.OnClickListener() {
                                       public void onClick(View v) {
                                           try {
                                               Intent intent = new Intent(MainActivity.this, Inbox.class);
                                               startActivity(intent);
                                           } catch (Exception e) {
                                               e.printStackTrace();
                                           }
                                       }
                                   }
        );
        settings.setOnClickListener(new View.OnClickListener() {
                                     public void onClick(View v) {
                                         try {
//                                             Intent intent = new Intent(MainActivity.this, PKeyScanner.class);
                                             Intent intent = new Intent(MainActivity.this, Wallets.class);
                                             startActivity(intent);
                                         } catch (Exception e) {
                                             e.printStackTrace();
                                         }
                                     }
                                 }
        );
        sendMsg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    new sendMessage().start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if(deets != null) {
            try {
                new update().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
    //        Log.d("TEST", "FXRP Balance of Flare Testnet Address " + deets.get("walletAddress") + " = " + getMyBalance(deets.get("walletAddress")).toString());
            //   FlareConnection.
        }
    }

    class sendMessage extends Thread {

        @Override
        public void run() {
            Log.d("TEST", "Running SendMSG thread");
            String rawString = message.getText().toString();
            byte[] bytes = rawString.getBytes(StandardCharsets.UTF_8);

            String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
            try {
                receipt = contract.sendMessage(addresses.getSelectedItem().toString(), utf8EncodedString).send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String text;
            if(receipt.isStatusOK()){
                text = "Message sent!\n"+receipt.getGasUsed().toString();
            } else {
                text = "Message sending failed";
            }

            runOnUiThread(new Runnable() {

                @Override
                public void run(){
                    showToast(text.toString());
                    try {
                        new update().start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                };
            });
        }
    }


    public void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = AccountManager.get(this).addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.i("info" , "Account was created");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    public static byte[] stringToBytes32(String string) {
        byte[] byteValue = string.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return byteValueLen32;
    }

    public HashMap<String, String> getPkey() {
        String pKey;
        HashMap<String, String> bob = new HashMap<String, String>();
        Pair<String, String> tmp;
        bob.put("walletPrvKey", prefs.getString("walletPrvKey", null));
        bob.put("walletPubKey", prefs.getString("walletPubKey", null));
        bob.put("walletAddress", prefs.getString("walletAddress", null));
        return bob;
    }

    @Override
    public void onResume() {

        super.onResume();

        if(prefs.getInt("walletCount", 0) > 0) {
            try {
                deets = Utils.getPkey(this, prefs.getInt("currentWallet", 0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            c = Credentials.create(deets.get("walletPrvKey"));
            contract = Smstest3.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT);
            try {
                new update().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class update extends Thread {
        String msgCount;
        Tuple2<String, List<String>> registeredAddresses;
        Tuple2<BigInteger, BigInteger> messageCount;
        String myTvString;
        String myBalanceS;

        @Override
        public void run() {
            c = Credentials.create(deets.get("walletPrvKey"));
            contract = Smstest3.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT);
            try {
                receipt = contract.registerUser().send();
            } catch (Exception e) {
                e.printStackTrace();
            }

            myTvString = "Current Coston\nBlock Number:\n" + bob.getBlockNumber().toString() + "  Wallets: " + String.valueOf(prefs.getInt("walletCount", 0) + "  Current: " + String.valueOf(prefs.getInt("currentWallet", 0)));
            myBalanceS = "FXRP Balance of Flare Testnet Address " + deets.get("walletAddress") + " = " + getMyBalance(deets.get("walletAddress")).toString();



            try {  // get list of registered addresses
                registeredAddresses = contract.getContractProperties().send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                messageCount = contract.getMyInboxSize().send();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //    TransactionReceipt receipt2 = Transfer.sendFunds(FlareConnection,c,"0x6B4b502Dc21Aa25d384B5725610272B596ca88ab",
            //            BigDecimal.valueOf(1), org.web3j.utils.Convert.Unit.ETHER).send();

            //     Log.d("TEST", "isRrgistered: "+isRegistered);
            //     Log.d("TEST", "Transaction receipt from registeruser: "+ receipt.toString());
            //     Log.d("TEST", "Transaction receipt from Transfer: "+ receipt2.toString());
            // //load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    msgCount = messageCount.getValue2().toString();
                    inbox.setText("Inbox: " + msgCount + " messages");
                    populateSpinner(registeredAddresses);
                    Log.d("TEST", "List: " + registeredAddresses);
                    Log.d("TEST", "Inbox count: " + messageCount);
                    myTV.setText(myTvString);
                    myBalance.setText(myBalanceS);
                }
            });
        }
    }

    public void populateSpinner(Tuple2<String, List<String>> lista) {

        List<String> list = lista.getValue2();
        //list.add("Speed Test 150(min) PO Set-01");

        Log.d("TEST", "Data out: "+list.toString());

        ArrayAdapter<String> dataAdapter;
       dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       addresses.setAdapter(dataAdapter);
    }

    public BigDecimal getMyBalance(String walletAddress) {

        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = FlareConnection
                    .ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        BigDecimal wei = new BigDecimal(ethGetBalance.getBalance());

        BigDecimal FXRP;
        FXRP = Convert.fromWei(wei, Convert.Unit.ETHER);

        return FXRP;
    }
    public EthBlockNumber getBlockNumber() {
        EthBlockNumber result = new EthBlockNumber();

        try {
            result = FlareConnection.ethBlockNumber()
                    .sendAsync()
                    .get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Log.d("TEST", result.getBlockNumber().toString());
        return result;
    }
}