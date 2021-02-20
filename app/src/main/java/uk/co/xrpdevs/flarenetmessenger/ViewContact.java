package uk.co.xrpdevs.flarenetmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ViewContact extends AppCompatActivity {
    TextView contactName;
    TextView xrpAddress;
    Button deleteContact;
    Long rawContactID = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact);
        Intent myIntent = getIntent();
        Bundle bundle = myIntent.getExtras();
        String action = myIntent.getAction();

        contactName   = findViewById(R.id.textView7);
        xrpAddress    = findViewById(R.id.textView8);
        deleteContact = findViewById(R.id.button7);

        deleteContact.setOnClickListener((View.OnClickListener) v -> {
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
            String cNameText = contactsCursor.getString(cNameIndex);

            Log.d("TEST", "XRP Address: " + XRPAddress + " Cname: " + cNameText);
            xrpAddress.setText(XRPAddress);
            contactName.setText(cNameText);
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
}