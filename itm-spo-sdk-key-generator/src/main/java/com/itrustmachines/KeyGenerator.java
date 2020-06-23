package com.itrustmachines;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;

import java.math.BigInteger;
import java.util.UUID;

@Slf4j
public class KeyGenerator {

    public static void main(String[] args) {
        final String seed = UUID.randomUUID()
                .toString();
        final KeyInfo keyInfo = generateKeyWithPassword(seed);
        System.out.println("privateKey = " + keyInfo.getPrivateKey());
        System.out.println("publicKey = " + keyInfo.getPublicKey());
        System.out.println("clientWalletAddress = " + keyInfo.getAddress());
    }

    @Data
    @Builder
    public static class KeyInfo {
        String address;
        String privateKey;
        String publicKey;
    }

    public static KeyInfo generateKeyWithPassword(String password) {
        KeyInfo.KeyInfoBuilder builder = KeyInfo.builder();

        try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
            BigInteger publicKeyInDec = ecKeyPair.getPublicKey();
            String sPublicKeyInHex = publicKeyInDec.toString(16);
            String sPrivatekeyInHex = privateKeyInDec.toString(16);
            WalletFile aWallet = Wallet.createLight(password, ecKeyPair);
            String sAddress = aWallet.getAddress();

            builder = builder.address("0x" + sAddress)
                    .privateKey(sPrivatekeyInHex)
                    .publicKey(sPublicKeyInHex);
        } catch (Exception e) {
            log.error("generateKeyWithSeed error: {}", e);
        }

        return builder.build();
    }


}
