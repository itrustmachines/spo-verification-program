# SPO VERIFICATION PROGRAM使用手冊

## 環境需求

1. **Java** 
    - 版本 8 以上
    - 需安裝 Maven
2. **建立私鑰**
    - 可使用線上開源的工具來產生
         - [VANITY-ETH](https://vanity-eth.tk/)
3. **取得infura endpoint**
    - 若要驗證的證據使用私有鏈，則可跳過此步驟
    - [infura 教學](./doc/infura_zh.md)

## 建立package 連線設定檔
此設定檔將導入ITM函式庫，請在您的 .m2 資料夾中建立 settings.xml 檔，並將下列內容複製貼上。
```
<settings>
  <servers>
    <server>
      <id>kuro-nexus-releases</id>
      <username>guest</username>
      <password>guest</password>
    </server>
  </servers>
</settings>
```

## 選定驗證資料

- **SPO Server**

    透過API取得證據的json字串後存成json檔
    
    可使用SPO Server提供的Swagger UI來進行操作，路徑為/swagger-ui/#/verification-proof-api
    
    - 使用多個ClearanceOrder及IndexValue取得證據
    
        - API Path：/ledger/verify/verificationProof
        
        - Request Body：
        
            ```
                {
                  "clearanceOrderAndIndexValuePairs": [
                    {
                      "clearanceOrder": 0,
                      "indexValue": "string"
                    }
                  ]
                }
            ```
          
            - Example：
            
                ```
                {
                  "clearanceOrderAndIndexValuePairs": [
                    {
                      "clearanceOrder": 1,
                      "indexValue": "Example_R0"
                    },
                    {
                      "clearanceOrder": 1,
                      "indexValue": "Example_R1"
                    }
                  ]
                }
                ```
          
    - 使用ClearanceOrder區間來取得證據
    
        - API Path：/ledger/verify/verificationProofClearanceOrder
        
        - Request Body：
        
            ```
            {
              "fromClearanceOrder": 0,
              "indexValueKey": "string",
              "toClearanceOrder": 0
            }
            ```
          
            - Example：
                ```
                {
                  "fromClearanceOrder": 1,
                  "indexValueKey": "Example",
                  "toClearanceOrder": 2
                }
                ```

    - 使用時間區間取得證據
    
        - API Path：/ledger/verify/verificationProofQuery
        
        - Request Body：
        
            ```
            {
              "fromTimestamp": 0,
              "indexValueKey": "string",
              "toTimestamp": 0
            }
            ```
          
            - Example：
                ```
                {
                  "fromTimestamp": 1606780800000,
                  "indexValueKey": "Example",
                  "toTimestamp": 1606867200000
                }
                ```

-  **Dashboard**
    1. 首先，至 Dashboard 選定欲驗證之資料
    2. 下載此筆資料之 Off-Chain Proof (即為 verificationProof )

## [驗證流程](./src/main/java/com/itrustmachines/verification/VerificationApi.java)

-  使用程式碼驗證

    1. 建立驗證服務

        ```
        final VerificationApi verificationApi = VerificationApi.getInstance();
        ```

    2. 驗證
        若該證據使用的是私有鏈，infuraProjectId可以輸入null
        - 方法一：使用json檔案驗證，其中filePath為證據的檔案路徑，infuraProjectId為環境需求3.取得的ID

            ```
            final VerifyVerificationProofResult result = verificationApi.verify(filePath, infuraProjectId);
            ```

        - 方法二：使用json字串驗證，其中jsonString為驗證的Json字串，infuraProjectId為環境需求3.取得的ID

            ```
            final VerifyVerificationProofResult result = verificationApi.verifyJsonString(jsonString, infuraProjectId);
            ```

    3. 驗證結果

        若result.isPass()結果為true，表示驗證成功，若結果為false，可以從result.getVerifyReceiptResults()中確認是哪一筆資料有誤
    
- 使用CLI進行驗證

    若該證據使用的是私有鏈，無須輸入infuraProjectId
    
    ```
    java -jar spo-verification-program-{VERSION}.jar --proof sample/queryByCO.json --result result.json --infuraProjectId {INFURA_PROJECT_ID}
    ```

    - help

        ```
        usage: verification-api
            --infuraProjectId <infuraProjectId>   required if env is MAINNET, KOVAN, GOERLI, RINKEBY, ROPSTEN
            --proof <filePath>                    input verification proof file path (sample/queryByCO.json)
            --result <filePath>                   output verify result file path (result.json)
        ```