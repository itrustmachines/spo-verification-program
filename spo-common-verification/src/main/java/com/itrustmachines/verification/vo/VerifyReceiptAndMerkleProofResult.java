package com.itrustmachines.verification.vo;

import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.constants.VerifyNotExistProofStatus;
import com.itrustmachines.verification.constants.VerifyStatus;

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
  private ExistenceType existenceType;
  private boolean pass;
  private String status;
  private VerifyStatus verifyStatus;
  private String txHash;
  
  /**
   * verify timestamp
   */
  private Long timestamp;
  private Long ledgerInputTimestamp;
  private Long receiptTimestamp;
  private String rootHash;
  
  // TODO
  private String description;
  
  private String cmd;
  
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
  
  /**
   * only use for not exist proof verify <br>
   * 3 results: not_exist, not_between_time, error_indexValue_in_Pair </br>
   */
  private VerifyNotExistProofStatus verifyNotExistProofResult;
  
}
