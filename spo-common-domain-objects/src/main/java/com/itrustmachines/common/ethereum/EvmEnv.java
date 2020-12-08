package com.itrustmachines.common.ethereum;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;

import com.itrustmachines.common.util.OkHttpClientUtil;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Slf4j
public class EvmEnv {
  
  private String nodeUrl;
  private Web3j web3j;
  private ContractGasProvider contractGasProvider;
  
  public static EvmEnv getInstance(final String nodeUrl, final double gasPriceMultiply) {
    return getInstance(nodeUrl, gasPriceMultiply, false, "", "");
  }
  
  public static EvmEnv getInstance(final String nodeUrl, final double gasPriceMultiply, final boolean isNeedAuth,
      final String userName, final String password) {
    log.debug("getInstance() nodeUrl={}, gasPriceMultiply={}, isNeedAuth={}, userName={}, password={}", nodeUrl,
        gasPriceMultiply, isNeedAuth, userName, password);
    final Web3j web3j = getWeb3j(nodeUrl, isNeedAuth, userName, password);
    
    final EvmEnv evmEnv = EvmEnv.builder()
                                .nodeUrl(nodeUrl)
                                .web3j(web3j)
                                .build();
    
    // create gasProvider and set to evmEnv
    final DynamicGasProvider gasProvider = new DynamicGasProvider(evmEnv, gasPriceMultiply);
    evmEnv.setContractGasProvider(gasProvider);
    
    log.info("create EvmEnv[{}]={}", nodeUrl, evmEnv);
    return evmEnv;
  }
  
  static Web3j getWeb3j(String nodeUrl, boolean isNeedAuth, String userName, String password) {
    final Web3j web3j;
    if (isNeedAuth) {
      web3j = Web3j.build(new HttpService(nodeUrl, OkHttpClientUtil.getOkHttpClient(userName, password), false));
    } else {
      web3j = Web3j.build(new HttpService(nodeUrl, OkHttpClientUtil.getOkHttpClient()));
    }
    return web3j;
  }
  
  public static EvmEnv getInstance(final String nodeUrl, final ContractGasProvider contractGasProvider) {
    return getInstance(nodeUrl, false, "", "", contractGasProvider);
  }
  
  public static EvmEnv getInstance(final String nodeUrl, final boolean isNeedAuth, final String userName,
      final String password, final ContractGasProvider contractGasProvider) {
    log.debug("getInstance() nodeUrl={}, isNeedAuth={}, userName={}, password={}, contractGasProvider={}", nodeUrl,
        isNeedAuth, userName, password, contractGasProvider);
    final Web3j web3j = getWeb3j(nodeUrl, isNeedAuth, userName, password);
    
    final EvmEnv evmEnv = EvmEnv.builder()
                                .nodeUrl(nodeUrl)
                                .web3j(web3j)
                                .contractGasProvider(contractGasProvider)
                                .build();
    
    log.info("create EvmEnv[{}]={}", nodeUrl, evmEnv);
    return evmEnv;
  }
  
}
