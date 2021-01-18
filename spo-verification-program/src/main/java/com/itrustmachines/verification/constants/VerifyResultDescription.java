package com.itrustmachines.verification.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VerifyResultDescription {
  public final String VERIFY_OK = "Verify success";
  public final String COVERT_FILE_TO_VERIFICATION_PROOF_FAIL = "Convert file to verificationProof fail";
  public final String VERIFICATION_PROOF_CONTENT_NULL_ERROR = "VerificationProof content null error";
  public final String VERIFY_VERIFICATION_PROOF_ERROR = "Verify verificationProof error";
  public final String FILE_NAME_OR_CONTENT_ERROR = "Verify raw data file name or content error";
  public final String VERIFICATION_PROOF_SIGNATURE_ERROR = "VerificationProof signature error";
  public final String CONTRACT_CONNECTION_ERROR = "Contract connection error";
}
