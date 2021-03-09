package uk.co.xrpdevs.flarenetmessenger.ui.home;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.zxing.qrcode.QRCodeWriter;

import uk.co.xrpdevs.flarenetmessenger.BuildConfig;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    SharedPreferences prefs;
    SharedPreferences.Editor pEdit;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("FlareNetMessenger\nVersion "+ BuildConfig.VERSION_NAME+" Build "+BuildConfig.VERSION_CODE);


    }

    public LiveData<String> getText() {
        return mText;
    }
}