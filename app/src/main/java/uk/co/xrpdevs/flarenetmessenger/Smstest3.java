package uk.co.xrpdevs.flarenetmessenger;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.8.4.
 */
@SuppressWarnings("rawtypes")
public class Smstest3 extends Contract {
    public static final String BINARY = "6080604052600060065534801561001557600080fd5b5061002764010000000061003e810204565b600a8054600160a060020a031916331790556100d8565b3360009081526001602052604090205460ff166100c1576100666401000000006100c3810204565b3360008181526001602081905260408220805460ff191682179055600b805491820181559091527f0175b7a638427703f0dbe7bb9bbf987a2551717b34e79f33b5b1008d1fa01db9018054600160a060020a03191690911790555b565b33600090815260208190526040812060010155565b610a8a806100e76000396000f3fe608060405234801561001057600080fd5b506004361061009a576000357c0100000000000000000000000000000000000000000000000000000000900480636e9a215e116100785780636e9a215e146100cf578063a544f67f146100e5578063de6f24bb146100fb578063ec7d3e641461010e5761009a565b8063056e7e391461009f5780634d3820eb146100a95780636252194d146100b1575b600080fd5b6100a7610125565b005b6100a761013a565b6100b96101c3565b6040516100c691906109ba565b60405180910390f35b6100d76101d9565b6040516100c69291906108ae565b6100ed610252565b6040516100c69291906109c5565b6100a76101093660046107a1565b61026d565b6101166103af565b6040516100c6939291906108d8565b33600090815260208190526040812060010155565b3360009081526001602052604090205460ff166101c157610159610125565b3360008181526001602081905260408220805460ff191682179055600b805491820181559091527f0175b7a638427703f0dbe7bb9bbf987a2551717b34e79f33b5b1008d1fa01db901805473ffffffffffffffffffffffffffffffffffffffff191690911790555b565b3360009081526001602052604090205460ff1690565b600a54600b805460408051602080840282018101909252828152600094606094600160a060020a0390911693909283919083018282801561024357602002820191906000526020600020905b8154600160a060020a03168152600190910190602001808311610225575b50505050509050915091509091565b33600090815260208190526040902080546001909101549091565b805161028090600890602084019061068d565b5042600955600780543373ffffffffffffffffffffffffffffffffffffffff19918216811780845560009182526020828152604080842080548552600281019092529092208054600160a060020a039092169190931617825560088054919392916001830191906102f0906109d3565b6102fb929190610711565b50600291820154910155805481600061031383610a11565b9091555050600160a060020a03808416600090815260208181526040808320600180820154855260038201909352922060078054825473ffffffffffffffffffffffffffffffffffffffff1916951694909417815560088054939493919283019161037d906109d3565b610388929190610711565b506002918201549101556001810180549060006103a483610a11565b919050555050505050565b33600090815260208190526040812060609182918291806103ce610252565b9150915060608167ffffffffffffffff8111156103fe5760e060020a634e487b7102600052604160045260246000fd5b60405190808252806020026020018201604052801561043157816020015b606081526020019060019003908161041c5790505b50905060008267ffffffffffffffff8111156104605760e060020a634e487b7102600052604160045260246000fd5b604051908082528060200260200182016040528015610489578160200160208202803683370190505b50905060008367ffffffffffffffff8111156104b85760e060020a634e487b7102600052604160045260246000fd5b6040519080825280602002602001820160405280156104e1578160200160208202803683370190505b50905060005b8481101561067e57600081815260038801602090815260408083208151606081019092528054600160a060020a03168252600181018054929391929184019161052f906109d3565b80601f016020809104026020016040519081016040528092919081815260200182805461055b906109d3565b80156105a85780601f1061057d576101008083540402835291602001916105a8565b820191906000526020600020905b81548152906001019060200180831161058b57829003601f168201915b50505050508152602001600282015481525050905080602001518583815181106105e55760e060020a634e487b7102600052603260045260246000fd5b602002602001018190525080600001518483815181106106185760e060020a634e487b7102600052603260045260246000fd5b6020026020010190600160a060020a03169081600160a060020a03168152505080604001518383815181106106605760e060020a634e487b7102600052603260045260246000fd5b6020908102919091010152508061067681610a11565b9150506104e7565b50975095509350505050909192565b828054610699906109d3565b90600052602060002090601f0160209004810192826106bb5760008555610701565b82601f106106d457805160ff1916838001178555610701565b82800160010185558215610701579182015b828111156107015782518255916020019190600101906106e6565b5061070d92915061078c565b5090565b82805461071d906109d3565b90600052602060002090601f01602090048101928261073f5760008555610701565b82601f106107505780548555610701565b8280016001018555821561070157600052602060002091601f016020900482015b82811115610701578254825591600101919060010190610771565b5b8082111561070d576000815560010161078d565b600080604083850312156107b3578182fd5b8235600160a060020a03811681146107c9578283fd5b9150602083013567ffffffffffffffff808211156107e5578283fd5b818501915085601f8301126107f8578283fd5b81358181111561080a5761080a610a3b565b604051601f8201601f19908116603f0116810190838211818310171561083257610832610a3b565b8160405282815288602084870101111561084a578586fd5b82602086016020830137856020848301015280955050505050509250929050565b6000815180845260208085019450808401835b838110156108a3578151600160a060020a03168752958201959082019060010161087e565b509495945050505050565b6000600160a060020a0384168252604060208301526108d0604083018461086b565b949350505050565b606080825284519082018190526000906020906080840190828801845b82811015610911578151845292840192908401906001016108f5565b50505083810382850152610925818761086b565b848103604086015285518082529091508282019083810283018401848801865b838110156109aa57601f1980878503018652825180518086528a5b8181101561097b578281018b01518782018c01528a01610960565b8181111561098b578b8b83890101525b5096890196601f01909116939093018701925090860190600101610945565b50909a9950505050505050505050565b901515815260200190565b918252602082015260400190565b6002810460018216806109e757607f821691505b60208210811415610a0b5760e060020a634e487b7102600052602260045260246000fd5b50919050565b6000600019821415610a345760e060020a634e487b710281526011600452602481fd5b5060010190565b60e060020a634e487b7102600052604160045260246000fdfea2646970667358221220e250a10039109ab57e37e6c4248c779f29c308bdc74e0be5d259ef17cf84117664736f6c63430008010033";

    public static final String FUNC_CHECKUSERREGISTRATION = "checkUserRegistration";

    public static final String FUNC_CLEARINBOX = "clearInbox";

    public static final String FUNC_GETCONTRACTPROPERTIES = "getContractProperties";

    public static final String FUNC_GETMYINBOXSIZE = "getMyInboxSize";

    public static final String FUNC_RECEIVEMESSAGES = "receiveMessages";

    public static final String FUNC_REGISTERUSER = "registerUser";

    public static final String FUNC_SENDMESSAGE = "sendMessage";

    @Deprecated
    protected Smstest3(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Smstest3(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Smstest3(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Smstest3(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteFunctionCall<Boolean> checkUserRegistration() {
        final Function function = new Function(FUNC_CHECKUSERREGISTRATION,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<TransactionReceipt> clearInbox() {
        final Function function = new Function(
                FUNC_CLEARINBOX,
                Arrays.asList(),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple2<String, List<String>>> getContractProperties() {
        final Function function = new Function(FUNC_GETCONTRACTPROPERTIES,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Address>() {}, new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteFunctionCall<Tuple2<String, List<String>>>(function,
                new Callable<Tuple2<String, List<String>>>() {
                    @Override
                    public Tuple2<String, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<String, List<String>>(
                                (String) results.get(0).getValue(),
                                convertToNative((List<Address>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<BigInteger, BigInteger>> getMyInboxSize() {
        final Function function = new Function(FUNC_GETMYINBOXSIZE,
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple2<BigInteger, BigInteger>>(function,
                new Callable<Tuple2<BigInteger, BigInteger>>() {
                    @Override
                    public Tuple2<BigInteger, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<BigInteger, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (BigInteger) results.get(1).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple3<List<BigInteger>, List<String>, List<String>>> receiveMessages() {
        final Function function = new Function(FUNC_RECEIVEMESSAGES,
                Arrays.asList(),
                Arrays.asList(new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<DynamicArray<Address>>() {}, new TypeReference<DynamicArray<Utf8String>>() {}));
        return new RemoteFunctionCall<Tuple3<List<BigInteger>, List<String>, List<String>>>(function,
                new Callable<Tuple3<List<BigInteger>, List<String>, List<String>>>() {
                    @Override
                    public Tuple3<List<BigInteger>, List<String>, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<List<BigInteger>, List<String>, List<String>>(
                                convertToNative((List<Uint256>) results.get(0).getValue()),
                                convertToNative((List<Address>) results.get(1).getValue()),
                                convertToNative((List<Utf8String>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> registerUser() {
        final Function function = new Function(
                FUNC_REGISTERUSER,
                Arrays.asList(),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> sendMessage(String _receiver, String _content) {
        final Function function = new Function(
                FUNC_SENDMESSAGE,
                Arrays.asList(new org.web3j.abi.datatypes.Address(160, _receiver),
                        new org.web3j.abi.datatypes.Utf8String(_content)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Smstest3 load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Smstest3(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Smstest3 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Smstest3(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Smstest3 load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Smstest3(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Smstest3 load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Smstest3(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Smstest3> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Smstest3.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    public static RemoteCall<Smstest3> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(Smstest3.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Smstest3> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Smstest3.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<Smstest3> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(Smstest3.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }
}