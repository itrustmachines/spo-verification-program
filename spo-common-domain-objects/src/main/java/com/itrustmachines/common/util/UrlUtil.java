package com.itrustmachines.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class UrlUtil {
  
  public static final String URL_REGEX_PATTERN = "(https?):\\/\\/[-a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*[-a-zA-Z0-9+]";
  
  public String urlWithoutSlash(final String url) {
    Pattern r = Pattern.compile(URL_REGEX_PATTERN);
    Matcher m = r.matcher(url);
    String resultUrl = url;
    if (m.find()) {
      resultUrl = m.group(0);
    }
    return resultUrl;
  }
  
}
