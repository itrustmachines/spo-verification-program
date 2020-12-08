package com.itrustmachines.common.ethereum;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import org.web3j.tx.gas.DefaultGasProvider;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString(exclude = "evmService")
@Slf4j
public class DynamicGasProvider extends DefaultGasProvider {
  
  public static final long MAX_GAS_LIMIT = 800_0000L;
  public static final long WRITE_CR_GAS_LIMIT = 50_0000L;
  
  final private EvmService evmService;
  
  @Setter
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
  
  // key = functionCall, value = gasLimit
  final static private Map<String, Long> gasLimitMap = new LinkedHashMap<>();
  static {
    gasLimitMap.put("deploy", MAX_GAS_LIMIT);
    gasLimitMap.put("objectionReceipt", MAX_GAS_LIMIT);
    gasLimitMap.put("objectionMerkleProof", MAX_GAS_LIMIT);
    gasLimitMap.put("writeClearanceRecord", WRITE_CR_GAS_LIMIT);
    gasLimitMap.put("addMaxTxCount", MAX_GAS_LIMIT);
  }
  
  @Override
  public BigInteger getGasLimit(final String contractFunc) {
    final Long gasLimit = gasLimitMap.getOrDefault(contractFunc, MAX_GAS_LIMIT);
    log.debug("getGasLimit() [{}]={}", contractFunc, gasLimit);
    return BigInteger.valueOf(gasLimit);
  }
  
}
