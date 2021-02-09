package uk.co.xrpdevs.flarenetmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    // NOTE: The credentials here are from the testnet.. this is also VERY hacky at the moment!
    // NOTE: Code quality can, and will, improve!

    public Web3j FlareConnection;
    private Object TextView;
    TextView myTV;
    EthBlockNumber bob;
    Button refresh;
    Button sendMsg;
    TextView myBalance;
    EditText message;
    public String walletAddress;
    public String contractAddress;
    public String walletPrivateKey;
    Credentials c;
    ContractGasProvider cgp;
    TransactionReceipt receipt;
    Spinner addresses;
    Button inbox;
    BigInteger GAS_LIMIT = BigInteger.valueOf(670025L);
    BigInteger GAS_PRICE = BigInteger.valueOf(200000L);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FlareConnection = MyService.initWeb3j();
        bob = getBlockNumber();
        Log.d("TEST", bob.getBlockNumber().toString());

        myBalance = findViewById(R.id.balance);
        myTV = findViewById(R.id.text1);
        message = findViewById(R.id.message);
StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);
        walletAddress    = "0x24423475227b49376d72E863bB6c5b6cB4E60Cea";
        walletPrivateKey = "0xc6d66f9d9cd4c607e742d27c5fb9a9140465226c19578c02a13931f1fd0c8ef2";
        contractAddress  = "0xa49D5f1f6e63406E9dd6BF6BbAC5A9ac085527e7";
        inbox = findViewById(R.id.button3);
        addresses = (Spinner) findViewById(R.id.spinner);





        String account = null;
        try {
            account = FlareConnection.ethAccounts().send().getAccounts().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        c = Credentials.create(walletPrivateKey);

        cgp = new DefaultGasProvider();



        ContractBindings contract = ContractBindings.load(contractAddress, FlareConnection, c, GAS_PRICE, GAS_LIMIT );


        //   List<Type> inputParameters = new ArrayList<>();
     //   inputParameters.add(new DynamicArray(toAddressList));
     //   inputParameters.add(new DynamicArray(scoresList));
     //   inputParameters.add(new Uint256(amount));


        try {
            Tuple2<String, List<String>> registeredAddresses = contract.getContractProperties().send();
            populateSpinner(registeredAddresses);
            Log.d("TEST", "List: "+registeredAddresses);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            receipt = contract.registerUser().send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //   c = FlareConnection.getCredentialsFromPrivateKey("private-key");
     //   cgp = FlareConnection.ethGasPrice();

        refresh = findViewById(R.id.button);
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
        sendMsg = findViewById(R.id.button2);
        sendMsg.setOnClickListener(new View.OnClickListener() {
                                       public void onClick(View v) {
                                           try {
                                               byte[] bob = stringToBytes32(message.getText().toString());
                                               receipt = contract.sendMessage(walletAddress, bob).send();
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
      //  myTV.setText("Current Coston\nBlock Number:\n"+bob.getBlockNumber().toString());
      //  myBalance.setText("FXRP Balance of Flare Testnet Address "+walletAddress+" = "+getMyBalance(walletAddress).toString());
        try {
            update();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("TEST", "FXRP Balance of Flare Testnet Address "+walletAddress+" = "+getMyBalance(walletAddress).toString());
     //   FlareConnection.

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
    public void update() throws Exception {
        myTV.setText("Current Coston\nBlock Number:\n" + bob.getBlockNumber().toString());
        myBalance.setText("FXRP Balance of Flare Testnet Address " + walletAddress + " = " + getMyBalance(walletAddress).toString());
        ContractBindings contract = ContractBindings.load(contractAddress, FlareConnection, c, cgp);

        Boolean isRegistered = contract.checkUserRegistration().send();
        if(!isRegistered) {
            try {
                RemoteCall<TransactionReceipt> register = contract.registerUser();
                receipt = register.send();
                Log.d("TEST", "receipt: "+ receipt.isStatusOK());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Tuple2<BigInteger, BigInteger> messageCount = contract.getMyInboxSize().send();
            Log.d("TEST", "Inbox count: "+messageCount);
            String msgCount = messageCount.getValue1().toString();
            inbox.setText("Inbox: "+msgCount+" messages");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("TEST", "isRrgistered: "+isRegistered);
        Log.d("TEST", "Transaction receipt from registeruser: "+ receipt.toString());
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