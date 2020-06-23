package com.itrustmachines.verification.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyReceiptAndMerkleProofResult {
  
  private Long clearanceOrder;
  private String indexValue;
  private boolean pass;
  private String status;
  private Long timestamp;
  
  /**
   * verify merkleProof signature
   */
  private boolean merkleproofSignatureOk;
  
  /**
   * verify receipt signature
   */
  private boolean receiptSignatureOk;
  
  /**
   * check Receipt txhash equal to MerkleProof acktxHash
   */
  private boolean clearanceOrderOk;
  
  /**
   * check txHash contain pbpair and pbpair equal to slice
   */
  private boolean pbPairOk;
  
  /**
   * check slice equal to TpmTree rootHash
   */
  private boolean sliceOk;
  
  /**
   * check clearanceRecord RootHash equal to TpmTree RootHash
   */
  private boolean clearanceRecordRootHashOk;
  
}
