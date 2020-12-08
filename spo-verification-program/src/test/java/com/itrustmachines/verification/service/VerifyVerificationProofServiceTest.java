package com.itrustmachines.verification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.itrustmachines.common.util.IndexValueProperties;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.verification.config.InfuraNodeUrlConfig;
import com.itrustmachines.verification.constants.VerifyNotExistProofStatus;
import com.itrustmachines.verification.util.QueryStringParser;
import com.itrustmachines.verification.util.VerificationProofParserTest;
import com.itrustmachines.verification.vo.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyVerificationProofServiceTest {
  
  private final static String NODE_URL = "https://rinkeby.infura.io/v3/bcd2ba30ecc442168521ce75db01a120";
  
  @Test
  public void test_verify_1() {
    final VerificationProof proof = VerificationProofParserTest.getTestVerificationProof();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    final VerifyVerificationProofResult result = service.verify(proof, InfuraNodeUrlConfig.toProjectId(NODE_URL));
    log.info("result={}", result);
    assertThat(result).isNotNull();
    assertThat(result.isPass()).isTrue();
    assertThat(result.getProofCount()).isEqualTo(1);
    assertThat(result.getVerifyReceiptResults()
                     .size()).isEqualTo(1);
  }
  
  @Test
  public void test_verify_3() {
    final VerificationProof proof = VerificationProofParserTest.getTestAzureQuorumVerificationProof();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    final VerifyVerificationProofResult result = service.verify(proof, InfuraNodeUrlConfig.toProjectId(NODE_URL));
    log.info("result={}", result);
    assertThat(result).isNotNull();
    assertThat(result.getQuery()).isEqualTo("Locators=(58:Swanky-VirtualSpoClient_R62)");
    assertThat(result.isPass()).isTrue();
  }
  
  @Test
  public void test_buildClearanceRecordMap() {
    // given
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    List<ClearanceRecord> crList = new ArrayList<>();
    List<Long> coList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      final ClearanceRecord cr = ClearanceRecord.builder()
                                                .clearanceOrder((long) i)
                                                .rootHash("" + i)
                                                .createTime(System.currentTimeMillis())
                                                .description("" + i)
                                                .chainHash("" + i)
                                                .txHash("" + i)
                                                .build();
      crList.add(cr);
      coList.add((long) i);
    }
    
    // when
    final Map<Long, ClearanceRecord> crMap = service.buildClearanceRecordMap(crList);
    
    // then
    assertThat(crMap.keySet()).isNotEmpty();
    assertThat(new ArrayList<>(crMap.keySet())).isEqualTo(coList);
  }
  
  @Test
  public void test_collectExistenceProofByClearanceOrder() {
    
    final VerificationProof proof = VerificationProofParserTest.getTestNotExistVerificationProof();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    final List<ExistenceProof> existenceProofList = service.collectExistenceProofByClearanceOrder(32170L, proof);
    assertThat(existenceProofList.size()).isEqualTo(8);
    for (int i = 0; i < existenceProofList.size() - 2; i++) {
      assertThat(existenceProofList.get(i)
                                   .isExist()).isTrue();
    }
    assertThat(existenceProofList.get(existenceProofList.size() - 1)
                                 .isExist()).isFalse();
    long currentSn = 0;
    for (ExistenceProof existenceProof : existenceProofList) {
      assertThat(IndexValueProperties.of(existenceProof.getIndexValue())
                                     .getSn()).isEqualTo(currentSn);
      currentSn++;
    }
  }
  
  @Test
  public void test_buildRemovedProofResultList() {
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    final long co = 5L;
    final String ivk = "123";
    final long fromSn = 0;
    final long toSn = 5;
    final List<VerifyReceiptAndMerkleProofResult> resultList = service.buildRemovedProofResultList(co, ivk, fromSn,
        toSn);
    
    assertThat(resultList.size()).isEqualTo(6);
    assertThat(resultList.stream()
                         .map(VerifyReceiptAndMerkleProofResult::getIndexValue)
                         .collect(Collectors.toList())).contains("123_R0", "123_R1", "123_R2", "123_R3", "123_R4",
                             "123_R5");
  }
  
  @Test
  public void test_verifyExistenceProofWithNotExist_ok() {
    // given
    final VerificationProof proof = VerificationProofParserTest.getTestNotExistVerificationProof();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    final long co = 32170L;
    final List<ExistenceProof> existenceProofList = service.collectExistenceProofByClearanceOrder(co, proof);
    
    final ClearanceRecord cr = proof.getClearanceRecords()
                                    .stream()
                                    .filter(clearanceRecord -> co == clearanceRecord.getClearanceOrder())
                                    .collect(Collectors.toList())
                                    .get(0);
    final VerifyReceiptAndMerkleProofService verifyServiceSet = service.buildVerifyService(proof.getContractAddress(),
        NODE_URL, proof.getServerWalletAddress());
    
    // when
    final VerifyExistenceProofResult verifyExistenceProofResult = service.verifyExistenceProofWithNotExist(
        existenceProofList, proof.getServerWalletAddress(), cr, QueryStringParser.parse(proof.getQuery()),
        verifyServiceSet);
    
    // then
    assertThat(verifyExistenceProofResult.getSuccessCount()).isEqualTo(proof.getExistenceProofs()
                                                                            .size());
    assertThat(verifyExistenceProofResult.getModifiedCount()).isEqualTo(0L);
    assertThat(verifyExistenceProofResult.getRemovedCount()).isEqualTo(0L);
    assertThat(verifyExistenceProofResult.getAddedCount()).isEqualTo(0L);
    assertThat(verifyExistenceProofResult.isPass()).isTrue();
    
    assertThat(verifyExistenceProofResult.getNotExistProofResult()
                                         .get(0)
                                         .getVerifyNotExistProofResult()).isEqualTo(
                                             VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND);
    assertThat(verifyExistenceProofResult.getProofResultList()
                                         .stream()
                                         .map(VerifyReceiptAndMerkleProofResult::isPass)
                                         .collect(Collectors.toList())).containsOnly(true);
  }
  
  @Test
  public void test_verifyExistenceProofWithNotExist_failAndRedundant() {
    // given
    final VerificationProof proof = VerificationProofParserTest.getFailAndRedundantVerificationProof();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    final long co = 32170L;
    final List<ExistenceProof> existenceProofList = service.collectExistenceProofByClearanceOrder(co, proof);
    
    final ClearanceRecord cr = proof.getClearanceRecords()
                                    .stream()
                                    .filter(clearanceRecord -> co == clearanceRecord.getClearanceOrder())
                                    .collect(Collectors.toList())
                                    .get(0);
    final VerifyReceiptAndMerkleProofService verifyServiceSet = service.buildVerifyService(proof.getContractAddress(),
        NODE_URL, proof.getServerWalletAddress());
    
    // when
    final VerifyExistenceProofResult verifyExistenceProofResult = service.verifyExistenceProofWithNotExist(
        existenceProofList, proof.getServerWalletAddress(), cr, QueryStringParser.parse(proof.getQuery()),
        verifyServiceSet);
    
    // then collect ExistenceProofByClearanceOrder
    assertThat(verifyExistenceProofResult.getSuccessCount()).isEqualTo(5L);
    assertThat(verifyExistenceProofResult.getModifiedCount()).isEqualTo(2L);
    assertThat(verifyExistenceProofResult.getRemovedCount()).isEqualTo(1L);
    assertThat(verifyExistenceProofResult.getAddedCount()).isEqualTo(0L);
    assertThat(verifyExistenceProofResult.isPass()).isFalse();
    
    final List<VerifyReceiptAndMerkleProofResult> notExistProofResult = verifyExistenceProofResult.getNotExistProofResult();
    assertThat(notExistProofResult.size()).isEqualTo(1L);
    assertThat(notExistProofResult.get(0)
                                  .getVerifyNotExistProofResult()).isEqualTo(
                                      VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND);
    assertThat(verifyExistenceProofResult.getExistProofResult()
                                         .size()).isEqualTo(6L);
    assertThat(verifyExistenceProofResult.getNAProofResult()
                                         .size()).isEqualTo(1L);
  }
  
  @Test
  public void test_verifyExistenceProofWithNotExist_removedAndAddBigSn() {
    // given
    final VerificationProof proof = VerificationProofParserTest.getRemovedAndAddBigSn();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    final long co = 33397L;
    final List<ExistenceProof> existenceProofList = service.collectExistenceProofByClearanceOrder(co, proof);
    
    final ClearanceRecord cr = proof.getClearanceRecords()
                                    .stream()
                                    .filter(clearanceRecord -> co == clearanceRecord.getClearanceOrder())
                                    .collect(Collectors.toList())
                                    .get(0);
    final VerifyReceiptAndMerkleProofService verifyServiceSet = service.buildVerifyService(proof.getContractAddress(),
        NODE_URL, proof.getServerWalletAddress());
    
    // when
    final VerifyExistenceProofResult verifyExistenceProofResult = service.verifyExistenceProofWithNotExist(
        existenceProofList, proof.getServerWalletAddress(), cr, QueryStringParser.parse(proof.getQuery()),
        verifyServiceSet);
    
    // then collect ExistenceProofByClearanceOrder
    assertThat(verifyExistenceProofResult.getSuccessCount()).isEqualTo(53L);
    assertThat(verifyExistenceProofResult.getModifiedCount()).isEqualTo(1L);
    assertThat(verifyExistenceProofResult.getRemovedCount()).isEqualTo(1L);
    assertThat(verifyExistenceProofResult.getAddedCount()).isEqualTo(1L);
    assertThat(verifyExistenceProofResult.isPass()).isFalse();
    
    final List<VerifyReceiptAndMerkleProofResult> notExistProofResult = verifyExistenceProofResult.getNotExistProofResult();
    assertThat(notExistProofResult.get(0)
                                  .getVerifyNotExistProofResult()).isEqualTo(
                                      VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND);
    assertThat(notExistProofResult.size()).isEqualTo(2L);
    assertThat(verifyExistenceProofResult.getNAProofResult()
                                         .size()).isEqualTo(1L);
    assertThat(verifyExistenceProofResult.getExistProofResult()
                                         .size()).isEqualTo(53L);
  }
  
  @Test
  public void test_verifyOnlyExistenceProof_ok() {
    // given
    final VerificationProof proof = VerificationProofParserTest.getOnlyExistenceVerificationProof();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    final long co = 32170L;
    final List<ExistenceProof> existenceProofList = service.collectExistenceProofByClearanceOrder(co, proof);
    
    final ClearanceRecord cr = proof.getClearanceRecords()
                                    .stream()
                                    .filter(clearanceRecord -> co == clearanceRecord.getClearanceOrder())
                                    .collect(Collectors.toList())
                                    .get(0);
    final VerifyReceiptAndMerkleProofService verifyServiceSet = service.buildVerifyService(proof.getContractAddress(),
        NODE_URL, proof.getServerWalletAddress());
    
    // when
    final VerifyExistenceProofResult verifyExistenceProofResult = service.verifyOnlyExistenceProof(existenceProofList,
        cr, verifyServiceSet);
    
    // then
    
    assertThat(verifyExistenceProofResult.isPass()).isTrue();
    assertThat(verifyExistenceProofResult.getSuccessCount()).isEqualTo(5L);
    assertThat(verifyExistenceProofResult.getProofResultList()
                                         .stream()
                                         .map(VerifyReceiptAndMerkleProofResult::isPass)
                                         .collect(Collectors.toList())).containsOnly(true);
  }
  
  @Test
  public void test_findNotExistProof() {
    // given
    final VerificationProof proof = VerificationProofParserTest.getTestNotExistVerificationProof();
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    final Query query = QueryStringParser.parse(proof.getQuery());
    
    final long co = 32170L;
    final List<ExistenceProof> existenceProofList = service.collectExistenceProofByClearanceOrder(co, proof);
    
    final ExistenceProof notExistProof = service.findNotExistProof(query.getIndexValueKey(), existenceProofList);
    
    assertThat(notExistProof.isExist()).isFalse();
    assertThat(notExistProof.getClearanceOrder()).isEqualTo(co);
    assertThat(notExistProof.getIndexValue()).isEqualTo("SPO_C_Client_Example_R7");
  }
  
  @Test
  public void test_collectProofClearanceOrderReversedList() {
    // given
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    int maxCo = 10;
    List<ExistenceProof> existenceProofList = new ArrayList<>();
    for (int i = 0; i < maxCo; i++) {
      existenceProofList.add(ExistenceProof.builder()
                                           .clearanceOrder((long) i)
                                           .build());
    }
    
    final List<Long> longList = service.collectProofClearanceOrderReversedList(existenceProofList);
    
    for (int i = 0; i < maxCo; i++) {
      assertThat(longList.get(i)).isEqualTo(maxCo - i - 1);
    }
  }
  
  @Test
  public void test_buildVerifiedClearanceRecordMap_ok() {
    // given
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    final VerificationProof proof = VerificationProofParserTest.getQueryByCo();
    
    final ClearanceRecord latestCr = ClearanceRecord.builder()
                                                    .clearanceOrder(35770L)
                                                    .rootHash(
                                                        "00f850ae0bc4cf781e91f46cf5abb46b7c08c5e165aa0d3e1e84080850c1e0d8")
                                                    .createTime(1606183632000L)
                                                    .chainHash(
                                                        "ec49c63bf8f0f0d1bbf94c84924a5bd55a44388221ac76ef6551b2fb508a668a")
                                                    .txHash(
                                                        "0xcd78c23f22601d07e524030777b6ebc0ae0e04186cddac054a01a7daa90e1b28")
                                                    .build();
    
    final List<ClearanceRecord> crList = proof.getClearanceRecords();
    final Map<Long, ClearanceRecord> clearanceRecordMap = service.buildVerifiedClearanceRecordMap(crList, latestCr);
    
    assertThat(clearanceRecordMap.size()).isEqualTo(crList.size());
  }
  
  @Test
  public void test_verify_proofs_with_different_contractAddress_by_same_nodeUrl() {
    // given
    final VerificationProof proof1 = VerificationProofParserTest.getAzureRCProof();
    final VerificationProof proof2 = VerificationProofParserTest.getAzureDev2Proof();
    final VerifyVerificationProofService service = new VerifyVerificationProofService();
    
    // when
    final VerifyVerificationProofResult result1 = service.verify(proof1, InfuraNodeUrlConfig.toProjectId(NODE_URL));
    final VerifyVerificationProofResult result2 = service.verify(proof2, InfuraNodeUrlConfig.toProjectId(NODE_URL));
    
    // then
    assertThat(result1).isNotNull();
    assertThat(result1.isPass()).isTrue();
    assertThat(result1.getProofCount()).isEqualTo(1);
    assertThat(result1.getVerifyReceiptResults()
                      .size()).isEqualTo(1);
    
    assertThat(result2).isNotNull();
    assertThat(result2.isPass()).isTrue();
    assertThat(result2.getProofCount()).isEqualTo(1);
    assertThat(result2.getVerifyReceiptResults()
                      .size()).isEqualTo(1);
  }
  
}