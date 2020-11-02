package com.skcc.bcsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * An example web3j application to show how to use web3j to interact with smart contracts deployed on Ethereum network.
 */

public class Application {

    private static final Logger log = LoggerFactory.getLogger(com.skcc.bcsvc.Application.class);

    public static void main(String[] args) throws Exception {
        new com.skcc.bcsvc.Application().run();
    }

    private void run() throws Exception {

        // Creating a web3j instance to connect to ChainZ Besu Testnet.
        Web3j web3j = Web3j.build(new HttpService(
                "https://besutest.chainz.network:443"));
        log.info("Connected to Ethereum client version: "
                + web3j.web3ClientVersion().send().getWeb3ClientVersion());

        // Load Ethereum wallet file.
        // New wallet files can be created using `WalletUtils.generateNewWalletFile()`.
        Credentials credentials =
                WalletUtils.loadCredentials(
                        "1234",
                        "src/main/resources/ca1fe1c6382be7563f7a57a53932a23f702a43a5.json");
        log.info("Credentials loaded");

        String callerAddr = credentials.getAddress();
        // Address of a `SimpleCoin` Smart Contract deployed on ChainZ Besu Testnet.
        // Source code at src/main/smartcontracts/SimpleCoin.sol.
        String contractAddr = "0x1E5FA5DEEc9aeF3Bba765d00B41F41BA618E519A";

        /*
            1. Call Transaction; Read values from smart contracts.
            - Get current balance of `ownerAddr`
            - Calling the function `balanceOf` of SimpleCoin.sol
         */

        final String ownerAddr = "0xca1fe1c6382be7563f7a57a53932a23f702a43a5";

        String functionName = "balanceOf";
        Address owner = new Address(ownerAddr);

        log.info("1. Check the initial balance of {}.", ownerAddr);

        Function func1 = new Function(
                functionName,
                Arrays.asList(owner),
                Arrays.asList(new TypeReference<Uint256>() {})
        );

        final String encodedFunc1 = FunctionEncoder.encode(func1);

        log.info("The address to process : {}", callerAddr);
        log.info("ABI encode for 'balanceOf(address:...)' : {}", encodedFunc1);

        Transaction tx = Transaction.createEthCallTransaction(callerAddr, contractAddr, encodedFunc1);
        EthCall resp = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).sendAsync().get();

        List<Type> output = FunctionReturnDecoder.decode(resp.getResult(), func1.getOutputParameters());

        log.info("The output size : {}", output.size());

        Uint balance = (Uint)output.get(0);

        log.info("The balance : {}", balance.getValue().toString());


        /*
            2. Send Transaction; Update values of smart contracts
            - Add balances to `ownerAdr`
            - Calling `mint` of SimpleCoin.sol
         */

        String functionName2 = "mint";
        Uint amount = new Uint(BigInteger.valueOf(100));

        log.info("2. Mint {} tokens to {}", amount.getValue(), owner );

        Function func2 = new Function(
                functionName2,
                Arrays.asList(owner, amount),
                Collections.emptyList()
        );

        final String encodedFunc2 = FunctionEncoder.encode(func2);

        FastRawTransactionManager txMgr = new FastRawTransactionManager(web3j, credentials);

        String txHash = txMgr.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(10_000_000), contractAddr, encodedFunc2, BigInteger.ZERO).getTransactionHash();
        log.info("Waiting receipt - txHahs: {}, function: {}, contract: {}", new Object[]{txHash, functionName2, contractAddr});
        TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3j, 100, 1000);
        TransactionReceipt receipt = receiptProcessor.waitForTransactionReceipt(txHash);

        log.info("Recetipt: " + receipt);

        /*
            3. Call Transaction; Read values from smart contracts after minting the `ownerAddr`.
            - Get current balance of `ownerAddr`
            - Calling the function `balanceOf` of SimpleCoin.sol again.
         */

        log.info("3. Check the balance of {} after mint.", ownerAddr);

        tx = Transaction.createEthCallTransaction(callerAddr, contractAddr, encodedFunc1);
        resp = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).sendAsync().get();

        output = FunctionReturnDecoder.decode(resp.getResult(), func1.getOutputParameters());

        log.info("The output size : {}", output.size());

        balance = (Uint)output.get(0);

        log.info("The balance : {}", balance.getValue().toString());
    }
}
