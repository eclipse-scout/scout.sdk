package org.eclipse.scout.sdk.extensions.codeid.parsers;

import org.eclipse.scout.sdk.util.jdt.JdtUtility;

public class StringCodeIdParser implements ICodeIdParser {
  @Override
  public boolean isValid(String val) {
    return true;
  }

  @Override
  public String getSource(String val) {
    if (val == null) return null;
    return JdtUtility.toStringLiteral(val);
  }
}
