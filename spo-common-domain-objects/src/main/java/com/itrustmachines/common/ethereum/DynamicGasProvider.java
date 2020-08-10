package com.itrustmachines.common.ethereum;

import java.math.BigInteger;

import org.web3j.tx.gas.DefaultGasProvider;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString(exclude = "evmService")
@Slf4j
public class DynamicGasProvider extends DefaultGasProvider {
  
  public static final BigInteger GAS_LIMIT = BigInteger.valueOf(800_0000L);
  
  final private EvmService evmService;

  private double multiplyValue;
  
  public DynamicGasProvider(final EvmEnv evmEnv, final double multiply) {
    this.evmService = EvmService.getInstance(evmEnv);
    this.multiplyValue = multiply;
    log.debug("new instance={}", this);
  }
  
  @Override
  public BigInteger getGasPrice(final String contractFunc) {
    final BigInteger gasPrice = evmService.getGasPrice(multiplyValue);
    log.debug("getGasPrice() [{}]={}", contractFunc, gasPrice);
    return gasPrice;
  }
  
  @Override
  public BigInteger getGasLimit(final String contractFunc) {
    log.debug("getGasLimit() [{}]={}", contractFunc, GAS_LIMIT);
    return GAS_LIMIT;
  }
  
}
