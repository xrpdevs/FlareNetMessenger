package uk.co.xrpdevs.flarenetmessenger;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import org.xmlpull.v1.XmlPullParser;

public class SettingsActivity extends AppCompatActivity {
    static Context bob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        bob = this;

    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference myPref = (Preference) findPreference("myKey");
            assert myPref != null;
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    XmlPullParser parser = getResources().getXml(R.xml.root_preferences);
                    try {
                        parser.next();
                        parser.nextTag();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    AttributeSet attr = Xml.asAttributeSet(parser);
                    int count = attr.getAttributeCount();
                    PrivateKeyDialog poo = new PrivateKeyDialog(bob, attr);
                    return true;
                }
            });
        }
    }
}