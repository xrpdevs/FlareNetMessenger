package uk.co.xrpdevs.flarenetmessenger;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import net.lingala.zip4j.exception.ZipException;

import java.security.GeneralSecurityException;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

/**
 * DialogFragment to display a PIN-code entry dialog.
 */
public class EnterMsgDialogFragment extends android.app.DialogFragment {

    public static final String TAG = "PinCodeEntryDialog";
    EnterMsgDialogFragment mThis = this;
    public String titletxt = "No title";
    public String prompt = "Enter Pin Code";
    public Boolean cancelable = true;
    public Boolean isEncReqd = false;

    public interface OnResultListener {
        void onResult(String pinCode, Boolean enc) throws ZipException, GeneralSecurityException;
    }

    /**
     * Factory method to produce PinCodeEntryDialogFragment instance. Use this method for instantiation.
     */
    public EnterMsgDialogFragment newInstance(final OnResultListener listener, String title, String prompt, Boolean cancelable) {
        mThis.prompt = prompt;
        this.prompt = prompt;
        EnterMsgDialogFragment fragment = new EnterMsgDialogFragment();
        fragment.prompt = prompt;
        fragment.cancelable = cancelable;
        fragment.titletxt = title;
        fragment.setOnResultListener(listener);
        return fragment;
    }

    private OnResultListener onResultListener;
    private EditText editMessage;
    private SwitchCompat encSwitch;

    /**
     * Sets the ConfigSetListener to receive callback when the PIN code is set.
     */
   private void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View content = getActivity().getLayoutInflater().inflate(R.layout.dialog_message_reply, null);
/*        getDialog().getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        WindowManager.LayoutParams p = getDialog().getWindow().getAttributes();
        p.width = ViewGroup.LayoutParams.MATCH_PARENT;
        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        getDialog().getWindow().setAttributes(p);*/
        editMessage = content.findViewById(R.id.editMessage);
        int inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD;
        //editMessage.setInputType(inputType);
        SwitchCompat encryptionSwitch = content.findViewById(R.id.switch1);

        encryptionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isEncReqd = isChecked;
            }
        });

        editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int maxlen; String error;
                if(isEncReqd){
                    maxlen = 150;
                    error="Encrypted Message\nMax Length: 150";
                } else {
                    maxlen = 320;
                    error="Max Length: 320";
                }
                maxlen = 1000;
                if(s.toString().length() > maxlen){
                    editMessage.setError(error);
                } else if (isFieldValid(s.toString())) {
                    editMessage.setError(null);
                } else  {
                    editMessage.setError(getString(R.string.empty_field));
                }
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
                // Button clicks are handled by the DialogFragment!
            //    .setPositiveButton(R.string.connect, null);

        TextView title = content.findViewById(R.id.replyTitle);
        TextView rText = content.findViewById(R.id.theirMessage);
        ImageButton img = content.findViewById(R.id.imageButton);
        rText.setText(prompt);
// You Can Customise your Title here
        title.setText(titletxt);
        title.setBackgroundColor(Color.BLACK);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

      //  builder.setCustomTitle(title);

        builder.setCancelable(true);
        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.x = 0;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
        //dialog.BUTTON_POSITIVE = img;
        return dialog;
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
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            ImageButton positiveButton = dialog.findViewById(R.id.imageButton);
            encSwitch = dialog.findViewById(R.id.switch1);
            positiveButton.setOnClickListener(v -> {
                final String pinCode = editMessage.getText().toString();
                myLog("PIN", pinCode);
                if (isFieldValid(pinCode)) {
                    myLog("PIN", "ifv: "+isFieldValid(pinCode));
                    if (onResultListener != null) {
                        myLog("PIN", "oRL: "+(onResultListener != null));
                        try {
                            onResultListener.onResult(pinCode, encSwitch.isChecked());
                        } catch (ZipException | GeneralSecurityException e) {
                            e.printStackTrace();
                        }
                        //dialog.dismiss();
                    }
                } else {
                    editMessage.setError(getString(R.string.empty_field));
                }
            });
        }
    }

    private boolean isFieldValid(final String value) {
        return !TextUtils.isEmpty(value.trim());
    }
}
