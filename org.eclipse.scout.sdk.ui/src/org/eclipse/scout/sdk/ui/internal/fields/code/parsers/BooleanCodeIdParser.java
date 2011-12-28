package org.eclipse.scout.sdk.ui.internal.fields.code.parsers;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.sdk.ui.extensions.ICodeIdParser;

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
