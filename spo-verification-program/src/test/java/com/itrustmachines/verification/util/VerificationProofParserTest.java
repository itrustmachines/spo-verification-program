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
    String filePath = "src/test/resources/verificationProof/SPO_DEVICE_CLIENT_RX_2020-04-29.json";
    final Path path = Paths.get(filePath);
    assertThat(path.toFile()).canRead();
    
    return VerificationProofParser.parse(filePath);
  }
  
  @Test
  public void test_parse() {
    final VerificationProof verificationProof = getTestVerificationProof();
    assertThat(verificationProof).isNotNull();
    assertThat(verificationProof.getContractAddress()).isEqualTo("0x1Bbe2D131a42DaEd0110fd2bE08AF56906A5a1Ce");
  }
  
}