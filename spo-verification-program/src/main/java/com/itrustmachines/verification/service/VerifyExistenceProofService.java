package com.itrustmachines.verification.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.itrustmachines.common.util.IndexValueProperties;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.constants.ProofExistStatus;
import com.itrustmachines.verification.vo.ExistenceProof;
import com.itrustmachines.verification.vo.Query;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyExistenceProofService {
  
  private final VerifyNotExistProofService verifyNotExistProofService;
  
  public VerifyExistenceProofService() {
    verifyNotExistProofService = new VerifyNotExistProofService();
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  static class NotExistProofVerifySuccessInfo {
    ExistenceProof notExistProof;
    VerifyReceiptAndMerkleProofResult verifyResult;
  }
  
  public VerifyExistenceProofResult verifyExistenceProofWithNotExist(
      @NonNull final List<ExistenceProof> existenceProofList, @NonNull final String serverWalletAddress,
      final ClearanceRecord clearanceRecord, @NonNull final Query query,
      @NonNull final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService) {
    log.debug(
        "verifyExistenceProofWithNotExist() start, existenceProofListSize={}, serverWalletAddress={}, clearanceRecord={}",
        existenceProofList.size(), serverWalletAddress, clearanceRecord);
    
    List<VerifyReceiptAndMerkleProofResult> proofResultList = new ArrayList<>();
    
    boolean pass = true;
    long successCount = 0L, modifiedCount = 0L, removedCount = 0L, addedCount = 0L;
    
    final List<ExistenceProof> notExistProofList = findNotExistProofList(existenceProofList);
    log.debug("verifyExistenceProofWithNotExist() notExistProofList={}", notExistProofList);
    final NotExistProofVerifySuccessInfo successNotExistProofInfo = verifyNotExistProofList(notExistProofList,
        serverWalletAddress, clearanceRecord, query);
    log.debug("verifyExistenceProofWithNotExist() successNotExistProofInfo={}", successNotExistProofInfo);
    
    final Long firstSuccessNotExistProofSn = getSnFromNotExistProofSuccessInfo(successNotExistProofInfo);
    long currentSN = 0;
    for (int i = 0; i < existenceProofList.size(); i++) {
      final ExistenceProof currentProof = existenceProofList.get(i);
      final IndexValueProperties indexValueProperties = IndexValueProperties.of(currentProof.getIndexValue());
      if (currentSN < indexValueProperties.getSn() && indexValueProperties.getSn() < firstSuccessNotExistProofSn) {
        final List<VerifyReceiptAndMerkleProofResult> removedProofResultList = buildRemovedProofResultList(
            clearanceRecord.getClearanceOrder(), query.getIndexValueKey(), currentSN, indexValueProperties.getSn() - 1);
        log.debug("verifyExistenceProofWithNotExist() removedProofResultListSize={}", removedProofResultList.size());
        removedCount += removedProofResultList.size();
        currentSN = indexValueProperties.getSn();
        proofResultList.addAll(removedProofResultList);
      }
      
      VerifyReceiptAndMerkleProofResult result;
      if (currentSN == indexValueProperties.getSn() && indexValueProperties.getSn() < firstSuccessNotExistProofSn) {
        result = verifyReceiptAndMerkleProofService.verify(currentProof, clearanceRecord);
      } else if (currentSN == firstSuccessNotExistProofSn && successNotExistProofInfo.getNotExistProof()
                                                                                     .equals(currentProof)) {
        result = successNotExistProofInfo.getVerifyResult();
      } else {
        result = verifyNotExistProofService.verifyNotExistProof(currentProof, serverWalletAddress, clearanceRecord,
            query.getFromCO(), query.getToCO());
        result.setProofExistStatus(ProofExistStatus.ADDED);
      }
      pass &= result.isPass();
      log.debug("verifyExistenceProofWithNotExist() verify pass={}, result={}", pass, result.isPass());
      if (ProofExistStatus.PASS.equals(result.getProofExistStatus())) {
        successCount++;
      } else if (ProofExistStatus.MODIFIED.equals(result.getProofExistStatus())) {
        modifiedCount++;
      } else {
        addedCount++;
      }
      proofResultList.add(result);
      if (i + 1 < existenceProofList.size()
          && !isNextExistenceProofHaveSameCOIV(currentProof, existenceProofList.get(i + 1))) {
        currentSN++;
      }
      
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
  
  public VerifyExistenceProofResult verifyOnlyExistenceProof(@NonNull final List<ExistenceProof> existenceProofList,
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
      log.debug("verify pass={}, result={}", pass, result.isPass());
      existProofResultList.add(result);
      if (ProofExistStatus.PASS.equals(result.getProofExistStatus())) {
        successCount++;
      } else if (ProofExistStatus.MODIFIED.equals(result.getProofExistStatus())) {
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
  
  public List<ExistenceProof> collectExistenceProofByClearanceOrder(@NonNull final Long clearanceOrder,
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
  
  ExistenceProof getNextExistenceProof(@NonNull final int index,
      @NonNull final List<ExistenceProof> existenceProofList) {
    log.debug("getNextExistenceProof() start, index={}, existenceProofListSize={}", index, existenceProofList.size());
    ExistenceProof result = null;
    int nextIndex = index + 1;
    if (nextIndex < existenceProofList.size()) {
      result = existenceProofList.get(nextIndex);
    }
    log.debug("getNextExistenceProof() end, result={}", result);
    return result;
  }
  
  boolean isNextExistenceProofHaveSameCOIV(@NonNull final ExistenceProof currentProof,
      @NonNull final ExistenceProof nextProof) {
    log.debug("isNextExistenceProofHaveSameCOIV() start, currentProof={}, nextProof={}", currentProof, nextProof);
    boolean result = false;
    if (currentProof.getIndexValue()
                    .equalsIgnoreCase(nextProof.getIndexValue())
        && currentProof.getClearanceOrder()
                       .equals(nextProof.getClearanceOrder())) {
      result = true;
    }
    log.debug("isNextExistenceProofHaveSameCOIV() start, result={}", result);
    return result;
  }
  
  List<VerifyReceiptAndMerkleProofResult> buildRemovedProofResultList(@NonNull final Long clearanceOrder,
      @NonNull final String indexValueKey, @NonNull final Long fromSn, @NonNull final Long toSn) {
    log.debug("buildRemovedProofResultList() start, clearanceOrder={}, indexValueKey={}, fromSn={}, toSn={}",
        clearanceOrder, indexValueKey, fromSn, toSn);
    List<VerifyReceiptAndMerkleProofResult> resultList = new ArrayList<>();
    for (long i = fromSn; i <= toSn; i++) {
      resultList.add(VerifyReceiptAndMerkleProofResult.builder()
                                                      .clearanceOrder(clearanceOrder)
                                                      .indexValue(indexValueKey + "_R" + i)
                                                      .pass(false)
                                                      .proofExistStatus(ProofExistStatus.REMOVED)
                                                      .existenceType(ExistenceType.EXIST)
                                                      .build());
    }
    
    log.debug("buildRemovedProofResultList() end, resultList={}", resultList);
    return resultList;
  }
  
  List<ExistenceProof> findNotExistProofList(@NonNull final List<ExistenceProof> existenceProofList) {
    log.debug("findNotExistProofList() start, existenceProofListSize={}", existenceProofList.size());
    final List<ExistenceProof> notExistProofList = existenceProofList.stream()
                                                                     .filter(
                                                                         existenceProof -> !existenceProof.isExist())
                                                                     .collect(Collectors.toList());
    
    log.debug("findNotExistProofList() start, existenceProof={}", notExistProofList);
    return notExistProofList;
  }
  
  NotExistProofVerifySuccessInfo verifyNotExistProofList(@NonNull final List<ExistenceProof> notExistProofList,
      @NonNull final String serverWalletAddress, @NonNull final ClearanceRecord clearanceRecord,
      @NonNull final Query query) {
    log.debug(
        "verifyNotExistProofList() start, notExistProofListSize={}, serverWalletAddress={}, clearanceRecord={}, query={}",
        notExistProofList.size(), serverWalletAddress, clearanceRecord, query);
    NotExistProofVerifySuccessInfo result = null;
    
    for (ExistenceProof notExistProof : notExistProofList) {
      final VerifyReceiptAndMerkleProofResult verifyResult = verifyNotExistProofService.verifyNotExistProof(
          notExistProof, serverWalletAddress, clearanceRecord, query.getFromCO(), query.getToCO());
      if (verifyResult.isPass()) {
        result = NotExistProofVerifySuccessInfo.builder()
                                               .notExistProof(notExistProof)
                                               .verifyResult(verifyResult)
                                               .build();
        break;
      }
    }
    log.debug("verifyNotExistProofList() end, result={}", result);
    return result;
  }
  
  Long getSnFromNotExistProofSuccessInfo(final NotExistProofVerifySuccessInfo info) {
    log.debug("getSnFromNotExistProofSuccessInfo() start, info={}", info);
    Long sn = null;
    if (Objects.isNull(info) || Objects.isNull(info.getNotExistProof())) {
      sn = Long.MAX_VALUE;
    } else {
      final IndexValueProperties properties = IndexValueProperties.of(info.getNotExistProof()
                                                                          .getIndexValue());
      sn = properties.getSn();
    }
    log.debug("getSnFromNotExistProofSuccessInfo() end, sn={}", sn);
    return sn;
  }
  
}
