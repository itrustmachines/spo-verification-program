# SPO VERIFICATION PROGRAM使用手冊

## 環境需求

1. Java 8以上，需安裝 Maven
2. 建立私鑰(請執行itm-spo-sdk-key-generator專案):[itm-spo-sdk-key-generator](./itm-spo-sdk-key-generator/README.md) 
3. 註冊infura,並且取得infura endpoint
    - infura教學:[infura](./infura.md)

## 使用流程

1. 建立VerificationApi服務:
    - getInstance(String contractAddress,String serverWalletAddress,EthereumNodeConfig config):VerificationApi
    - [Example](./spo-verification-program/src/main/java/com/itrustmachines/verification/VerificationApi.java)

2. 使用VerificationProof驗證VerificationProof:
    - verify(VerificationProof proof):VerifyVerificationProofResult
    - [Example](./spo-verification-program/src/main/java/com/itrustmachines/verification/VerificationApi.java)
    
3. 使用filePath驗證VerificationProof:
    - verify(String filePath):VerifyVerificationProofResult
    - [Example](./spo-verification-program/src/main/java/com/itrustmachines/verification/VerificationApi.java)
    
4. 使用VerificationProof jsonString驗證VerificationProof:
    - verifyJsonString(String jsonString):VerifyVerificationProofResult
    - [Example](./spo-verification-program/src/main/java/com/itrustmachines/verification/VerificationApi.java)
