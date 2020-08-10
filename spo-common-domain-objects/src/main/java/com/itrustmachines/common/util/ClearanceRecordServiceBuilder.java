package com.itrustmachines.common.util;

import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.ethereum.config.EthereumNodeConfig;

public class ClearanceRecordServiceBuilder {
  
  public static ClientContractService build(String contractAddress, EthereumNodeConfig config) {
    final ClientContractService result;
    final EthereumNodeConfig.Authentication authentication = config.getAuthentication();
    if ((authentication != null) && authentication.getNeedAuth()) {
      result = new ClientContractService(contractAddress, config.getPrivateKeyEnv(),
          config.getNodeUrl() + config.getInfuraProjectIdEnv(), authentication.getNeedAuth(),
          authentication.getUsername(), authentication.getPassword());
    } else {
      result = new ClientContractService(contractAddress, config.getPrivateKeyEnv(),
          config.getNodeUrl() + config.getInfuraProjectIdEnv());
    }
    return result;
  }
  
}
