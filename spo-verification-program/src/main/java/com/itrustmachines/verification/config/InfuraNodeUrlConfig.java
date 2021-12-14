package com.itrustmachines.verification.config;

import org.apache.commons.lang3.StringUtils;

import com.itrustmachines.common.ethereum.EthereumEnv;
import com.itrustmachines.common.util.UrlUtil;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfuraNodeUrlConfig {
  
  private String infuraProjectId;
  
  public static InfuraNodeUrlConfig of(@NonNull final String nodeUrl) {
    final String urlWithoutSlash = UrlUtil.urlWithoutSlash(nodeUrl);
    final String projectId = urlWithoutSlash.substring(urlWithoutSlash.lastIndexOf("/") + 1);
    return InfuraNodeUrlConfig.builder()
                              .infuraProjectId(projectId)
                              .build();
  }
  
  public static String toProjectId(final String nodeUrl) {
    final String urlWithoutSlash = UrlUtil.urlWithoutSlash(nodeUrl);
    return urlWithoutSlash.substring(urlWithoutSlash.lastIndexOf("/") + 1);
  }
  
  public static String toNodeUrl(@NonNull final EthereumEnv env, @NonNull final String infuraProjectId) {
    if (StringUtils.isBlank(infuraProjectId)) {
      return null;
    } else {
      return "https://" + env.name()
                             .toLowerCase()
          + ".infura.io/v3/" + infuraProjectId;
    }
  }
  
  public String getNodeUrl(@NonNull final EthereumEnv env) {
    return toNodeUrl(env, infuraProjectId);
  }
  
}
