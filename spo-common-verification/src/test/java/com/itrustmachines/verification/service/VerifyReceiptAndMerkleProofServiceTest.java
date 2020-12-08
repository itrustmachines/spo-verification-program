package com.itrustmachines.verification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyReceiptAndMerkleProofServiceTest {
  
  // test data are from SpoServer: http://40.114.116.111:4430/
  final String privateKey = "b8059c31844941a8b37d4cac37b331d7b8059c31844941a8b37d4cac37b331d7";
  final String nodeUrl = "https://rinkeby.infura.io/v3/32ccbb538dbf4112a2f50b35bfff2a41";
  final String serverWalletAddress = "0xA197013CA3978962B91f471C4e8C8b6DB42B14D5";
  final String contractAddress = "0x1Bbe2D131a42DaEd0110fd2bE08AF56906A5a1Ce";
  final Gson gson = new Gson();
  
  final String merkleProofJson = "{\"slice\":\"232.8791c49d0fd5e6f701380fa1424c7eaf234dc79b8ff792200eaa3fa9bd6e10d3.e07fc73d85cb7ccb257a438bd489f9fa52f670c6bc6195f5f1638ac2ec6669ad.a224c1d8169880b57cdde9801059c510955f070dac8dfb0f7f66f4c6c508c6b2.a9011f179bb6506a8978595372755c629036db19cec017bdf9c8d10ab5008d50.dc6a30b5a07dfecbabbd437f19f63a47e569d46f48bf66c9c82e2d28a2646bae.0ccf7a2fdaf5c800957223691558cfb1ec3018e147645fdd7c1e64d2f33bc831.c1069b284232710871ea7d907283efba7f6435d40d09f53bf02db3fab1742d84.be89acde1b27e6b9da90a3eb8026d0cfe63f9f6458da3d379dfa75b926f9a065.a4a9a7a1530f85db21caa16711402de24d5c83f16f04a2404c46f6383e6f6818.b55bb08b1978f19a4b97e2e1c52f2d4b151f33c5741b857678f4382614897c48.324fec32b3b28ef363d2830543fcf8216e4e0b9145e5a8b265d11ccdb6353549.554fc02ae671497a7162f4e9b5e00cec01001ceecdf62e023586e6747624cb68.a6c2e02bba70bde5433c4ec6b390cf385a8fc9c4e7d7462d77d8d31549dc2331.983cc722bb263e08bfac0de74aec37559bcd9285623ac253f1cf58a19ea04d40.5f11281bc109ec663bab756bd776204c4614be42a16e9e7fce430da7eecf0ff1\",\"pbPair\":[{\"index\":1,\"keyHash\":\"51c42a1007d27d2962c37a0349bfc6f1981d3087725bea9334aa96b1838c6025\",\"value\":\"8e71557787988fa23c5d74ed4b9a357ca0728cef3a4dfe699b128d4e08828167\"},{\"index\":2,\"keyHash\":\"4133215df9479b5b2efc6c45a9fa8bf8d67b350e37074b8aa6af0618810187fa\",\"value\":\"f33e8d41497dce7b3f8e48e50beac5c67634407493f6bb15c789a7ce0eb3d843\"},{\"index\":3,\"keyHash\":\"b8768182593f12c0a51f8aa30598268b3561ce5a038dd48dc10f5d7a995ef625\",\"value\":\"803c0c577a32761bebd99e00797abd767c7aa4106ab0a9c1c44fad9026baea93\"},{\"index\":4,\"keyHash\":\"cb6bf5c1154fae21f461411fcf7e6099a6cbfa3549c72e41820d2890d26b0d6f\",\"value\":\"7c3a6515a41d33b35dcf9488a1e1d4cfe298789ab7a958c06034959a25a77bde\"}],\"clearanceOrder\":22599,\"sigServer\":{\"r\":\"826113851df2433f73367ee0d06b305aa6cac1ca96eb7c87e72c176bd694f8e5\",\"s\":\"0ed3ef5018403ff95c5d4739859fb91be26ae17fadf297bc85a20af82efc3aed\",\"v\":\"1c\"}}";
  final String receiptJson = "{\"callerAddress\":\"0x281d8fbe2e0d83db1231b6b29c351553d4eb3afe\",\"timestamp\":1594871609074,\"cmd\":\"{\\\"deviceId\\\":\\\"SolarPanel\\\",\\\"timestamp\\\":1594871608847,\\\"watt\\\":1.5264203954214357,\\\"voltage\\\":2.4709677419354836,\\\"current\\\":0.6177419354838709,\\\"solarUtilizationRate\\\":0.12720169961845298}\",\"indexValue\":\"SolarPanel_R129\",\"metadata\":\"\",\"clearanceOrder\":22599,\"sigClient\":{\"r\":\"15bcb1174248f291265b879e1bc8a01253694f0db377838f9128f8d5a07d9387\",\"s\":\"0caa5b2347c56058b02c59e908df430e728bbe8a572fb708e4746b66953aa6b8\",\"v\":\"1b\"},\"timestampSPO\":1594871609186,\"result\":\"ok\",\"sigServer\":{\"r\":\"304152da13ea4d44b5ed2d9b426b47fe685fb8b6fa37d4914520421ff6b57cf3\",\"s\":\"4a8f79ed0618f960a9d1d86f0e8ed3f325c34537a889f8e86eac1e960fdb5b86\",\"v\":\"1c\"}}";
  final String clearanceRecordJson = "{\"id\":3914,\"clearanceOrder\":22599,\"rootHash\":\"5f11281bc109ec663bab756bd776204c4614be42a16e9e7fce430da7eecf0ff1\",\"chainHash\":\"40485da0bfd6815e510756869a2f4f668120c42c3e556891f94a451644df1faa\",\"description\":\"\\n[ITM,RH:5f11281bc109ec663bab756bd776204c4614be42a16e9e7fce430da7eecf0ff1,TS:1594871660818,CO:22599]\",\"createTime\":1594871665000,\"txHash\":\"0xcc6d6b343d61b3fef26959717346f573145b623f3e89d35934bae979128a54c6\"}";
  
  private VerifyReceiptAndMerkleProofService buildService() {
    final ClientContractService clearanceRecordService = new ClientContractService(contractAddress, privateKey, nodeUrl,
        1.0, 5);
    return new VerifyReceiptAndMerkleProofService(serverWalletAddress, clearanceRecordService);
  }
  
  @Test
  public void test_verify() {
    // given
    final VerifyReceiptAndMerkleProofService service = buildService();
    final MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    final Receipt receipt = gson.fromJson(receiptJson, Receipt.class);
    final ClearanceRecord clearanceRecord = gson.fromJson(clearanceRecordJson, ClearanceRecord.class);
    
    // when
    final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, merkleProof, clearanceRecord);
    
    // then
    assertThat(result.isPass()).isTrue();
    assertThat(result.getRootHash()).isEqualTo("5f11281bc109ec663bab756bd776204c4614be42a16e9e7fce430da7eecf0ff1");
  }
  
  @Test
  public void test_verify_merkleProofNull() {
    // given
    final VerifyReceiptAndMerkleProofService service = buildService();
    final Receipt receipt = gson.fromJson(receiptJson, Receipt.class);
    final ClearanceRecord clearanceRecord = gson.fromJson(clearanceRecordJson, ClearanceRecord.class);
    
    assertThatThrownBy(() -> {
      final VerifyReceiptAndMerkleProofResult result = service.verify(receipt, null, clearanceRecord);
    }).isInstanceOf(NullPointerException.class);
  }
  
  @Test
  public void test_verifyMerkleProofSignature_true() {
    // given
    final VerifyReceiptAndMerkleProofService service = buildService();
    final MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    log.debug("merkleProof={}", merkleProof);
    assertThat(merkleProof).isNotNull();
    
    // when
    final boolean result = service.verifyMerkleProofSignature(merkleProof, serverWalletAddress);
    
    // then
    assertThat(result).isTrue();
  }
  
  @Test
  public void test_verifyMerkleProofSignature_changeV() {
    // given: merkleProof signature is wrong, modify merkleProof sigServer::V
    final VerifyReceiptAndMerkleProofService service = buildService();
    MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    SpoSignature sigServer = merkleProof.getSigServer();
    sigServer.setV("2b");
    merkleProof.setSigServer(sigServer);
    
    // when
    final boolean result = service.verifyMerkleProofSignature(merkleProof, serverWalletAddress);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifyMerkleProofSignature_changeS() {
    // given: merkleProof signature is wrong, modify merkleProof sigServer::S
    final VerifyReceiptAndMerkleProofService service = buildService();
    MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    SpoSignature sigServer = merkleProof.getSigServer();
    sigServer.setS("123eea0d5189771346959eb3fd7b7a0e15cd52b2623e05e57d37691c29d33c83");
    merkleProof.setSigServer(sigServer);
    
    // when
    final boolean result = service.verifyMerkleProofSignature(merkleProof, serverWalletAddress);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifyReceiptSignature_true() {
    // given
    final VerifyReceiptAndMerkleProofService service = buildService();
    Receipt receipt = gson.fromJson(receiptJson, Receipt.class);
    
    // when
    final boolean result = service.verifyReceiptSignature(receipt, serverWalletAddress);
    
    // then
    assertThat(result).isTrue();
  }
  
  @Test
  public void test_verifyReceiptSignature_false() {
    // given: modify receipt sigServer::S
    final VerifyReceiptAndMerkleProofService service = buildService();
    Receipt receipt = gson.fromJson(receiptJson, Receipt.class);
    SpoSignature sigServer = receipt.getSigServer();
    sigServer.setS("abcd46e7489b3c4533c650db64a08dc9082b54801028396cce78357d855fa0cd");
    receipt.setSigServer(sigServer);
    
    // when
    final boolean result = service.verifyReceiptSignature(receipt, serverWalletAddress);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifyClearanceOrder() {
    // given
    final VerifyReceiptAndMerkleProofService service = buildService();
    
    // when
    final boolean sameCOResult = service.verifyClearanceOrder(1L, 1L, 1L);
    final boolean diffCOResult = service.verifyClearanceOrder(1L, 1L, 2L);
    
    // then
    assertThat(sameCOResult).isTrue();
    assertThat(diffCOResult).isFalse();
  }
  
  @Test
  public void test_verifyPbPair_true() {
    // given
    final VerifyReceiptAndMerkleProofService service = buildService();
    final MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    final Receipt receipt = gson.fromJson(receiptJson, Receipt.class);
    
    // when
    final boolean result = service.verifyPbPair(receipt, merkleProof);
    
    // then
    assertThat(result).isTrue();
  }
  
  @Test
  public void test_verifyPbPair_modifyReceipt() {
    // given: modify receipt callerAddress
    final VerifyReceiptAndMerkleProofService service = buildService();
    final MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    
    Receipt receipt = gson.fromJson(receiptJson, Receipt.class);
    receipt.setCallerAddress("123");
    
    // when
    final boolean result = service.verifyPbPair(receipt, merkleProof);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifyPbPair_modifyMerkleProof() {
    // given: modify one of the pbPair keyHash in merkleProof
    final VerifyReceiptAndMerkleProofService service = buildService();
    final Receipt receipt = gson.fromJson(receiptJson, Receipt.class);
    
    MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    List<PBPair.PBPairValue> pbPair = merkleProof.getPbPair();
    PBPair.PBPairValue pbPairValue = pbPair.get(0);
    pbPairValue.setKeyHash("123");
    pbPair.set(1, pbPairValue);
    
    // when
    final boolean result = service.verifyPbPair(receipt, merkleProof);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifyMerkleProofSlice_true() {
    // given
    final VerifyReceiptAndMerkleProofService service = buildService();
    final MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    
    // when
    final boolean result = service.verifyMerkleProofSlice(merkleProof);
    
    // then
    assertThat(result).isTrue();
  }
  
  @Test
  public void test_verifyMerkleProofSlice_false() {
    // given: modify merkleProof slice from 150.~ to 123.~
    final VerifyReceiptAndMerkleProofService service = buildService();
    
    MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    merkleProof.setSlice(
        "123.e795d213092445ebd7c9d69ac2abb2f92261bfc3fb5089b880df99ac30d63fe4.3f75cd5138c01ad60f7cdf0fd643e7e9a722d34793caad7908f84ca4e6ca6f02.f8f3363a8f8e4a2865a53f59b5d8f8d5665562148fd384f105f39b09c9851306.655b453b3a7c545a82c4c78f77050b881b70a2b342a027fa81ca51c753455e00.30447ee74b7ca9cae2e1eb4f14f36f7233a74e50c89f51bd4ad95431d10a24c0.53bb846e044428bf0f8e731ee72bfd93c36abbf9a985d5a717a630a539f28dcd.2a92d70a1ff2f2e1080714215773e6eb1843363171ac0609a5e5fffc36be2fd7.efb5da2398678e5b2414152352829bf4b87e8595bd17bb4b919e4e147f62d036.40f70fb830da30c47cc50818c13df1755219d6c114d7bacc26bec1fdf0ab93f5.40d1d730a76e2e6f9bf96b3e6297b2d323ea3224cc2cb897b5c166ca92d774ed.fbcdd8404ad4cfbbfa4bf9bde73c42527b78233123f78255af4de7bc7e781ab5.8a7e62e24e6c4d7b59f2a530582ffae3a2fd34aa0225aad5730e4d8e2af71225.86b626ae10df53688ae134adf550f50fc8bb9d286006ef913bce90a09a0aa185.3f62c7cde4f5245eab5e1fda9455898302653259b2a6041b4f5e29bce48eef4b.8de7dee463f093490fcb57b03b407185448051691520e3afe55936c668871fbc");
    
    // when
    final boolean result = service.verifyMerkleProofSlice(merkleProof);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifyRootHash_true() {
    // given
    final ClearanceRecord clearanceRecord = gson.fromJson(clearanceRecordJson, ClearanceRecord.class);
    
    final VerifyReceiptAndMerkleProofService service = buildService();
    final MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    
    // when
    final boolean result = service.verifyRootHash(merkleProof, clearanceRecord);
    
    // then
    assertThat(result).isTrue();
  }
  
  @Test
  public void test_verifyRootHash_modifyMerkleProof() {
    // given: modify the slice of merkle proof
    final ClearanceRecord clearanceRecord = ClearanceRecord.builder()
                                                           .clearanceOrder(25605L)
                                                           .rootHash(
                                                               "8de7dee463f093490fcb57b03b407185448051691520e3afe55936c668871fbc")
                                                           .chainHash(
                                                               "69062aa7d64f080316fa01720507347c8e28d2a9c693f0d70f1e13b220f2b101")
                                                           .description(
                                                               "[ITM,RH:8de7dee463f093490fcb57b03b407185448051691520e3afe55936c668871fbc,TS:1590030838653,CO:25605]")
                                                           .createTime(1590030845000L)
                                                           .build();
    
    final VerifyReceiptAndMerkleProofService service = buildService();
    MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    merkleProof.setSlice(
        "123.e795d213092445ebd7c9d69ac2abb2f92261bfc3fb5089b880df99ac30d63fe4.3f75cd5138c01ad60f7cdf0fd643e7e9a722d34793caad7908f84ca4e6ca6f02.f8f3363a8f8e4a2865a53f59b5d8f8d5665562148fd384f105f39b09c9851306.655b453b3a7c545a82c4c78f77050b881b70a2b342a027fa81ca51c753455e00.30447ee74b7ca9cae2e1eb4f14f36f7233a74e50c89f51bd4ad95431d10a24c0.53bb846e044428bf0f8e731ee72bfd93c36abbf9a985d5a717a630a539f28dcd.2a92d70a1ff2f2e1080714215773e6eb1843363171ac0609a5e5fffc36be2fd7.efb5da2398678e5b2414152352829bf4b87e8595bd17bb4b919e4e147f62d036.40f70fb830da30c47cc50818c13df1755219d6c114d7bacc26bec1fdf0ab93f5.40d1d730a76e2e6f9bf96b3e6297b2d323ea3224cc2cb897b5c166ca92d774ed.fbcdd8404ad4cfbbfa4bf9bde73c42527b78233123f78255af4de7bc7e781ab5.8a7e62e24e6c4d7b59f2a530582ffae3a2fd34aa0225aad5730e4d8e2af71225.86b626ae10df53688ae134adf550f50fc8bb9d286006ef913bce90a09a0aa185.3f62c7cde4f5245eab5e1fda9455898302653259b2a6041b4f5e29bce48eef4b.8de7dee463f093490fcb57b03b407185448051691520e3afe55936c668871fbc");
    
    // when
    final boolean result = service.verifyRootHash(merkleProof, clearanceRecord);
    
    // then
    assertThat(result).isFalse();
  }
  
  @Test
  public void test_verifyRootHash_modifyRecord() {
    // given: modify the rootHash of clearance record
    final ClearanceRecord clearanceRecord = ClearanceRecord.builder()
                                                           .clearanceOrder(25605L)
                                                           .rootHash(
                                                               "1234dee463f093490fcb57b03b407185448051691520e3afe55936c668871fbc")
                                                           .chainHash(
                                                               "69062aa7d64f080316fa01720507347c8e28d2a9c693f0d70f1e13b220f2b101")
                                                           .description(
                                                               "[ITM,RH:8de7dee463f093490fcb57b03b407185448051691520e3afe55936c668871fbc,TS:1590030838653,CO:25605]")
                                                           .createTime(1590030845000L)
                                                           .build();
    
    final VerifyReceiptAndMerkleProofService service = buildService();
    final MerkleProof merkleProof = gson.fromJson(merkleProofJson, MerkleProof.class);
    
    // when
    final boolean result = service.verifyRootHash(merkleProof, clearanceRecord);
    
    // then
    assertThat(result).isFalse();
  }
  
}