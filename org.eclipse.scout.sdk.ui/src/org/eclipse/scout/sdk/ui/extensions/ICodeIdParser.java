package org.eclipse.scout.sdk.ui.extensions;

public interface ICodeIdParser {
  boolean isValid(String val);

  String getSource(String val);
}
