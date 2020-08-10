package com.itrustmachines.verification.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.itrustmachines.common.util.HashUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SliceValidationUtilTest {
  
  String correctRootHash = "5bcb5a95489585b134ec83ef36ef707f1b85829a78443af1369d972742bb78d3";
  
  @Test
  public void test_getRootHash_correct() {
    // given
    String validData = "191.fdc1bdff33361c2c4b2f8f7204ce4d8148f42af0db6ddc92642d1d99753c5dca.cb7d91259f8fa169ed0f7f251ab8135c93bd53b54ad9c7d30005d829ec339bb0.bfaef49b3b4684b2ccbbc76ea47470ad2b422e685aab2b42370cf2dc527f43d9.fc45355051b16ffa7c55cc5b4b7690790bbe68f68ea066c13016f270c2d714be.98012ea16b016ac533444ce71d2e39a901650b75ab30deaadc15f568e5561e38.4d4e4f485bb565a67345bcf4d03247cc2d1308804f84812ec598bf719540c3ab.85aecb9e050b020bc7bc5584a208831a5d3373e38bff965db9c66500fbc77288.b04da04c0bf18e93f9c881ebcf7707ef47a1cc70acad1c8f824ec74752b698f2.bf220e9f2ba41bd1a2f57d017f41553a82c6e89704bb4d2bfcbe21f472d9e745.694027ce1a3ab2c2146474e377d669806ecf4ffcd5044aac03e0eb193057155a.72f39cd1207e014ce5c1632bb908a8332ae98f2a2822b304216790474b32be4f.8c443ef4143410dbd1fc84927879448b4977d91ef3e025e3072206e20bedd656.99b30d1620cc1dc217e59f42f4250255aa1ec1236c5c7db061dd389bd048f3d2.865b08002986d16372f5a1a53f2189f32ff945c75c865bdef5b9bd3748d83c2f.5bcb5a95489585b134ec83ef36ef707f1b85829a78443af1369d972742bb78d3";
    
    // when
    final byte[] rootHash = SliceValidationUtil.getRootHash(validData);
    
    // then
    final boolean equals = Arrays.equals(HashUtils.hex2byte(correctRootHash), rootHash);
    assertThat(equals).isTrue();
  }
  
  @Test
  public void test_getRootHash_invalid() {
    // given
    String invalidData = "191.2020bdff33361c2c4b2f8f7204ce4d8148f42af0db6ddc92642d1d99753c5dca.cb7d91259f8fa169ed0f7f251ab8135c93bd53b54ad9c7d30005d829ec339bb0.bfaef49b3b4684b2ccbbc76ea47470ad2b422e685aab2b42370cf2dc527f43d9.fc45355051b16ffa7c55cc5b4b7690790bbe68f68ea066c13016f270c2d714be.98012ea16b016ac533444ce71d2e39a901650b75ab30deaadc15f568e5561e38.4d4e4f485bb565a67345bcf4d03247cc2d1308804f84812ec598bf719540c3ab.85aecb9e050b020bc7bc5584a208831a5d3373e38bff965db9c66500fbc77288.b04da04c0bf18e93f9c881ebcf7707ef47a1cc70acad1c8f824ec74752b698f2.bf220e9f2ba41bd1a2f57d017f41553a82c6e89704bb4d2bfcbe21f472d9e745.694027ce1a3ab2c2146474e377d669806ecf4ffcd5044aac03e0eb193057155a.72f39cd1207e014ce5c1632bb908a8332ae98f2a2822b304216790474b32be4f.8c443ef4143410dbd1fc84927879448b4977d91ef3e025e3072206e20bedd656.99b30d1620cc1dc217e59f42f4250255aa1ec1236c5c7db061dd389bd048f3d2.865b08002986d16372f5a1a53f2189f32ff945c75c865bdef5b9bd3748d83c2f.5bcb5a95489585b134ec83ef36ef707f1b85829a78443af1369d972742bb78d3";
    
    // when
    final byte[] rootHash = SliceValidationUtil.getRootHash(invalidData);
    
    // then
    final boolean equals = Arrays.equals(HashUtils.hex2byte(correctRootHash), rootHash);
    assertThat(equals).isFalse();
  }
  
}