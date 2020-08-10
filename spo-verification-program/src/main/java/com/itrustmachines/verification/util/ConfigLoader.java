package com.itrustmachines.verification.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.itrustmachines.common.ethereum.config.EthereumNodeConfig;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ConfigLoader {
  
  public final String PRIVATE_KEY_ENV_KEY = "privateKeyEnv";
  public final String INFURA_PROJECT_ID_KEY = "infuraProjectId";
  
  public EthereumNodeConfig load(final @NonNull String propertiesFilePath) {
    log.debug("load() propertiesFilePath={}, path={}", propertiesFilePath, System.getProperty("user.dir"));
    final Properties prop = new Properties();
    try {
      InputStream input = new FileInputStream(propertiesFilePath);
      prop.load(input);
    } catch (Exception e) {
      log.error("load() error", e);
    }
    final String privateKeyEnv = prop.getProperty(PRIVATE_KEY_ENV_KEY);
    final String infuraProjectId = prop.getProperty(INFURA_PROJECT_ID_KEY);
    
    final EthereumNodeConfig config = EthereumNodeConfig.builder()
                                                        .privateKeyEnv(privateKeyEnv)
                                                        .infuraProjectIdEnv(infuraProjectId)
                                                        .build();
    log.debug("load() propertiesFilePath={}, config={}", propertiesFilePath, config);
    return config;
  }
  
}
