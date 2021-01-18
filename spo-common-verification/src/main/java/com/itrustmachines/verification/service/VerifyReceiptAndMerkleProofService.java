package com.itrustmachines.verification.service;

import java.nio.charset.StandardCharsets;

import com.itrustmachines.verification.constants.ProofExistStatus;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;
import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.util.ClearanceRecordVerifyUtil;
import com.itrustmachines.verification.util.SliceValidationUtil;
import com.itrustmachines.verification.vo.ExistenceProof;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerifyReceiptAndMerkleProofService {
  
  private final String serverWalletAddress;
  private final ClientContractService clearanceRecordService;
  
  public VerifyReceiptAndMerkleProofService(@NonNull final String serverWalletAddress,
      @NonNull final ClientContractService clearanceRecordService) {
    this.serverWalletAddress = serverWalletAddress;
    this.clearanceRecordService = clearanceRecordService;
    log.info("new instance={}", this);
  }
  
  public ClearanceRecord obtainClearanceRecord(final long clearanceOrder) {
    log.debug("obtainClearanceRecord() start, clearanceOrder={}", clearanceOrder);
    ClearanceRecord clearanceRecord = null;
    try {
      clearanceRecord = clearanceRecordService.obtainClearanceRecord(clearanceOrder);
    } catch (Exception e) {
      log.error("obtainClearanceRecord() error, obtainClearanceRecord error", e);
    }
    log.debug("obtainClearanceRecord() end, clearanceRecord={}", clearanceRecord);
    return clearanceRecord;
  }
  
  public VerifyReceiptAndMerkleProofResult verify(@NonNull final Receipt receipt,
      @NonNull final MerkleProof merkleProof) {
    log.debug("verify() start, receipt={}, merkleProof={}", receipt, merkleProof);
    final ClearanceRecord clearanceRecord = clearanceRecordService.obtainClearanceRecord(receipt.getClearanceOrder());
    if (clearanceRecord == null) {
      final String errMsg = String.format("clearanceRecord is null, CO=%d", receipt.getClearanceOrder());
      log.warn(errMsg);
      throw new RuntimeException(errMsg);
    }
    return verify(receipt, merkleProof, clearanceRecord);
  }
  
  public VerifyReceiptAndMerkleProofResult verify(@NonNull final ExistenceProof existenceProof,
      final ClearanceRecord clearanceRecord) {
    log.debug("verify() start, existenceProof={}, clearanceRecord={}", existenceProof, clearanceRecord);
    final Receipt receipt = existenceProof.getReceipt();
    VerifyReceiptAndMerkleProofResult result = this.verify(receipt, existenceProof.getMerkleProof(), clearanceRecord);
    
    boolean clearanceOrderAndIndexValueOk = Objects.equal(existenceProof.getClearanceOrder(),
        receipt.getClearanceOrder()) && StringUtils.equals(existenceProof.getIndexValue(), receipt.getIndexValue());
    result.setPass(result.isPass() && existenceProof.isExist() && clearanceOrderAndIndexValueOk);
    result.setProofExistStatus(result.isPass() ? ProofExistStatus.PASS : ProofExistStatus.MODIFIED);
    result.setClearanceOrder(existenceProof.getClearanceOrder());
    result.setIndexValue(existenceProof.getIndexValue());
    log.debug("verify() end, result={}", result);
    return result;
  }
  
  public VerifyReceiptAndMerkleProofResult verify(@NonNull final Receipt receipt,
      @NonNull final MerkleProof merkleProof, final ClearanceRecord clearanceRecord) {
    log.debug("verify() start, receipt={}, merkleProof={}, clearanceRecord={}", receipt, merkleProof, clearanceRecord);
    
    boolean isMerkleProofSignatureOk;
    boolean isReceiptSignatureOk;
    boolean isClearanceOrderCorrect;
    boolean isPbPairOk;
    boolean isSliceOk;
    boolean isRootHashCorrect;
    final String rootHash = SliceValidationUtil.getRootHashString(merkleProof.getSlice());
    
    final long timestamp = System.currentTimeMillis();
    
    final VerifyReceiptAndMerkleProofResult result = VerifyReceiptAndMerkleProofResult.builder()
                                                                                      .existenceType(
                                                                                          ExistenceType.EXIST)
                                                                                      .pass(false)
                                                                                      .status(
                                                                                          StatusConstantsString.ERROR)
                                                                                      .txHash(
                                                                                          clearanceRecord.getTxHash())
                                                                                      .timestamp(timestamp)
                                                                                      .ledgerInputTimestamp(
                                                                                          receipt.getTimestamp())
                                                                                      .receiptTimestamp(
                                                                                          receipt.getTimestampSPO())
                                                                                      .clearanceOrder(
                                                                                          receipt.getClearanceOrder())
                                                                                      .indexValue(
                                                                                          receipt.getIndexValue())
                                                                                      .cmd(receipt.getCmd())
                                                                                      .proofExistStatus(
                                                                                          ProofExistStatus.MODIFIED)
                                                                                      .contractRootHash(
                                                                                          clearanceRecord.getRootHash())
                                                                                      .merkleProofRootHash(rootHash)
                                                                                      .description("verify fail")
                                                                                      .build();
    log.debug("verify() initiate verify result={}", result);
    
    // verify receipt signature
    isReceiptSignatureOk = verifyReceiptSignature(receipt, serverWalletAddress);
    
    if (!isReceiptSignatureOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setReceiptSignatureOk(true);
    }
    
    // verify merkleProof signature
    isMerkleProofSignatureOk = verifyMerkleProofSignature(merkleProof, serverWalletAddress);
    if (!isMerkleProofSignatureOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setMerkleproofSignatureOk(true);
    }
    
    // verify clearanceOrder
    isClearanceOrderCorrect = verifyClearanceOrder(receipt.getClearanceOrder(), merkleProof.getClearanceOrder(),
        clearanceRecord.getClearanceOrder());
    if (!isClearanceOrderCorrect) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setClearanceOrderOk(true);
    }
    
    // verify PbPair
    isPbPairOk = verifyPbPair(receipt, merkleProof);
    if (!isPbPairOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setPbPairOk(true);
    }
    
    // verify slice
    isSliceOk = verifyMerkleProofSlice(merkleProof);
    if (!isSliceOk) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setSliceOk(true);
    }
    
    // verify if clearanceRecord rootHash and merkle proof slice rootHash match
    isRootHashCorrect = verifyRootHash(merkleProof, clearanceRecord);
    if (!isRootHashCorrect) {
      log.debug("verify() result={}", result);
      return result;
    } else {
      result.setClearanceRecordRootHashOk(true);
    }
    
    // overall result
    result.setPass(true);
    result.setStatus(StatusConstantsString.OK);
    result.setProofExistStatus(ProofExistStatus.PASS);
    result.setDescription(StatusConstantsString.OK);
    log.debug("verify() result={}", result);
    return result;
  }
  
  boolean verifyRootHash(final MerkleProof merkleProof, final ClearanceRecord clearanceRecord) {
    log.debug("verifyRootHash() start, merkleProof={}, clearanceRecord={}", merkleProof, clearanceRecord);
    final byte[] merkleProofRootHash = SliceValidationUtil.getRootHash(merkleProof.getSlice());
    boolean result = ClearanceRecordVerifyUtil.isRootHashEqual(clearanceRecord, merkleProofRootHash);
    log.debug("verifyRootHash() end, result={}", result);
    return result;
  }
  
  boolean verifyMerkleProofSlice(final MerkleProof merkleProof) {
    log.debug("verifyMerkleProofSlice() start, merkleProof={}", merkleProof);
    boolean result = SliceValidationUtil.evalRootHashFromSlice(merkleProof.getSlice());
    log.debug("verifyMerkleProofSlice() result={}", result);
    return result;
  }
  
  boolean verifyPbPair(final Receipt receipt, final MerkleProof merkleProof) {
    log.debug("verifyPbPair() start, receipt={}, merkleProof={}", receipt, merkleProof);
    boolean result = false;
    for (PBPair.PBPairValue pbPairValue : merkleProof.getPbPair()) {
      final String indexValueHash = HashUtils.byte2hex(HashUtils.sha256(receipt.getIndexValue()
                                                                               .getBytes(StandardCharsets.UTF_8)));
      final String keyHash = pbPairValue.getKeyHash();
      
      result = indexValueHash.equalsIgnoreCase(keyHash);
      log.debug("verify IV digest={}: indexValueHash={}, pbPairKeyHash={}", result, indexValueHash, keyHash);
      if (result) {
        break;
      }
    }
    final boolean verifyReceiptDigest = SliceValidationUtil.isLeafNode(merkleProof.getSlice(), merkleProof.getPbPair(),
        receipt.toDigestValue());
    log.debug("verify Receipt digest={}", verifyReceiptDigest);
    result &= verifyReceiptDigest;
    log.debug("verifyPbPair() end, result={}", result);
    return result;
  }
  
  boolean verifyClearanceOrder(final Long receiptClearanceOrder, final Long merkleProofClearanceOrder,
      final Long clearanceRecordClearanceOrder) {
    log.debug(
        "verifyClearanceOrder() start, receiptClearanceOrder={}, merkleProofClearanceOrder={}, clearanceRecordClearanceOrder={}",
        receiptClearanceOrder, merkleProofClearanceOrder, clearanceRecordClearanceOrder);
    boolean result = merkleProofClearanceOrder.equals(clearanceRecordClearanceOrder)
        && receiptClearanceOrder.equals(merkleProofClearanceOrder);
    log.debug("verifyClearanceOrder() end, result={}", result);
    return result;
  }
  
  boolean verifyReceiptSignature(final Receipt receipt, final String serverWalletAddress) {
    log.debug("verifyReceiptSignature() start, receipt={}, serverWalletAddress={}", receipt, serverWalletAddress);
    boolean result = SignatureUtil.verifySignature(serverWalletAddress, receipt.getSigServer(),
        receipt.toSignDataSha3());
    log.debug("verifyReceiptSignature() end, result={}", result);
    return result;
  }
  
  boolean verifyMerkleProofSignature(final MerkleProof merkleProof, final String serverWalletAddress) {
    log.debug("verifyMerkleProofSignature() start, merkleProof={}, serverWalletAddress={}", merkleProof,
        serverWalletAddress);
    boolean result = SignatureUtil.verifySignature(serverWalletAddress, merkleProof.getSigServer(),
        merkleProof.toSignDataSha3());
    log.debug("verifyMerkleProofSignature() end, result={}", result);
    return result;
  }
  
}
