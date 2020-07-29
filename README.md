# SPO VERIFICATION PROGRAM使用手冊

## 環境需求

-  **Java** 
    - 版本 8 以上
    - 需安裝 Maven
-  **建立私鑰**
    1. 切換至本專案 [itm-spo-sdk-key-generator](./itm-spo-sdk-key-generator/README.md) 目錄下
    2. 依作業系統執行檔案
        - macOS : run_SpoKeyGenerator.sh
        - Linux : run_SpoKeyGenerator.sh
        - Windows : run_SpoKeyGenerator.bat
    3. Output 如下
    ```
    privateKey = <privateKey>
    publicKey = <publicKey>
    clientWalletAddress = <clientWalletAddress>
    ```
-  **取得infura endpoint**
    - [infura 教學](./infura.md)

## 選定驗證資料

-  **Swagger UI**
    1. 首先，至 Batch Server 選定欲驗證之資料
    2. 記錄此筆資料之 CO (Clearance Order) 和 IV (Index Value)
    3. 至 Swagger UI 的 verification-proof-api 選取 Try it out 輸入 CO 和 IV，下載 response (即為 verificationProof )  
-  **Dashboard**
    1. 首先，至 Dashboard 選定欲驗證之資料
    2. 下載此筆資料之 Off-Chain Proof (即為 verificationProof )

## 建立驗證服務

-  VerificationApi 結構如下 :
    ```
    public class VerificationApi {
    
        private final String contractAddress; //該筆資料之contractAddress
        private final String serverWalletAddress; //該筆資料之serverWalletAddress
        private final EthereumNodeConfig config;
        private final VerifyVerificationProofService service;
        
        ...
    }
    
    public class EthereumNodeConfig {
  
        private String privateKeyEnv; //設置環境時建立的私鑰
        private String nodeUrl; //從 infura endpoints 取得，詳述於下
        private String infuraProjectIdEnv; //從 infura endpoints 取得，詳述於下
        private String blockchainExplorerUrl;
        
        ...
        
        public static class Authentication {
            Boolean needAuth; //若為私有鏈則為 true，反之為 false(若為 false，則 username 和 password 可設為空值)
            String username;
            String password;
        }

        private Authentication authentication;
    
    }
-  其中，nodeUrl 和 infuraProjectIdEnv 為 infura endpoints 之拆解，範例如下 : 
   若 infura endpoints 為"https://rinkeby.infura.io/v3/1a2b3c4d5e6f7g8h" ，則
   -  nodeUrl 為"https://rinkeby.infura.io/v3/"
   -  infuraProjectIdEnv 為"1a2b3c4d5e6f7g8h"
   
-  以下為 VerificationApi 建構範例 : 
    ```
    package com.itrustmachines.verification;
    import com.itrustmachines.common.config.EthereumNodeConfig;
    import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;
    import com.itrustmachines.verification.vo.VerifyVerificationProofResult;
    import java.util.List

    public class Demo {

        public static void main(String[] args) {
    
            String contractAddress = <contractAddress>;
            String serverWalletAddress = <serverWalletAddress>;
            EthereumNodeConfig.Authentication auth = new EthereumNodeConfig.Authentication(<needAuth>, <username>, <password>);
            EthereumNodeConfig config = new EthereumNodeConfig(<privateKey>, <nodeUrl>, <infuraProjectIdEnv>, <blockchainExplorerUrl>, auth);
            VerificationApi api = VerificationApi.getInstance(contractAddress, serverWalletAddress, config);

        ...

        }
    }
    ```

## 驗證流程

-  使用 [VerificationProof filePath](./spo-verification-program/src/test/java/com/itrustmachines/verification/sample/filePathSample.java) 驗證
    1. 將下載的 verificationProof.json 檔之路徑貼上至 filePath。

   2. 執行此段程式碼，若是驗證成功則 output 為 true； 反之則為 false。

-  使用 [VerificationProof jsonString](./spo-verification-program/src/test/java/com/itrustmachines/verification/sample/jsonStringSample.java) 驗證
   1. 將下載的 verificationProof.json 檔打開，複製全部內容，貼上至 jsonString。 
    
   2. 執行此段程式碼，若是驗證成功則 output 為 true； 反之則為 false。
    
-  直接使用 VerificationProof 驗證(過程較繁瑣，較不推薦)
   1. 利用下載的 verificationProof.json 檔的資訊，宣告所有的參數，如下 :
    ```
    package com.itrustmachines.verification;
    import com.itrustmachines.common.config.EthereumNodeConfig;
    import com.itrustmachines.verification.vo.VerificationProof;
    import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;
    import com.itrustmachines.verification.vo.VerifyVerificationProofResult;
    import java.util.List;

    public class Demo {

        public static void main(String[] args) {

            String contractAddress = <contractAddress>;
            String serverWalletAddress = <serverWalletAddress>;
            EthereumNodeConfig.Authentication auth = new EthereumNodeConfig.Authentication(<needAuth>, <username>, <password>);
            EthereumNodeConfig config = new EthereumNodeConfig(<privateKey>, <nodeUrl>, <infuraProjectIdEnv>, <blockchainExplorerUrl>, auth);
            VerificationApi api = VerificationApi.getInstance(contractAddress, serverWalletAddress, config);

            ...

	        VerificationProof proof = VerificationProof.builder().query(<query>).contractAddress(<contractAddress>).serverWalletAddress(<serverWalletAddress>).env(<env>).existenceProofs(<existenceProofs>).clearanceRecords(<clearanceRecords>).build();
            VerifyVerificationProofResult result = api.verify(proof);
            List<VerifyReceiptAndMerkleProofResult> proofResultList = result.getVerifyReceiptResults();
            for (int i = 0; i < proofResultList.size(); i++) {
            System.out.println(proofResultList.get(i).isPass());
            }
        }
    }
    ```
   2. 執行此段程式碼，若是驗證成功則 output 為 true； 反之則為 false。
