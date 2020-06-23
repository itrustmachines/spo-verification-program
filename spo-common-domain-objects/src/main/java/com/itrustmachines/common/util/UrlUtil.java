package com.itrustmachines.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class UrlUtil {
  
  public String urlWithoutSlash(String url) {
    String pattern = "(https?):\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+]";
    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(url);
    if (m.find()) {
      url = m.group(0);
    }
    return url;
  }
  
}
