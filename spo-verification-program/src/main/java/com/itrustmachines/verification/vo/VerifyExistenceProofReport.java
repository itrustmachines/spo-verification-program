package com.itrustmachines.verification.vo;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = { "verifyMerkleProofReportList" })
public class VerifyExistenceProofReport {
  
  private String verifyCODescription;
  
  private List<VerifyMerkleProofReport> verifyMerkleProofReportList;
  
  public void addVerifyMerkleProofReport(VerifyMerkleProofReport verifyMerkleProofReport) {
    this.verifyMerkleProofReportList.add(verifyMerkleProofReport);
  }
}
