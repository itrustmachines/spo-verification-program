package com.itrustmachines.common.ethereum.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.itrustmachines.common.vo.ClearanceRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientContractServiceTest {
  
  @Test
  public void test_obtainContractClearanceOrder() {
    final String contractAddress = "0x1Bbe2D131a42DaEd0110fd2bE08AF56906A5a1Ce";
    final String privateKey = "b8059c31844941a8b37d4cac37b331d7b8059c31844941a8b37d4cac37b331d7";
    final String nodeUrl = "https://rinkeby.infura.io/v3/c889a8d21e2b4179ab331713efb92a7d";
    
    final ClientContractService service = new ClientContractService(contractAddress, privateKey, nodeUrl);
    final long contractCO = service.obtainContractClearanceOrder();
    log.info("contractCO={}", contractCO);
    assertThat(contractCO).isGreaterThan(1);
    
    final ClearanceRecord cr_notExist = service.obtainClearanceRecord(contractCO);
    log.info("cr_notExist={}", cr_notExist);
    assertThat(cr_notExist).isNull();
    
    final ClearanceRecord cr = service.obtainClearanceRecord(contractCO - 1);
    log.info("cr={}", cr);
    assertThat(cr).isNotNull();
    assertThat(cr.getClearanceOrder()).isEqualTo(contractCO - 1);
  }
  
}