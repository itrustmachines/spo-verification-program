package com.itrustmachines.common.ethereum.embedded_evm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.itrustmachines.common.ethereum.EvmEnv;
import com.itrustmachines.common.ethereum.embedded_evm.util.EmbeddedEvmEnvUtil;
import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.util.KeyGeneratorUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmbeddedEvmEmvWithContractTest {
  
  @Test
  public void test() throws Exception {
    final KeyGeneratorUtil.KeyInfo spoServerKeyInfo = EmbeddedEvmEnvUtil.spoServerKeyInfo;
    final EvmEnv embeddedEvmEnv = EmbeddedEvmEnvUtil.evmEnv();
    final String contractAddress = EmbeddedEvmEnvUtil.deployContract(embeddedEvmEnv);
    
    log.info("contractAddress={}", contractAddress);
    assertThat(contractAddress).isNotBlank();
    
    final ClientContractService clientContractService = new ClientContractService(contractAddress,
        spoServerKeyInfo.getPrivateKey(), embeddedEvmEnv, 0);
    
    final long clearanceOrder = clientContractService.obtainContractClearanceOrder();
    
    log.info("clearanceOrder={}", clearanceOrder);
    assertThat(clearanceOrder).isEqualTo(1);
    
    final String version = clientContractService.getContractVersion();
    log.info("version={}", version);
    assertThat(version).isEqualTo("2.1.0.RELEASE");
  }
  
}
