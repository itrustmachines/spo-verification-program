package com.itrustmachines.verification.service;

import java.util.List;
import java.util.stream.Collectors;

import com.itrustmachines.verification.constants.ExistenceType;
import com.itrustmachines.verification.vo.VerifyProofCount;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = { "proofResultList" })
class VerifyProofListResult {
  
  private boolean pass;
  
  private VerifyProofCount count;
  
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
