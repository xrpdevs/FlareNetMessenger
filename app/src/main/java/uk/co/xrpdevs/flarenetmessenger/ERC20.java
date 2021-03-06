package uk.co.xrpdevs.flarenetmessenger;

import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;

public class ERC20 {
    String erc20Contract;

    TransactionReceipt sendFunds;
    String walletAddress;

    BigDecimal getBalance(){
        String wA = walletAddress;

        return(new BigDecimal(0));
    }
}
