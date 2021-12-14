package com.itrustmachines.verification.vo;

import java.util.List;

import com.itrustmachines.verification.constants.VerifyReportType;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = { "verifyProofSigReport", "verifyLastClearanceRecordReport", "verifyClearanceRecordReportList",
    "verifyExistenceProofList" })
public class VerifyReport {
  
  private VerifyReportType verifyReportType;
  
  private String generalReport;
  
  private String verifyExceptionReport;
  
  private String verifyProofSigReport;
  
  private String verifyLastClearanceRecordReport;
  
  private List<String> verifyClearanceRecordReportList;
  
  private List<VerifyExistenceProofReport> verifyExistenceProofList;
  
  public void addVerifyExistenceProofList(@NonNull final VerifyExistenceProofReport verifyExistenceProofReport) {
    this.verifyExistenceProofList.add(verifyExistenceProofReport);
  }
}