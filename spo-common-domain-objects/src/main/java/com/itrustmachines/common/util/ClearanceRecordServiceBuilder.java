package com.itrustmachines.common.util;

import com.itrustmachines.common.config.EthereumNodeConfig;
import com.itrustmachines.common.contract.ClearanceRecordService;

public class ClearanceRecordServiceBuilder {
  
  public static ClearanceRecordService build(String contractAddress, EthereumNodeConfig config) {
    final ClearanceRecordService result;
    final EthereumNodeConfig.Authentication authentication = config.getAuthentication();
    if ((authentication != null) && (authentication.getNeedAuth() == true)) {
      result = new ClearanceRecordService(contractAddress, config.getPrivateKeyEnv(),
          config.getNodeUrl() + config.getInfuraProjectIdEnv(), authentication.getNeedAuth(),
          authentication.getUsername(), authentication.getPassword());
    } else {
      result = new ClearanceRecordService(contractAddress, config.getPrivateKeyEnv(),
          config.getNodeUrl() + config.getInfuraProjectIdEnv());
    }
    return result;
  }
  
}
