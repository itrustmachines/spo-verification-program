package com.itrustmachines.verification;

import com.itrustmachines.common.config.EthereumNodeConfig;
import com.itrustmachines.common.util.ClearanceRecordServiceBuilder;
import com.itrustmachines.verification.service.VerifyVerificationProofService;
import com.itrustmachines.verification.util.VerificationProofParser;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyVerificationProofResult;

import lombok.NonNull;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Slf4j
public class VerificationApi {
  
  private final String contractAddress;
  private final String serverWalletAddress;
  private final EthereumNodeConfig config;
  private final VerifyVerificationProofService service;
  
  private VerificationApi(final @NonNull String contractAddress, final @NonNull String serverWalletAddress,
      final @NonNull EthereumNodeConfig config) {
    this.contractAddress = contractAddress;
    this.serverWalletAddress = serverWalletAddress;
    this.config = config;
    this.service = new VerifyVerificationProofService(serverWalletAddress,
        ClearanceRecordServiceBuilder.build(contractAddress, config));
    log.info("new instance: {}", this);
  }
  
  private static VerificationApi instance;
  
  @Synchronized
  public static VerificationApi getInstance(final @NonNull String contractAddress,
      final @NonNull String serverWalletAddress, final @NonNull EthereumNodeConfig config) {
    if (VerificationApi.instance == null) {
      VerificationApi.instance = new VerificationApi(contractAddress, serverWalletAddress, config);
    }
    return VerificationApi.instance;
  }
  
  public VerifyVerificationProofResult verify(final @NonNull String filePath) {
    final VerificationProof verificationProof = VerificationProofParser.parse(filePath);
    return verify(verificationProof);
  }
  
  public VerifyVerificationProofResult verifyJsonString(final @NonNull String jsonString) {
    final VerificationProof verificationProof = VerificationProofParser.parseJsonString(jsonString);
    return verify(verificationProof);
  }
  
  public VerifyVerificationProofResult verify(final VerificationProof proof) {
    log.debug("verify() proof={}", proof);
    VerifyVerificationProofResult result = null;
    if (proof != null) {
      result = service.verify(proof);
    }
    return result;
  }
  
}