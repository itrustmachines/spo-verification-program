package com.itrustmachines.common.ethereum.util;

import java.util.Objects;

import com.itrustmachines.common.ethereum.EthereumEnv;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class EthereumEnvUtil {
  
  private static final String AZURE_BLOCKCHAIN_URL_CONTAINS = "blockchain.azure.com";
  
  public EthereumEnv getEthereumEnv(@NonNull final String nodeUrl) {
    if (nodeUrl.contains(AZURE_BLOCKCHAIN_URL_CONTAINS)) {
      return EthereumEnv.AZURE_QUORUM;
    }
    
    for (EthereumEnv env : EthereumEnv.values()) {
      final String envName = env.name()
                                .toLowerCase();
      if (nodeUrl.toLowerCase()
                 .contains(envName)) {
        return env;
      }
    }
    
    if (isURL(nodeUrl)) {
      return EthereumEnv.PRIVATE_GETH;
    } else {
      return EthereumEnv.ERROR_ENV;
    }
  }
  
  boolean isURL(String url) {
    try {
      (new java.net.URL(url)).openStream()
                             .close();
      return true;
    } catch (Exception ex) {
      log.error("not url: {}", url);
    }
    return false;
  }
  
  public String getInfuraProjectId(@NonNull final String nodeUrl) {
    String result = null;
    if (Objects.nonNull(nodeUrl) && nodeUrl.contains("infura.io/v3/")) {
      result = nodeUrl.substring(nodeUrl.lastIndexOf("/") + 1);
    }
    return result;
  }
  
}
