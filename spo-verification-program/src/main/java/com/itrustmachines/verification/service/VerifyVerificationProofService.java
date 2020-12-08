package com.itrustmachines.verification.service;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.util.IndexValueProperties;
import com.itrustmachines.common.util.KeyGeneratorUtil;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.verification.config.InfuraNodeUrlConfig;
import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.constants.VerifyNotExistProofStatus;
import com.itrustmachines.verification.constants.VerifyStatus;
import com.itrustmachines.verification.util.QueryStringParser;
import com.itrustmachines.verification.vo.*;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerifyVerificationProofService {
  
  private final VerifyNotExistProofService verifyNotExistProofService;
  
  // key: nodeUrl+contractAddress
  // value: VerifyReceiptAndMerkleProofService
  private final Cache<String, VerifyReceiptAndMerkleProofService> verifyServiceCache;
  
  public VerifyVerificationProofService() {
    this.verifyNotExistProofService = new VerifyNotExistProofService();
    this.verifyServiceCache = CacheBuilder.newBuilder()
                                          .maximumSize(1000)
                                          .expireAfterWrite(1, TimeUnit.DAYS)
                                          .build();
    log.info("new instance={}", this);
  }
  
  public VerifyVerificationProofResult verify(@NonNull final VerificationProof proof, final String infuraProjectId) {
    log.debug("verify() begin, proof={}, infuraProjectId={}", proof, infuraProjectId);
    
    final String nodeUrl;
    VerifyReceiptAndMerkleProofService service;
    
    switch (proof.getEnv()) {
      case MAINNET:
      case KOVAN:
      case GOERLI:
      case RINKEBY:
      case ROPSTEN: {
        if (StringUtils.isBlank(infuraProjectId)) {
          final String errMsg = "verify() error, infuraProjectId is empty";
          throw new RuntimeException(errMsg);
        }
        nodeUrl = InfuraNodeUrlConfig.toNodeUrl(proof.getEnv(), infuraProjectId);
      }
        break;
      case AZURE_QUORUM:
      case PRIVATE_GETH: {
        if (StringUtils.isBlank(proof.getNodeConnectionString())) {
          final String errMsg = "verify() error, nodeConnectionString is empty";
          throw new RuntimeException(errMsg);
        }
        nodeUrl = proof.getNodeConnectionString();
      }
        break;
      default: {
        final String errMsg = String.format("verify() error, env={%s} error", proof.getEnv());
        throw new IllegalArgumentException(errMsg);
      }
    }
    service = verifyServiceCache.getIfPresent(nodeUrl + proof.getContractAddress());
    if (Objects.isNull(service)) {
      service = buildVerifyService(proof.getContractAddress(), nodeUrl, proof.getServerWalletAddress());
      this.verifyServiceCache.put(nodeUrl + proof.getContractAddress(), service);
    }
    
    final long latestCO = findLatestCO(proof);
    ClearanceRecord latestCR = null;
    if (latestCO != -1) {
      latestCR = service.obtainClearanceRecord(latestCO);
    }
    
    log.debug("verify() latestCR={}, verifyService={}", latestCR, service);
    
    return verify(proof, latestCR, service);
  }
  
  VerifyReceiptAndMerkleProofService buildVerifyService(@NonNull final String contractAddress,
      @NonNull final String nodeUrl, @NonNull final String serverWalletAddress) {
    log.debug("buildVerifyService() start, contractAddress={}, nodeUrl={}, severWalletAddress={}", contractAddress,
        nodeUrl, serverWalletAddress);
    final String privateKey = KeyGeneratorUtil.generateKeyWithPassword("verify")
                                              .getPrivateKey();
    final ClientContractService clearanceRecordService = new ClientContractService(contractAddress, privateKey, nodeUrl,
        1.0, 5);
    final VerifyReceiptAndMerkleProofService service = new VerifyReceiptAndMerkleProofService(serverWalletAddress,
        clearanceRecordService);
    log.debug("buildVerifyService() end");
    return service;
  }
  
  long findLatestCO(@NonNull final VerificationProof proof) {
    long latestCO = -1;
    for (ClearanceRecord cr : proof.getClearanceRecords()) {
      if (cr.getClearanceOrder() > latestCO) {
        latestCO = cr.getClearanceOrder();
      }
    }
    return latestCO;
  }
  
  VerifyVerificationProofResult verify(@NonNull final VerificationProof proof, final ClearanceRecord latestCR,
      @NonNull final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService) {
    log.debug("verify() proof={}, latestCR={}", proof, latestCR);
    
    final Query query = QueryStringParser.parse(proof.getQuery());
    log.debug("verify() query={}", query);
    
    final boolean proofSignaturePass = verifyProofSignature(proof);
    log.debug("verify() proofSignaturePass={}", proofSignaturePass);
    
    final List<Long> clearanceOrderList = collectProofClearanceOrderReversedList(proof.getExistenceProofs());
    
    final Map<Long, ClearanceRecord> verifiedCrMap = buildVerifiedClearanceRecordMap(proof.getClearanceRecords(),
        latestCR);
    final boolean crPass = verifiedCrMap.size() == proof.getClearanceRecords()
                                                        .size();
    
    log.debug("verify() crPass={}, verifiedCrMap={}", crPass, verifiedCrMap);
    
    final List<VerifyReceiptAndMerkleProofResult> existProofResults = new ArrayList<>();
    long totalCount = 0L, successCount = 0L, modifiedCount = 0L, removedCount = 0L, addedCount = 0L;
    
    boolean pass = proofSignaturePass;
    for (Long clearanceOrder : clearanceOrderList) {
      final ClearanceRecord contractClearanceRecord = verifiedCrMap.get(clearanceOrder);
      final List<ExistenceProof> existenceProofList = collectExistenceProofByClearanceOrder(clearanceOrder, proof);
      totalCount += existenceProofList.size();
      
      if (Objects.nonNull(contractClearanceRecord)) {
        VerifyExistenceProofResult verifyExistenceProofResult;
        if (!Query.QueryType.LOCATOR.equals(query.getType())) {
          verifyExistenceProofResult = verifyExistenceProofWithNotExist(existenceProofList,
              proof.getServerWalletAddress(), contractClearanceRecord, query, verifyReceiptAndMerkleProofService);
        } else {
          verifyExistenceProofResult = verifyOnlyExistenceProof(existenceProofList, contractClearanceRecord,
              verifyReceiptAndMerkleProofService);
        }
        successCount += verifyExistenceProofResult.getSuccessCount();
        modifiedCount += verifyExistenceProofResult.getModifiedCount();
        removedCount += verifyExistenceProofResult.getRemovedCount();
        addedCount += verifyExistenceProofResult.getAddedCount();
        pass &= verifyExistenceProofResult.isPass();
        addAllIfNotNull(existProofResults, verifyExistenceProofResult.getProofResultList());
      } else {
        log.warn("no contractClearanceRecord for clearanceOrder={}", clearanceOrder);
        pass = false;
        existProofResults.addAll(buildClearanceRecordFailResult(existenceProofList));
      }
    }
    
    final VerifyVerificationProofResult result = VerifyVerificationProofResult.builder()
                                                                              .query(proof.getQuery())
                                                                              .queryType(query.getType())
                                                                              .proofCount(totalCount)
                                                                              .successCount(successCount)
                                                                              .modifiedCount(modifiedCount)
                                                                              .removedCount(removedCount)
                                                                              .addedCount(addedCount)
                                                                              .pass(pass)
                                                                              .clearanceRecordPass(crPass)
                                                                              .verifyReceiptResults(existProofResults)
                                                                              .build();
    log.debug("verify() result={}", result);
    return result;
  }
  
  <E> void addAllIfNotNull(List<E> list, Collection<? extends E> c) {
    if (c != null) {
      list.addAll(c);
    }
  }
  
  List<VerifyReceiptAndMerkleProofResult> buildClearanceRecordFailResult(
      @NonNull final List<ExistenceProof> existenceProofs) {
    log.debug("buildClearanceRecordFailResult() start, existenceProofs={}", existenceProofs);
    final List<VerifyReceiptAndMerkleProofResult> crFailResult = existenceProofs.stream()
                                                                                .map(proof -> {
                                                                                  final Receipt receipt = proof.getReceipt();
                                                                                  return failResult(proof, receipt);
                                                                                })
                                                                                .collect(Collectors.toList());
    log.debug("buildClearanceRecordFailResult() start, crFailResult={}", crFailResult);
    return crFailResult;
  }
  
  private VerifyReceiptAndMerkleProofResult failResult(ExistenceProof proof, Receipt receipt) {
    return VerifyReceiptAndMerkleProofResult.builder()
                                            .status(StatusConstantsString.ERROR)
                                            .existenceType(ExistenceType.NA)
                                            .verifyStatus(VerifyStatus.CLEARANCE_RECORD_ERROR)
                                            .pass(false)
                                            .indexValue(proof.getIndexValue())
                                            .clearanceOrder(proof.getClearanceOrder())
                                            .verifyNotExistProofResult(VerifyNotExistProofStatus.CLEARANCE_RECORD_ERROR)
                                            .receiptTimestamp(
                                                Objects.nonNull(receipt) ? receipt.getTimestampSPO() : null)
                                            .ledgerInputTimestamp(
                                                Objects.nonNull(receipt) ? receipt.getTimestamp() : null)
                                            .merkleproofSignatureOk(false)
                                            .clearanceOrderOk(false)
                                            .clearanceRecordRootHashOk(false)
                                            .receiptSignatureOk(false)
                                            .pbPairOk(false)
                                            .sliceOk(false)
                                            .description(StatusConstantsString.ERROR)
                                            .timestamp(System.currentTimeMillis())
                                            .cmd(Objects.nonNull(receipt) ? receipt.getCmd() : null)
                                            .build();
  }
  
  List<ExistenceProof> collectExistenceProofByClearanceOrder(@NonNull final Long clearanceOrder,
      @NonNull final VerificationProof proof) {
    log.debug("collectExistenceProofByClearanceOrder() start, clearanceOrder={}, verificationProof={}", clearanceOrder,
        proof);
    final List<ExistenceProof> existenceProofList = proof.getExistenceProofs()
                                                         .stream()
                                                         .filter(existenceProof -> clearanceOrder.equals(
                                                             existenceProof.getClearanceOrder()))
                                                         .sorted(Comparator.comparingLong(
                                                             existenceProof -> IndexValueProperties.of(
                                                                 existenceProof.getIndexValue())
                                                                                                   .getSn()))
                                                         .collect(Collectors.toList());
    log.debug("collectExistenceProofByClearanceOrder() end, existenceProofList={}", existenceProofList);
    return existenceProofList;
  }
  
  List<Long> collectProofClearanceOrderReversedList(@NonNull final List<ExistenceProof> existenceProofList) {
    log.debug("collectProofClearanceOrderReversedList() start, existenceProofList={}", existenceProofList);
    final List<Long> clearanceOrderList = existenceProofList.stream()
                                                            .map(ExistenceProof::getClearanceOrder)
                                                            .distinct()
                                                            .sorted(Comparator.reverseOrder())
                                                            .collect(Collectors.toList());
    log.debug("collectProofClearanceOrderReversedList() end, clearanceOrderList={}", clearanceOrderList);
    return clearanceOrderList;
  }
  
  boolean verifyProofSignature(@NonNull final VerificationProof proof) {
    log.debug("verifyProofSignature() start, proof={}", proof);
    final boolean result = SignatureUtil.verifySignature(proof.getServerWalletAddress(), proof.getSigServer(),
        proof.toSignDataSha3());
    
    log.debug("verifyProofSignature() start, result={}", result);
    return result;
  }
  
  Map<Long, ClearanceRecord> buildClearanceRecordMap(@NonNull final List<ClearanceRecord> clearanceRecordList) {
    log.debug("buildClearanceRecordMap() start, clearanceRecordListSize={}", clearanceRecordList.size());
    final Map<Long, ClearanceRecord> clearanceRecordMap = clearanceRecordList.stream()
                                                                             .collect(Collectors.toMap(
                                                                                 ClearanceRecord::getClearanceOrder,
                                                                                 clearanceRecord -> clearanceRecord));
    log.debug("buildClearanceRecordMap() end, clearanceRecordMapKeySet={}", clearanceRecordMap.keySet());
    return clearanceRecordMap;
  }
  
  // key: clearanceOrder, value: passClearanceRecord
  Map<Long, ClearanceRecord> buildVerifiedClearanceRecordMap(@NonNull final List<ClearanceRecord> crList,
      final ClearanceRecord latestCR) {
    log.debug("buildVerifiedClearanceRecordMap() crList={}, latestCR={}", crList, latestCR);
    Map<Long, ClearanceRecord> verifiedClearanceRecordMap = new HashMap<>();
    if (Objects.isNull(latestCR)) {
      log.warn("buildVerifiedClearanceRecordMap() latestCR is null");
      return verifiedClearanceRecordMap;
    }
    final Map<Long, ClearanceRecord> clearanceRecordMap = buildClearanceRecordMap(crList);
    
    Long currentCo = latestCR.getClearanceOrder();
    
    // verify latest cr
    ClearanceRecord currentRecord = clearanceRecordMap.get(currentCo);
    if (Objects.nonNull(currentRecord) && currentRecord.getRootHash()
                                                       .equals(latestCR.getRootHash())
        && currentRecord.getClearanceOrder()
                        .equals(latestCR.getClearanceOrder())
        && currentRecord.getChainHash()
                        .equals(latestCR.getChainHash())) {
      verifiedClearanceRecordMap.put(currentCo, currentRecord);
    }
    
    while (clearanceRecordMap.containsKey(currentCo)) {
      final ClearanceRecord previousRecord = clearanceRecordMap.get(currentCo - 1);
      boolean result;
      if (Objects.nonNull(previousRecord)) {
        // have previous cr
        result = verifyChainHash(currentRecord, previousRecord);
      } else {
        // latest
        result = true;
      }
      
      if (result) {
        verifiedClearanceRecordMap.putIfAbsent(currentCo, currentRecord);
      } else {
        break;
      }
      currentRecord = previousRecord;
      currentCo--;
    }
    
    log.debug("buildPassClearanceRecord() verifiedClearanceRecordMap={}", verifiedClearanceRecordMap);
    return verifiedClearanceRecordMap;
  }
  
  boolean verifyChainHash(@NonNull final ClearanceRecord currentRecord, @NonNull final ClearanceRecord previousRecord) {
    log.debug("verifyChainHash() start, currentRecord={}, previousRecord={}", currentRecord, previousRecord);
    
    /**
     * TODO: in 3.0.0.SNAPSHOT
     * chainHash method = Chi = H(Ri-1|COi-1|CHi-1)
     * RH 0 = Hash.sha3("")
     */
    byte[] concatByteArray = concatByteArray(HashUtils.hex2byte(currentRecord.getRootHash()),
        Numeric.toBytesPadded(BigInteger.valueOf(currentRecord.getClearanceOrder()), 32));
    
    concatByteArray = concatByteArray(concatByteArray, HashUtils.hex2byte(previousRecord.getChainHash()));
    final byte[] clearanceRecordHash = Hash.sha3(concatByteArray);
    final boolean result = Arrays.equals(clearanceRecordHash, HashUtils.hex2byte(currentRecord.getChainHash()));
    log.debug("verifyChainHash() end, result={}", result);
    
    return result;
  }
  
  byte[] concatByteArray(@NonNull final byte[] previousByte, final @NonNull byte[] currentByte) {
    byte[] result = new byte[previousByte.length + currentByte.length];
    System.arraycopy(previousByte, 0, result, 0, previousByte.length);
    System.arraycopy(currentByte, 0, result, previousByte.length, currentByte.length);
    return result;
  }
  
  ExistenceProof findNotExistProof(@NonNull final String indexValueKey,
      @NonNull final List<ExistenceProof> existenceProofList) {
    log.debug("findNotExistProof() start, existenceProofListSize={}", existenceProofList.size());
    final Optional<ExistenceProof> maxIvExistenceProof = existenceProofList.stream()
                                                                           .filter(
                                                                               existenceProof -> !existenceProof.isExist())
                                                                           .max(Comparator.comparingLong(
                                                                               existenceProof -> IndexValueProperties.of(
                                                                                   existenceProof.getIndexValue())
                                                                                                                     .getSn()));
    ExistenceProof existenceProof = null;
    if (maxIvExistenceProof.isPresent()) {
      existenceProof = maxIvExistenceProof.get();
      log.debug("findNotExistProof(), maxIvExistenceProof={}", existenceProof);
    }
    if (Objects.isNull(existenceProof)) {
      log.debug("findNotExistProof(), noExistenceProof");
      final Optional<Long> maxSn = existenceProofList.stream()
                                                     .map(ExistenceProof::getIndexValue)
                                                     .map(IndexValueProperties::of)
                                                     .map(IndexValueProperties::getSn)
                                                     .max(Comparator.comparingLong(sn -> sn));
      if (maxSn.isPresent()) {
        existenceProof = ExistenceProof.builder()
                                       .indexValue(indexValueKey + "_R" + maxSn.get())
                                       .build();
      } else {
        existenceProof = ExistenceProof.builder()
                                       .indexValue(indexValueKey + "_R0")
                                       .build();
        
      }
    } else {
      log.debug("findNotExistProof(), getNotExistProof");
    }
    
    log.debug("findNotExistProof() start, existenceProof={}", existenceProof);
    return existenceProof;
  }
  
  VerifyExistenceProofResult verifyExistenceProofWithNotExist(@NonNull final List<ExistenceProof> existenceProofList,
      @NonNull final String serverWalletAddress, final ClearanceRecord clearanceRecord, @NonNull final Query query,
      @NonNull final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService) {
    log.debug(
        "verifyExistenceProofWithNotExist() start, existenceProofListSize={}, serverWalletAddress={}, clearanceRecord={}",
        existenceProofList.size(), serverWalletAddress, clearanceRecord);
    
    List<VerifyReceiptAndMerkleProofResult> proofResultList = new ArrayList<>();
    
    boolean pass = true;
    long successCount = 0L, modifiedCount = 0L, removedCount = 0L, addedCount = 0L;
    
    final ExistenceProof notExistProof = findNotExistProof(query.getIndexValueKey(), existenceProofList);
    final Long maxSn = IndexValueProperties.of(notExistProof.getIndexValue())
                                           .getSn();
    log.debug("verifyExistenceProofWithNotExist() notExistProof={}, maxSn={}", notExistProof, maxSn);
    
    Long firstSuccessNotExistProofSn = Long.MAX_VALUE;
    long currentSN = 0;
    for (ExistenceProof existenceProof : existenceProofList) {
      final IndexValueProperties indexValueProperties = IndexValueProperties.of(existenceProof.getIndexValue());
      if (currentSN < indexValueProperties.getSn() && indexValueProperties.getSn() < firstSuccessNotExistProofSn) {
        final List<VerifyReceiptAndMerkleProofResult> removedProofResultList = buildRemovedProofResultList(
            clearanceRecord.getClearanceOrder(), query.getIndexValueKey(), currentSN, indexValueProperties.getSn() - 1);
        log.debug("verifyExistenceProofWithNotExist() removedProofResultListSize={}", removedProofResultList.size());
        removedCount += removedProofResultList.size();
        currentSN = indexValueProperties.getSn();
        proofResultList.addAll(removedProofResultList);
      }
      VerifyReceiptAndMerkleProofResult result;
      if (currentSN == indexValueProperties.getSn() && currentSN < maxSn) {
        result = verifyReceiptAndMerkleProofService.verify(existenceProof, clearanceRecord);
      } else {
        result = verifyNotExistProofService.verifyNotExistProof(existenceProof, serverWalletAddress, clearanceRecord,
            query.getFromCO(), query.getToCO());
        if (result.isPass() && firstSuccessNotExistProofSn == Long.MAX_VALUE) {
          firstSuccessNotExistProofSn = indexValueProperties.getSn();
        }
        if (indexValueProperties.getSn() > firstSuccessNotExistProofSn) {
          result.setVerifyStatus(VerifyStatus.ADDED);
        }
      }
      pass &= result.isPass();
      log.debug("verifyExistenceProofWithNotExist() verify pass={}, result={}", pass, result.isPass());
      if (VerifyStatus.PASS.equals(result.getVerifyStatus())) {
        successCount++;
      } else if (VerifyStatus.MODIFIED.equals(result.getVerifyStatus())) {
        modifiedCount++;
      } else {
        addedCount++;
      }
      proofResultList.add(result);
      currentSN++;
      
    }
    final VerifyExistenceProofResult verifyExistenceProofResult = VerifyExistenceProofResult.builder()
                                                                                            .pass(pass)
                                                                                            .successCount(successCount)
                                                                                            .addedCount(addedCount)
                                                                                            .modifiedCount(
                                                                                                modifiedCount)
                                                                                            .removedCount(removedCount)
                                                                                            .proofResultList(
                                                                                                proofResultList)
                                                                                            .build();
    log.debug("verifyExistenceProofWithNotExist() end, verifyExistenceProofResult={}", verifyExistenceProofResult);
    return verifyExistenceProofResult;
  }
  
  List<VerifyReceiptAndMerkleProofResult> buildRemovedProofResultList(@NonNull final Long clearanceOrder,
      @NonNull final String indexValueKey, @NonNull final Long fromSn, @NonNull final Long toSn) {
    log.debug("buildMissingIndexValueList() start, clearanceOrder={}, indexValueKey={}, fromSn={}, toSn={}",
        clearanceOrder, indexValueKey, fromSn, toSn);
    List<VerifyReceiptAndMerkleProofResult> resultList = new ArrayList<>();
    for (long i = fromSn; i <= toSn; i++) {
      resultList.add(VerifyReceiptAndMerkleProofResult.builder()
                                                      .clearanceOrder(clearanceOrder)
                                                      .indexValue(indexValueKey + "_R" + i)
                                                      .pass(false)
                                                      .verifyStatus(VerifyStatus.REMOVED)
                                                      .existenceType(ExistenceType.NA)
                                                      .build());
    }
    
    log.debug("buildMissingIndexValueList() end, resultList={}", resultList);
    return resultList;
  }
  
  VerifyExistenceProofResult verifyOnlyExistenceProof(@NonNull final List<ExistenceProof> existenceProofList,
      @NonNull final ClearanceRecord clearanceRecord,
      @NonNull final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService) {
    log.debug("verifyOnlyExistenceProof() start, existenceProofList={}, clearanceRecord={}", existenceProofList,
        clearanceRecord);
    boolean pass = true;
    long successCount = 0L, modifiedCount = 0L;
    
    List<VerifyReceiptAndMerkleProofResult> existProofResultList = new ArrayList<>();
    
    for (ExistenceProof existenceProof : existenceProofList) {
      final VerifyReceiptAndMerkleProofResult result = verifyReceiptAndMerkleProofService.verify(existenceProof,
          clearanceRecord);
      pass &= result.isPass();
      result.setRootHash(clearanceRecord.getRootHash());
      log.debug("verify pass={}, result={}", pass, result.isPass());
      existProofResultList.add(result);
      if (VerifyStatus.PASS.equals(result.getVerifyStatus())) {
        successCount++;
      } else if (VerifyStatus.MODIFIED.equals(result.getVerifyStatus())) {
        modifiedCount++;
      }
      
    }
    final VerifyExistenceProofResult existenceProofResult = VerifyExistenceProofResult.builder()
                                                                                      .pass(pass)
                                                                                      .successCount(successCount)
                                                                                      .modifiedCount(modifiedCount)
                                                                                      .removedCount(0L)
                                                                                      .addedCount(0L)
                                                                                      .proofResultList(
                                                                                          existProofResultList)
                                                                                      .build();
    
    log.debug("verifyOnlyExistenceProof() end, existenceProofResult={}", existenceProofResult);
    return existenceProofResult;
  }
  
}
