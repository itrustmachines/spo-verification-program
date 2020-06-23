package com.itrustmachines.verification.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyVerificationProofResult {
  
  private String query;
  
  private boolean pass;
  
  private boolean clearanceRecordPass;
  
  private List<VerifyReceiptAndMerkleProofResult> verifyReceiptResults;
  
  private List<VerifyNotExistProofResult> verifyNotExistProofResults;
  
  // TODO delete
  private List<String> rawDataList;
  
  // TODO verify merkleProofVerifyStatus and data for display
  
}
