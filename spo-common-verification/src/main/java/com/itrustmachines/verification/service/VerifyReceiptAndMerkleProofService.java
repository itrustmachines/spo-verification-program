package com.itrustmachines.verification.service;

import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.contract.ClearanceRecordService;
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
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerifyReceiptAndMerkleProofService {
  
  private final String serverWalletAddress;
  private final ClearanceRecordService clearanceRecordService;
  
  public VerifyReceiptAndMerkleProofService(final @NonNull String serverWalletAddress,
      final @NonNull ClearanceRecordService clearanceRecordService) {
    this.serverWalletAddress = serverWalletAddress;
    this.clearanceRecordService = clearanceRecordService;
    log.info("new instance={}", this);
  }
  
  @SneakyThrows
  public VerifyReceiptAndMerkleProofResult verify(final @NonNull Receipt receipt,
      final @NonNull MerkleProof merkleProof) {
    log.debug("verify() begin, receipt={}, merkleProof={}", receipt, merkleProof);
    final ClearanceRecord clearanceRecord = clearanceRecordService.obtainClearanceRecord(receipt.getClearanceOrder());
    return verify(receipt, merkleProof, clearanceRecord);
  }
  
  public VerifyReceiptAndMerkleProofResult verify(final @NonNull Receipt receipt,
      final @NonNull MerkleProof merkleProof, final @NonNull ClearanceRecord clearanceRecord) {
    log.debug("verify() begin, receipt={}, merkleProof={}", receipt, merkleProof);
    
    boolean isMerkleProofSignatureOk = false;
    boolean isReceiptSignatureOk = false;
    boolean isClearanceOrderCorrect = false;
    boolean isPbPairOk = false;
    boolean isSliceOk = false;
    boolean isRootHashCorrect = false;
    boolean isVerifyPass = false;
    
    // TODO verify receipt?
    // TODO change verify order, no need to continue verify when verify fail
    
    // verify merkleProof signature
    isMerkleProofSignatureOk = verifyMerkleProofSignature(merkleProof, serverWalletAddress);
    
    // verify receipt signature
    isReceiptSignatureOk = verifyReceiptSignature(receipt, serverWalletAddress);
    
    // verify clearanceOrder
    isClearanceOrderCorrect = verifyClearanceOrder(merkleProof.getClearanceOrder(),
        clearanceRecord.getClearanceOrder());
    
    // verify PbPair
    isPbPairOk = verifyPbPair(receipt, merkleProof);
    
    // verify slice
    isSliceOk = verifyMerkleProofSlice(merkleProof);
    
    // verify if clearanceRecord rootHash and merkle proof slice rootHash match
    isRootHashCorrect = verifyRootHash(merkleProof, clearanceRecord);
    
    // overall result
    isVerifyPass = isMerkleProofSignatureOk && isReceiptSignatureOk && isClearanceOrderCorrect && isPbPairOk
        && isSliceOk && isRootHashCorrect;
    log.debug("verify() isVerifyPass={}", isVerifyPass);
    
    final VerifyReceiptAndMerkleProofResult result = VerifyReceiptAndMerkleProofResult.builder()
                                                                                      .clearanceOrder(
                                                                                          receipt.getClearanceOrder())
                                                                                      .indexValue(
                                                                                          receipt.getIndexValue())
                                                                                      .timestamp(receipt.getTimestamp())
                                                                                      .merkleproofSignatureOk(
                                                                                          isMerkleProofSignatureOk)
                                                                                      .receiptSignatureOk(
                                                                                          isReceiptSignatureOk)
                                                                                      .clearanceOrderOk(
                                                                                          isClearanceOrderCorrect)
                                                                                      .pbPairOk(isPbPairOk)
                                                                                      .sliceOk(isSliceOk)
                                                                                      .clearanceRecordRootHashOk(
                                                                                          isRootHashCorrect)
                                                                                      .pass(isVerifyPass)
                                                                                      .status(isVerifyPass
                                                                                          ? StatusConstantsString.OK
                                                                                          : StatusConstantsString.ERROR)
                                                                                      .build();
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
  
  boolean verifyClearanceOrder(final Long merkleProofClearanceOrder, final Long clearanceRecordClearanceOrder) {
    log.debug("verifyClearanceOrder() begin, merkleProofClearanceOrder={}, clearanceRecordClearanceOrder={}",
        merkleProofClearanceOrder, clearanceRecordClearanceOrder);
    boolean result;
    result = merkleProofClearanceOrder.equals(clearanceRecordClearanceOrder);
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
