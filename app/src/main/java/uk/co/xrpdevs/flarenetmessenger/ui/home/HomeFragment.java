package uk.co.xrpdevs.flarenetmessenger.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import uk.co.xrpdevs.flarenetmessenger.BuildConfig;
import uk.co.xrpdevs.flarenetmessenger.ContactList;
import uk.co.xrpdevs.flarenetmessenger.FirstRun;
import uk.co.xrpdevs.flarenetmessenger.MainActivity;
import uk.co.xrpdevs.flarenetmessenger.PleaseWaitDialog;
import uk.co.xrpdevs.flarenetmessenger.R;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    PleaseWaitDialog notify;

    HomeFragment mThis = this;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_menu_home, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d("FRAG", "HomeFragment");
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });



        //webview.loadUrl("https://xrpdevs.co.uk/");

        return root;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.fr:
                myLog("FR", "FirstRun Selected");
                Intent intent2 = new Intent(mThis.getContext(), FirstRun.class);
                startActivity(intent2);
                return true;
            case R.id.version:
                showDialog("Version: "+ BuildConfig.VERSION_NAME+"\n\nBuild: "+BuildConfig.VERSION_CODE, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean showDialog(String prompt, Boolean cancelable) {
        FragmentManager manager = getParentFragmentManager();

        notify = new PleaseWaitDialog();
        notify.prompt = prompt;
        notify.cancelable = cancelable;

        notify.show(getActivity().getFragmentManager(), "aaa");
        return true;
    }
}