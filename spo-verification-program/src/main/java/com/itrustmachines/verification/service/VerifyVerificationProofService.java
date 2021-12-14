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
import com.itrustmachines.verification.constants.ProofExistStatus;
import com.itrustmachines.verification.constants.VerifyNotExistProofStatus;
import com.itrustmachines.verification.constants.VerifyVerificationProofStatus;
import com.itrustmachines.verification.service.adapter.VerifyClearanceRecordServiceAdapter;
import com.itrustmachines.verification.service.adapter.VerifyClearanceRecordServiceAdapter_2_3_0;
import com.itrustmachines.verification.util.QueryStringParser;
import com.itrustmachines.verification.util.SliceValidationUtil;
import com.itrustmachines.verification.util.VerifyReportUtil;
import com.itrustmachines.verification.vo.*;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerifyVerificationProofService {
  
  private final VerifyProofListService verifyProofListService;
  private final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService;
  private final VerifyClearanceRecordService oldVerifyClearanceRecordService;
  private final VerifyClearanceRecordService newVerifyClearanceRecordService;
  
  // key: nodeUrl+contractAddress
  // value: ClientContractService
  private final Cache<String, ClientContractService> clientContractServiceCache;
  
  private VerifyVerificationProofService() {
    this.verifyProofListService = new VerifyProofListService();
    this.verifyReceiptAndMerkleProofService = new VerifyReceiptAndMerkleProofService();
    this.oldVerifyClearanceRecordService = new VerifyClearanceRecordServiceAdapter();
    this.newVerifyClearanceRecordService = new VerifyClearanceRecordServiceAdapter_2_3_0();
    
    this.clientContractServiceCache = CacheBuilder.newBuilder()
                                                  .maximumSize(1000)
                                                  .expireAfterWrite(1, TimeUnit.DAYS)
                                                  .build();
    log.info("new instance={}", this);
  }
  
  private static VerifyVerificationProofService instance;
  
  @Synchronized
  public static VerifyVerificationProofService getInstance() {
    if (VerifyVerificationProofService.instance == null) {
      VerifyVerificationProofService.instance = new VerifyVerificationProofService();
    }
    return VerifyVerificationProofService.instance;
  }
  
  public VerifyVerificationProofResult verify(@NonNull final VerificationProof proof, final String infuraProjectId) {
    log.debug("verify() start, proof={}, infuraProjectId={}", proof, infuraProjectId);
    
    final String nodeUrl = getNodeUrl(proof, infuraProjectId);
    
    buildClientContractServiceIfNotExist(proof.getContractAddress(), nodeUrl);
    
    final ClearanceRecord latestClearanceRecord = obtainLatestClearanceRecord(proof, nodeUrl);
    log.debug("verify() latestClearanceRecord={}", latestClearanceRecord);
    final Query query = QueryStringParser.parse(proof.getQuery());
    final VerifyVerificationProofResult result = verifyExistenceProof(proof, latestClearanceRecord, nodeUrl);
    
    log.debug("verify() end, result={}", result);
    return result;
  }
  
  ClearanceRecord obtainLatestClearanceRecord(@NonNull final VerificationProof proof, @NonNull final String nodeUrl) {
    final long latestCO = findLatestCO(proof);
    final String key = nodeUrl + proof.getContractAddress();
    final ClientContractService clientContractService = clientContractServiceCache.getIfPresent(key);
    if (Objects.isNull(clientContractService)) {
      String errMsg = String.format(
          "obtainLatestClearanceRecord() error, clientContractService not found in cache, latestCO=%d, nodeUrl=%s",
          latestCO, nodeUrl);
      log.error(errMsg);
      throw new RuntimeException(errMsg);
    }
    ClearanceRecord latestCR = null;
    if (latestCO != -1) {
      latestCR = clientContractService.obtainClearanceRecord(latestCO);
    }
    log.debug("obtainLatestClearanceRecord() latestCR={}", latestCR);
    
    if (Objects.isNull(latestCR)) {
      String errMsg = String.format("verify() error, find clearanceRecord of latestCo=%s is null", latestCO);
      log.error(errMsg);
      throw new RuntimeException(errMsg);
    }
    return latestCR;
  }
  
  void buildClientContractServiceIfNotExist(@NonNull final String contractAddress, @NonNull final String nodeUrl) {
    log.debug("buildClientContractServiceIfNotExist() start, contractAddress={}, nodeUrl={}", contractAddress, nodeUrl);
    final String privateKey = KeyGeneratorUtil.generateKeyWithPassword("verify")
                                              .getPrivateKey();
    final String key = nodeUrl + contractAddress;
    if (Objects.isNull(clientContractServiceCache.getIfPresent(key))) {
      log.debug("buildClientContractServiceIfNotExist() service not found by key={}", key);
      final ClientContractService clientContractService = new ClientContractService(contractAddress, privateKey,
          nodeUrl, 1.0, 5);
      clientContractServiceCache.put(key, clientContractService);
    } else {
      log.debug("buildClientContractServiceIfNotExist() service exist");
    }
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
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @ToString(exclude = { "existenceProofList" })
  static class VerifySingleCOExistenceProofInput {
    private boolean isLatestCO;
    private String serverWalletAddress;
    private Query query;
    private VerifiedClearanceRecordInfo verifiedClearanceRecordInfo;
    private List<ExistenceProof> existenceProofList;
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @ToString(exclude = { "existenceProofResultList" })
  static class VerifySingleCOExistenceProofResult {
    boolean pass;
    boolean isCoError;
    VerifyProofCount count;
    List<VerifyReceiptAndMerkleProofResult> existenceProofResultList;
  }
  
  VerifyVerificationProofResult verifyExistenceProof(@NonNull final VerificationProof proof,
      final @NonNull ClearanceRecord latestCR, @NonNull final String nodeUrl) {
    log.debug("verifyExistenceProof() proof={}, latestCR={}, nodeUrl={}", proof, latestCR, nodeUrl);
    VerifyReport report = new VerifyReport();
    report.setVerifyExistenceProofList(new ArrayList<>());
    final ClientContractService contractService = getClientContractService(nodeUrl, proof.getContractAddress());
    final Query query = QueryStringParser.parse(proof.getQuery());
    log.debug("verifyExistenceProof() query={}", query);
    
    boolean proofSignaturePass = isProofSignaturePass(proof, report);
    
    final VerifyClearanceRecordService verifyClearanceRecordService = getVerifyClearanceRecordService(
        contractService.getContractVersion());
    final Map<Long, VerifiedClearanceRecordInfo> verifiedCrMap = verifyClearanceRecordService.buildVerifiedClearanceRecordInfoMap(
        proof.getClearanceRecords(), latestCR, report);
    final boolean crPass = isCrListPass(verifiedCrMap);
    
    log.debug("verifyExistenceProof() crPass={}, verifiedCrMap={}", crPass, verifiedCrMap);
    
    final List<VerifyReceiptAndMerkleProofResult> existProofResults = new ArrayList<>();
    final List<Long> errorCoList = new ArrayList<>();
    VerifyProofCount count = new VerifyProofCount();
    
    boolean pass = proofSignaturePass && crPass;
    final List<Long> clearanceOrderList = collectProofClearanceOrderReversedList(proof.getExistenceProofs());
    for (Long clearanceOrder : clearanceOrderList) {
      final VerifiedClearanceRecordInfo verifiedClearanceRecordInfo = verifiedCrMap.get(clearanceOrder);
      final List<ExistenceProof> existenceProofList = verifyProofListService.collectExistenceProofByClearanceOrder(
          clearanceOrder, proof);
      final VerifyExistenceProofReport verifyExistenceProofReport = new VerifyExistenceProofReport();
      final VerifySingleCOExistenceProofResult result = verifySingleCOExistenceProof(
          VerifySingleCOExistenceProofInput.builder()
                                           .query(query)
                                           .serverWalletAddress(proof.getServerWalletAddress())
                                           .isLatestCO(latestCR.getClearanceOrder()
                                                               .equals(clearanceOrder))
                                           .verifiedClearanceRecordInfo(verifiedClearanceRecordInfo)
                                           .existenceProofList(existenceProofList)
                                           .build(),
          verifyExistenceProofReport);
      log.debug("verifyExistenceProof() verifyExistenceProofReport={}", verifyExistenceProofReport);
      report.addVerifyExistenceProofList(verifyExistenceProofReport);
      count = count.add(result.getCount());
      pass &= result.isPass();
      addAllIfNotNull(existProofResults, result.getExistenceProofResultList());
      if (result.isCoError) {
        errorCoList.add(clearanceOrder);
      }
    }
    
    final VerifyVerificationProofResult result = VerifyVerificationProofResult.builder()
                                                                              .query(proof.getQuery())
                                                                              .queryType(query.getType())
                                                                              .totalCount(count.getTotalCount())
                                                                              .successCount(count.getSuccessCount())
                                                                              .modifiedCount(count.getModifiedCount())
                                                                              .removedCount(count.getRemovedCount())
                                                                              .addedCount(count.getAddedCount())
                                                                              .status(pass
                                                                                  ? VerifyVerificationProofStatus.ALL_PASS
                                                                                  : VerifyVerificationProofStatus.SIG_ERROR)
                                                                              .verifyReceiptResults(existProofResults)
                                                                              .errorClearanceOrderInClearanceRecordList(
                                                                                  errorCoList)
                                                                              .verifyReport(report)
                                                                              .build();
    log.debug("verifyExistenceProof() result={}", result);
    return result;
  }
  
  boolean isProofSignaturePass(@NonNull final VerificationProof proof, @NonNull final VerifyReport report) {
    String signData = proof.toSignData();
    boolean proofSignaturePass = SignatureUtil.verifySignature(proof.getServerWalletAddress(), proof.getSigServer(),
        signData);
    report.setVerifyProofSigReport(VerifyReportUtil.buildProofSignaturePassReport(proof.getServerWalletAddress(),
        proof.getSigServer(), signData, proofSignaturePass));
    if (!proofSignaturePass) {
      signData = proof.toSignDataWithLombok();
      proofSignaturePass = SignatureUtil.verifySignature(proof.getServerWalletAddress(), proof.getSigServer(),
          signData);
      if (proofSignaturePass) {
        report.setVerifyProofSigReport(VerifyReportUtil.buildProofSignaturePassReport(proof.getServerWalletAddress(),
            proof.getSigServer(), signData, proofSignaturePass));
      }
    }
    log.debug("verifyExistenceProof() proofSignaturePass={}", proofSignaturePass);
    return proofSignaturePass;
  }
  
  VerifySingleCOExistenceProofResult verifySingleCOExistenceProof(
      @NonNull final VerifySingleCOExistenceProofInput input, @NonNull final VerifyExistenceProofReport report) {
    log.debug("verifySingleCOExistenceProof() start, input={}", input);
    report.setVerifyCODescription(VerifyReportUtil.buildVerifyExistenceSingleCODescriptionReport(
        input.getVerifiedClearanceRecordInfo(), input.getExistenceProofList()));
    report.setVerifyMerkleProofReportList(new ArrayList<>());
    boolean pass = false;
    boolean isCoError = false;
    VerifyProofCount count = new VerifyProofCount();
    List<VerifyReceiptAndMerkleProofResult> resultList = new ArrayList<>();
    final VerifiedClearanceRecordInfo verifiedClearanceRecordInfo = input.getVerifiedClearanceRecordInfo();
    if ((Objects.nonNull(verifiedClearanceRecordInfo) && verifiedClearanceRecordInfo.isPass()) || input.isLatestCO()) {
      final ClearanceRecord contractClearanceRecord = verifiedClearanceRecordInfo.getClearanceRecord();
      VerifyProofListResult verifyProofListResult;
      if (!Query.QueryType.LOCATOR.equals(input.getQuery()
                                               .getType())) {
        verifyProofListResult = verifyProofListService.verifyExistenceProofWithNotExist(input.getExistenceProofList(),
            input.getServerWalletAddress(), contractClearanceRecord, input.getQuery());
      } else {
        verifyProofListResult = verifyProofListService.verifyOnlyExistenceProof(input.getExistenceProofList(),
            contractClearanceRecord, input.getServerWalletAddress());
      }
      log.debug("verifySingleCOExistenceProof() verifyProofListResult={}", verifyProofListResult);
      count = count.add(verifyProofListResult.getCount());
      pass = verifyProofListResult.isPass();
      addAllIfNotNull(resultList, verifyProofListResult.getProofResultList());
    }
    if (Objects.isNull(verifiedClearanceRecordInfo) || !verifiedClearanceRecordInfo.isPass()) {
      isCoError = true;
      if (!input.isLatestCO()) {
        final List<VerifyReceiptAndMerkleProofResult> failResult = buildClearanceRecordFailResult(
            input.getExistenceProofList(), verifiedClearanceRecordInfo);
        count = count.add(VerifyProofCount.builder()
                                          .totalCount(failResult.size())
                                          .modifiedCount(failResult.size())
                                          .build());
        resultList.addAll(failResult);
      }
    }
    addAllReport(resultList, report);
    final VerifySingleCOExistenceProofResult result = VerifySingleCOExistenceProofResult.builder()
                                                                                        .count(count)
                                                                                        .pass(pass)
                                                                                        .existenceProofResultList(
                                                                                            resultList)
                                                                                        .isCoError(isCoError)
                                                                                        .build();
    log.debug("verifySingleCOExistenceProof() end, result={}", result);
    return result;
  }
  
  void addAllReport(List<VerifyReceiptAndMerkleProofResult> resultList, VerifyExistenceProofReport report) {
    log.debug("addAllReport() start, verifyProofListResultSize={}, reportSize={}", resultList.size(), report);
    resultList.forEach(result -> {
      final VerifyMerkleProofReport verifyMerkleProofReport = toMerkleProofReport(result);
      report.addVerifyMerkleProofReport(verifyMerkleProofReport);
    });
    log.debug("addAllReport() end");
  }
  
  private VerifyMerkleProofReport toMerkleProofReport(@NonNull final VerifyReceiptAndMerkleProofResult result) {
    log.debug("toMerkleProofReport() start, result={}", result);
    final VerifyMerkleProofReport report = VerifyMerkleProofReport.builder()
                                                                  .descriptionReport(result.getDescriptionReport())
                                                                  .verifyPbPairsReport(result.getVerifyPbPairsReport())
                                                                  .verifySliceReport(result.getVerifySliceReport())
                                                                  .build();
    log.debug("toMerkleProofReport() end, report={}", report);
    return report;
  }
  
  private ClientContractService getClientContractService(@NonNull final String nodeUrl,
      @NonNull final String contractAddress) {
    final String key = nodeUrl + contractAddress;
    final ClientContractService contractService = clientContractServiceCache.getIfPresent(key);
    if (Objects.isNull(contractService)) {
      final String errMsg = String.format(
          "verify() error, get contractService from cache fail, nodeUrl=%s, contractAddress=%s", nodeUrl,
          contractAddress);
      log.error(errMsg);
      throw new RuntimeException(errMsg);
    }
    return contractService;
  }
  
  String getNodeUrl(@NonNull final VerificationProof proof, @NonNull final String infuraProjectId) {
    final String nodeUrl;
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
    return nodeUrl;
  }
  
  VerifyClearanceRecordService getVerifyClearanceRecordService(@NonNull final String contractVersion) {
    log.debug("getVerifyClearanceRecordService() start, contractVersion={}", contractVersion);
    VerifyClearanceRecordService service;
    if (contractVersion.startsWith("2.3.0")) {
      service = newVerifyClearanceRecordService;
    } else {
      service = oldVerifyClearanceRecordService;
    }
    log.debug("getVerifyClearanceRecordService() end, service={}", service);
    return service;
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
                                            .descriptionReport(
                                                "[Verify] fail to obtain clearance record, all Merkle proofs in current clearance order is not available to verify.")
                                            .verifySliceReport("")
                                            .verifyPbPairsReport("")
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
}
