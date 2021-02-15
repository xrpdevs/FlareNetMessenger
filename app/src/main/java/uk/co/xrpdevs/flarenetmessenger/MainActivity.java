package uk.co.xrpdevs.flarenetmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    Smstest3 contract;
    BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    HashMap<String, String> deets;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        if(!prefs.contains("walletCount")){
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.commit();
        }
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
                                               update();
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
                                               String rawString = message.getText().toString();
                                               byte[] bytes = rawString.getBytes(StandardCharsets.UTF_8);

                                               String utf8EncodedString = new String(bytes, StandardCharsets.UTF_8);
                                               receipt = contract.sendMessage(addresses.getSelectedItem().toString(), utf8EncodedString).send();
                                               String text;
                                               if(receipt.isStatusOK()){
                                                   text = "Message sent!\n"+receipt.getGasUsed().toString();
                                               } else {
                                                   text = "Message sending failed";
                                               }
                                               showToast(text.toString());
                                               update();
                                           } catch (Exception e) {
                                               e.printStackTrace();
                                           }
                                       }
                                   }
        );

        if(deets != null) {
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
    //        Log.d("TEST", "FXRP Balance of Flare Testnet Address " + deets.get("walletAddress") + " = " + getMyBalance(deets.get("walletAddress")).toString());
            //   FlareConnection.
        }
    }

    public void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void update() throws Exception {

        c = Credentials.create(deets.get("walletPrvKey"));
        contract = Smstest3.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT );
        try {
            receipt = contract.registerUser().send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String myTvString = "Current Coston\nBlock Number:\n" + bob.getBlockNumber().toString() + "  Wallets: " + String.valueOf(prefs.getInt("walletCount", 0) +"  Current: " + String.valueOf(prefs.getInt("currentWallet", 0)));
        String myBalanceS = "FXRP Balance of Flare Testnet Address " + deets.get("walletAddress") + " = " + getMyBalance(deets.get("walletAddress")).toString();
        myTV.setText(myTvString);
        myBalance.setText(myBalanceS);



        // todo: make sure this is only called after a user adds a wallet.
        //Boolean isRegistered = contract.checkUserRegistration().send();
        //if(!isRegistered) {
        //    try {
        //        RemoteCall<TransactionReceipt> register = contract.registerUser();
        //        receipt = register.send();
        //        Log.d("TEST", "receipt: "+ receipt.isStatusOK());
        //    } catch (Exception e) {
        //        e.printStackTrace();
        //    }
        //}



        try {
            Tuple2<String, List<String>> registeredAddresses = contract.getContractProperties().send();
            populateSpinner(registeredAddresses);
            Log.d("TEST", "List: "+registeredAddresses);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Tuple2<BigInteger, BigInteger> messageCount = contract.getMyInboxSize().send();
            Log.d("TEST", "Inbox count: "+messageCount);
            String msgCount = messageCount.getValue2().toString();
            inbox.setText("Inbox: "+msgCount+" messages");
        } catch (Exception e) {
            e.printStackTrace();
        }

   //    TransactionReceipt receipt2 = Transfer.sendFunds(FlareConnection,c,"0x6B4b502Dc21Aa25d384B5725610272B596ca88ab",
   //            BigDecimal.valueOf(1), org.web3j.utils.Convert.Unit.ETHER).send();

   //     Log.d("TEST", "isRrgistered: "+isRegistered);
   //     Log.d("TEST", "Transaction receipt from registeruser: "+ receipt.toString());
   //     Log.d("TEST", "Transaction receipt from Transfer: "+ receipt2.toString());
        // //load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider

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