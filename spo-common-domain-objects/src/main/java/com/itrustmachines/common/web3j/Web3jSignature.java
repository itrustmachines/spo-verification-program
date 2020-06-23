package com.itrustmachines.common.web3j;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Web3jSignature {

  private byte[] content;
  private byte[] r;
  private byte[] s;
  private byte[] v;
  
}
