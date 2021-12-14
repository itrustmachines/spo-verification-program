package com.itrustmachines.verification.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.util.IndexValueProperties;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.constants.ProofExistStatus;
import com.itrustmachines.verification.constants.VerifyNotExistProofStatus;
import com.itrustmachines.verification.util.ClearanceRecordVerifyUtil;
import com.itrustmachines.verification.util.SliceValidationUtil;
import com.itrustmachines.verification.vo.*;
import com.itrustmachines.verification.vo.report.VerifyNotExistReport;
import com.itrustmachines.verification.vo.report.VerifyPbPairReport;
import com.itrustmachines.verification.vo.report.VerifySliceReport;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyProofListService {
  
  private final VerifyReceiptAndMerkleProofService verifyReceiptAndMerkleProofService;
  
  public VerifyProofListService() {
    this.verifyReceiptAndMerkleProofService = new VerifyReceiptAndMerkleProofService();
  }
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  static class NotExistProofVerifySuccessInfo {
    ExistenceProof notExistProof;
    VerifyReceiptAndMerkleProofResult verifyResult;
  }
  
  VerifyProofListResult verifyExistenceProofWithNotExist(@NonNull final List<ExistenceProof> existenceProofListInSameCO,
      @NonNull final String serverWalletAddress, final ClearanceRecord clearanceRecord, @NonNull final Query query) {
    log.debug(
        "verifyExistenceProofWithNotExist() start, existenceProofListSize={}, serverWalletAddress={}, clearanceRecord={}",
        existenceProofListInSameCO.size(), serverWalletAddress, clearanceRecord);
    
    List<VerifyReceiptAndMerkleProofResult> proofResultList = new ArrayList<>();
    
    boolean pass = true;
    long successCount = 0L, modifiedCount = 0L, removedCount = 0L, addedCount = 0L;
    
    final List<ExistenceProof> notExistProofList = findNotExistProofList(existenceProofListInSameCO);
    log.debug("verifyExistenceProofWithNotExist() notExistProofListSize={}", notExistProofList.size());
    final NotExistProofVerifySuccessInfo successNotExistProofInfo = verifyNotExistProofList(notExistProofList,
        serverWalletAddress, clearanceRecord, query);
    log.debug("verifyExistenceProofWithNotExist() successNotExistProofInfo={}", successNotExistProofInfo);
    
    final Long firstSuccessNotExistProofSn = getSnFromNotExistProofSuccessInfo(successNotExistProofInfo);
    long currentSN = getCurrentSnFromQuery(clearanceRecord, query);
    for (int i = 0; i < existenceProofListInSameCO.size(); i++) {
      final ExistenceProof currentProof = existenceProofListInSameCO.get(i);
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
        result = verifyReceiptAndMerkleProof(currentProof, serverWalletAddress, clearanceRecord);
      } else if (currentSN == firstSuccessNotExistProofSn && successNotExistProofInfo.getNotExistProof()
                                                                                     .equals(currentProof)) {
        result = successNotExistProofInfo.getVerifyResult();
      } else {
        result = verifyNotExistProof(currentProof, serverWalletAddress, clearanceRecord, query.getFromCO(),
            query.getToCO());
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
      if (i + 1 < existenceProofListInSameCO.size()
          && !isNextExistenceProofHaveSameCOIV(currentProof, existenceProofListInSameCO.get(i + 1))) {
        currentSN++;
      }
      
    }
    final VerifyProofCount count = VerifyProofCount.builder()
                                                   .totalCount(existenceProofListInSameCO.size())
                                                   .successCount(successCount)
                                                   .addedCount(addedCount)
                                                   .modifiedCount(modifiedCount)
                                                   .removedCount(removedCount)
                                                   .build();
    final VerifyProofListResult verifyProofListResult = VerifyProofListResult.builder()
                                                                             .pass(pass)
                                                                             .count(count)
                                                                             .proofResultList(proofResultList)
                                                                             .build();
    log.debug("verifyExistenceProofWithNotExist() end, verifyExistenceProofResult={}", verifyProofListResult);
    return verifyProofListResult;
  }
  
  long getCurrentSnFromQuery(@NonNull final ClearanceRecord clearanceRecord, @NonNull final Query query) {
    log.debug("getCurrentSnFromQuery() start, clearanceRecord={}, query={}", clearanceRecord, query);
    long sn = 0L;
    if (Query.QueryType.CLEARANCE_ORDER_AND_SN.equals(query.getType())) {
      if (clearanceRecord.getClearanceOrder()
                         .equals(query.getFromCO())) {
        sn = query.getFromSN();
      }
    }
    log.debug("getCurrentSnFromQuery() end, sn={}", sn);
    return sn;
  }
  
  VerifyProofListResult verifyOnlyExistenceProof(@NonNull final List<ExistenceProof> existenceProofListInSameCO,
      @NonNull final ClearanceRecord clearanceRecord, @NonNull final String serverWalletAddress) {
    log.debug("verifyOnlyExistenceProof() start, existenceProofListSize={}, clearanceRecord={}",
        existenceProofListInSameCO.size(), clearanceRecord);
    boolean pass = true;
    long successCount = 0L, modifiedCount = 0L;
    
    List<VerifyReceiptAndMerkleProofResult> existProofResultList = new ArrayList<>();
    
    for (ExistenceProof existenceProof : existenceProofListInSameCO) {
      final VerifyReceiptAndMerkleProofResult result = verifyReceiptAndMerkleProof(existenceProof, serverWalletAddress,
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
    final VerifyProofCount count = VerifyProofCount.builder()
                                                   .totalCount(existenceProofListInSameCO.size())
                                                   .successCount(successCount)
                                                   .modifiedCount(modifiedCount)
                                                   .build();
    final VerifyProofListResult existenceProofResult = VerifyProofListResult.builder()
                                                                            .pass(pass)
                                                                            .count(count)
                                                                            .proofResultList(existProofResultList)
                                                                            .build();
    
    log.debug("verifyOnlyExistenceProof() end, existenceProofResult={}", existenceProofResult);
    return existenceProofResult;
  }
  
  VerifyReceiptAndMerkleProofResult verifyNotExistProof(@NonNull final ExistenceProof proof,
      @NonNull final String serverWalletAddress, @NonNull final ClearanceRecord contractClearanceRecord,
      final long fromCO, final long toCO) {
    
    final SpoSignature sigServer = proof.getMerkleProof()
                                        .getSigServer();
    // verify isMerkleproofSignatureOk
    final boolean isMerkleProofSignature = SignatureUtil.verifySignature(serverWalletAddress, sigServer,
        proof.getMerkleProof()
             .toSignData());
    
    final VerifyNotExistReport verifyNotExistReport = verifyNotExistMerkleProofAndReceipt(proof, fromCO, toCO,
        contractClearanceRecord, serverWalletAddress);
    VerifyNotExistProofStatus verifyNotExistProofStatus;
    if (isMerkleProofSignature) {
      verifyNotExistProofStatus = verifyNotExistReport.getVerifyNotExistProofStatus();
    } else {
      verifyNotExistProofStatus = VerifyNotExistProofStatus.ERROR_SIGNATURE;
    }
    
    boolean pass = !proof.isExist();
    if (!VerifyNotExistProofStatus.OK_CLEARANCE_NOT_BETWEEN_SEARCH_TIME.equals(verifyNotExistProofStatus)
        && !VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND.equals(verifyNotExistProofStatus)
        && !VerifyNotExistProofStatus.OK_RECEIPT_AND_MERKLE_PROOF_SHOULD_BE_END_OF_PROOF.equals(
            verifyNotExistProofStatus)) {
      pass = false;
    }
    
    String merkleProofRootHash = null;
    if (Objects.nonNull(proof.getMerkleProof())) {
      merkleProofRootHash = SliceValidationUtil.getRootHashString(proof.getMerkleProof()
                                                                       .getSlice());
    }
    return VerifyReceiptAndMerkleProofResult.builder()
                                            .pass(pass)
                                            .proofExistStatus(pass ? ProofExistStatus.PASS : ProofExistStatus.MODIFIED)
                                            .status(pass ? StatusConstantsString.OK : StatusConstantsString.ERROR)
                                            .existenceType(ExistenceType.NOT_EXIST)
                                            .clearanceOrder(proof.getClearanceOrder())
                                            .indexValue(proof.getIndexValue())
                                            .verifyNotExistProofResult(verifyNotExistProofStatus)
                                            .merkleproofSignatureOk(isMerkleProofSignature)
                                            .merkleProofRootHash(merkleProofRootHash)
                                            .contractRootHash(contractClearanceRecord.getRootHash())
                                            .txHash(contractClearanceRecord.getTxHash())
                                            .descriptionReport(verifyNotExistReport.getDescriptionReport())
                                            .verifyPbPairsReport(verifyNotExistReport.getVerifyPbPairsReport())
                                            .verifySliceReport(verifyNotExistReport.getVerifySliceReport())
                                            .build();
  }
  
  VerifyNotExistReport verifyNotExistMerkleProofAndReceipt(@NonNull final ExistenceProof proof, final long fromCO,
      final long toCO, final ClearanceRecord cr, @NonNull final String serverWalletAddress) {
    log.debug(
        "verifyNotExistMerkleProofAndReceipt start(), proof={}, fromCO={}, toCO={}, cr={}, serverWalletAddress={}",
        proof, fromCO, toCO, cr, serverWalletAddress);
    // verify isInPbPair
    final MerkleProof merkleProof = proof.getMerkleProof();
    final String indexValue = proof.getIndexValue();
    final Receipt receipt = proof.getReceipt();
    
    final VerifyPbPairReport verifyPbPairReport = verifyNotExistPbPair(merkleProof, indexValue);
    final VerifySliceReport verifySliceReport = SliceValidationUtil.evalRootHashFromSlice(merkleProof.getSlice(),
        proof.getIndexValue());
    final boolean isRootHashEqual = ClearanceRecordVerifyUtil.isRootHashEqual(cr,
        SliceValidationUtil.getRootHash(merkleProof.getSlice()));
    
    VerifyNotExistProofStatus status = null;
    // if isInPbPair : check timestamp
    if (verifyPbPairReport.isInPbPair()) {
      if (receipt != null) {
        if (proof.getClearanceOrder() > toCO || proof.getClearanceOrder() < fromCO) {
          status = VerifyNotExistProofStatus.OK_CLEARANCE_NOT_BETWEEN_SEARCH_TIME;
        } else {
          if (!verifyReceiptAndMerkleProofService.verify(receipt, merkleProof, serverWalletAddress, cr)
                                                 .isPass()) {
            status = VerifyNotExistProofStatus.ERROR_INDEX_VALUE_IN_PAIR;
          } else {
            status = VerifyNotExistProofStatus.OK_RECEIPT_AND_MERKLE_PROOF_SHOULD_BE_END_OF_PROOF;
          }
        }
      } else {
        status = VerifyNotExistProofStatus.ERROR_INDEX_VALUE_IN_PAIR;
      }
    } else {
      status = VerifyNotExistProofStatus.OK_INDEX_VALUE_NOT_FOUND;
    }
    if (!isRootHashEqual) {
      status = VerifyNotExistProofStatus.ERROR_ROOT_HASH_ERROR;
    }
    if (!verifySliceReport.isPass()) {
      status = VerifyNotExistProofStatus.ERROR_SLICE_ERROR;
    }
    final VerifyNotExistReport verifyNotExistReport = VerifyNotExistReport.builder()
                                                                          .descriptionReport(
                                                                              "[Verify Merkle proof] current proof for proof-of-not-existence\n")
                                                                          .verifyPbPairsReport(
                                                                              verifyPbPairReport.getIsInPbPairReport())
                                                                          .verifySliceReport(
                                                                              verifySliceReport.getVerifySliceReport())
                                                                          .verifyNotExistProofStatus(status)
                                                                          .build();
    log.debug("verifyNotExistMerkleProofAndReceipt() end, verifyNotExistReport={}", verifyNotExistReport);
    return verifyNotExistReport;
  }
  
  VerifyPbPairReport verifyNotExistPbPair(@NonNull final MerkleProof merkleProof, @NonNull final String indexValue) {
    log.debug("verifyNotExistPbPair() start, merkleProof={}, indexValue={}", merkleProof, indexValue);
    final StringBuilder pbPairReportBuilder = new StringBuilder();
    boolean isInPbPair = false;
    pbPairReportBuilder.append(String.format(
        "Current proof is for proof-of-not-existence, \n"
            + "the hash value of index value should not involve in any key value of PbPairs content\n"
            + "Number of content in PbPairs=%d\n",
        merkleProof.getPbPair()
                   .size()));
    for (PBPair.PBPairValue pbPairValue : merkleProof.getPbPair()) {
      final String indexValueHash = HashUtils.byte2hex(HashUtils.sha256(indexValue.getBytes(StandardCharsets.UTF_8)));
      final String key = pbPairValue.getKeyHash();
      isInPbPair = indexValueHash.equalsIgnoreCase(key);
      if (isInPbPair) {
        break;
      }
    }
    for (PBPair.PBPairValue pbPairValue : merkleProof.getPbPair()) {
      
      pbPairReportBuilder.append(String.format("[PbPair list content, index=%d]\n\tkey=%s, \n\tvalue=%s\n",
          pbPairValue.getIndex(), pbPairValue.getKeyHash(), pbPairValue.getValue()));
    }
    final VerifyPbPairReport verifyPbPairReport = VerifyPbPairReport.builder()
                                                                    .inPbPair(isInPbPair)
                                                                    .isInPbPairReport(pbPairReportBuilder.toString())
                                                                    .build();
    log.debug("verifyNotExistPbPair() start, verifyPbPairReport={}", verifyPbPairReport);
    return verifyPbPairReport;
  }
  
  VerifyReceiptAndMerkleProofResult verifyReceiptAndMerkleProof(@NonNull final ExistenceProof existenceProof,
      @NonNull final String serverWalletAddress, final ClearanceRecord clearanceRecord) {
    log.debug("verify() start, existenceProof={}, clearanceRecord={}", existenceProof, clearanceRecord);
    final Receipt receipt = existenceProof.getReceipt();
    VerifyReceiptAndMerkleProofResult result = verifyReceiptAndMerkleProofService.verify(receipt,
        existenceProof.getMerkleProof(), serverWalletAddress, clearanceRecord);
    
    boolean clearanceOrderAndIndexValueOk = com.google.common.base.Objects.equal(existenceProof.getClearanceOrder(),
        receipt.getClearanceOrder()) && StringUtils.equals(existenceProof.getIndexValue(), receipt.getIndexValue());
    result.setPass(result.isPass() && existenceProof.isExist() && clearanceOrderAndIndexValueOk);
    result.setProofExistStatus(result.isPass() ? ProofExistStatus.PASS : ProofExistStatus.MODIFIED);
    result.setClearanceOrder(existenceProof.getClearanceOrder());
    result.setIndexValue(existenceProof.getIndexValue());
    log.debug("verify() end, result={}", result);
    return result;
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
  
  ExistenceProof getNextExistenceProof(final int index, @NonNull final List<ExistenceProof> existenceProofList) {
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
    
    log.debug("buildRemovedProofResultList() end, resultListSize={}", resultList.size());
    return resultList;
  }
  
  List<ExistenceProof> findNotExistProofList(@NonNull final List<ExistenceProof> existenceProofList) {
    log.debug("findNotExistProofList() start, existenceProofListSize={}", existenceProofList.size());
    final List<ExistenceProof> notExistProofList = existenceProofList.stream()
                                                                     .filter(
                                                                         existenceProof -> !existenceProof.isExist())
                                                                     .collect(Collectors.toList());
    
    log.debug("findNotExistProofList() start, notExistProofList={}", notExistProofList);
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
      final VerifyReceiptAndMerkleProofResult verifyResult = verifyNotExistProof(notExistProof, serverWalletAddress,
          clearanceRecord, query.getFromCO(), query.getToCO());
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
