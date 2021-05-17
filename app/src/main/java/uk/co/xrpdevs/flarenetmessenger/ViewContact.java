package uk.co.xrpdevs.flarenetmessenger;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.Callback;
import org.web3j.tx.response.QueuingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import uk.co.xrpdevs.flarenetmessenger.contracts.ERC20;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PinCodeDialogFragment;
import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class ViewContact extends AppCompatActivity implements Button.OnClickListener, PinCodeDialogFragment.OnResultListener {
    TextView contactName;
    TextView xrpAddress;
    Button deleteContact, sendFunds;
    Long rawContactID = 0L;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    HashMap<String, String> deets;
    PleaseWaitDialog dialogActivity;
    TextView balancesInfo;
    Activity mThis = this;
    String to, from, theirWallet, myWallet, cNameText, tokenName, tokenAddress, tokenBalance;
    BigDecimal amount;
    EditText XRPAmount;
    PinCodeDialogFragment pinDialog;
    boolean token = false;
    ERC20 bob;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        Intent myIntent = getIntent();
        Bundle bundle = myIntent.getExtras();
        myLog("bundle", Utils.dump(bundle));
        if (bundle.containsKey("contactInfo")) {
            Bundle ci = bundle.getBundle("contactInfo");
            if (ci.containsKey("token")) {
                myLog("TOKEN", "dealing with a token");
                // we're dealing with a token, rather than base asset

                tokenName = ci.getString("token");
                tokenAddress = ci.getString("tAddr");
                token = true;
                bob = MyService.getERC20link(tokenAddress, MyService.c, MyService.rpc);
                myLog("bundle", ci.toString());
            }
        }
        String action = myIntent.getAction();
        //    final String myWallet, theirWallet, cNameText;
        String info;
        BigDecimal myBalance = null, theirBalance = null;


        //contactName   = findViewById(R.id.viewContactName);
        xrpAddress = findViewById(R.id.viewContactWalletAddress);
        deleteContact = findViewById(R.id.button7);
        deleteContact.setOnClickListener(this);
        balancesInfo = findViewById(R.id.balancesInfo);
        XRPAmount = findViewById(R.id.editTextNumberDecimal);
        sendFunds = findViewById(R.id.viewContactSendFunds);
        sendFunds.setOnClickListener(this);
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        String pinCode = prefs.getString("pinCode", "abcd");

        try {
            deets = Utils.getPkey(this, prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        /*deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TEST", "Deleted: " + ContactsManager.deleteRawContactID(ViewContact.this, rawContactID));
            }
        });*/


        Uri uri = myIntent.getData();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Log.e("TEST", key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
            }
        }


        // Querying the table ContactsContract.Contacts to retrieve all the contacts

        Cursor contactsCursor = getContentResolver().query(uri, null, null, null,
                null);

        //    Log.d("TEST", action);
        Log.d("TEST", "ViewContact URI: " + uri.toString());
        if (contactsCursor.moveToFirst()) {
            Log.d("TEST", "UriData: " + DatabaseUtils.dumpCurrentRowToString(contactsCursor));
            int addressColumnIndex = contactsCursor.getColumnIndex(ContactsContract.RawContacts.Entity.DATA3);
            int cNameIndex = contactsCursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);

            rawContactID = contactsCursor.getLong(contactsCursor.getColumnIndex("raw_contact_id"));

            //  rawContactID = Long.getLong(rcID);

            Log.d("TEST", "RAWCONTACTID " + rawContactID);

            String XRPAddress = contactsCursor.getString(addressColumnIndex);
            cNameText = contactsCursor.getString(cNameIndex);

            Log.d("TEST", "XRP Address: " + XRPAddress + " Cname: " + cNameText);
            xrpAddress.setText(XRPAddress);

            myWallet = deets.get("walletAddress");
            if (token) {
                theirWallet = XRPAddress;


                BigInteger bal = new BigInteger("0");
                try {
                    bal = bob.balanceOf(myWallet).send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //String balance = bob.balanceOf(deets.get("walletAddress")
                myBalance = new BigDecimal(bal, 18);
                try {
                    bal = bob.balanceOf(theirWallet).send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //String balance = bob.balanceOf(deets.get("walletAddress")
                theirBalance = new BigDecimal(bal, 18);
            } else {
                try {
                    myBalance = Utils.getMyBalance(myWallet).first;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                theirWallet = XRPAddress;
                try {
                    theirBalance = Utils.getMyBalance(theirWallet).first;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String pubkey = ContactsManager.getPubKey(mThis.getApplicationContext(), theirWallet);

            info = "Your balance: " + myBalance.stripTrailingZeros().toPlainString() + "\n" +
                    "Their balance: " + theirBalance.stripTrailingZeros().toPlainString() + "\n";

            if (pubkey != null) {
                info = info + "Pubkey Present";
            }

            balancesInfo.setText(info);

            /*sendFunds.setOnClickListener((View v) -> {

                FragmentManager manager = getFragmentManager();
                pinDialog = new PinCodeDialogFragment().newInstance(this, "Enter PIN:");
                pinDialog.show(manager, "1");



            });*/

            // contactName.setText(cNameText);
            if (token) {
                this.getSupportActionBar().setTitle(cNameText + " (" + tokenName + ")");
            } else {
                this.getSupportActionBar().setTitle(cNameText);
            }
            QRCodeWriter writer = new QRCodeWriter();
            if (XRPAddress != null) {
                try {
                    BitMatrix bitMatrix = writer.encode(XRPAddress, BarcodeFormat.QR_CODE, 512, 512);
                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();
                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    ((ImageView) findViewById(R.id.imageView2)).setImageBitmap(bmp);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    public void onResult(String pinCode) {
        myLog("PIN", "Onresult called - PINCODE = " + pinCode);
        if (pinCode.equals(prefs.getString("pinCode", "abcd"))) {
            pinDialog.dismiss();

            to = theirWallet;
            from = myWallet;
            amount = BigDecimal.valueOf(Double.parseDouble(XRPAmount.getText().toString()));
            ViewContact.sendFunds bob = new sendFunds();
            bob.cNameText = cNameText;
            bob.start();
        } else {
            showDialog("Incorrect PIN", true);
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("ONCLICK", "id: " + v.getId());
        switch (v.getId()) {
            case R.id.button7:
                // delete contact
                Log.d("TEST", "Deleted: " + ContactsManager.deleteRawContactID(ViewContact.this, rawContactID));
                break;
            case R.id.viewContactSendFunds:
                FragmentManager manager = getFragmentManager();
                pinDialog = new PinCodeDialogFragment().newInstance(this, "Enter PIN:");
                pinDialog.show(manager, "1");
                // send funds
                break;
            //todo: case R.id.thusFarUndefined:
            // cheange contact

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

    /* public String serialize(final Transaction transaction) {
         final byte[] bytesToSign = transaction.rlpEncode(chainId);

         final Signature signature = signer.sign(bytesToSign);

         final Sign.SignatureData web3jSignature =
                 new Sign.SignatureData(
                         signature.getV().toByteArray(),
                         signature.getR().toByteArray(),
                         signature.getS().toByteArray());

         final Sign.SignatureData eip155Signature =
                 TransactionEncoder.createEip155SignatureData(web3jSignature, chainId);

         final byte[] serializedBytes = transaction.rlpEncode(eip155Signature);
         return Numeric.toHexString(serializedBytes);
     }
 */
    class sendFunds extends Thread {

        String cNameText;
        Boolean isReplaced = false;
        BigDecimal myBalance, theirBalance;
        String ts;

        @Override
        public void run() {
            ts = "FLR";
            if (token) ts = tokenName;
            //String myWallet, String theirWallet, BigDecimal XRPAmount) {
            showDialog("Sending " + amount + " " + ts + " to \n" + cNameText + "\nPlease wait for transaction completion.", false);

            /* TODO
                Transactions should be moved to a Service, that way, we don't need to wait within an activity etc
                or do network IO on the UI thread. This also means that any failed transactions etc can be shown by way
                of the app's notification bar.
                *
                Need to investigate why the TransactionManager callback doesn't get called. This works fine for now though.


             */

            try {
                if (token) {
                    String aStr = amount.toPlainString();
                    BigDecimal scale = new BigDecimal("1e18");
                    BigDecimal bd = amount.multiply(scale);
                    BigInteger value = bd.toBigIntegerExact();

                    Log.d("POO", value.toString());


                    // Todo: This blocks the UI thread, move to completableFuture as in the EIP-155 code for non-ERC20 sends.
                    TransactionReceipt transactionReceipt = bob.transfer(theirWallet, value).send();
                    String TAG = "RECEIPT";
                    if (!transactionReceipt.isStatusOK()) {
                        Log.d(TAG, "transactionReceipt: Error: " + transactionReceipt.getStatus());
                    } else {
                        //Log.d(TAG, "transactionReceipt: Block hash: " + transactionReceipt.getTransactionReceipt().getBlockHash());
                        Log.d(TAG, "transactionReceipt: Root: " + transactionReceipt.getRoot());
                        Log.d(TAG, "transactionReceipt: Contract address: " + transactionReceipt.getContractAddress());
                        Log.d(TAG, "transactionReceipt: From: " + transactionReceipt.getFrom());
                        Log.d(TAG, "transactionReceipt: To: " + transactionReceipt.getTo());
                        Log.d(TAG, "transactionReceipt: Block hash: " + transactionReceipt.getBlockHash());
                        Log.d(TAG, "transactionReceipt: Block number: " + transactionReceipt.getBlockNumber());
                        Log.d(TAG, "transactionReceipt: Block number raw: " + transactionReceipt.getBlockNumberRaw());
                        Log.d(TAG, "transactionReceipt: Gas used: " + transactionReceipt.getGasUsed());
                        Log.d(TAG, "transactionReceipt: Gas used raw: " + transactionReceipt.getGasUsedRaw());
                        Log.d(TAG, "transactionReceipt: Cumulative gas used: " + transactionReceipt.getCumulativeGasUsed());
                        Log.d(TAG, "transactionReceipt: Cumulative gas used raw: " + transactionReceipt.getCumulativeGasUsedRaw());
                        Log.d(TAG, "transactionReceipt: Transaction hash: " + transactionReceipt.getTransactionHash());
                        Log.d(TAG, "transactionReceipt: Transaction index: " + transactionReceipt.getTransactionIndex());
                        Log.d(TAG, "transactionReceipt: Transaction index raw: " + transactionReceipt.getTransactionIndexRaw());
//                        Log.d(TAG, "transactionReceipt: JSON-RPC response: " + transactionReceipt.s
                    }
                } else {

                    //    TransactionReceipt receipt2 = Transfer.sendFunds(Utils.initWeb3j(), Utils.getCreds(deets), to,
                    //             amount, org.web3j.utils.Convert.Unit.ETHER).send();
                    Web3j sendObj = MyService.initConnection(
                            prefs.getString("csbc_rpc", ""),
                            //Integer.decode(prefs.getString("csbc_cid", "0"))
                            43113
                    );

                    //  String address;
                    EthGetTransactionCount ethGetTransactionCount = sendObj.ethGetTransactionCount(
                            myWallet, DefaultBlockParameterName.LATEST).sendAsync().get();

                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();

                    TransactionReceiptProcessor transactionReceiptProcessor =
                            new QueuingTransactionReceiptProcessor(sendObj, new Callback() {
                                @Override
                                public void accept(TransactionReceipt transactionReceipt) {
                                    myLog("RECEIPT:", transactionReceipt.toString());
                                }

                                @Override
                                public void exception(Exception exception) {
                                    myLog("RECEIPT exception:", exception.toString());
                                    // handle exception
                                }
                            }, 20000, 5000);


                    RawTransactionManager transactionManager = new RawTransactionManager(
                            sendObj,
                            Utils.getCreds(deets),
                            Integer.decode(prefs.getString("csbc_cid", "1")),
                            transactionReceiptProcessor);


                    RawTransaction rawTransaction = RawTransaction.createEtherTransaction(

                            nonce,
                            getNetworkGasPrice(sendObj),
                            new BigInteger("100000"),
                            to,
                            Convert.toWei(amount, Convert.Unit.ETHER).toBigIntegerExact());


                    EthSendTransaction moo = transactionManager.signAndSend(rawTransaction);


                    CompletableFuture<EthGetTransactionReceipt> transactionReceiptCompletableFuture = sendObj.ethGetTransactionReceipt(moo.getResult()).sendAsync();

                    transactionReceiptCompletableFuture.thenAccept(transactionReceipt -> {
                        myLog("RECEIPT", transactionReceipt.toString());
                        String TAG = "RECEIPT";

                        if (transactionReceipt.hasError()) {
                            Log.d(TAG, "transactionReceipt: Error: " + transactionReceipt.getError().getMessage());
                        } else if (transactionReceipt.getResult() != null || transactionReceipt.getTransactionReceipt() != null) {
                            //Log.d(TAG, "transactionReceipt: Block hash: " + transactionReceipt.getTransactionReceipt().getBlockHash());
                            Log.d(TAG, "transactionReceipt: Root: " + transactionReceipt.getResult().getRoot());
                            Log.d(TAG, "transactionReceipt: Contract address: " + transactionReceipt.getResult().getContractAddress());
                            Log.d(TAG, "transactionReceipt: From: " + transactionReceipt.getResult().getFrom());
                            Log.d(TAG, "transactionReceipt: To: " + transactionReceipt.getResult().getTo());
                            Log.d(TAG, "transactionReceipt: Block hash: " + transactionReceipt.getResult().getBlockHash());
                            Log.d(TAG, "transactionReceipt: Block number: " + transactionReceipt.getResult().getBlockNumber());
                            Log.d(TAG, "transactionReceipt: Block number raw: " + transactionReceipt.getResult().getBlockNumberRaw());
                            Log.d(TAG, "transactionReceipt: Gas used: " + transactionReceipt.getResult().getGasUsed());
                            Log.d(TAG, "transactionReceipt: Gas used raw: " + transactionReceipt.getResult().getGasUsedRaw());
                            Log.d(TAG, "transactionReceipt: Cumulative gas used: " + transactionReceipt.getResult().getCumulativeGasUsed());
                            Log.d(TAG, "transactionReceipt: Cumulative gas used raw: " + transactionReceipt.getResult().getCumulativeGasUsedRaw());
                            Log.d(TAG, "transactionReceipt: Transaction hash: " + transactionReceipt.getResult().getTransactionHash());
                            Log.d(TAG, "transactionReceipt: Transaction index: " + transactionReceipt.getResult().getTransactionIndex());
                            Log.d(TAG, "transactionReceipt: Transaction index raw: " + transactionReceipt.getResult().getTransactionIndexRaw());
                            Log.d(TAG, "transactionReceipt: JSON-RPC response: " + transactionReceipt.getJsonrpc());
                        }

                        // then accept gets transaction receipt only if the transaction is successful

                    }).exceptionally(transactionReceipt -> {
                        return null;
                    });


                    EthGetTransactionReceipt receipt = sendObj.ethGetTransactionReceipt(moo.getResult()).sendAsync().get();

                    String oot = "";
                    oot = oot + " " + prefs.getString("csbc_rpc", "");
//                    Sign.
                    //          nonce,
                    //         new BigInteger("41000000000"), //DefaultGasProvider.GAS_PRICE,
                    //         DefaultGasProvider.GAS_LIMIT,
                    //          to,
                    //       new BigInteger("12344"));
//rawTransaction.getData();
                    myLog("CONV",
                            "\nNonce: " + nonce + "\nOOT: " + oot + " " +
                                    " \nVal: " + Convert.toWei(amount, Convert.Unit.ETHER).toBigIntegerExact().toString() +
                                    " \nGasP: " + DefaultGasProvider.GAS_PRICE + " GasL: " + DefaultGasProvider.GAS_LIMIT);

/*                    byte[] signedMessage;

                    signedMessage = TransactionEncoder.signMessage(rawTransaction, Utils.getCreds(deets));
                    //  sendObj.ethSendRawTransaction()
                    try {

                        EthSendTransaction receipt2;
                        receipt2 = sendObj.ethSendRawTransaction(String.valueOf(rawTransaction)).sendAsync().getNow(null);
                        //     receipt2 = sendObj.ethSendRawTransaction(rawTransaction.getData()).sendAsync().get();
                        myLog("REC", receipt2.getTransactionHash());
                    } catch (Exception e) {
                        myLog("REC", e.getMessage());

                    }
*/

                          /*  DefaultGasProvider.GAS_PRICE,
                            DefaultGasProvider.GAS_LIMIT,
                            to,
                            Hex.toHexString(signedMessage),
                            Convert.toWei(amount, Convert.Unit.ETHER).toBigIntegerExact());
*/

                    //              String hexValue = "0x" + Hex.toHexString(signedMessage);
                    //           Log.d("ETHMSG", hexValue);


                    //   sendObj.ethChainId().send().setId(43113);
                    myLog("CHAINID",
                            "NetwkID: " + sendObj.netVersion().getId() + "\n" +
                                    "ChainID: " + sendObj.ethChainId().send().getChainId());
                    //     Transfer.
                    //   transactionManager.sendTransaction()
                    //   TransactionReceipt receipt2 = Transfer.sendFunds(Utils.initWeb3j(), Utils.getCreds(deets), to,
                    //           amount, org.web3j.utils.Convert.Unit.ETHER).send();
//                    sendObj.ethChainId().setId(43113);

                    //sendObj.netVersion().setId(43113);
                    myLog("CHAINID",
                            "ChainID: " + sendObj.netVersion().getId() + "\n" +
                                    "NetwkID: " + sendObj.ethChainId().getId());

                    //    aRawTransaction moo = new aRawTransaction(sendObj, Utils.getCreds(deets), null, null , 43113);
                    //    moo.Send(to, amount.toPlainString());
                }

            } catch (Exception e) {
                e.printStackTrace();
                dialogActivity.dismiss();
                isReplaced = true;
                showDialog("Transaction to " + cNameText + " failed.\n" + e.getMessage(), true);
            }

            if (token) {
                BigInteger bal = new BigInteger("0");
                try {
                    bal = bob.balanceOf(myWallet).send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //String balance = bob.balanceOf(deets.get("walletAddress")
                myBalance = new BigDecimal(bal, 18);
                try {
                    bal = bob.balanceOf(theirWallet).send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //String balance = bob.balanceOf(deets.get("walletAddress")
                theirBalance = new BigDecimal(bal, 18);
            } else {
                try {
                    myBalance = Utils.getMyBalance(myWallet).first;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    theirBalance = Utils.getMyBalance(theirWallet).first;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String info2 = "Your balance: " + myBalance.stripTrailingZeros().toPlainString() + "\n" +
                    "Their balance: " + theirBalance.stripTrailingZeros().toPlainString() + "\n";


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // mContactList.setAdapter(cursorAdapter);
                    if (!isReplaced) {
                        dialogActivity.dismiss();

                    }
                    balancesInfo.setText(info2);

                    Log.d("TEST", "Running UI thread");


                }
            });

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
}