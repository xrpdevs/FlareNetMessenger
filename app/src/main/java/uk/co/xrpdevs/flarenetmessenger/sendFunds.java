package uk.co.xrpdevs.flarenetmessenger;

import java.math.BigDecimal;

public interface sendFunds {
    void run(String myWallet, String theirWallet, BigDecimal XRPAmount);
}
