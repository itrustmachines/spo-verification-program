package com.itrustmachines.verification.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.itrustmachines.common.ethereum.service.ClientContractService;
import com.itrustmachines.common.tpm.PBPair;
import com.itrustmachines.common.vo.MerkleProof;
import com.itrustmachines.common.vo.Receipt;
import com.itrustmachines.common.vo.SpoSignature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerifyReceiptAndMerkleProofService_ChineseIndexValue_Test {
  
  @Test
  public void test_verifyPbPair() {
    Receipt receipt = Receipt.builder()
                             .callerAddress("0x2f55ff516d1f0851b19c357a27d99c71509451b7")
                             .timestamp(1602054511080L)
                             .cmd(
                                 "{\"fileHash\":\"54507450ec53dc30bdfec29c01d12179a3a7d48c81f816cdf52c6d5366b9ad47\",\"timestamp\":1602054509854,\"email\":\"wesley@itm.com\",\"fileName\":\"2020年9月出勤紀錄.ods\"}")
                             .indexValue("wesley-2020年9月出勤紀錄.ods-itm.com_R0")
                             .metadata("")
                             .clearanceOrder(213L)
                             .sigClient(SpoSignature.builder()
                                                    .r("3471f98d107c6e332a53adfec09ae2a7b5b88ba290e035e3d3a876567f4f4ac8")
                                                    .s("072506cf9ada70ed21fcc81597ca8a6446ce316ce5d925bfeb35e9ee49ba1215")
                                                    .v("1b")
                                                    .build())
                             .timestampSPO(1602054512167L)
                             .result("ok")
                             .sigServer(SpoSignature.builder()
                                                    .r("2e964036a23cdb454e9c37731b4ac6ab97380e4b19652b51dddc56acfac9c837")
                                                    .s("110ce5d44a623634dd82196d7dbd858f8c1692b9c4cb4eba5af7ff0c56879d4c")
                                                    .v("1c")
                                                    .build())
                             .build();
    
    List pbPair = new ArrayList();
    pbPair.add(PBPair.PBPairValue.builder()
                                 .index(1)
                                 .keyHash("4b2b08becb7ab09a64c229ca447644c9cea4361494562782713770db79b8e01f")
                                 .value("85002faceb9a7c0d6be9429d755609e0f7db21cb39b8be83c85027aca43b3304")
                                 .build());
    
    MerkleProof merkleProof = MerkleProof.builder()
                                         .slice("1.bb077d7864269e19e730365a3029f422521dc20438a248466ef06a8ec0390af7")
                                         .pbPair(pbPair)
                                         .clearanceOrder(213L)
                                         .sigServer(SpoSignature.builder()
                                                                .r("feae92e3e9ac5e041e9fe690ab510514621bc3870b60e5c98b3c8ea790b6a007")
                                                                .s("1c7b1bfc7cd84e31552afa4e891c236e70aa29a827dd5a0ac584dab674ff618f")
                                                                .v("1b")
                                                                .build())
                                         .build();
    final boolean result = buildService().verifyPbPair(receipt, merkleProof);
    
    log.info("result={}", result);
  }
  
  final String privateKey = "b8059c31844941a8b37d4cac37b331d7b8059c31844941a8b37d4cac37b331d7";
  final String nodeUrl = "https://rinkeby.infura.io/v3/32ccbb538dbf4112a2f50b35bfff2a41";
  final String serverWalletAddress = "0xA197013CA3978962B91f471C4e8C8b6DB42B14D5";
  final String contractAddress = "0x1Bbe2D131a42DaEd0110fd2bE08AF56906A5a1Ce";
  
  private VerifyReceiptAndMerkleProofService buildService() {
    final ClientContractService clearanceRecordService = new ClientContractService(contractAddress, privateKey, nodeUrl,
        1.0, 5);
    return new VerifyReceiptAndMerkleProofService(serverWalletAddress, clearanceRecordService);
  }
  
}