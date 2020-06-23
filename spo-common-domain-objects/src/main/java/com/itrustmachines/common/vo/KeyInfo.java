package com.itrustmachines.common.vo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "privateKey" })
@Builder
public class KeyInfo {

  private String address;
  private String privateKey;
  private String publicKey;
  
}
