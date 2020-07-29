package com.itrustmachines.verification.sample;

import com.itrustmachines.common.config.EthereumNodeConfig;
import com.itrustmachines.verification.VerificationApi;
import com.itrustmachines.verification.vo.VerifyReceiptAndMerkleProofResult;
import com.itrustmachines.verification.vo.VerifyVerificationProofResult;
import java.util.List;

public class jsonStringSample {

    public static void main(String[] args) {

        String contractAddress = "<contractAddress>";
        String serverWalletAddress = "<serverWalletAddress>";
        EthereumNodeConfig.Authentication auth = new EthereumNodeConfig.Authentication(false, "<username>", "<password>");
        EthereumNodeConfig config = new EthereumNodeConfig("<privateKey>", "<nodeUrl>", "<infuraProjectIdEnv>", "<blockchainExplorerUrl>", auth);
        VerificationApi api = VerificationApi.getInstance(contractAddress, serverWalletAddress, config);

        String filePath = "<verificationProof.json檔之路徑>";
        VerifyVerificationProofResult result = api.verify(filePath);
        List<VerifyReceiptAndMerkleProofResult> proofResultList = result.getVerifyReceiptResults();

        for (int i = 0; i < proofResultList.size(); i++) {
            System.out.println(proofResultList.get(i).isPass());
        }
    }
}
