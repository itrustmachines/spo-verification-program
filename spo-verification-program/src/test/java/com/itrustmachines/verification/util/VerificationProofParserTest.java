package com.itrustmachines.verification.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.itrustmachines.verification.vo.VerificationProof;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerificationProofParserTest {
  
  public static VerificationProof getTestVerificationProof() {
    String filePath = "src/test/resources/verificationProof/SolarPanel_R107_30545.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();
    
    return VerificationProofParser.parse(filePath);
  }
  
  public static VerificationProof getTestAzureQuorumVerificationProof() {
    String filePath = "src/test/resources/verificationProof/Swanky-VirtualSpoClient_R62_58.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();
    
    return VerificationProofParser.parse(filePath);
  }
  
  public static VerificationProof getTestNotExistVerificationProof() {
    String filePath = "src/test/resources/verificationProof/response_1604653374814.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();
    
    return VerificationProofParser.parse(filePath);
  }
  
  public static VerificationProof getFailAndRedundantVerificationProof() {
    String filePath = "src/test/resources/verificationProof/failAndRedundantProof.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();
    
    return VerificationProofParser.parse(filePath);
  }

  public static VerificationProof getRemovedAndAddBigSn() {
    String filePath = "src/test/resources/verificationProof/removedAndAddBigSn.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();

    return VerificationProofParser.parse(filePath);
  }

  public static VerificationProof getOnlyExistenceVerificationProof(){
    String filePath = "src/test/resources/verificationProof/onlyExistenceProof.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();

    return VerificationProofParser.parse(filePath);
  }

  public static VerificationProof getQueryByCo() {
    String filePath = "src/test/resources/verificationProof/queryByCO.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();

    return VerificationProofParser.parse(filePath);
  }

  public static VerificationProof getAzureRCProof() {
    String filePath = "src/test/resources/verificationProof/azure-rc-VirtualSpoClient_R1_31185.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();

    return VerificationProofParser.parse(filePath);
  }

  public static VerificationProof getAzureDev2Proof() {
    String filePath = "src/test/resources/verificationProof/azure-dev2-SolarPanel_R115_37239.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();

    return VerificationProofParser.parse(filePath);
  }

  @Test
  public void test_parse() {
    final VerificationProof verificationProof = getTestVerificationProof();
    assertThat(verificationProof).isNotNull();
    assertThat(verificationProof.getContractAddress()).isEqualTo("0xd91ad5999CF874453d3dD891b2d8b45580F9ac6d");
  }
  
}