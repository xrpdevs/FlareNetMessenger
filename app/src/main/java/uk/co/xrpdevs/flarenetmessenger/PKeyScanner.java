package uk.co.xrpdevs.flarenetmessenger;

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

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;
import org.web3j.crypto.Credentials;

import java.util.HashMap;

import uk.co.xrpdevs.flarenetmessenger.ui.dialogs.PleaseWaitDialog;

public class PKeyScanner extends AppCompatActivity implements View.OnClickListener {
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;
    private static final int ADDRESS_REQUEST_CODE = 7541;
    public static final int PRIV_KEY_REQUEST_CODE = 9554;
    public int SCAN_TYPE;
    Button scan;
    Context mThis = this;
    IntentIntegrator integrator;
    EditText wName;
    PleaseWaitDialog dialogActivity;

        @Override
        public void onCreate(Bundle state) {


            super.onCreate(state);
            setContentView(R.layout.activity_importwallet);

            if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))  {
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission_group.CONTACTS}, 50);
            }

            prefs = this.getSharedPreferences("fnm", 0);
            pEdit = prefs.edit();
            wName = findViewById(R.id.newWalletName);
            scan = findViewById(R.id.importPubKeyQR);
            scan.setOnClickListener(this);
        }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PRIV_KEY_REQUEST_CODE) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(resultCode, intent);

            if (resultCode == RESULT_OK) {

                    if (scanningResult != null) {

                        String scanContent = scanningResult.getContents();
                        String scanFormat = scanningResult.getFormatName();
                        Log.d("TEST", scanContent);



                        Credentials cs = Credentials.create(scanContent);

                        String privateKey = cs.getEcKeyPair().getPrivateKey().toString(16);
                        String publicKey = cs.getEcKeyPair().getPublicKey().toString(16);
                        String addr = cs.getAddress();

                        //Pair<BigDecimal, String> testAddr = Utils.getMyBalance(addr);


                        if(scanContent.startsWith("0x") && scanContent.length() == 66){
                            int wC = prefs.getInt("walletCount", 0);
                            wC++;

                            System.out.println("Private key: " + privateKey);
                            System.out.println("Public key: " + publicKey);
                            System.out.println("Address: " + addr);

                            HashMap<String, String> tmp = new HashMap<String, String>();
                            if(wName.getText().toString().equals("")){
                                tmp.put("walletName", "Wallet "+ wC);
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

    private boolean showDialog(String prompt, Boolean cancelable) {
        FragmentManager manager = getFragmentManager();

        dialogActivity = new PleaseWaitDialog();
        dialogActivity.prompt = prompt;
        dialogActivity.cancelable = cancelable;
        dialogActivity.show(manager, "DialogActivity");
        return true;
    }


}