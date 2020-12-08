package com.itrustmachines.verification.vo;

import java.util.List;
import java.util.stream.Collectors;

import com.itrustmachines.verification.constants.ExistenceType;

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
  
  private Query.QueryType queryType;
  
  private boolean pass;
  
  private boolean clearanceRecordPass;
  
  private List<VerifyReceiptAndMerkleProofResult> verifyReceiptResults;
  
  private Long proofCount;
  private Long successCount;
  private Long modifiedCount;
  private Long removedCount;
  private Long addedCount;
  
  // TODO verify merkleProofVerifyStatus and data for display
  
  public List<VerifyReceiptAndMerkleProofResult> getExistProofResult() {
    return this.verifyReceiptResults.stream()
                                    .filter(result -> ExistenceType.EXIST.equals(result.getExistenceType()))
                                    .collect(Collectors.toList());
  }
  
  public List<VerifyReceiptAndMerkleProofResult> getNotExistProofResult() {
    return this.verifyReceiptResults.stream()
                                    .filter(result -> ExistenceType.NOT_EXIST.equals(result.getExistenceType()))
                                    .collect(Collectors.toList());
  }
  
}
