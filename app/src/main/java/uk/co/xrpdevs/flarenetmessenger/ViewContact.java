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
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Transfer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;

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
        BigDecimal myBalance, theirBalance;


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
                myBalance = Utils.getMyBalance(myWallet).first;
                theirWallet = XRPAddress;
                theirBalance = Utils.getMyBalance(theirWallet).first;
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
            try {
                if (token) {
                    String aStr = amount.toPlainString();
                    BigDecimal scale = new BigDecimal("1e18");
                    BigDecimal bd = amount.multiply(scale);
                    BigInteger value = bd.toBigIntegerExact();

                    Log.d("POO", value.toString());

                    TransactionReceipt receipt2 = bob.transfer(theirWallet, value).send();
                } else {
                    TransactionReceipt receipt2 = Transfer.sendFunds(Utils.initWeb3j(), Utils.getCreds(deets), to,
                            amount, org.web3j.utils.Convert.Unit.ETHER).send();
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
                myBalance = Utils.getMyBalance(myWallet).first;
                theirBalance = Utils.getMyBalance(theirWallet).first;
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