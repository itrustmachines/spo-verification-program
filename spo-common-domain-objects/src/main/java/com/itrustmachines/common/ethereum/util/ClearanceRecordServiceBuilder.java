package com.itrustmachines.common.ethereum.util;

import com.itrustmachines.common.ethereum.config.EthereumNodeConfig;
import com.itrustmachines.common.ethereum.service.ClientContractService;

import lombok.NonNull;

public class ClearanceRecordServiceBuilder {
  
  private static final double DEFAULT_GAS_PRICE_MULTIPLY = 1.0;
  
  public static ClientContractService build(@NonNull final String contractAddress,
      @NonNull final EthereumNodeConfig config) {
    final ClientContractService result;
    final EthereumNodeConfig.Authentication authentication = config.getAuthentication();
    
    final double gasPriceMultiply = config.getGasPriceMultiply() != null ? config.getGasPriceMultiply()
        : DEFAULT_GAS_PRICE_MULTIPLY;
    if ((authentication != null) && authentication.getNeedAuth()) {
      result = new ClientContractService(contractAddress, config.getPrivateKeyEnv(),
          config.getNodeUrl() + config.getInfuraProjectIdEnv(), gasPriceMultiply, authentication.getNeedAuth(),
          authentication.getUsername(), authentication.getPassword(), 5);
    } else {
      result = new ClientContractService(contractAddress, config.getPrivateKeyEnv(),
          config.getNodeUrl() + config.getInfuraProjectIdEnv(), gasPriceMultiply, 5);
    }
    return result;
  }
  
}
