package uk.co.xrpdevs.flarenetmessenger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static Web3j initWeb3j(){

        Web3j myEtherWallet = Web3j.build(
                new HttpService("https://costone.flare.network/ext/bc/C/rpc"));

        return myEtherWallet;
    }

}