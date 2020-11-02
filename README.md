# web3j-example-project
web3j-example-project 는 web3j 를 이용한 Java Project 에서 ChainZ Besu 테스트넷에 배포되어 있는 SimpleCoin 스마트컨트랙트와 연동하는 방법을 간략히 보여주며 다음 기능들을 포함한다.
- web3j Instance 생성
- Wallet File 로부터 Credentil 설정
- Call Transaction 전송
- Send Transaction 전송

# 개요

web3j 는 이더리움 노드와 통신 및 이더리움 네트워크에 배포되어 있는 스마트컨트랙트와 연동에 필요한 자바 라이브러리 이다.
![enter image description here](http://docs.web3j.io/img/web3j_network.png)
출처: docs.web3j.io

# 설정

Java 프로젝트에서 web3j 를 사용하기 위해서는 Maven 또는 Gradle 을 이용해서 Dependency 를 설정해 주어야 한다.

### Maven
``` Java
<dependency>
        <groupId>org.web3j</groupId>
        <artifactId>core</artifactId>
        <version>4.7.0</version>
</dependency>
```
### Gradle
``` Java
compile ('org.web3j:core:4.7.0')
```
# 샘플 코드

### Web3j 인스턴스 생성

블록체인 노드에 접속하기 위한 Web3j 인스턴스 생성
``` Java
// Creating a web3j instance to connect to ChainZ Besu Testnet.
Web3j web3j = Web3j.build(new HttpService(
        "https://besutest.chainz.network:443"));
```
### Credential 설정

트랜잭션전송 시 서명에 필요한 사용자 계정 정보
``` Java
// Load Ethereum wallet file.
// New wallet files can be created using `WalletUtils.generateNewWalletFile()`.
Credentials credentials =
        WalletUtils.loadCredentials(
                "1234",
                "src/main/resources/ca1fe1c6382be7563f7a57a53932a23f702a43a5.json");
log.info("Credentials loaded");
```
### Call 트랜잭션 전송

스마트컨트랙트에 저장된 데이터를 쿼리
``` Java
...
Function func1 = new Function(
        functionName,
        Arrays.asList(owner),
        Arrays.asList(new TypeReference<Uint256>() {})
);

final String encodedFunc1 = FunctionEncoder.encode(func1);


Transaction tx = Transaction.createEthCallTransaction(callerAddr, contractAddr, encodedFunc1);
EthCall resp = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).sendAsync().get();
...
```
### Send 트랜잭션 전송

스마트컨트랙트에 저장된 데이터를 변경
``` Java
...
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

...
```
# 참고

-   [](http://docs.web3j.io/)[Web3j](http://docs.web3j.io/)﻿﻿
-   [](http://docs.web3j.io/)[ETHEREUM FOR JAVA DEVELOPERS](https://ethereum.org/java/#getting-started-with-smart-contracts-and-solidity)
