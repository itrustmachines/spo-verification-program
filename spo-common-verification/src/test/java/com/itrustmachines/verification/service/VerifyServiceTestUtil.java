package com.itrustmachines.verification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.constants.VerifyStatus;
import com.itrustmachines.verification.vo.ExistenceProof;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyServiceTestUtil {
  
  public static void assertIsOK(VerifyReceiptAndMerkleProofResult result, Receipt receipt, MerkleProof merkleProof,
      ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(receipt.getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(receipt.getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(true);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.OK);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.PASS);
    // FIXME null / clearanceRecord.getTxHash()
    // assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(receipt.getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(receipt.getTimestampSPO());
    assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo(StatusConstantsString.OK);
    assertThat(result.getCmd()).isEqualTo(receipt.getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(true);
    assertThat(result.isClearanceOrderOk()).isEqualTo(true);
    assertThat(result.isPbPairOk()).isEqualTo(true);
    assertThat(result.isSliceOk()).isEqualTo(true);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(true);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertIsReceiptSignatureError(VerifyReceiptAndMerkleProofResult result, Receipt receipt,
      MerkleProof merkleProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(receipt.getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(receipt.getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    // FIXME null / clearanceRecord.getTxHash()
    // assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(receipt.getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(receipt.getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(receipt.getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(false);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(false);
    assertThat(result.isClearanceOrderOk()).isEqualTo(false);
    assertThat(result.isPbPairOk()).isEqualTo(false);
    assertThat(result.isSliceOk()).isEqualTo(false);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertIsMerkleProofSignatureError(VerifyReceiptAndMerkleProofResult result, Receipt receipt,
      MerkleProof merkleProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(receipt.getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(receipt.getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    // FIXME null / clearanceRecord.getTxHash()
    // assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(receipt.getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(receipt.getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(receipt.getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(false);
    assertThat(result.isClearanceOrderOk()).isEqualTo(false);
    assertThat(result.isPbPairOk()).isEqualTo(false);
    assertThat(result.isSliceOk()).isEqualTo(false);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertIsClearanceOrderError(VerifyReceiptAndMerkleProofResult result, Receipt receipt,
      MerkleProof merkleProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(receipt.getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(receipt.getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    // FIXME null / clearanceRecord.getTxHash()
    // assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(receipt.getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(receipt.getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(receipt.getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(true);
    assertThat(result.isClearanceOrderOk()).isEqualTo(false);
    assertThat(result.isPbPairOk()).isEqualTo(false);
    assertThat(result.isSliceOk()).isEqualTo(false);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertIsClearanceRecordRootHashError(VerifyReceiptAndMerkleProofResult result, Receipt receipt,
      MerkleProof merkleProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(receipt.getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(receipt.getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    // FIXME null / clearanceRecord.getTxHash()
    // assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(receipt.getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(receipt.getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(receipt.getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(true);
    assertThat(result.isClearanceOrderOk()).isEqualTo(true);
    assertThat(result.isPbPairOk()).isEqualTo(true);
    assertThat(result.isSliceOk()).isEqualTo(true);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertExistenceProofIsOK(VerifyReceiptAndMerkleProofResult result, ExistenceProof existenceProof,
      ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(existenceProof.getReceipt()
                                                                   .getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(existenceProof.getReceipt()
                                                               .getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(true);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.OK);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.PASS);
    assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                         .getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                     .getTimestampSPO());
    assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo(StatusConstantsString.OK);
    assertThat(result.getCmd()).isEqualTo(existenceProof.getReceipt()
                                                        .getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(true);
    assertThat(result.isClearanceOrderOk()).isEqualTo(true);
    assertThat(result.isPbPairOk()).isEqualTo(true);
    assertThat(result.isSliceOk()).isEqualTo(true);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(true);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertExistenceProofIsNotPassError(VerifyReceiptAndMerkleProofResult result,
      ExistenceProof existenceProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(existenceProof.getReceipt()
                                                                   .getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(existenceProof.getReceipt()
                                                               .getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.OK);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                         .getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                     .getTimestampSPO());
    assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo(StatusConstantsString.OK);
    assertThat(result.getCmd()).isEqualTo(existenceProof.getReceipt()
                                                        .getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(true);
    assertThat(result.isClearanceOrderOk()).isEqualTo(true);
    assertThat(result.isPbPairOk()).isEqualTo(true);
    assertThat(result.isSliceOk()).isEqualTo(true);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(true);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertExistenceProofIsReceiptSignatureError(VerifyReceiptAndMerkleProofResult result,
      ExistenceProof existenceProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(existenceProof.getReceipt()
                                                                   .getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(existenceProof.getReceipt()
                                                               .getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                         .getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                     .getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(existenceProof.getReceipt()
                                                        .getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(false);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(false);
    assertThat(result.isClearanceOrderOk()).isEqualTo(false);
    assertThat(result.isPbPairOk()).isEqualTo(false);
    assertThat(result.isSliceOk()).isEqualTo(false);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertExistenceProofIsMerkleProofSignatureError(VerifyReceiptAndMerkleProofResult result,
      ExistenceProof existenceProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(existenceProof.getReceipt()
                                                                   .getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(existenceProof.getReceipt()
                                                               .getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                         .getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                     .getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(existenceProof.getReceipt()
                                                        .getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(false);
    assertThat(result.isClearanceOrderOk()).isEqualTo(false);
    assertThat(result.isPbPairOk()).isEqualTo(false);
    assertThat(result.isSliceOk()).isEqualTo(false);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertExistenceProofIsClearanceOrderError(VerifyReceiptAndMerkleProofResult result,
      ExistenceProof existenceProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(existenceProof.getReceipt()
                                                                   .getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(existenceProof.getReceipt()
                                                               .getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                         .getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                     .getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(existenceProof.getReceipt()
                                                        .getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(true);
    assertThat(result.isClearanceOrderOk()).isEqualTo(false);
    assertThat(result.isPbPairOk()).isEqualTo(false);
    assertThat(result.isSliceOk()).isEqualTo(false);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
  public static void assertExistenceProofIsClearanceRecordRootHashError(VerifyReceiptAndMerkleProofResult result,
      ExistenceProof existenceProof, ClearanceRecord clearanceRecord) {
    assertThat(result).isNotNull();
    
    assertThat(result.getClearanceOrder()).isEqualTo(existenceProof.getReceipt()
                                                                   .getClearanceOrder());
    assertThat(result.getIndexValue()).isEqualTo(existenceProof.getReceipt()
                                                               .getIndexValue());
    assertThat(result.getExistenceType()).isEqualTo(ExistenceType.EXIST);
    assertThat(result.isPass()).isEqualTo(false);
    assertThat(result.getStatus()).isEqualTo(StatusConstantsString.ERROR);
    assertThat(result.getVerifyStatus()).isEqualTo(VerifyStatus.MODIFIED);
    assertThat(result.getTxHash()).isEqualTo(clearanceRecord.getTxHash());
    assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    assertThat(result.getLedgerInputTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                         .getTimestamp());
    assertThat(result.getReceiptTimestamp()).isEqualTo(existenceProof.getReceipt()
                                                                     .getTimestampSPO());
    // FIXME null / clearanceRecord.getRootHash()
    // assertThat(result.getRootHash()).isEqualTo(clearanceRecord.getRootHash());
    assertThat(result.getDescription()).isEqualTo("verify fail");
    assertThat(result.getCmd()).isEqualTo(existenceProof.getReceipt()
                                                        .getCmd());
    assertThat(result.isReceiptSignatureOk()).isEqualTo(true);
    assertThat(result.isMerkleproofSignatureOk()).isEqualTo(true);
    assertThat(result.isClearanceOrderOk()).isEqualTo(true);
    assertThat(result.isPbPairOk()).isEqualTo(true);
    assertThat(result.isSliceOk()).isEqualTo(true);
    assertThat(result.isClearanceRecordRootHashOk()).isEqualTo(false);
    assertThat(result.getVerifyNotExistProofResult()).isEqualTo(null);
  }
  
}
