package com.itrustmachines.verification.util;

import java.util.ArrayList;
import java.util.List;

import com.itrustmachines.common.constants.StatusConstantsString;
import com.itrustmachines.verification.constants.*;
import com.itrustmachines.verification.vo.Query;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;
import com.itrustmachines.verification.vo.VerifyVerificationProofResult;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class VerificationUtil {
  
  public boolean isVerificationContentNotNull(final VerificationProof proof) {
    log.debug("isVerificationContentNull() proof={}", proof);
    boolean result = true;
    if (proof == null) {
      result = false;
    } else if (proof.getEnv() == null || proof.getClearanceRecords() == null || proof.getContractAddress() == null
        || proof.getServerWalletAddress() == null || proof.getExistenceProofs() == null) {
      result = false;
    }
    log.debug("isVerificationContentNotNull() result={}", result);
    return result;
  }
  
  public VerifyVerificationProofResult buildExceptionResult(@NonNull final String exceptionMessage) {
    log.debug("buildExceptionResult() start, exceptionMessage={}", exceptionMessage);
    VerifyVerificationProofStatus status = VerifyVerificationProofStatus.SIG_ERROR;
    if (exceptionMessage.contains("verify() error")) {
      status = VerifyVerificationProofStatus.CONTRACT_CONNECTION_ERROR;
    }
    
    final VerifyVerificationProofResult result = VerifyVerificationProofResult.builder()
                                                                              .errorClearanceOrderInClearanceRecordList(
                                                                                  new ArrayList<>())
                                                                              .queryType(Query.QueryType.ERROR)
                                                                              .totalCount(0L)
                                                                              .successCount(0L)
                                                                              .modifiedCount(0L)
                                                                              .removedCount(0L)
                                                                              .addedCount(0L)
                                                                              .verifyReceiptResults(
                                                                                  buildErrorVerifyReceiptAndMerkleProofResult())
                                                                              .query("")
                                                                              .status(status)
                                                                              .build();
    log.debug("buildExceptionResult() end, result={}", result);
    return result;
  }
  
  private List<VerifyReceiptAndMerkleProofResult> buildErrorVerifyReceiptAndMerkleProofResult() {
    List<VerifyReceiptAndMerkleProofResult> verifyReceiptAndMerkleProofResultList = new ArrayList<>();
    verifyReceiptAndMerkleProofResultList.add(VerifyReceiptAndMerkleProofResult.builder()
                                                                               .indexValue("")
                                                                               .clearanceOrder(-1L)
                                                                               .cmd("")
                                                                               .merkleProofRootHash("")
                                                                               .contractRootHash("")
                                                                               .timestamp(-1L)
                                                                               .description("")
                                                                               .sliceOk(false)
                                                                               .pbPairOk(false)
                                                                               .receiptSignatureOk(false)
                                                                               .clearanceRecordRootHashOk(false)
                                                                               .clearanceOrderOk(false)
                                                                               .merkleproofSignatureOk(false)
                                                                               .ledgerInputTimestamp(-1L)
                                                                               .receiptTimestamp(-1L)
                                                                               .verifyNotExistProofResult(null)
                                                                               .pass(false)
                                                                               .proofExistStatus(
                                                                                   ProofExistStatus.CLEARANCE_RECORD_ERROR)
                                                                               .existenceType(ExistenceType.NA)
                                                                               .status(StatusConstantsString.ERROR)
                                                                               .txHash("")
                                                                               .build());
    return verifyReceiptAndMerkleProofResultList;
  }
  
  public VerifyResult getVerifyResult(@NonNull final VerifyVerificationProofStatus status) {
    log.debug("getVerifyResult() start, status={}", status);
    final VerifyResult verifyResult = status.equals(VerifyVerificationProofStatus.ALL_PASS) ? VerifyResult.PASS
        : VerifyResult.FAIL;
    log.debug("getVerifyResult() end, verifyResult={}", verifyResult);
    return verifyResult;
  }
  
  public String getVerifyResultDescription(@NonNull final VerifyVerificationProofStatus status) {
    log.debug("getVerifyResultDescription() start, status={}", status);
    String description = null;
    switch (status) {
      case ALL_PASS:
        description = VerifyResultDescription.VERIFY_OK;
        break;
      case SIG_ERROR:
        description = VerifyResultDescription.VERIFICATION_PROOF_SIGNATURE_ERROR;
        break;
      case CONTRACT_CONNECTION_ERROR:
        description = VerifyResultDescription.CONTRACT_CONNECTION_ERROR;
        break;
      default:
        description = VerifyResultDescription.VERIFY_VERIFICATION_PROOF_ERROR;
    }
    log.debug("getVerifyResultDescription() end, description={}", description);
    return description;
  }
}
