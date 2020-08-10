package com.itrustmachines.verification.service;

import com.itrustmachines.common.constants.StatusConstants;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.util.SignatureUtil;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.verification.util.ClearanceRecordVerifyUtil;
import com.itrustmachines.verification.util.SliceValidationUtil;
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
  
  public VerifyReceiptAndMerkleProofResult verify(@NonNull final Receipt receipt,
      @NonNull final MerkleProof merkleProof) {
    log.debug("verify() begin, receipt={}, merkleProof={}", receipt, merkleProof);
    final ClearanceRecord clearanceRecord = clearanceRecordService.obtainClearanceRecord(receipt.getClearanceOrder());
    if (clearanceRecord == null) {
      final String errMsg = String.format("clearanceRecord is null, CO=%d", receipt.getClearanceOrder());
      log.warn(errMsg);
      throw new RuntimeException(errMsg);
    }
    return verify(receipt, merkleProof, clearanceRecord);
  }
  
  public VerifyReceiptAndMerkleProofResult verify(@NonNull final Receipt receipt,
      @NonNull final MerkleProof merkleProof, @NonNull final ClearanceRecord clearanceRecord) {
    log.debug("verify() begin, receipt={}, merkleProof={}, clearanceRecord={}", receipt, merkleProof, clearanceRecord);
    
    boolean isMerkleProofSignatureOk = false;
    boolean isReceiptSignatureOk = false;
    boolean isClearanceOrderCorrect = false;
    boolean isPbPairOk = false;
    boolean isSliceOk = false;
    boolean isRootHashCorrect = false;
    
    final long timestamp = System.currentTimeMillis();
    
    final VerifyReceiptAndMerkleProofResult result = VerifyReceiptAndMerkleProofResult.builder()
                                                                                      .pass(false)
                                                                                      .timestamp(timestamp)
                                                                                      .clearanceOrder(
                                                                                          receipt.getClearanceOrder())
                                                                                      .indexValue(
                                                                                          receipt.getIndexValue())
                                                                                      .status(
                                                                                          StatusConstants.ERROR.name())
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
    result.setStatus(StatusConstants.OK.name());
    log.debug("verify() result={}", result);
    return result;
  }
  
  boolean verifyRootHash(final MerkleProof merkleProof, final ClearanceRecord clearanceRecord) {
    log.debug("verifyRootHash() begin, merkleProof={}, clearanceRecord={}", merkleProof, clearanceRecord);
    boolean result;
    final byte[] merkleProofRootHash = SliceValidationUtil.getRootHash(merkleProof.getSlice());
    result = ClearanceRecordVerifyUtil.isRootHashEqual(clearanceRecord, merkleProofRootHash);
    log.debug("verifyRootHash() end, result={}", result);
    return result;
  }
  
  boolean verifyMerkleProofSlice(final MerkleProof merkleProof) {
    log.debug("verifySlice() begin, merkleProof={}", merkleProof);
    boolean result;
    result = SliceValidationUtil.evalRootHashFromSlice(merkleProof.getSlice());
    log.debug("verify() result={}", result);
    return result;
  }
  
  boolean verifyPbPair(final Receipt receipt, final MerkleProof merkleProof) {
    log.debug("verifyPbPair() begin, receipt={}, merkleProof={}", receipt, merkleProof);
    boolean result = false;
    for (PBPair.PBPairValue pbPairValue : merkleProof.getPbPair()) {
      final String indexValue = HashUtils.byte2hex(HashUtils.sha256(receipt.getIndexValue()
                                                                           .getBytes()));
      final String key = pbPairValue.getKeyHash();
      result = indexValue.equalsIgnoreCase(key);
      if (result) {
        break;
      }
    }
    result &= SliceValidationUtil.isLeafNode(merkleProof.getSlice(), merkleProof.getPbPair(), receipt.toDigestValue());
    log.debug("verifyPbPair() end, result={}", result);
    return result;
  }
  
  boolean verifyClearanceOrder(final Long receiptClearanceOrder, final Long merkleProofClearanceOrder,
      final Long clearanceRecordClearanceOrder) {
    log.debug(
        "verifyClearanceOrder() begin, receiptClearanceOrder={}, merkleProofClearanceOrder={}, clearanceRecordClearanceOrder={}",
        receiptClearanceOrder, merkleProofClearanceOrder, clearanceRecordClearanceOrder);
    boolean result;
    result = merkleProofClearanceOrder.equals(clearanceRecordClearanceOrder)
        && receiptClearanceOrder.equals(merkleProofClearanceOrder);
    log.debug("verifyClearanceOrder() end, result={}", result);
    return result;
  }
  
  boolean verifyReceiptSignature(final Receipt receipt, final String serverWalletAddress) {
    log.debug("verifyReceiptSignature() begin, receipt={}, serverWalletAddress={}", receipt, serverWalletAddress);
    boolean result = false;
    result = SignatureUtil.verifySignature(serverWalletAddress, receipt.getSigServer(), receipt.toSignDataSha3());
    log.debug("verifyReceiptSignature() end, result={}", result);
    return result;
  }
  
  boolean verifyMerkleProofSignature(final MerkleProof merkleProof, final String serverWalletAddress) {
    log.debug("verifyMerkleProofSignature() begin, merkleProof={}, serverWalletAddress={}", merkleProof,
        serverWalletAddress);
    boolean result = false;
    
    result = SignatureUtil.verifySignature(serverWalletAddress, merkleProof.getSigServer(),
        merkleProof.toSignDataSha3());
    log.debug("verifyMerkleProofSignature() end, result={}", result);
    return result;
  }
  
}
