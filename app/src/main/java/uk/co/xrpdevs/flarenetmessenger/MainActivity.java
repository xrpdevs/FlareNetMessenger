package uk.co.xrpdevs.flarenetmessenger;
/* TODO: Redundant class, check dependencies and remove */

import android.Manifest;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONException;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Cipher;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;
import static uk.co.xrpdevs.flarenetmessenger.Utils.toByte;

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
    Provider secP;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_menu_home, menu);
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
            /*case R.id.select:
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
            case R.id.theNav:
                Intent intent2 = new Intent(getApplicationContext(), FirstRun.class);
                //intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                //intent.putExtra("lType", 2000);
                startActivity(intent2);
            case R.id.ma2:
                Intent intent3 = new Intent(getApplicationContext(), MainActivity2.class);
                //intent.setType("vnd.android.cursor.item/com.sample.profile");  //should filter only contacts with phone numbers
                //intent.putExtra("lType", 2000);
                startActivity(intent3);
*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
   //     Security.addProvider(new BouncyCastleProvider)
        setContentView(R.layout.activity_main);
        secP = new org.spongycastle.jce.provider.BouncyCastleProvider();
        //     Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
      //  EasyLock.checkPassword(this);
     //  if(!prefs.contains("pinCode")) {
     //   }
        if(!prefs.contains("walletCount")){
            pEdit.putInt("walletCount", 0);
            pEdit.putInt("currentWallet", 0);
            pEdit.apply();
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

        addresses = findViewById(R.id.spinner);
        cgp = new DefaultGasProvider();



        if(deets != null) {
            Log.d("TEST", deets.get("walletAddress"));

            //   c = FlareConnection.getCredentialsFromPrivateKey("private-key");
            //   cgp = FlareConnection.ethGasPrice();

        }

        refresh.setOnClickListener(v -> {
            try {
                new update().start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        );
        inbox.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, Inbox.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        );
        settings.setOnClickListener(v -> {
            try {
//                                             Intent intent = new Intent(MainActivity.this, PKeyScanner.class);
                Intent intent = new Intent(MainActivity.this, Wallets.class);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        );
        sendMsg.setOnClickListener(v -> {
            try {
                new sendMessage().start();

            } catch (Exception e) {
                e.printStackTrace();
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


    public ECPublicKey rawToEncodedECPublicKey(String curveName, byte[] rawBytes) throws GeneralSecurityException {
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("EC", secP);
//        KeyFactory kf = KeyFactory.getInstance("EC");
        byte[] x = Arrays.copyOfRange(rawBytes, 0, rawBytes.length/2);
        byte[] y = Arrays.copyOfRange(rawBytes, rawBytes.length/2, rawBytes.length);
       // ECPoint w = new ECPoint(new BigInteger(1,x), new BigInteger(1,y));
        ECPoint w = new ECPoint(new BigInteger(1,x), new BigInteger(1,y));
        return (ECPublicKey) kf.generatePublic(new java.security.spec.ECPublicKeySpec(w, ecParameterSpecForCurve(curveName)));
    }

    public java.security.spec.ECParameterSpec ecParameterSpecForCurve(String curveName) throws java.security.GeneralSecurityException {
        AlgorithmParameters params = AlgorithmParameters.getInstance("EC", secP);
        params.init(new ECGenParameterSpec(curveName));
        return params.getParameterSpec(java.security.spec.ECParameterSpec.class);
    }

    class sendMessage extends Thread {

        @Override
        public void run() {
            Log.d("TEST", "Running SendMSG thread");
            String rawString = message.getText().toString();
            byte[] bytes = rawString.getBytes(StandardCharsets.UTF_8);
            String walletPrvKey = deets.get("walletPrvKey");
            String walletPubKey = deets.get("walletPubKey");
            //walletPrvKey = walletPrvKey.replace("0x", "");
            walletPubKey = walletPubKey.replace("0x", "");
            PublicKey x509key;
            myLog("walletPubKey", walletPubKey);
            //wpk="6RLj4k7CmA7RLsphpi/LwyXNaSsc1MbYmCa3iPcIzLk8jgaPCq3EqeyhJcmpOzzeHjnXnwbK6J9yF8RozFiuvQ==";
            byte[] wpkBytes = toByte(walletPubKey);
           // wpk=Base64.decode()

            String b;byte[] ciphertext = new byte[]{0}; //ciphertext[0]=0;
            String text;

            try {
                x509key = Utils.rawToEncodedECPublicKey("secp256k1", wpkBytes); //.decode(wpk));
                myLog("KeyInfo:", x509key.getFormat());
                Cipher iesCipher = Cipher.getInstance("ECIES");
                iesCipher.init(Cipher.ENCRYPT_MODE, x509key);

                ciphertext = iesCipher.doFinal(rawString.getBytes());





                //b = new String(ciphertext, StandardCharsets.UTF_8);
                String hexStr = Hex.toHexString(ciphertext);
                String hexStr2 = hexStr.substring(2);
                byte[] hexByt = toByte(hexStr2);
                b = new String(Base64.toBase64String(ciphertext).getBytes(), StandardCharsets.UTF_8);
                myLog("Ciphered: ",
                        "Hex: "+hexStr+"\nDrp: "+Hex.toHexString(hexByt)+"\n"+
                        "B64: "+Base64.toBase64String(ciphertext)+"\n"+
                        "Len: "+b.length());
                b = new String(Base64.toBase64String(ciphertext).getBytes(), StandardCharsets.UTF_8);
//                receipt = contract.sendMessage(addresses.getSelectedItem().toString(), utf8EncodedString).send();
                receipt = contract.sendMessage(addresses.getSelectedItem().toString(), b).send();
                text = "Message sent!\n\nGas used:"+receipt.getGasUsed().toString();
            } catch (Exception e) {

                text = "FAILED\n\nReason:\n"+e.getMessage()+"\n\n"+receipt.getGasUsed().toString();
                e.printStackTrace();

            }


            try {
                myLog("PRIVATE KEY", "Len (Hex ) "+walletPrvKey.length()+"\nLen (Byte) "+(walletPrvKey.length()/2)+"\nKey: "+walletPrvKey);
                Cipher iesDecipher = Cipher.getInstance("ECIES");

                //BigInteger s = new BigInteger(walletPrvKey, 16);
                //PrivateKey X509_priv = Utils.getPrivateKeyFromECBigIntAndCurve(s, "secp256k1");

                ECKeyPair pair = c.getEcKeyPair();

                PrivateKey X509_priv = Utils.getPrivateKeyFromECBigIntAndCurve(pair.getPrivateKey(), "secp256k1");
                //PrivateKey X509_priv = (PrivateKey) pair.getPublicKey();

                //java.security.KeyFactory keyFactory = KeyFactory.getInstance("EC", secP);
                //PrivateKey X509_priv = Utils.gPK(toByte(walletPrvKey));
                iesDecipher.init(Cipher.DECRYPT_MODE, X509_priv);
                myLog("DECIPHERED TEXT", "" + new String(iesDecipher.doFinal(ciphertext)));
            } catch (Exception e){
                myLog("DECRYPTION FAILED", e.getMessage());
                e.printStackTrace();
            }
     /*       if(receipt.isStatusOK()){
                text = "Message sent!\n"+receipt.getGasUsed().toString();
            } else {
                text = "Message sending failed";
            }
*/
            String poo = Utils.deCipherText(c, ciphertext);
            String finalText = text;
            runOnUiThread(() -> {
                showToast(finalText);
                try {
                    new update().start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        }
    }


    public void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    private void addNewAccount(String accountType, String authTokenType) {
        AccountManager.get(this).addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    Log.i("info", "Account was created");
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
        HashMap<String, String> bob = new HashMap<>();
        Pair<String, String> tmp;
        bob.put("walletPrvKey", prefs.getString("walletPrvKey", null));
        bob.put("walletPubKey", prefs.getString("walletPubKey", null));
        bob.put("walletAddress", prefs.getString("walletAddress", null));
        return bob;
    }

    @Override
    public void onResume() {

        super.onResume();

        if ((checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))  {
            Log.d("TEST", "Requesting permissions...");
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS}, 50);
        }

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
        } else {
            Intent i = new Intent(this, FirstRun.class);
            startActivity(i);
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

            //Pair<BigDecimal, String> gmb = Utils.getMyBalance(deets.get("walletAddress"));

            myTvString = "Current Coston\nBlock Number:\n" + bob.getBlockNumber().toString() + "  Wallets: " + prefs.getInt("walletCount", 0) + "  Current: " + prefs.getInt("currentWallet", 0);
            myBalanceS = "FLR Balance of Flare Testnet Address " + deets.get("walletAddress") + " = " + Utils.getMyBalance(deets.get("walletAddress")).first.toString();



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

            runOnUiThread(() -> {
                msgCount = messageCount.component2().toString();
                inbox.setText("Inbox: " + msgCount + " messages");
                populateSpinner(registeredAddresses);
                Log.d("TEST", "List: " + registeredAddresses);
                Log.d("TEST", "Inbox count: " + messageCount);
                myTV.setText(myTvString);
                myBalance.setText(myBalanceS);
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