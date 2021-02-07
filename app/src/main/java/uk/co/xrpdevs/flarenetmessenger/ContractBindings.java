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
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.StaticArray16;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

// This file provides Java Bindings to our Solidity smart contract which we deployed in a previous step.

    /**
     * <p>Auto generated code.
     * <p><strong>Do not modify!</strong>
     * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
     * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
     * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
     *
     * <p>Generated with web3j version 4.0.1.
     */
    public class ContractBindings extends Contract {
        private static final String BINARY = "6080604052600060065534801561001557600080fd5b5061002461003b60201b60201c565b600a80546001600160a01b031916331790556100c4565b3360009081526001602052604090205460ff16151"+
"56100c257336000818152602081815260408083206002548155600354600191820155918290528220805460ff191682179055600b805491820181559091527f0175b7a638427703f0dbe7bb9bbf987a2551717b34e79f33b5b1008d1fa01db9018054600160"+
"0160a01b03191690911790555b565b6105d4806100d36000396000f3fe608060405234801561001057600080fd5b506004361061007d5760003560e01c80636df2ddd81161005b5780636df2ddd8146100b05780636e9a215e146100dc578063a544f67f146"+
"10151578063ec7d3e64146101725761007d565b8063056e7e39146100825780634d3820eb1461008c5780636252194d14610094575b600080fd5b61008a61023d565b005b61008a61025c565b61009c6102e5565b6040805191151582525190819003602001"+
"90f35b61008a600480360360408110156100c657600080fd5b506001600160a01b0381351690602001356102fb565b6100e46103a5565b60405180836001600160a01b03166001600160a01b031681526020018060200182810382528381815181526020019"+
"1508051906020019060200280838360005b8381101561013c578181015183820152602001610124565b50505050905001935050505060405180910390f35b61015961041e565b6040805192835260208301919091528051918290030190f35b61017a610439"+
"565b604051808461020080838360005b838110156101a0578181015183820152602001610188565b505050509050018060200180602001838103835285818151815260200191508051906020019060200280838360005b838110156101e7578181015183820"+
"1526020016101cf565b50505050905001838103825284818151815260200191508051906020019060200280838360005b8381101561022657818101518382015260200161020e565b505050509050019550505050505060405180910390f35b336000908152"+
"6020819052604090206002548155600354600190910155565b3360009081526001602052604090205460ff1615156102e357336000818152602081815260408083206002548155600354600191820155918290528220805460ff191682179055600b8054918"+
"20181559091527f0175b7a638427703f0dbe7bb9bbf987a2551717b34e79f33b5b1008d1fa01db90180546001600160a01b03191690911790555b565b3360009081526001602052604090205460ff1690565b60089081554260099081556007805433600160"+
"0160a01b03199182168117808455600091825260208281526040808420805485526002818101845282862080546001600160a01b039687169089161781558a546001828101919091558a5491830191909155825481019092559984168552848352818520808"+
"20180548752600390910190935293209454855492169190931617835593548285015591549301929092558154019055565b600a54600b8054604080516020808402820181019092528281526000946060946001600160a01b03909116939092839190830182"+
"82801561040f57602002820191906000526020600020905b81546001600160a01b031681526001909101906020018083116103f1575b50505050509050915091509091565b33600090815260208190526040902080546001909101549091565b61044161056"+
"9565b336000908152602081905260409020606090819061045d610569565b6040805160108082526102208201909252606091602082016102008038833950506040805160108082526102208201909252929350606092915060208201610200803883390190"+
"5050905060005b600f81101561055c576104bc610588565b506000818152600386016020908152604091829020825160608101845281546001600160a01b0316815260018201549281018390526002909101549281019290925285836010811061050a57fe5"+
"b60200201528051845185908490811061051f57fe5b6001600160a01b039092166020928302909101909101526040810151835184908490811061054957fe5b60209081029091010152506001016104ab565b5091969195509350915050565b604051806102"+
"0001604052806010906020820280388339509192915050565b60408051606081018252600080825260208201819052918101919091529056fea165627a7a72305820ca1b96b4b6479f4605ed87cc9174380a689e016ab333a75006aa9e003d2ee76c0029";;


        public static final String FUNC_CLEARINBOX = "clearInbox";

        public static final String FUNC_REGISTERUSER = "registerUser";

        public static final String FUNC_CHECKUSERREGISTRATION = "checkUserRegistration";

        public static final String FUNC_SENDMESSAGE = "sendMessage";

        public static final String FUNC_GETCONTRACTPROPERTIES = "getContractProperties";

        public static final String FUNC_GETMYINBOXSIZE = "getMyInboxSize";

        public static final String FUNC_RECEIVEMESSAGES = "receiveMessages";

        @Deprecated
        protected ContractBindings(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
            super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
        }

        protected ContractBindings(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
            super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
        }

        @Deprecated
        protected ContractBindings(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
            super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
        }

        protected ContractBindings(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
            super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
        }

        protected ContractBindings(String contractBinary, String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
            super(contractBinary, contractAddress, web3j, credentials, gasProvider);
        }

        public RemoteCall<TransactionReceipt> clearInbox() {
            final Function function = new Function(
                    FUNC_CLEARINBOX,
                    Arrays.<Type>asList(),
                    Collections.<TypeReference<?>>emptyList());
            return executeRemoteCallTransaction(function);
        }

        public RemoteCall<TransactionReceipt> registerUser() {
            final Function function = new Function(
                    FUNC_REGISTERUSER,
                    Arrays.<Type>asList(),
                    Collections.<TypeReference<?>>emptyList());
            return executeRemoteCallTransaction(function);
        }

        public RemoteCall<Boolean> checkUserRegistration() {
            final Function function = new Function(FUNC_CHECKUSERREGISTRATION,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
            return executeRemoteCallSingleValueReturn(function, Boolean.class);
        }

        public RemoteCall<TransactionReceipt> sendMessage(String _receiver, byte[] _content) {
            final Function function = new Function(
                    FUNC_SENDMESSAGE,
                    Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_receiver),
                            new org.web3j.abi.datatypes.generated.Bytes32(_content)),
                    Collections.<TypeReference<?>>emptyList());
            return executeRemoteCallTransaction(function);
        }

        public RemoteCall<Tuple2<String, List<String>>> getContractProperties() {
            final Function function = new Function(FUNC_GETCONTRACTPROPERTIES,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<DynamicArray<Address>>() {}));
            return new RemoteCall<Tuple2<String, List<String>>>(
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

        public RemoteCall<Tuple2<BigInteger, BigInteger>> getMyInboxSize() {
            final Function function = new Function(FUNC_GETMYINBOXSIZE,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
            return new RemoteCall<Tuple2<BigInteger, BigInteger>>(
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

        public RemoteCall<Tuple3<List<byte[]>, List<BigInteger>, List<String>>> receiveMessages() {
            final Function function = new Function(FUNC_RECEIVEMESSAGES,
                    Arrays.<Type>asList(),
                    Arrays.<TypeReference<?>>asList(new TypeReference<StaticArray16<Bytes32>>() {}, new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<DynamicArray<Address>>() {}));
            return new RemoteCall<Tuple3<List<byte[]>, List<BigInteger>, List<String>>>(
                    new Callable<Tuple3<List<byte[]>, List<BigInteger>, List<String>>>() {
                        @Override
                        public Tuple3<List<byte[]>, List<BigInteger>, List<String>> call() throws Exception {
                            List<Type> results = executeCallMultipleValueReturn(function);
                            return new Tuple3<List<byte[]>, List<BigInteger>, List<String>>(
                                    convertToNative((List<Bytes32>) results.get(0).getValue()),
                                    convertToNative((List<Uint256>) results.get(1).getValue()),
                                    convertToNative((List<Address>) results.get(2).getValue()));
                        }
                    });
        }

        @Deprecated
        public static ContractBindings load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
            return new ContractBindings(contractAddress, web3j, credentials, gasPrice, gasLimit);
        }

        @Deprecated
        public static ContractBindings load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
            return new ContractBindings(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
        }

        public static ContractBindings load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
            return new ContractBindings(contractAddress, web3j, credentials, contractGasProvider);
        }

        public static ContractBindings load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
            return new ContractBindings(contractAddress, web3j, transactionManager, contractGasProvider);
        }

        public static RemoteCall<ContractBindings> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
            return deployRemoteCall(ContractBindings.class, web3j, credentials, contractGasProvider, BINARY, "");
        }

        public static RemoteCall<ContractBindings> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
            return deployRemoteCall(ContractBindings.class, web3j, transactionManager, contractGasProvider, BINARY, "");
        }

        @Deprecated
        public static RemoteCall<ContractBindings> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
            return deployRemoteCall(ContractBindings.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
        }

        @Deprecated
        public static RemoteCall<ContractBindings> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
            return deployRemoteCall(ContractBindings.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
        }


}
