package com.example.web3j.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.model.Sum;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction;


@RestController
public class Web3JController {


    @Autowired
    private Credentials credentials;

    @Autowired
    private Web3j web3j;

    @Value("${to.account}")
    private String toAccount;


    @Value("${from.account}")
    private String fromAccount;


    @Value("${gas.price}")
    private String gasPrice;

    @Value("${gas.limit}")
    private String gasLimit;

    @Value("${transfer.amount}")
    private String transferAmount;

    @Value("${contract.address}")
    private String contractAddress;

    @Value("${test.num1}")
    private String num1;

    @Value("${test.num2}")
    private String num2;


    @GetMapping("/hi")
    public String sayHello() {
        return "Hello Web3!!";
    }

    @GetMapping("/balance")
    public BigInteger getBalance() {
        EthGetBalance balance = null;
        try {
            balance = web3j.ethGetBalance(fromAccount, DefaultBlockParameterName.LATEST).send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return balance.getBalance();
    }

    @GetMapping("/accounts")
    public List<String> getEthAccounts() {
        EthAccounts accounts = null;
        try {
            accounts = web3j.ethAccounts().send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return accounts.getAccounts();
    }


    @GetMapping("/sendFunds")
    public String sendFunds() throws Exception {
        try {
            TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j, credentials, toAccount, new BigDecimal(transferAmount), Convert.Unit.ETHER).send();
            String etherReceipt = transactionReceipt.getTransactionHash();
            return "Receipt" + etherReceipt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/calc")
    public String calcUsingDeployedSmartContract() throws Exception {
        try {
            List<Type> inputParams = Arrays.asList(new Uint256(new BigInteger(num1)), new Uint256(new BigInteger(num2)));
            List<TypeReference<?>> outputParams = Arrays.asList(new TypeReference<Uint256>() {
            });
            Function function = new Function(Sum.FUNC_TAKER, inputParams, outputParams);
            String encodedFunction = FunctionEncoder.encode(function);
            Transaction transaction = createEthCallTransaction(fromAccount, contractAddress, encodedFunction);
            EthCall response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List output = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            Uint256 calcVal = (Uint256) output.get(0);
            return "Calculated value is" + calcVal.getValue();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    @GetMapping("deployContract")
    public String deploySmartContract() {
        Sum contract = null;
        BigInteger calcGasPrice = null;
        try {
            calcGasPrice = Convert.toWei(gasPrice, Convert.Unit.GWEI).toBigInteger();
            contract = Sum.deploy(web3j, credentials, calcGasPrice, new BigInteger(gasLimit)).send();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String contractAddr = contract.getContractAddress();
        return "Contract Deployed At" + contractAddr;

    }


    @GetMapping("transfer")
    public String transferFunds() throws Exception {

        BigInteger calcGas = Convert.toWei(gasPrice, Convert.Unit.GWEI).toBigInteger();
        EthGetTransactionCount transactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.PENDING).send();
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(transactionCount.getTransactionCount(), calcGas, new BigInteger(gasLimit), toAccount, new BigInteger(transferAmount));
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
        String transactionHash = ethSendTransaction.getTransactionHash();
        return "Success " + transactionHash;
    }


}
