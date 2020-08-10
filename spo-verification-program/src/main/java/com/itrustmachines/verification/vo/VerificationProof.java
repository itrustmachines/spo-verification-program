package com.itrustmachines.verification.vo;

import java.util.List;

import com.itrustmachines.common.ethereum.EthereumEnv;
import com.itrustmachines.common.vo.ClearanceRecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerificationProof {
  
  /**
   * 1. Locator List: <code>Locators=(5:XXX_R0),(5:XXX_R1),(5:XXX_R2)</code>
   *
   * 2. TS: <code>IV_Key=XXX,FromTS=xxxxx,ToTS=xxxxx,FromCO=xx,ToCO=xx</code>
   *
   * 3. CO: <code>IV_Key=XXX,FromCO=xx,ToCO=xx</code>
   */
  private String query;
  
  private String contractAddress;
  private String serverWalletAddress;
  
  private EthereumEnv env;
  
  private List<ExistenceProof> existenceProofs;
  
  private List<ClearanceRecord> clearanceRecords;
  
}
