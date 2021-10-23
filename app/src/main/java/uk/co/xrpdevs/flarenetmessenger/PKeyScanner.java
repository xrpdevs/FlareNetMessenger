package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;
import org.xrpl.xrpl4j.wallet.DefaultWalletFactory;
import org.xrpl.xrpl4j.wallet.Wallet;
import org.xrpl.xrpl4j.wallet.WalletFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.HttpUrl;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.SelectBlockChainDialogFragment;

public class PKeyScanner extends AppCompatActivity implements View.OnClickListener, SelectBlockChainDialogFragment.OnResultListener {
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    private static final int ADDRESS_REQUEST_CODE = 7541;
    public static final int PRIV_KEY_REQUEST_CODE = 9554;
    public int SCAN_TYPE;
    Button scan, cnew, save;
    Context mThis = this;
    IntentIntegrator integrator;
    EditText wName;
    EditText wKey;
    PleaseWaitDialog dialogActivity;
    ListView lv;
    SelectBlockChainDialogFragment sbcdf;
    HashMap<String, ?> bcData;

    @Override
    public void onCreate(Bundle state) {


        super.onCreate(state);
        setContentView(R.layout.activity_importwallet);

        if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission_group.CONTACTS}, 50);
        }

        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        wName = findViewById(R.id.newWalletName);
        scan = findViewById(R.id.importPrvKeyQR);
        scan.setOnClickListener(this);
        cnew = findViewById(R.id.importPrvKeyQR2);
        cnew.setOnClickListener(this);
        save = findViewById(R.id.savePrivKey);
        save.setOnClickListener(this);
        //  lv = findViewById(R.id.import_chainsList);
        //  lv.setAdapter(fillListView(getAvailChains()));

        // call SelectBlockChainDialogFragment

        bcDialog("New Wallet Type", true);


    }

    private boolean bcDialog(String title, Boolean cancelable) {
        android.app.FragmentManager manager = this.getFragmentManager();

        sbcdf = new SelectBlockChainDialogFragment().newInstance(this, title, title, true);
        //sbcdf.titleText = "About this App";
        sbcdf.prompt = title;
        sbcdf.cancelable = cancelable;

        sbcdf.show(manager, "aaa");
        return true;
    }


    public SimpleAdapter fillListView(final ArrayList<HashMap<String, String>> lines) {
        //myLog("Lines", lines.toString());
        SimpleAdapter simpleAdapter = new SimpleAdapter(
                mThis,
                lines,
                R.layout.listitem_blockchains,
                new String[]{"Name", "NativeCurrency", "Icon"},
                new int[]{R.id.bcListChainName, R.id.bcListTokenName, R.id.bcListIcon}) {
        };
        return simpleAdapter;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PRIV_KEY_REQUEST_CODE) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(resultCode, intent);

            if (resultCode == RESULT_OK) {

                if (scanningResult != null) {

                    String scanContent = scanningResult.getContents();
                    String scanFormat = scanningResult.getFormatName();
                    myLog("TEST", scanContent);


                    Credentials cs = Credentials.create(scanContent);

                    String privateKey = cs.getEcKeyPair().getPrivateKey().toString(16);
                    String publicKey = cs.getEcKeyPair().getPublicKey().toString(16);
                    String addr = cs.getAddress();

                    //Pair<BigDecimal, String> testAddr = Utils.getMyBalance(addr);


                    if (scanContent.startsWith("0x") && scanContent.length() == 66) {
                        int wC = prefs.getInt("walletCount", 0);
                        wC++;

                        System.out.println("Private key: " + privateKey);
                        System.out.println("Public key: " + publicKey);
                        System.out.println("Address: " + addr);

                        HashMap<String, String> tmp = new HashMap<String, String>();
                        if (wName.getText().toString().equals("")) {
                            tmp.put("walletName", "Wallet " + wC);
                        } else {
                            tmp.put("walletName", wName.getText().toString());
                        }
                        tmp.put("walletPrvKey", scanContent);
                        tmp.put("walletPubKey", "0x" + publicKey);
                        tmp.put("walletAddress", addr);

                        pEdit.putString("wallet" + wC, new JSONObject(tmp).toString());
                        pEdit.putInt("walletCount", wC);
                        pEdit.putInt("currentWallet", wC);
                        pEdit.commit();
                        pEdit.apply();
                        Intent i = new Intent(PKeyScanner.this, MainActivity.class);
                        startActivity(i);
                    } else {
                        showDialog("Error: Not a valid private key", true);
                    }
                } else {
                    showDialog("No scan data received, please try again!", true);
                    Log.e("TEST", " Scan unsuccessful");
                }
            } else { //resultCode == RESULT_CANCELED) {
                super.onActivityResult(requestCode, resultCode, intent);
                // Handle cancel
                Log.i("App", "Scan unsuccessful");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.savePrivKey:
                myLog("TEST", "Save private key button pressed");
                myLog("TEST", Utils.getAvailChains().toString());
                break;
            case R.id.importPrvKeyQR:
                myLog("TEST", "QR Scan Button Pressed");
                integrator = new IntentIntegrator(this);
                integrator.setPrompt("QR code will be scanned automatically on focus");
                integrator.setCameraId(0);
                integrator.setRequestCode(PKeyScanner.PRIV_KEY_REQUEST_CODE);
                integrator.setOrientationLocked(true);
                integrator.setBeepEnabled(true);
                integrator.setCaptureActivity(CaptureActivityPortrait.class);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
                break;
            case R.id.importPrvKeyQR2:

                String bcType = prefs.getString("csbc_type", "ETH");
                myLog("bcData", bcData.toString());
                if (bcType.equals("XRPL")) {  // todo: check sql database here for blockchain type
                    newXrplWallet();          // todo: probably use a switch() statement here
                } else {
                    newEthWallet();
                }
                break;
        }
    }

    private void newXrplWallet() {
        String scanContent = null;
        //     HttpUrl rippledUrl = HttpUrl
        //              .get("https://s.altnet.rippletest.net:51234/");
//        XrplClient xrplClient = new XrplClient(rippledUrl);


        // Create a Wallet using a WalletFactory
        WalletFactory walletFactory = DefaultWalletFactory.getInstance();
        Wallet testWallet = walletFactory.randomWallet(true).wallet();

        String privateKey = String.valueOf(testWallet.privateKey());
        String publicKey = testWallet.publicKey();
        String addr = testWallet.classicAddress().value();
        String xaddr = testWallet.xAddress().value();

        if (bcData.containsKey("Testnet")) {
            myLog("BCDATA", bcData.toString());
            //if (bcData.get("Testnet").toString().equals("true")) {
                FaucetClient faucetClient = FaucetClient
                        .construct(HttpUrl.get("https://faucet.altnet.rippletest.net"));
                faucetClient.fundAccount(FundAccountRequest.of(testWallet.classicAddress()));
            // }
        }

        System.out.println("Private key: " + privateKey);
        System.out.println("Public key: " + publicKey);
        System.out.println("Address: " + addr);
        System.out.println("X-Address: " + addr);
        int wC = prefs.getInt("walletCount", 0);
        wC++;
        HashMap<String, String> tmp = new HashMap<String, String>();
        if (wName.getText().toString().equals("")) {
            tmp.put("walletName", "Wallet " + wC);
        } else {
            tmp.put("walletName", wName.getText().toString()); // wallet name
        }
        tmp.put("walletPrvKey", privateKey);// private key
        tmp.put("walletPubKey", publicKey); // public key
        tmp.put("walletAddress", addr);     // primary wallet address
        tmp.put("walletAltAddress", xaddr); // generify so we can support hex and alternate addresses
        tmp.put("walletType", "XRPL");      // remove later as will be referenced from blockchain sql table
        tmp.put("bcid", String.valueOf(bcData.get("bcid"))); // blockchain identifier

        privateKey = privateKey.replace("Optional[", "").replace("]", "");

        // Utils.xorStrings()

        boolean b = FlareNetMessenger.dbH.addWallet(tmp.get("walletName"), Integer.valueOf(tmp.get("bcid")), publicKey, privateKey, addr, xaddr, 0, "0");

        pEdit.putString("wallet" + wC, new JSONObject(tmp).toString());
        pEdit.putInt("walletCount", wC);
        pEdit.putInt("currentWallet", wC);
        pEdit.commit();
        pEdit.apply();
        Intent i = new Intent(PKeyScanner.this, MainActivity.class);
        startActivity(i);

        // Get the Classic and X-Addresses from testWallet
        Address classicAddress = testWallet.classicAddress();
        XAddress xAddress = testWallet.xAddress();
        System.out.println("Classic Address: " + classicAddress);
        System.out.println("X-Address: " + xAddress);
    }

    private void newEthWallet() {
        String scanContent = null;


        try {
            scanContent = Utils.newKeys();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        Credentials cs = Credentials.create(scanContent);

        String privateKey = cs.getEcKeyPair().getPrivateKey().toString(16);
        String publicKey = cs.getEcKeyPair().getPublicKey().toString(16);
        String addr = cs.getAddress();

        //Pair<BigDecimal, String> testAddr = Utils.getMyBalance(addr);


        if (scanContent.startsWith("0x") && scanContent.length() == 66) {
            int wC = prefs.getInt("walletCount", 0);
            wC++;

            System.out.println("Private key: " + privateKey);
            System.out.println("Public key: " + publicKey);
            System.out.println("Address: " + addr);

            HashMap<String, String> tmp = new HashMap<String, String>();
            if (wName.getText().toString().equals("")) {
                tmp.put("walletName", "Wallet " + wC);
            } else {
                tmp.put("walletName", wName.getText().toString());
            }
            tmp.put("walletPrvKey", scanContent);
            tmp.put("walletPubKey", "0x" + publicKey);
            tmp.put("walletAddress", addr);
            tmp.put("walletType", "ETH");
            tmp.put("bcid", String.valueOf(bcData.get("bcid")));

            pEdit.putString("wallet" + wC, new JSONObject(tmp).toString());
            pEdit.putInt("walletCount", wC);
            pEdit.putInt("currentWallet", wC);
            pEdit.commit();
            pEdit.apply();
            Intent i = new Intent(PKeyScanner.this, MainActivity.class);
            startActivity(i);
        }
    }

    private boolean showDialog(String prompt, Boolean cancelable) {
        FragmentManager manager = getFragmentManager();

        dialogActivity = new PleaseWaitDialog();
        dialogActivity.prompt = prompt;
        dialogActivity.cancelable = cancelable;
        dialogActivity.show(manager, "DialogActivity");
        return true;
    }


    @Override
    public void onResult(HashMap<String, ?> data) {
        sbcdf.dismiss();
        bcData = data;
        String RPC = (String) data.get("RPC");
        int CID = Integer.decode((String) data.get("ChainID"));
        MyService.fCoinLink = MyService.initConnection(RPC, CID); // maybe just keep this to FLR
        MyService.rpc = RPC;
        MyService.tmpCID = CID;
        MyService.isXRPL = true;
        // restart the service here?


    }

}