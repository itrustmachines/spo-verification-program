package com.itrustmachines.verification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.*;

import com.google.gson.Gson;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.verification.vo.ExistenceProof;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("CodeBlock2Expr")
@Slf4j
public class VerifyReceiptAndMerkleProofServiceTest_Null_TestCase {
  
  // test data are from SpoServer: http://40.114.116.111:4430/
  public static final String privateKey = "b8059c31844941a8b37d4cac37b331d7b8059c31844941a8b37d4cac37b331d7";
  public static final String nodeUrl = "https://rinkeby.infura.io/v3/32ccbb538dbf4112a2f50b35bfff2a41";
  public static final String serverWalletAddress = "0xA197013CA3978962B91f471C4e8C8b6DB42B14D5";
  public static final String contractAddress = "0x1Bbe2D131a42DaEd0110fd2bE08AF56906A5a1Ce";
  public static final Gson gson = new Gson();
  
  public static final String merkleProofJson = "{\"slice\":\"232.8791c49d0fd5e6f701380fa1424c7eaf234dc79b8ff792200eaa3fa9bd6e10d3.e07fc73d85cb7ccb257a438bd489f9fa52f670c6bc6195f5f1638ac2ec6669ad.a224c1d8169880b57cdde9801059c510955f070dac8dfb0f7f66f4c6c508c6b2.a9011f179bb6506a8978595372755c629036db19cec017bdf9c8d10ab5008d50.dc6a30b5a07dfecbabbd437f19f63a47e569d46f48bf66c9c82e2d28a2646bae.0ccf7a2fdaf5c800957223691558cfb1ec3018e147645fdd7c1e64d2f33bc831.c1069b284232710871ea7d907283efba7f6435d40d09f53bf02db3fab1742d84.be89acde1b27e6b9da90a3eb8026d0cfe63f9f6458da3d379dfa75b926f9a065.a4a9a7a1530f85db21caa16711402de24d5c83f16f04a2404c46f6383e6f6818.b55bb08b1978f19a4b97e2e1c52f2d4b151f33c5741b857678f4382614897c48.324fec32b3b28ef363d2830543fcf8216e4e0b9145e5a8b265d11ccdb6353549.554fc02ae671497a7162f4e9b5e00cec01001ceecdf62e023586e6747624cb68.a6c2e02bba70bde5433c4ec6b390cf385a8fc9c4e7d7462d77d8d31549dc2331.983cc722bb263e08bfac0de74aec37559bcd9285623ac253f1cf58a19ea04d40.5f11281bc109ec663bab756bd776204c4614be42a16e9e7fce430da7eecf0ff1\",\"pbPair\":[{\"index\":1,\"keyHash\":\"51c42a1007d27d2962c37a0349bfc6f1981d3087725bea9334aa96b1838c6025\",\"value\":\"8e71557787988fa23c5d74ed4b9a357ca0728cef3a4dfe699b128d4e08828167\"},{\"index\":2,\"keyHash\":\"4133215df9479b5b2efc6c45a9fa8bf8d67b350e37074b8aa6af0618810187fa\",\"value\":\"f33e8d41497dce7b3f8e48e50beac5c67634407493f6bb15c789a7ce0eb3d843\"},{\"index\":3,\"keyHash\":\"b8768182593f12c0a51f8aa30598268b3561ce5a038dd48dc10f5d7a995ef625\",\"value\":\"803c0c577a32761bebd99e00797abd767c7aa4106ab0a9c1c44fad9026baea93\"},{\"index\":4,\"keyHash\":\"cb6bf5c1154fae21f461411fcf7e6099a6cbfa3549c72e41820d2890d26b0d6f\",\"value\":\"7c3a6515a41d33b35dcf9488a1e1d4cfe298789ab7a958c06034959a25a77bde\"}],\"clearanceOrder\":22599,\"sigServer\":{\"r\":\"826113851df2433f73367ee0d06b305aa6cac1ca96eb7c87e72c176bd694f8e5\",\"s\":\"0ed3ef5018403ff95c5d4739859fb91be26ae17fadf297bc85a20af82efc3aed\",\"v\":\"1c\"}}";
  public static final String receiptJson = "{\"callerAddress\":\"0x281d8fbe2e0d83db1231b6b29c351553d4eb3afe\",\"timestamp\":1594871609074,\"cmd\":\"{\\\"deviceId\\\":\\\"SolarPanel\\\",\\\"timestamp\\\":1594871608847,\\\"watt\\\":1.5264203954214357,\\\"voltage\\\":2.4709677419354836,\\\"current\\\":0.6177419354838709,\\\"solarUtilizationRate\\\":0.12720169961845298}\",\"indexValue\":\"SolarPanel_R129\",\"metadata\":\"\",\"clearanceOrder\":22599,\"sigClient\":{\"r\":\"15bcb1174248f291265b879e1bc8a01253694f0db377838f9128f8d5a07d9387\",\"s\":\"0caa5b2347c56058b02c59e908df430e728bbe8a572fb708e4746b66953aa6b8\",\"v\":\"1b\"},\"timestampSPO\":1594871609186,\"result\":\"ok\",\"sigServer\":{\"r\":\"304152da13ea4d44b5ed2d9b426b47fe685fb8b6fa37d4914520421ff6b57cf3\",\"s\":\"4a8f79ed0618f960a9d1d86f0e8ed3f325c34537a889f8e86eac1e960fdb5b86\",\"v\":\"1c\"}}";
  public static final String clearanceRecordJson = "{\"id\":3914,\"clearanceOrder\":22599,\"rootHash\":\"5f11281bc109ec663bab756bd776204c4614be42a16e9e7fce430da7eecf0ff1\",\"chainHash\":\"40485da0bfd6815e510756869a2f4f668120c42c3e556891f94a451644df1faa\",\"description\":\"\\n[ITM,RH:5f11281bc109ec663bab756bd776204c4614be42a16e9e7fce430da7eecf0ff1,TS:1594871660818,CO:22599]\",\"createTime\":1594871665000,\"txHash\":\"0xcc6d6b343d61b3fef26959717346f573145b623f3e89d35934bae979128a54c6\"}";
  
  private static VerifyReceiptAndMerkleProofService service;
  
  private Receipt receipt;
  private MerkleProof merkleProof;
  private ClearanceRecord clearanceRecord;
  private ExistenceProof existenceProof;
  
  @BeforeAll
  public static void initService() {
    final ClientContractService clearanceRecordService = new ClientContractService(contractAddress, privateKey, nodeUrl,
        1.0, 5);
    service = new VerifyReceiptAndMerkleProofService(serverWalletAddress, clearanceRecordService);
    assertThat(service).isNotNull();
  }
  
  @BeforeEach
  public void reset() {
    receipt = gson.fromJson(receiptJson, Receipt.class);
    merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    clearanceRecord = gson.fromJson(clearanceRecordJson, ClearanceRecord.class);
    existenceProof = ExistenceProof.builder()
                                   .clearanceOrder(receipt.getClearanceOrder())
                                   .indexValue(receipt.getIndexValue())
                                   .exist(true)
                                   .receipt(receipt)
                                   .merkleProof(merkleProof)
                                   .build();
  }
  
  @Test
  @Order(1)
  public void test_receipt_null() {
    // given
    receipt = null;
    existenceProof.setReceipt(null);
    
    // then
    assertThatThrownBy(() -> {
      service.verify(receipt, merkleProof, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> {
      service.verify(existenceProof, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  @Order(2)
  public void test_receipt_callerAddress_null() {
    // given
    receipt.setCallerAddress(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(3)
  public void test_receipt_timestamp_null() {
    // given
    receipt.setTimestamp(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(4)
  public void test_receipt_cmd_null() {
    // given
    receipt.setCmd(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(5)
  public void test_receipt_indexValue_null() {
    // given
    receipt.setIndexValue(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(6)
  public void test_receipt_metadata_null() {
    // given
    receipt.setMetadata(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(7)
  public void test_receipt_clearanceOrder_null() {
    // given
    receipt.setClearanceOrder(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Disabled
  @Test
  @Order(8)
  public void test_receipt_sigClient_null() {
    // given
    receipt.setSigClient(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(9)
  public void test_receipt_sigClient_r_null() {
    // given
    receipt.getSigClient()
           .setR(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(10)
  public void test_receipt_sigClient_s_null() {
    // given
    receipt.getSigClient()
           .setS(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(11)
  public void test_receipt_sigClient_v_null() {
    // given
    receipt.getSigClient()
           .setV(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(12)
  public void test_receipt_timestampSPO_null() {
    // given
    receipt.setTimestampSPO(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(13)
  public void test_receipt_result_null() {
    // given
    receipt.setResult(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Disabled
  @Test
  @Order(14)
  public void test_receipt_sigServer_null() {
    // given
    receipt.setSigServer(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(15)
  public void test_receipt_sigServer_r_null() {
    // given
    receipt.getSigServer()
           .setR(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(16)
  public void test_receipt_sigServer_s_null() {
    // given
    receipt.getSigServer()
           .setS(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(17)
  public void test_receipt_sigServer_v_null() {
    // given
    receipt.getSigServer()
           .setV(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsReceiptSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsReceiptSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(18)
  public void test_merkleProof_null() {
    // given
    merkleProof = null;
    existenceProof.setMerkleProof(null);
    
    // then
    assertThatThrownBy(() -> {
      service.verify(receipt, merkleProof, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> {
      service.verify(existenceProof, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  @Order(19)
  public void test_verify_merkleProof_slice_null() {
    // given
    merkleProof.setSlice(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Disabled
  @Test
  @Order(20)
  public void test_verify_merkleProof_pbPair_null() {
    // given
    merkleProof.setPbPair(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(21)
  public void test_verify_merkleProof_pbPair_index_null() {
    // given
    merkleProof.getPbPair()
               .forEach(pbPairValue -> {
                 pbPairValue.setIndex(null);
               });
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(22)
  public void test_verify_merkleProof_pbPair_keyHash_null() {
    // given
    merkleProof.getPbPair()
               .forEach(pbPairValue -> {
                 pbPairValue.setKeyHash(null);
               });
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(23)
  public void test_verify_merkleProof_pbPair_value_null() {
    // given
    merkleProof.getPbPair()
               .forEach(pbPairValue -> {
                 pbPairValue.setValue(null);
               });
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(24)
  public void test_verify_merkleProof_clearanceOrder_null() {
    // given
    merkleProof.setClearanceOrder(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(25)
  public void test_verify_merkleProof_sigServer_r_null() {
    // given
    merkleProof.getSigServer()
               .setR(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(26)
  public void test_verify_merkleProof_sigServer_s_null() {
    // given
    merkleProof.getSigServer()
               .setS(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(27)
  public void test_verify_merkleProof_sigServer_v_null() {
    // given
    merkleProof.getSigServer()
               .setV(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsMerkleProofSignatureError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsMerkleProofSignatureError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(28)
  public void test_clearanceRecord_null() {
    // given
    clearanceRecord = null;
    
    // then
    assertThatThrownBy(() -> {
      service.verify(receipt, merkleProof, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> {
      service.verify(existenceProof, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  @Order(29)
  public void test_verify_clearanceRecord_clearanceOrder_null() {
    // given
    clearanceRecord.setClearanceOrder(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsClearanceOrderError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsClearanceOrderError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Disabled
  @Test
  @Order(30)
  public void test_verify_clearanceRecord_rootHash_null() {
    // given
    clearanceRecord.setRootHash(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsClearanceRecordRootHashError(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsClearanceRecordRootHashError(existenceProofResult, existenceProof,
        clearanceRecord);
  }
  
  @Test
  @Order(31)
  public void test_verify_clearanceRecord_chainHash_null() {
    // given
    clearanceRecord.setChainHash(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsOK(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsOK(existenceProofResult, existenceProof, clearanceRecord);
  }
  
  @Test
  @Order(32)
  public void test_verify_clearanceRecord_description_null() {
    // given
    clearanceRecord.setDescription(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsOK(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsOK(existenceProofResult, existenceProof, clearanceRecord);
  }
  
  @Test
  @Order(33)
  public void test_verify_clearanceRecord_createTime_null() {
    // given
    clearanceRecord.setCreateTime(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsOK(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsOK(existenceProofResult, existenceProof, clearanceRecord);
  }
  
  @Test
  @Order(34)
  public void test_verify_clearanceRecord_txHash_null() {
    // given
    clearanceRecord.setTxHash(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertIsOK(result, receipt, merkleProof, clearanceRecord);
    VerifyServiceTestUtil.assertExistenceProofIsOK(existenceProofResult, existenceProof, clearanceRecord);
  }
  
  @Test
  @Order(35)
  public void test_existenceProof_null() {
    // given
    existenceProof = null;
    
    // then
    assertThatThrownBy(() -> {
      service.verify(existenceProof, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Disabled
  @Test
  @Order(36)
  public void test_verify_existenceProof_clearanceOrder_null() {
    // given
    existenceProof.setClearanceOrder(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertExistenceProofIsNotPassError(existenceProofResult, existenceProof, clearanceRecord);
  }
  
  @Disabled
  @Test
  @Order(37)
  public void test_verify_existenceProof_indexValue_null() {
    // given
    existenceProof.setIndexValue(null);
    
    // when
    final VerifyReceiptAndMerkleProofResult existenceProofResult = service.verify(existenceProof, clearanceRecord);
    
    // then
    VerifyServiceTestUtil.assertExistenceProofIsNotPassError(existenceProofResult, existenceProof, clearanceRecord);
  }
  
}
