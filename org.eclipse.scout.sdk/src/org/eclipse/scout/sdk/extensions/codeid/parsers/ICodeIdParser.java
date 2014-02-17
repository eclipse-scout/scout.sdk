package org.eclipse.scout.sdk.extensions.codeid.parsers;

public interface ICodeIdParser {
  boolean isValid(String val);

  String getSource(String val);
}
