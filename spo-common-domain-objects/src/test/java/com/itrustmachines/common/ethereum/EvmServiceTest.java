package com.itrustmachines.common.ethereum;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Disabled
@Slf4j
class EvmServiceTest {
  
  @SneakyThrows
  @Test
  public void test_getTransactionFeeInEther() {
    final String nodeUrl = "https://rinkeby.infura.io/v3/c889a8d21e2b4179ab331713efb92a7d";
    EvmService service = EvmService.getInstance(EvmEnv.getInstance(nodeUrl, 1.0));
    
    String txHash = "0xe7182e8f568439221b550b67ec848cf62152956bdd127a7d15e632162cef7a23";
    
    final Optional<BigDecimal> _transactionFeeInEther = service.getTransactionFeeInEther(txHash);
    log.info("_transactionFeeInEther={}", _transactionFeeInEther);
    assertThat(_transactionFeeInEther).isPresent();
  }
  
}