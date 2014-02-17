package org.eclipse.scout.sdk.extensions.codeid.parsers;

import org.eclipse.scout.commons.StringUtility;

public class BooleanCodeIdParser implements ICodeIdParser {
  @Override
  public boolean isValid(String val) {
    return StringUtility.isNullOrEmpty(val) || "true".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val);
  }

  @Override
  public String getSource(String val) {
    if (val == null) return null;
    else return val.toLowerCase();
  }
}
