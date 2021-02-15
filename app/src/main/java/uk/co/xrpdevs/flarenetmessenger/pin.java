package uk.co.xrpdevs.flarenetmessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

public class pin extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    EditText enter_mpin;
    ImageView i1, i2, i3, i4;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        prefs = this.getSharedPreferences("fnm", 0);

        i1 = (ImageView) findViewById(R.id.imageview_circle1);
        i2 = (ImageView) findViewById(R.id.imageview_circle2);
        i3 = (ImageView) findViewById(R.id.imageview_circle3);
        i4 = (ImageView) findViewById(R.id.imageview_circle4);

        enter_mpin = (EditText) findViewById(R.id.editText_enter_mpin);
        enter_mpin.requestFocus();
        enter_mpin.setInputType(InputType.TYPE_CLASS_NUMBER);
        enter_mpin.setFocusableInTouchMode(true);

        enter_mpin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }


       //     @Override
         //   public void afterTextChanged(Editable s) {
                @Override
                public void afterTextChanged(Editable s) {
                    switch (s.length()) {
                        case 4:
                            i4.setImageResource(R.drawable.circle2);
                            break;
                        case 3:
                            i4.setImageResource(R.drawable.circle);
                            i3.setImageResource(R.drawable.circle2);
                            break;
                        case 2:
                            i3.setImageResource(R.drawable.circle);
                            i2.setImageResource(R.drawable.circle2);
                            break;
                        case 1:
                            i2.setImageResource(R.drawable.circle);
                            i1.setImageResource(R.drawable.circle2);
                            break;
                        default:
                            i1.setImageResource(R.drawable.circle);
                    }
           //     } Log.d(TAG, "onKey: screen key pressed");
           //     i1.setImageResource(R.drawable.circle2);
            }
        });
    }
}