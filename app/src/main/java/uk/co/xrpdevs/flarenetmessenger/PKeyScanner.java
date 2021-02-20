package uk.co.xrpdevs.flarenetmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;
import org.web3j.crypto.Credentials;

import java.util.HashMap;


//import me.dm7.barcodescanner.zxing.ZXingScannerView;
//import okhttp3.logging.HttpLoggingInterceptor;

public class PKeyScanner extends AppCompatActivity implements View.OnClickListener {
    SharedPreferences prefs; SharedPreferences.Editor pEdit;
    private static final int ADDRESS_REQUEST_CODE = 7541;
    public static final int PRIV_KEY_REQUEST_CODE = 9554;
    public int SCAN_TYPE;
    Button scan;
    Context mThis;
    IntentIntegrator integrator;
    EditText wName;
    //   @Override
 //   protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
 //       setContentView(R.layout.activity_p_key_scanner);
 //   }

   //     private ZXingScannerView mScannerView;

        @Override
        public void onCreate(Bundle state) {


            super.onCreate(state);
            setContentView(R.layout.activity_import_wallet);

            if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))  {
                Log.d("TEST", "No camera and storage permission");
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);
            }

            prefs = this.getSharedPreferences("fnm", 0);
            pEdit = prefs.edit();

            //integrator.setCaptureActivity(CaptureActivityPortrait.class);
            wName = findViewById(R.id.ImportWallet_name_editText);
            scan = findViewById(R.id.ImportWallet_import_button);
            scan.setOnClickListener(this);
        }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Toast toasty = Toast.makeText(this, "Content:" +requestCode, Toast.LENGTH_LONG);
        toasty.show();
        if (requestCode == PRIV_KEY_REQUEST_CODE) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(resultCode, intent);

            if (resultCode == RESULT_OK) {
                Toast toasty2 = Toast.makeText(this, "Content:" + scanningResult.toString(), Toast.LENGTH_LONG);
                toasty2.show();
                    if (scanningResult != null) {
                        //                        final TextView formatTxt = (TextView)findViewById(R.id.scan_format);
                        //                      final TextView contentTxt = (TextView)findViewById(R.id.scan_content);
                        String scanContent = scanningResult.getContents();
                        String scanFormat = scanningResult.getFormatName();
                        Toast toast = Toast.makeText(this, "Content:" + scanContent + " Format:" + scanFormat, Toast.LENGTH_LONG);
                        Log.d("TEST", scanContent);

                        Credentials cs = Credentials.create(scanContent);

                        String privateKey = cs.getEcKeyPair().getPrivateKey().toString(16);
                        String publicKey = cs.getEcKeyPair().getPublicKey().toString(16);
                        String addr = cs.getAddress();

                        int wC = prefs.getInt("walletCount", 0); wC++;

                        System.out.println("Private key: " + privateKey);
                        System.out.println("Public key: " + publicKey);
                        System.out.println("Address: " + addr);

                        HashMap<String, String> tmp = new HashMap<String, String>();
                        tmp.put("walletName", wName.getText().toString());
                        tmp.put("walletPrvKey", scanContent);
                        tmp.put("walletPubKey", "0x"+publicKey);
                        tmp.put("walletAddress", addr);

                        pEdit.putString("wallet"+String.valueOf(wC), new JSONObject(tmp).toString());
                        pEdit.putInt("walletCount", wC);
                        pEdit.putInt("currentWallet", wC);

                        //                        pEdit.putString("walletPrvKey", ""+scanContent);
//                        pEdit.putString("walletPubKey", "0x"+publicKey);
//                        pEdit.putString("walletAddress",""+addr);
                        pEdit.commit();
                        pEdit.apply();
                        //       formatTxt.setText("FORMAT: " + scanFormat);
                        //         contentTxt.setText("CONTENT: " + scanContent);
                        //we have a result
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "No scan data received!", Toast.LENGTH_SHORT);
                        toast.show();
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

        Log.d("TEST", "Button Pressed");
        integrator = new IntentIntegrator(this);
        integrator.setPrompt("QR code will be scanned automatically on focus");
        integrator.setCameraId(0);
        integrator.setRequestCode(PKeyScanner.PRIV_KEY_REQUEST_CODE);
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }


}