package com.itrustmachines.verification.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.util.KeyGeneratorUtil;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.verification.config.InfuraNodeUrlConfig;
import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.constants.VerifyNotExistProofStatus;
import com.itrustmachines.verification.constants.ProofExistStatus;
import com.itrustmachines.verification.constants.VerifyVerificationProofStatus;
import com.itrustmachines.verification.util.QueryStringParser;
import com.itrustmachines.verification.util.SliceValidationUtil;
import com.itrustmachines.verification.vo.*;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerifyVerificationProofService {
  
  private final VerifyClearanceRecordService verifyClearanceRecordService;
  private final VerifyExistenceProofService verifyExistenceProofService;
  
  // key: nodeUrl+contractAddress
  // value: VerifyReceiptAndMerkleProofService
  private final Cache<String, VerifyReceiptAndMerkleProofService> verifyServiceCache;
  
  public VerifyVerificationProofService() {
    this.verifyClearanceRecordService = new VerifyClearanceRecordService();
    this.verifyExistenceProofService = new VerifyExistenceProofService();
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
    
    return verify(proof, service);
  }
  
  public VerifyVerificationProofResult verify(@NonNull final VerificationProof proof,
      final VerifyReceiptAndMerkleProofService service) {
    final long latestCO = findLatestCO(proof);
    ClearanceRecord latestCR = null;
    if (latestCO != -1) {
      latestCR = service.obtainClearanceRecord(latestCO);
    }
    log.debug("verify() latestCR={}, verifyService={}", latestCR, service);
    
    if (Objects.isNull(latestCR)) {
      String errMsg = String.format("verify() error, find clearanceRecord of latestCo=%s is null", latestCO);
      log.error(errMsg);
      throw new RuntimeException(errMsg);
    }
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
  
  VerifyVerificationProofResult verify(@NonNull final VerificationProof proof, final @NonNull ClearanceRecord latestCR,
      @NonNull final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService) {
    log.debug("verify() proof={}, latestCR={}", proof, latestCR);
    
    final Query query = QueryStringParser.parse(proof.getQuery());
    log.debug("verify() query={}", query);
    
    final boolean proofSignaturePass = verifyProofSignature(proof);
    log.debug("verify() proofSignaturePass={}", proofSignaturePass);
    
    final List<Long> clearanceOrderList = collectProofClearanceOrderReversedList(proof.getExistenceProofs());
    
    final Map<Long, VerifiedClearanceRecordInfo> verifiedCrMap = verifyClearanceRecordService.buildVerifiedClearanceRecordInfoMap(
        proof.getClearanceRecords(), latestCR);
    final boolean crPass = isCrListPass(verifiedCrMap);
    
    log.debug("verify() crPass={}, verifiedCrMap={}", crPass, verifiedCrMap);
    
    final List<VerifyReceiptAndMerkleProofResult> existProofResults = new ArrayList<>();
    final List<Long> errorCoList = new ArrayList<>();
    long totalCount = 0L, successCount = 0L, modifiedCount = 0L, removedCount = 0L, addedCount = 0L;
    
    boolean pass = proofSignaturePass && crPass;
    for (Long clearanceOrder : clearanceOrderList) {
      final VerifiedClearanceRecordInfo verifiedClearanceRecordInfo = verifiedCrMap.get(clearanceOrder);
      final List<ExistenceProof> existenceProofList = verifyExistenceProofService.collectExistenceProofByClearanceOrder(
          clearanceOrder, proof);
      totalCount += existenceProofList.size();
      
      if ((Objects.nonNull(verifiedClearanceRecordInfo) && verifiedClearanceRecordInfo.isPass())
          || latestCR.getClearanceOrder()
                     .equals(clearanceOrder)) {
        final ClearanceRecord contractClearanceRecord = verifiedClearanceRecordInfo.getClearanceRecord();
        VerifyExistenceProofResult verifyExistenceProofResult;
        if (!Query.QueryType.LOCATOR.equals(query.getType())) {
          verifyExistenceProofResult = verifyExistenceProofService.verifyExistenceProofWithNotExist(existenceProofList,
              proof.getServerWalletAddress(), contractClearanceRecord, query, verifyReceiptAndMerkleProofService);
        } else {
          verifyExistenceProofResult = verifyExistenceProofService.verifyOnlyExistenceProof(existenceProofList,
              contractClearanceRecord, verifyReceiptAndMerkleProofService);
        }
        successCount += verifyExistenceProofResult.getSuccessCount();
        modifiedCount += verifyExistenceProofResult.getModifiedCount();
        removedCount += verifyExistenceProofResult.getRemovedCount();
        addedCount += verifyExistenceProofResult.getAddedCount();
        pass &= verifyExistenceProofResult.isPass();
        addAllIfNotNull(existProofResults, verifyExistenceProofResult.getProofResultList());
      }
      if (Objects.isNull(verifiedClearanceRecordInfo) || !verifiedClearanceRecordInfo.isPass()) {
        log.warn("contractClearanceRecord error for clearanceOrder={}", clearanceOrder);
        pass = false;
        errorCoList.add(clearanceOrder);
        if (!clearanceOrder.equals(latestCR.getClearanceOrder())) {
          existProofResults.addAll(buildClearanceRecordFailResult(existenceProofList, verifiedClearanceRecordInfo));
        }
      }
    }
    
    final VerifyVerificationProofResult result = VerifyVerificationProofResult.builder()
                                                                              .query(proof.getQuery())
                                                                              .queryType(query.getType())
                                                                              .totalCount(totalCount)
                                                                              .successCount(successCount)
                                                                              .modifiedCount(modifiedCount)
                                                                              .removedCount(removedCount)
                                                                              .addedCount(addedCount)
                                                                              .status(pass
                                                                                  ? VerifyVerificationProofStatus.ALL_PASS
                                                                                  : VerifyVerificationProofStatus.SIG_ERROR)
                                                                              .verifyReceiptResults(existProofResults)
                                                                              .errorClearanceOrderInClearanceRecordList(
                                                                                  errorCoList)
                                                                              .build();
    log.debug("verify() result={}", result);
    return result;
  }
  
  <E> void addAllIfNotNull(List<E> list, Collection<? extends E> c) {
    if (c != null) {
      list.addAll(c);
    }
  }
  
  boolean isCrListPass(@NonNull final Map<Long, VerifiedClearanceRecordInfo> crMap) {
    log.debug("isCrListPass() start, crMap={}", crMap);
    final List<VerifiedClearanceRecordInfo> falseInfo = crMap.values()
                                                             .stream()
                                                             .filter(info -> !info.isPass())
                                                             .collect(Collectors.toList());
    boolean result = true;
    if (falseInfo.size() > 0) {
      result = false;
    }
    log.debug("isCrListPass() end, result={}, falseInfo={}", result, falseInfo);
    return result;
  }
  
  List<VerifyReceiptAndMerkleProofResult> buildClearanceRecordFailResult(
      @NonNull final List<ExistenceProof> existenceProofs,
      final VerifiedClearanceRecordInfo verifiedClearanceRecordInfo) {
    log.debug("buildClearanceRecordFailResult() start, existenceProofs={}", existenceProofs);
    final List<VerifyReceiptAndMerkleProofResult> crFailResult = existenceProofs.stream()
                                                                                .map(existenceProof -> this.failResult(
                                                                                    existenceProof,
                                                                                    verifiedClearanceRecordInfo))
                                                                                .collect(Collectors.toList());
    log.debug("buildClearanceRecordFailResult() start, crFailResult={}", crFailResult);
    return crFailResult;
  }
  
  private VerifyReceiptAndMerkleProofResult failResult(ExistenceProof proof,
      VerifiedClearanceRecordInfo verifiedClearanceRecordInfo) {
    final Receipt receipt = proof.getReceipt();
    final String rootHash = SliceValidationUtil.getRootHashString(proof.getMerkleProof()
                                                                       .getSlice());
    final String clearanceRecordRootHash = Objects.isNull(verifiedClearanceRecordInfo) ? null
        : verifiedClearanceRecordInfo.getClearanceRecord()
                                     .getRootHash();
    return VerifyReceiptAndMerkleProofResult.builder()
                                            .status(StatusConstantsString.ERROR)
                                            .existenceType(ExistenceType.NA)
                                            .proofExistStatus(ProofExistStatus.CLEARANCE_RECORD_ERROR)
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
                                            .merkleProofRootHash(rootHash)
                                            .contractRootHash(clearanceRecordRootHash)
                                            .build();
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
  
}
