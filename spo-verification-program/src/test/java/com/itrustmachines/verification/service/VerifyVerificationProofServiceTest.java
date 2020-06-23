package com.itrustmachines.verification.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.itrustmachines.common.contract.ClearanceRecordService;
import com.itrustmachines.verification.util.VerificationProofParser;
import com.itrustmachines.verification.util.VerificationProofParserTest;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyVerificationProofResult;

import lombok.extern.slf4j.Slf4j;

@Disabled
@Slf4j
public class VerifyVerificationProofServiceTest {
  
  private final static String SWANKY_INFURA_ENDPOINT = "https://rinkeby.infura.io/v3/{INFURA_ID}";
  
  @Test
  public void test_verify_1() {
    final VerificationProof proof = VerificationProofParserTest.getTestVerificationProof();
    final String privateKey = "<privateKey>";
    final String nodeUrl = SWANKY_INFURA_ENDPOINT;
    final ClearanceRecordService clearanceRecordService = new ClearanceRecordService(proof.getContractAddress(),
        privateKey, nodeUrl);
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService(proof.getServerWalletAddress(),
        clearanceRecordService);
    
    final VerifyVerificationProofResult result = service.verify(proof);
    log.info("result={}", result);
    assertThat(result).isNotNull();
    assertThat(result.isPass()).isTrue();
  }
  
  @Test
  public void test_verify_2() {
    String filePath = "src/test/resources/verificationProof/VerificationProof_T01.json";
    final VerificationProof proof = VerificationProofParser.parse(filePath);
    
    final String privateKey = "<privateKey>";
    final String nodeUrl = SWANKY_INFURA_ENDPOINT;
    final ClearanceRecordService clearanceRecordService = new ClearanceRecordService(proof.getContractAddress(),
        privateKey, nodeUrl);
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService(proof.getServerWalletAddress(),
        clearanceRecordService);
    
    final VerifyVerificationProofResult result = service.verify(proof);
    log.info("result={}", result);
    assertThat(result).isNotNull();
    assertThat(result.getQuery()).isEqualTo("Locators=(619:ToolServerTest2_R1)");
    assertThat(result.isPass()).isTrue();
  }
  
}