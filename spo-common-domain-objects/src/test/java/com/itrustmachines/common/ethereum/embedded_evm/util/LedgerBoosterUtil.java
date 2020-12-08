package com.itrustmachines.common.ethereum.embedded_evm.util;

import org.web3j.crypto.Credentials;

import com.itrustmachines.common.ethereum.EvmEnv;
import com.itrustmachines.common.ethereum.contract.LedgerBooster;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class LedgerBoosterUtil {
  
  public LedgerBooster load(String contractAddress, EvmEnv evmEnv, String privateKey) {
    log.info("load() contractAddress={}, evmEnv={}", contractAddress, evmEnv);
    return LedgerBooster.load(contractAddress, evmEnv.getWeb3j(), Credentials.create(privateKey),
        evmEnv.getContractGasProvider());
  }
  
}
