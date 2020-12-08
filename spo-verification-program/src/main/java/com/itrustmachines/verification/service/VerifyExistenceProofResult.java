package com.itrustmachines.verification.service;

import java.util.List;
import java.util.stream.Collectors;

import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class VerifyExistenceProofResult {
  
  private boolean pass;
  
  private Long successCount;
  private Long modifiedCount;
  private Long removedCount;
  private Long addedCount;
  
  private List<VerifyReceiptAndMerkleProofResult> proofResultList;
  
  public List<VerifyReceiptAndMerkleProofResult> getExistProofResult() {
    return this.proofResultList.stream()
                               .filter(result -> ExistenceType.EXIST.equals(result.getExistenceType()))
                               .collect(Collectors.toList());
  }
  
  public List<VerifyReceiptAndMerkleProofResult> getNotExistProofResult() {
    return this.proofResultList.stream()
                               .filter(result -> ExistenceType.NOT_EXIST.equals(result.getExistenceType()))
                               .collect(Collectors.toList());
  }
  
  public List<VerifyReceiptAndMerkleProofResult> getNAProofResult() {
    return this.proofResultList.stream()
                               .filter(result -> ExistenceType.NA.equals(result.getExistenceType()))
                               .collect(Collectors.toList());
  }
  
}
