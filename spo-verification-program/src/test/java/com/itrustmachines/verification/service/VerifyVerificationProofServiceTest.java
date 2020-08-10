package com.itrustmachines.verification.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.verification.util.VerificationProofParser;
import com.itrustmachines.verification.util.VerificationProofParserTest;
import com.itrustmachines.verification.vo.VerificationProof;
import com.itrustmachines.verification.vo.VerifyVerificationProofResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyVerificationProofServiceTest {
  
  private final static String SWANKY_INFURA_ENDPOINT = "https://rinkeby.infura.io/v3/bcd2ba30ecc442168521ce75db01a120";
  
  @Test
  public void test_verify_1() {
    final VerificationProof proof = VerificationProofParserTest.getTestVerificationProof();
    final String privateKey = "b8059c31844941a8b37d4cac37b331d7b8059c31344941a8b37d4cac37b331d7";
    final String nodeUrl = SWANKY_INFURA_ENDPOINT;
    final ClientContractService clearanceRecordService = new ClientContractService(proof.getContractAddress(),
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
    
    final String privateKey = "b8059c31844941a8b37d4cac37b331d7b8059c31344941a8b37d4cac37b331d7";
    final String nodeUrl = SWANKY_INFURA_ENDPOINT;
    final ClientContractService clearanceRecordService = new ClientContractService(proof.getContractAddress(),
        privateKey, nodeUrl);
    
    final VerifyVerificationProofService service = new VerifyVerificationProofService(proof.getServerWalletAddress(),
        clearanceRecordService);
    
    final VerifyVerificationProofResult result = service.verify(proof);
    log.info("result={}", result);
    assertThat(result).isNotNull();
    assertThat(result.getQuery()).isEqualTo("Locators=(22599:SolarPanel_R129)");
    assertThat(result.isPass()).isTrue();
  }
  
}