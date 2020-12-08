package com.itrustmachines.verification.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.itrustmachines.verification.vo.Query;

public class QueryStringParserTest {
  
  @Test
  public void test_parse_query_by_Locators() {
    String queryStr = "Locators=(30545:SolarPanel_R107)";
    final Query query = QueryStringParser.parse(queryStr);
    
    assertThat(query.getType()).isEqualTo(Query.QueryType.LOCATOR);
    assertThat(query.getIndexValueKey()).isNull();
    assertThat(query.getFromTS()).isZero();
    assertThat(query.getToTS()).isZero();
    assertThat(query.getFromCO()).isZero();
    assertThat(query.getToCO()).isZero();
  }
  
  @Test
  public void test_parse_query_by_TS() {
    String queryStr = "IV_Key=Test,FromTS=123,ToTS=456,FromCO=87,ToCO=88";
    final Query query = QueryStringParser.parse(queryStr);
    
    assertThat(query.getType()).isEqualTo(Query.QueryType.TIMESTAMP);
    assertThat(query.getIndexValueKey()).isEqualTo("Test");
    assertThat(query.getFromTS()).isEqualTo(123L);
    assertThat(query.getToTS()).isEqualTo(456L);
    assertThat(query.getFromCO()).isEqualTo(87L);
    assertThat(query.getToCO()).isEqualTo(88L);
  }
  
  @Test
  public void test_parse_query_by_CO() {
    String queryStr = "IV_Key=SPO_C_Client_Example,FromCO=32170,ToCO=32170";
    final Query query = QueryStringParser.parse(queryStr);
    
    assertThat(query.getType()).isEqualTo(Query.QueryType.CLEARANCE_ORDER);
    assertThat(query.getIndexValueKey()).isEqualTo("SPO_C_Client_Example");
    assertThat(query.getFromTS()).isZero();
    assertThat(query.getToTS()).isZero();
    assertThat(query.getFromCO()).isEqualTo(32170L);
    assertThat(query.getToCO()).isEqualTo(32170L);
  }
  
}