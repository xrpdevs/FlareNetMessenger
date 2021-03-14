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
import java.util.HashMap;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class ViewContact extends AppCompatActivity implements PinCodeDialogFragment.OnResultListener {
    TextView contactName;
    TextView xrpAddress;
    Button deleteContact;
    Long rawContactID = 0L;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    HashMap<String, String> deets;
    PleaseWaitDialog dialogActivity;
    TextView balancesInfo;
    Activity mThis = this;
    String to, from, theirWallet, myWallet, cNameText;
    BigDecimal amount;
    EditText XRPAmount;
    PinCodeDialogFragment pinDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        Intent myIntent = getIntent();
        Bundle bundle = myIntent.getExtras();
        String action = myIntent.getAction();
    //    final String myWallet, theirWallet, cNameText;
        String info;
        BigDecimal myBalance, theirBalance;


        //contactName   = findViewById(R.id.viewContactName);
        xrpAddress    = findViewById(R.id.viewContactWalletAddress);
        deleteContact = findViewById(R.id.button7);
        balancesInfo = findViewById(R.id.balancesInfo);
        XRPAmount = findViewById(R.id.editTextNumberDecimal);
        Button sendFunds = findViewById(R.id.viewContactSendFunds);
        prefs = this.getSharedPreferences("fnm", 0);
        pEdit = prefs.edit();
        String pinCode = prefs.getString("pinCode", "abcd");

        try {
            deets = Utils.getPkey(this, prefs.getInt("currentWallet", 0));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        deleteContact.setOnClickListener(v -> {
            Log.d("TEST", "Deleted: "+ContactsManager.deleteRawContactID(this, rawContactID));
        });


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
        Log.d("TEST", "ViewContact URI: "+uri.toString());
        if(contactsCursor.moveToFirst()) {
            Log.d("TEST", "UriData: "+ DatabaseUtils.dumpCurrentRowToString(contactsCursor));
            int addressColumnIndex = contactsCursor.getColumnIndex(ContactsContract.RawContacts.Entity.DATA3);
            int cNameIndex = contactsCursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);

            rawContactID = contactsCursor.getLong(contactsCursor.getColumnIndex("raw_contact_id"));

          //  rawContactID = Long.getLong(rcID);

            Log.d("TEST", "RAWCONTACTID "+rawContactID);

            String XRPAddress = contactsCursor.getString(addressColumnIndex);
            cNameText = contactsCursor.getString(cNameIndex);

            Log.d("TEST", "XRP Address: " + XRPAddress + " Cname: " + cNameText);
            xrpAddress.setText(XRPAddress);

            myWallet = deets.get("walletAddress");
            myBalance = Utils.getMyBalance(myWallet).first;
            theirWallet = XRPAddress;
            theirBalance = Utils.getMyBalance(theirWallet).first;

            String pubkey = ContactsManager.getPubKey(mThis.getApplicationContext(), theirWallet);

            info = "Your balance: "+ myBalance +"\n"+
                    "Their balance: "+ theirBalance +"\n";

            if(pubkey != null){
                info=info+"Pubkey Present";
            }

            balancesInfo.setText(info);

            sendFunds.setOnClickListener(v -> {

                FragmentManager manager = getFragmentManager();
                pinDialog = new PinCodeDialogFragment().newInstance(this, "Enter PIN:");
                pinDialog.show(manager, "1");



            });

            // contactName.setText(cNameText);
            this.getSupportActionBar().setTitle(cNameText);
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
        myLog("PIN", "Onresult called - PINCODE = "+pinCode);
        if(pinCode.equals(prefs.getString("pinCode", "abcd"))) {
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

    class sendFunds extends Thread  {

        String cNameText;
        Boolean isReplaced = false;

        @Override
        public void run() {
            //String myWallet, String theirWallet, BigDecimal XRPAmount) {
            showDialog("Sending "+amount+" FLR to \n"+cNameText+"\nPlease wait for transaction completion.", false);
            try {
                TransactionReceipt receipt2 = Transfer.sendFunds(Utils.initWeb3j(), Utils.getCreds(deets), to,
                        amount, org.web3j.utils.Convert.Unit.ETHER).send();
            } catch (Exception e) {
                e.printStackTrace();
                dialogActivity.dismiss();
                isReplaced = true;
                showDialog("Transaction to "+cNameText+" failed.\n"+e.getMessage(), true);
            }



            String info2 = "Your balance: " + Utils.getMyBalance(from).first + "\n" +
                    "Their balance: " + Utils.getMyBalance(to).first + "\n";





            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    // mContactList.setAdapter(cursorAdapter);
                    if(!isReplaced){
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