package uk.co.xrpdevs.flarenetmessenger;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.ArrayList;

public class MyService extends Service {

    public SharedPreferences prefs;
    public SharedPreferences.Editor pEdit;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    static String contractAddress = "0x7884C21E95cBBF12A15F7EaF878224633d6ADF54";

   static String wallet = "0x24423475227b49376d72E863bB6c5b6cB4E60Cea";
   static String walletPkey = "0xc6d66f9d9cd4c607e742d27c5fb9a9140465226c19578c02a13931f1fd0c8ef2";

//    static String wallet     =  "0x6B4b502Dc21Aa25d384B5725610272B596ca88ab"; //new wallet
//    static String walletPkey = "0xce38ddf1e5bedd14412416140e7a87d954052864ef6c2d44b6914b5cae39194f"; // new wallet

    static Web3j initWeb3j(){

        Web3j myEtherWallet = Web3j.build(
                new HttpService("https://costone.flare.network/ext/bc/C/rpc"));

        return myEtherWallet;
    }

}