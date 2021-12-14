package com.itrustmachines.verification.vo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = { "verifyPbPairsReport", "verifySliceReport" })
public class VerifyMerkleProofReport {
  
  private String descriptionReport;
  
  private String verifyPbPairsReport;
  
  private String verifySliceReport;
}
