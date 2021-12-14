package com.itrustmachines.verification.util;

import java.util.List;
import java.util.Objects;

import com.itrustmachines.common.util.HashUtils;
import com.itrustmachines.common.vo.ClearanceRecord;
import com.itrustmachines.common.vo.SpoSignature;
import com.itrustmachines.verification.constants.VerifyVerificationProofStatus;
import com.itrustmachines.verification.service.VerifiedClearanceRecordInfo;
import com.itrustmachines.verification.vo.ExistenceProof;
import com.itrustmachines.verification.vo.VerificationProof;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class VerifyReportUtil {
  
  public String buildProofSignaturePassReport(@NonNull final String serverWalletAddress,
      @NonNull final SpoSignature sigServer, @NonNull final String signData,
      @NonNull final boolean proofSignaturePass) {
    log.debug(
        "buildProofSignaturePassReport() start, serverWalletAddress={}, sigServer={}, signData={}, proofSignaturePass={}",
        serverWalletAddress, sigServer, signData, proofSignaturePass);
    final String report = String.format(
        "[Step 1] verify signature of  SPO (sigServer), \n\n\tserverWalletAddress=%s, \n\tSPOSignature R=%s, \n\tS=%s, \n\tV=%s, \n\tsigned data=%s, \n\tpass=%b\n",
        serverWalletAddress, sigServer.getR(), sigServer.getS(), sigServer.getV(), signData, proofSignaturePass);
    log.debug("buildProofSignaturePassReport() end, reportSize={}", report.length());
    return report;
  }
  
  public String buildVerifyLastClearanceRecordReport(@NonNull final ClearanceRecord lastRecord,
      @NonNull final boolean verifyResult) {
    log.debug("buildVerifyLastClearanceRecordReport() start, lastRecord={}, verifyResult={}", lastRecord, verifyResult);
    final String report = String.format("[Step 2-1] verify last clearance record, \n\n")
        + String.format("\tTo find the transaction in Blockchain, transaction hash=%s\n\n", lastRecord.getTxHash())
        + String.format("\tclearanceOrder=%d\n", lastRecord.getClearanceOrder())
        + String.format("\trootHash=%s\n", lastRecord.getRootHash())
        + String.format("\tchainHash=%s\n", lastRecord.getChainHash())
        + String.format("\tdescription=%s\n", lastRecord.getDescription())
        + String.format("\tpass=%b\n\n", verifyResult);
    log.debug("buildVerifyLastClearanceRecordReport() end, reportSize={}", report.length());
    return report;
  }
  
  public String buildVerifyClearanceRecordReport(@NonNull final ClearanceRecord currentRecord,
      @NonNull final byte[] concatByteArray, @NonNull final byte[] calculateChainHash,
      @NonNull final boolean verifyResult) {
    log.debug(
        "buildVerifyClearanceRecordReport() start, currentRecord={}, concatByteArray={}, calculateChainHash={}, verifyResult={}",
        currentRecord, concatByteArray, calculateChainHash, verifyResult);
    
    final String report = "[Step 2-2] verify clearance record, \n\n"
        + String.format("\tclearanceOrder=%d\n", currentRecord.getClearanceOrder())
        + String.format("\trootHash=%s\n", currentRecord.getRootHash())
        + String.format("\tchainHash=%s\n", currentRecord.getChainHash())
        + String.format("\tdescription=%s\n", currentRecord.getDescription())
        + String.format("\tconcat previous clearance record result=%s\n", HashUtils.byte2HEX(concatByteArray))
        + String.format("\tcalculate concat clearance record with sha3, result=%s\n",
            HashUtils.byte2HEX(calculateChainHash))
        + String.format("\tpass=%b\n", verifyResult);
    log.debug("buildVerifyClearanceRecordReport() end, reportSize={}", report.length());
    return report;
  }
  
  public String buildVerifyExistenceSingleCODescriptionReport(
      final VerifiedClearanceRecordInfo verifiedClearanceRecordInfo,
      @NonNull final List<ExistenceProof> existenceProofList) {
    log.debug(
        "buildVerifyExistenceSingleCODescriptionReport() start, verifiedClearanceRecordInfo={}, existenceProofListSize={}",
        verifiedClearanceRecordInfo, existenceProofList.size());
    final StringBuilder reportBuilder = new StringBuilder();
    if (Objects.nonNull(verifiedClearanceRecordInfo)) {
      reportBuilder.append(
          String.format("[Step 3-1] Verify single clearance order of merkle proof in proof file, clearance order=%d\n",
              verifiedClearanceRecordInfo.getClearanceRecord()
                                         .getClearanceOrder()));
    } else {
      reportBuilder.append(
          "[Step 3-1] There's no corresponding verified clearance record info, \nverification proof may be edit!\n");
    }
    reportBuilder.append(
        String.format("[Step 3-2] There are %d existence proof/not existence proof in this clearance order\n",
            existenceProofList.size()));
    final String report = reportBuilder.toString();
    log.debug("buildVerifyExistenceSingleCODescriptionReport() end, reportSize={}", report.length());
    return report;
  }
  
  public String buildGeneralReport(@NonNull final String fileName, @NonNull final VerificationProof proof,
      @NonNull final VerifyVerificationProofStatus verificationProofStatus) {
    log.debug("buildGeneralReport() start, fileName={}, proof={}, verificationProofStatus={}", fileName, proof,
        verificationProofStatus);
    final String reportString = String.format("General report of %s\n", fileName)
        + String.format("Blockchain: %s\n", proof.getEnv()
                                                 .name())
        + String.format("# of clearance orders = %s\n", proof.getClearanceRecords()
                                                             .size())
        + String.format("# of Merkle proof = %s\n", proof.getExistenceProofs()
                                                         .size())
        + String.format("Pass = %b\n", VerifyVerificationProofStatus.ALL_PASS.equals(verificationProofStatus));
    log.debug("buildGeneralReport() end, reportStringSize={}", reportString.length());
    return reportString;
  }
}
