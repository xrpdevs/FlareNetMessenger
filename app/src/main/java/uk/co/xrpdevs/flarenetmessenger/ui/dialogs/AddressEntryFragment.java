package uk.co.xrpdevs.flarenetmessenger.ui.dialogs;

import static android.content.Context.CLIPBOARD_SERVICE;
import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;

import java.io.IOException;

import uk.co.xrpdevs.flarenetmessenger.CaptureActivityPortrait;
import uk.co.xrpdevs.flarenetmessenger.PKeyScanner;
import uk.co.xrpdevs.flarenetmessenger.R;
import uk.co.xrpdevs.flarenetmessenger.ViewContact;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
public class AddressEntryFragment extends android.app.DialogFragment {

    public static final String TAG = "PinCodeEntryDialog";
    AddressEntryFragment mThis = this;
    String prompt = "Wallet Address:";
    String tag;
    Activity mAct;

    public interface OnResultListener {
        void onResult(String pinCode, String tag) throws IOException, JSONException;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                //Cancelled
            } else {
                editPinCode.setText(result.getContents());
                myLog("PATAKS", "Scanned Result=" + result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * Factory method to produce PinCodeEntryDialogFragment instance. Use this method for instantiation.
     */
    public AddressEntryFragment newInstance(final OnResultListener listener, String prompt, String tag) {
        mThis.prompt = prompt;
        this.prompt = prompt;
        mThis.tag = tag;
        AddressEntryFragment fragment = new AddressEntryFragment();
        fragment.prompt = prompt;
        fragment.tag = tag;
        fragment.setOnResultListener(listener);
        return fragment;
    }

    private OnResultListener onResultListener;
    private EditText editPinCode;

    /**
     * Sets the ConfigSetListener to receive callback when the PIN code is set.
     */
    private void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View content = getActivity().getLayoutInflater().inflate(R.layout.addrentrydialog, null);

        editPinCode = content.findViewById(R.id.enterWalletAddress);
        //int inputType = InputType.TYPE_CLASS_TEXT;

        // editPinCode.setInputType(inputType);
        editPinCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFieldValid(s.toString())) {
                    editPinCode.setError(null);
                } else {
                    editPinCode.setError(getString(R.string.empty_field));
                }
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setView(content)
                // Button clicks are handled by the DialogFragment!
                .setPositiveButton("OK", null)
                .setNeutralButton("SCAN", null)
                .setNegativeButton("PASTE", null);

        TextView title = new TextView(builder.getContext());
// You Can Customise your Title here
        title.setText(prompt);
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);

        setCancelable(true);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAct = mThis.getActivity();
        final AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        if (dialog != null) {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(Dialog.BUTTON_NEGATIVE);
            Button neutralButton = dialog.getButton(Dialog.BUTTON_NEUTRAL);

            neutralButton.setOnClickListener(v -> { // SCAN button
                // pass a bundle to PKeyScanner.class ?
                // saves duplicating code - possibly update other flows to work this way too..
                myLog("TEST", "QR Scan Button Pressed");
                IntentIntegrator integrator;

                integrator = IntentIntegrator.forFragment(mThis);


                //      new IntentIntegrator(getActivity());
                myLog("DEST_ACTIV", this.getActivity().getLocalClassName());

                IntentIntegrator.forFragment(mThis).setPrompt("QR code will be scanned automatically on focus");
                IntentIntegrator.forFragment(mThis).setCameraId(0);
                IntentIntegrator.forFragment(mThis).setRequestCode(PKeyScanner.PRIV_KEY_REQUEST_CODE);
                IntentIntegrator.forFragment(mThis).setOrientationLocked(true);
                IntentIntegrator.forFragment(mThis).setBeepEnabled(true);
                IntentIntegrator.forFragment(mThis).setCaptureActivity(CaptureActivityPortrait.class);
                IntentIntegrator.forFragment(mThis).setBarcodeImageEnabled(false);
                IntentIntegrator.forFragment(mThis).initiateScan();

                // integrator.start

                //Intent intent = new Intent(mThis.getActivity(), PKeyScanner.class);
                //startActivity(intent);
            });

            negativeButton.setOnClickListener(v -> { // PASTE button
                ClipboardManager clipboard = (ClipboardManager) mAct.getSystemService(CLIPBOARD_SERVICE);

                editPinCode.setText(clipboard.getText());

            });

            positiveButton.setOnClickListener(v -> { // OK button (confirms pasted wallet address
                final String pinCode = editPinCode.getText().toString();
                myLog("PIN", pinCode);
                /*if (isFieldValid(pinCode)) {
                    myLog("PIN", "ifv: " + isFieldValid(pinCode));
                    if (onResultListener != null) {
                        myLog("PIN", "oRL: " + (onResultListener != null));
                        try {
                            onResultListener.onResult(pinCode, tag);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //dialog.dismiss();
                    }
                } else {
                    editPinCode.setError(getString(R.string.empty_field));
                }*/
                Intent abc = new Intent(mThis.getContext(), ViewContact.class);

                Bundle bundle = new Bundle();

                bundle.putString("addr", pinCode);
                bundle.putString("noContact", "1");
                abc.putExtra("contactInfo", bundle);
                //abc.setData(myUri);
                startActivity(abc);
                //    } else {
                //        myLog("doAddContact", "Empty bundle :(");
                //    }

            });
        }
    }

    private boolean isFieldValid(final String value) {
        return !TextUtils.isEmpty(value.trim());
    }
}
